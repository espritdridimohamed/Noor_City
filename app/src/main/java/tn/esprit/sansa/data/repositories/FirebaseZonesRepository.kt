package tn.esprit.sansa.data.repositories

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import tn.esprit.sansa.ui.screens.models.Zone
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

class FirebaseZonesRepository {
    private val database = FirebaseDatabase.getInstance()
    private val zonesRef = database.getReference("zones")

    fun getZones(): Flow<List<Zone>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val zones = snapshot.children.mapNotNull { it.getValue(Zone::class.java) }
                trySend(zones)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        zonesRef.addValueEventListener(listener)
        awaitClose { zonesRef.removeEventListener(listener) }
    }

    fun addZone(zone: Zone, onComplete: (Boolean) -> Unit) {
        val id = if (zone.id.isEmpty()) zonesRef.push().key ?: return else zone.id
        val newZone = zone.copy(id = id)
        zonesRef.child(id).setValue(newZone)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun deleteZone(id: String, onComplete: (Boolean) -> Unit) {
        zonesRef.child(id).removeValue()
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    suspend fun searchLocation(query: String): List<GeocodingResult> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = URL("https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&limit=5")
            val connection = url.openConnection()
            connection.setRequestProperty("User-Agent", "SansaApp/1.0")
            
            val response = connection.getInputStream().bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(response)
            
            val results = mutableListOf<GeocodingResult>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                results.add(GeocodingResult(
                    displayName = obj.getString("display_name"),
                    lat = obj.getDouble("lat"),
                    lon = obj.getDouble("lon")
                ))
            }
            results
        } catch (e: Exception) {
            emptyList()
        }
    }
}

data class GeocodingResult(
    val displayName: String,
    val lat: Double,
    val lon: Double
)
