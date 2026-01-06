package tn.esprit.sansa.data.repositories

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import tn.esprit.sansa.ui.screens.models.Streetlight

class FirebaseStreetlightsRepository {
    private val database = FirebaseDatabase.getInstance().getReference("streetlights")

    fun getStreetlights(): Flow<List<Streetlight>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val streetlights = snapshot.children.mapNotNull { it.getValue(Streetlight::class.java) }
                trySend(streetlights)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        database.addValueEventListener(listener)
        awaitClose { database.removeEventListener(listener) }
    }

    fun addStreetlight(streetlight: Streetlight, onComplete: (Boolean) -> Unit) {
        val id = streetlight.id.ifBlank { database.push().key ?: return }
        database.child(id).setValue(streetlight.copy(id = id))
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun deleteStreetlight(id: String, onComplete: (Boolean) -> Unit) {
        database.child(id).removeValue()
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    /**
     * Get all streetlights once (not a flow) for ID generation
     */
    suspend fun getAllStreetlightsOnce(): List<Streetlight> {
        return try {
            val snapshot = database.get().await()
            snapshot.children.mapNotNull { it.getValue(Streetlight::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun updateChargingStatus(streetlightId: String, isActive: Boolean, energy: Double = 0.0, status: String = "AVAILABLE") {
        val updates = mapOf(
            "isChargingActive" to isActive,
            "chargingEnergy" to energy,
            "chargerStatus" to if (isActive) "OCCUPIED" else status
        )
        database.child(streetlightId).updateChildren(updates)
    }
}
