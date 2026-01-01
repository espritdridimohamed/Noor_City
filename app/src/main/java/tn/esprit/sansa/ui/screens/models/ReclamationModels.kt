package tn.esprit.sansa.ui.screens.models
import androidx.compose.ui.graphics.Color
import tn.esprit.sansa.ui.theme.*


// La palette Noor est maintenant centralisée dans tn.esprit.sansa.ui.theme.NoorPalette


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
    val date: Long = 0L,
    val status: ReclamationStatus = ReclamationStatus.PENDING,
    val streetlightId: String = "",
    val priority: ReclamationPriority = ReclamationPriority.MEDIUM,
    val location: String = "",
    val reportedBy: String = "",
    val assignedTo: String? = null
)