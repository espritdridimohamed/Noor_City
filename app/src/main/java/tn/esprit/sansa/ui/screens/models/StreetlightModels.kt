package tn.esprit.sansa.ui.screens.models

import androidx.compose.ui.graphics.Color
import tn.esprit.sansa.ui.theme.*

enum class BulbType(val displayName: String) {
    LED("LED"),
    HALOGEN("Halogène"),
    SODIUM("Sodium haute pression"),
    MERCURY("Mercure")
}

enum class StreetlightStatus(val displayName: String, val color: Color) {
    ON("Allumé", NoorGreen),
    OFF("Éteint", Color.Gray),
    MAINTENANCE("Maintenance", NoorAmber),
    ERROR("Défaillance", NoorRed)
}

data class Streetlight(
    val id: String = "",
    val bulbType: BulbType = BulbType.LED,
    val status: StreetlightStatus = StreetlightStatus.ON,
    val zoneId: String = "", // Link to Zone
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val powerConsumption: Double = 0.0,
    val address: String = "",
    val lastMaintenance: Long = System.currentTimeMillis(),
    val installDate: Long = System.currentTimeMillis()
)
