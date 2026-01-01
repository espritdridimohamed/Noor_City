package tn.esprit.sansa.ui.screens.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import tn.esprit.sansa.ui.theme.*

enum class ZoneType(val displayName: String, val color: Color, val icon: ImageVector) {
    RESIDENTIAL("Résidentielle", NoorBlue, Icons.Default.Home),
    COMMERCIAL("Commerciale", NoorPurple, Icons.Default.Business),
    INDUSTRIAL("Industrielle", NoorCyan, Icons.Default.Factory),
    HISTORICAL("Historique", NoorAmber, Icons.Default.AccountBalance),
    PARK("Parc/Jardin", NoorGreen, Icons.Default.Park),
    DOWNTOWN("Centre-ville", NoorIndigo, Icons.Default.LocationCity),
    HIGHWAY("Route/Autoroute", NoorRed, Icons.Default.LocalShipping),
    MIXED("Mixte", Color(0xFF64748B), Icons.Default.Apps)
}

enum class ZoneStatus(val displayName: String, val color: Color) {
    ACTIVE("Active", NoorGreen),
    MAINTENANCE("En maintenance", NoorAmber),
    INACTIVE("Inactive", Color.Gray),
    PLANNING("En planification", NoorBlue)
}

data class Zone(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val associatedStreetlights: List<String> = emptyList(),
    val type: ZoneType = ZoneType.RESIDENTIAL,
    val status: ZoneStatus = ZoneStatus.PLANNING,
    val area: Double = 0.0,
    val population: Int = 0,
    val coordinator: String = "",
    val activeStreetlights: Int = 0,
    val latitude: Double = 36.8065, // Default Tunis
    val longitude: Double = 10.1815
)
