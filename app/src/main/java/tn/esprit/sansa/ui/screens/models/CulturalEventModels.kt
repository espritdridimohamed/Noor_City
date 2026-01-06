package tn.esprit.sansa.ui.screens.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import tn.esprit.sansa.ui.theme.*
import java.util.Date

enum class AmbianceType(
    val displayName: String,
    val color: Color,
    val icon: ImageVector,
    val description: String
) {
    FESTIVE("Festive", NoorOrange, Icons.Default.Celebration, "Éclairage dynamique et coloré"),
    ROMANTIC("Romantique", NoorPink, Icons.Default.Favorite, "Lumière douce et tamisée"),
    PATRIOTIC("Patriotique", NoorRed, Icons.Default.Flag, "Couleurs nationales"),
    ARTISTIC("Artistique", NoorPurple, Icons.Default.Palette, "Jeu de lumières créatif"),
    MODERN("Moderne", NoorBlue, Icons.Default.AutoAwesome, "Éclairage LED blanc"),
    TRADITIONAL("Traditionnel", NoorAmber, Icons.Default.Mosque, "Lumière chaude traditionnelle"),
    SPORT("Sportif", NoorGreen, Icons.Default.SportsScore, "Éclairage intense et vif"),
    CHRISTMAS("Noël", NoorIndigo, Icons.Default.AcUnit, "Illuminations de fêtes")
}

enum class EventStatus(val displayName: String, val color: Color) {
    PENDING("En attente", NoorAmber),
    UPCOMING("À venir", NoorBlue),
    ACTIVE("En cours", NoorGreen),
    COMPLETED("Terminé", Color.Gray),
    CANCELLED("Annulé", NoorRed)
}

data class CulturalEvent(
    val id: String = "",
    val name: String = "",
    val dateTime: Date = Date(),
    val zones: List<String> = emptyList(),
    val ambianceType: AmbianceType = AmbianceType.MODERN,
    val status: EventStatus = EventStatus.PENDING,
    val duration: Int = 0, // en heures
    val description: String = "",
    val attendees: Int = 0,
    val organizer: String = ""
)

enum class LightingAmbience(
    val displayName: String,
    val color: Color,
    val icon: ImageVector,
    val intensity: Int, // Default intensity (0-100)
    val description: String
) {
    FESTIVAL("Mode Festival", NoorOrange, Icons.Default.Celebration, 100, "Éclairage maximum et dynamique"),
    ECO("Éco-Minuit", NoorIndigo, Icons.Default.NightsStay, 20, "Économie d'énergie, détection de mouvement"),
    SECURITY("Alerte Sécurité", NoorRed, Icons.Default.Warning, 120, "Visibilité maximale pour urgences"),
    NORMAL("Normal", NoorBlue, Icons.Default.Lightbulb, 70, "Éclairage standard optimisé")
}

data class TimelinePoint(
    val hour: Int = 0, // 0-23
    val minute: Int = 0, // 0-59
    val intensity: Int = 0 // 0-100 (ou 120 pour sécurité)
)

enum class ProgramStatus(val displayName: String, val color: Color) { 
    PENDING("En attente", NoorAmber), 
    ACTIVE("Actif", NoorGreen), 
    COMPLETED("Terminé", Color.Gray), 
    CANCELLED("Annulé", NoorRed) 
}

enum class TechnicianAssignmentStatus(val displayName: String, val color: Color) { 
    WAITING("En attente", NoorAmber), 
    ACCEPTED("Accepté", NoorBlue), 
    REJECTED("Refusé", NoorRed), 
    COMPLETED("Terminé", NoorGreen) 
}

data class LightingProgram(
    val id: String = "",
    val name: String = "",
    val rules: List<String> = emptyList(),
    val timeline: List<TimelinePoint> = emptyList(),
    val ambience: LightingAmbience = LightingAmbience.NORMAL,
    val associatedStreetlights: List<String> = emptyList(),
    var status: ProgramStatus = ProgramStatus.PENDING,
    val createdDate: String = "",
    val lastModified: String = "",
    val priority: Int = 3,
    val description: String = "",
    val eventId: String? = null,
    val technicianId: String? = null,
    var technicianStatus: TechnicianAssignmentStatus = TechnicianAssignmentStatus.WAITING
)
