package tn.esprit.sansa.data.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

object GeocodingService {
    /**
     * Reverse geocoding using OpenStreetMap Nominatim API
     * Converts coordinates to a human-readable address
     */
    suspend fun getAddressFromCoordinates(lat: Double, lng: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://nominatim.openstreetmap.org/reverse?" +
                        "format=json&lat=$lat&lon=$lng&zoom=18&addressdetails=1"
                
                val connection = URL(url).openConnection()
                connection.setRequestProperty("User-Agent", "Sansa-NoorCity-App")
                
                val response = connection.getInputStream().bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                
                // Extract address components
                val address = json.optJSONObject("address")
                val displayName = json.optString("display_name", "")
                
                if (address != null) {
                    // Build a clean address from components
                    val road = address.optString("road", "")
                    val houseNumber = address.optString("house_number", "")
                    val suburb = address.optString("suburb", "")
                    val city = address.optString("city", address.optString("town", address.optString("village", "")))
                    val postcode = address.optString("postcode", "")
                    
                    buildString {
                        if (houseNumber.isNotBlank()) append("$houseNumber ")
                        if (road.isNotBlank()) append("$road, ")
                        if (suburb.isNotBlank()) append("$suburb, ")
                        if (city.isNotBlank()) append(city)
                        if (postcode.isNotBlank()) append(" $postcode")
                    }.trim().ifBlank { displayName }
                } else {
                    displayName
                }
            } catch (e: Exception) {
                android.util.Log.e("GeocodingService", "Error getting address: ${e.message}")
                "Coordonnées: ${String.format("%.5f", lat)}, ${String.format("%.5f", lng)}"
            }
        }
    }
}
