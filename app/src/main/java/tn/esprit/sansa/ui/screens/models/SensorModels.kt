// SensorModels.kt
package tn.esprit.sansa.ui.screens.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

enum class SensorType(
    val displayName: String,
    val icon: ImageVector,
    val color: Color,
    val unit: String
) {
    LIGHT("Luminosité", Icons.Default.LightMode, Color(0xFFF59E0B), "lux"),
    MOTION("Mouvement", Icons.Default.Sensors, Color(0xFF1E40AF), "dét./h"),
    TEMPERATURE("Température", Icons.Default.Thermostat, Color(0xFFEF4444), "°C"),
    HUMIDITY("Humidité", Icons.Default.WaterDrop, Color(0xFF3B82F6), "%"),
    POWER("Consommation", Icons.Default.Power, Color(0xFF10B981), "W")
}

enum class SensorStatus { ACTIVE, WARNING, ERROR, OFFLINE }