// InterventionModels.kt - Modèles pour les interventions
package tn.esprit.sansa.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// NOTE: Si ces enums et data class existent déjà dans InterventionsScreen.kt,
// supprimez-les de là-bas et gardez uniquement ce fichier

enum class InterventionStatus(val displayName: String, val color: Color) {
    SCHEDULED("Planifiée", Color(0xFFF59E0B)),
    IN_PROGRESS("En cours", Color(0xFF1E40AF)),
    COMPLETED("Terminée", Color(0xFF10B981)),
    CANCELLED("Annulée", Color(0xFFEF4444)),
    PENDING("En attente", Color(0xFF8B5CF6))
}

enum class InterventionType(val displayName: String, val color: Color, val icon: ImageVector) {
    REPAIR("Réparation", Color(0xFFEF4444), Icons.Default.Build),
    MAINTENANCE("Maintenance", Color(0xFF1E40AF), Icons.Default.Settings),
    INSPECTION("Inspection", Color(0xFFF59E0B), Icons.Default.RemoveRedEye),
    INSTALLATION("Installation", Color(0xFF10B981), Icons.Default.AddCircle),
    REPLACEMENT("Remplacement", Color(0xFF8B5CF6), Icons.Default.Refresh),
    EMERGENCY("Urgence", Color(0xFFEF4444), Icons.Default.Warning)
}

enum class InterventionPriority(val displayName: String, val color: Color) {
    LOW("Basse", Color(0xFF10B981)),
    MEDIUM("Moyenne", Color(0xFFF59E0B)),
    HIGH("Haute", Color(0xFFEF4444)),
    URGENT("Urgente", Color(0xFF8B5CF6))
}

data class Intervention(
    val id: String = "",
    val streetlightId: String = "",
    val technicianName: String = "",
    val type: InterventionType = InterventionType.MAINTENANCE,
    val date: String = "",
    val status: InterventionStatus = InterventionStatus.SCHEDULED,
    val location: String = "",
    val description: String = "",
    val estimatedDuration: Int = 60,
    val priority: String = "Moyenne",
    val notes: String = "",
    val assignedBy: String = ""
)