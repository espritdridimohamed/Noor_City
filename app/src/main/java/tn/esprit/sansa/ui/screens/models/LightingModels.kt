package tn.esprit.sansa.ui.screens.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import tn.esprit.sansa.ui.theme.*


// La palette Noor est maintenant centralisée dans tn.esprit.sansa.ui.theme.NoorPalette


// Note: LightingProgram, ProgramStatus and TechnicianAssignmentStatus have been moved
// to CulturalEventModels.kt to support the Noor Intelligent Lighting system.

enum class LightingRuleType(val displayName: String, val color: Color, val icon: ImageVector) {
    TIME_BASED("Basé sur l'heure", NoorBlue, Icons.Default.Schedule),
    SENSOR_BASED("Basé sur capteurs", NoorCyan, Icons.Default.Sensors),
    EVENT_BASED("Basé sur événements", NoorPurple, Icons.Default.Event),
    MANUAL("Manuel", NoorAmber, Icons.Default.TouchApp),
    ADAPTIVE("Adaptatif", NoorTeal, Icons.Default.Settings),
    EMERGENCY("Urgence", NoorRed, Icons.Default.Warning)
}

data class LightingRule(
    val type: LightingRuleType = LightingRuleType.TIME_BASED,
    val description: String = "",
    val parameters: String = ""
)