package tn.esprit.sansa.ui.screens.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import tn.esprit.sansa.ui.theme.*
import java.util.Date

// Palette Noor moved to Color.kt

enum class TechnicianStatus(val displayName: String, val color: Color) {
    AVAILABLE("Disponible", NoorGreen),
    BUSY("Occupé", NoorAmber),
    ON_MISSION("En mission", NoorBlue),
    OFFLINE("Hors ligne", NoorRed),
    ON_LEAVE("En congé", NoorPurple)
}

enum class TechnicianSpecialty(val displayName: String, val color: Color, val icon: ImageVector) {
    ELECTRICAL("Électricien", NoorAmber, Icons.Default.ElectricBolt),
    NETWORK("Réseau", NoorCyan, Icons.Default.Router),
    MAINTENANCE("Maintenance", NoorBlue, Icons.Default.Build),
    SURVEILLANCE("Surveillance", NoorPurple, Icons.Default.Videocam),
    TECHNICAL_SUPPORT("Support technique", NoorGreen, Icons.Default.SupportAgent)
}

data class Technician(
    val id: String,
    val name: String,
    val email: String,
    val specialty: TechnicianSpecialty,
    val status: TechnicianStatus,
    val phone: String,
    val interventionsCount: Int,
    val successRate: Float,
    val joinDate: String,
    val lastActivity: String,
    val zoneIds: List<String> = emptyList() // Zones covered by this technician
)

// EventLightingProgram and EventProgramStatus replaced by consolidated LightingProgram in LightingModels.kt

enum class TechnicianAssignmentStatus {
    WAITING, ACCEPTED, REJECTED
}

val mockTechnicians = listOf(
    Technician(
        "TECH001",
        "Ahmed Ben Salem",
        "ahmed.bensalem@sansa.tn",
        TechnicianSpecialty.ELECTRICAL,
        TechnicianStatus.AVAILABLE,
        "+216 98 765 432",
        156,
        98.5f,
        "Jan 2022",
        "Il y a 5 min",
        listOf("Zone A", "Zone B")
    ),
    Technician(
        "TECH002",
        "Fatma Khelifi",
        "fatma.khelifi@sansa.tn",
        TechnicianSpecialty.NETWORK,
        TechnicianStatus.ON_MISSION,
        "+216 22 345 678",
        203,
        99.2f,
        "Mar 2021",
        "Il y a 2h",
        listOf("Centre-ville")
    ),
    Technician(
        "TECH003",
        "Mohamed Trabelsi",
        "mohamed.trabelsi@sansa.tn",
        TechnicianSpecialty.MAINTENANCE,
        TechnicianStatus.BUSY,
        "+216 55 123 456",
        189,
        97.8f,
        "Jun 2022",
        "Il y a 30 min",
         listOf("Zone A", "Zone historique")
    ),
    Technician(
        "TECH004",
        "Sarra Amri",
        "sarra.amri@sansa.tn",
        TechnicianSpecialty.SURVEILLANCE,
        TechnicianStatus.AVAILABLE,
        "+216 28 901 234",
        142,
        98.9f,
        "Sep 2022",
        "Il y a 1h",
         listOf("Médina")
    )
)
