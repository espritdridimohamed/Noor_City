package tn.esprit.sansa.ui.screens.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import tn.esprit.sansa.ui.theme.*


// La palette Noor est maintenant centralisée dans tn.esprit.sansa.ui.theme.NoorPalette


enum class ProgramStatus(val displayName: String, val color: Color) {
    PENDING("En attente", Color.Gray), // For Event workflow
    ACTIVE("Actif", NoorGreen),
    SCHEDULED("Planifié", NoorBlue),
    PAUSED("En pause", NoorAmber),
    INACTIVE("Inactif", Color.Gray),
    COMPLETED("Terminé", NoorPurple) // For Event workflow
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
    val description: String,
    // Event Workflow Fields
    val eventId: String? = null,
    val technicianId: String? = null,
    val technicianStatus: TechnicianAssignmentStatus? = null
)