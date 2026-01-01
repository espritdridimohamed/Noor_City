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
    val id: String,
    val name: String,
    val dateTime: Date,
    val zones: List<String>,
    val ambianceType: AmbianceType,
    val status: EventStatus,
    val duration: Int, // en heures
    val description: String,
    val attendees: Int,
    val organizer: String
)
