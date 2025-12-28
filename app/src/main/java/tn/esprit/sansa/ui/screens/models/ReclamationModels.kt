// ReclamationModels.kt
import tn.esprit.sansa.models.*
import androidx.compose.ui.graphics.Color

val NoorBlue = Color(0xFF1E40AF)
val NoorGreen = Color(0xFF10B981)
val NoorAmber = Color(0xFFF59E0B)
val NoorRed = Color(0xFFEF4444)
val NoorPurple = Color(0xFF8B5CF6)

enum class ReclamationStatus(val displayName: String, val color: Color) {
    PENDING("En attente", NoorAmber),
    IN_PROGRESS("En cours", NoorBlue),
    RESOLVED("Résolue", NoorGreen),
    REJECTED("Rejetée", NoorRed)
}

enum class ReclamationPriority(val displayName: String, val color: Color) {
    LOW("Basse", NoorGreen),
    MEDIUM("Moyenne", NoorAmber),
    HIGH("Haute", NoorRed),
    URGENT("Urgente", NoorPurple)
}

data class Reclamation(
    val id: String = "",
    val description: String = "",
    val date: String = "",
    val status: ReclamationStatus = ReclamationStatus.PENDING,
    val streetlightId: String = "",
    val priority: ReclamationPriority = ReclamationPriority.MEDIUM,
    val location: String = "",
    val reportedBy: String = "",
    val assignedTo: String? = null
)