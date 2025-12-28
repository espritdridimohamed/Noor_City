// LightingModels.kt - Modèles partagés pour les programmes d'éclairage
package tn.esprit.sansa.screens.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// Palette Noor
val NoorBlue = Color(0xFF1E40AF)
val NoorGreen = Color(0xFF10B981)
val NoorAmber = Color(0xFFF59E0B)
val NoorRed = Color(0xFFEF4444)
val NoorPurple = Color(0xFF8B5CF6)
val NoorCyan = Color(0xFF06B6D4)
val NoorTeal = Color(0xFF14B8A6)

enum class ProgramStatus(val displayName: String, val color: Color) {
    ACTIVE("Actif", NoorGreen),
    SCHEDULED("Planifié", NoorBlue),
    PAUSED("En pause", NoorAmber),
    INACTIVE("Inactif", Color.Gray)
}

enum class LightingRuleType(val displayName: String, val color: Color, val icon: ImageVector) {
    TIME_BASED("Basé sur l'heure", NoorBlue, Icons.Default.Schedule),
    SENSOR_BASED("Basé sur capteurs", NoorCyan, Icons.Default.Sensors),
    EVENT_BASED("Basé sur événements", NoorPurple, Icons.Default.Event),
    MANUAL("Manuel", NoorAmber, Icons.Default.TouchApp),
    ADAPTIVE("Adaptatif", NoorTeal, Icons.Default.Settings),
    EMERGENCY("Urgence", NoorRed, Icons.Default.Warning)
}

data class LightingRule(
    val type: LightingRuleType,
    val description: String,
    val parameters: String
)

data class LightingProgram(
    val id: String,
    val name: String,
    val rules: List<LightingRule>,
    val associatedStreetlights: List<String>,
    val status: ProgramStatus,
    val createdDate: String,
    val lastModified: String,
    val priority: Int,
    val description: String
)