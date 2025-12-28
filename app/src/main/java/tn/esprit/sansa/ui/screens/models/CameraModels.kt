// CameraModels.kt — Modèles partagés pour les caméras (à placer dans package tn.esprit.sansa.models)
package tn.esprit.sansa.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// Palette Noor (déjà définie ailleurs, mais on les réexporte pour cohérence)
val NoorBlue = Color(0xFF1E40AF)
val NoorGreen = Color(0xFF10B981)
val NoorAmber = Color(0xFFF59E0B)
val NoorRed = Color(0xFFEF4444)
val NoorPurple = Color(0xFF8B5CF6)
val NoorCyan = Color(0xFF06B6D4)

enum class CameraStatus(val displayName: String, val color: Color) {
    ONLINE("En ligne", NoorGreen),
    OFFLINE("Hors ligne", NoorRed),
    MAINTENANCE("Maintenance", NoorAmber),
    RECORDING("Enregistrement", NoorBlue),
    ERROR("Erreur", NoorRed)
}

enum class CameraType(val displayName: String, val color: Color, val icon: ImageVector) {
    DOME("Dôme", NoorBlue, Icons.Default.Videocam),
    BULLET("Bullet", NoorPurple, Icons.Default.CameraAlt),
    PTZ("PTZ", NoorCyan, Icons.Default.CameraEnhance),
    THERMAL("Thermique", NoorRed, Icons.Default.ThermostatAuto),
    AI_POWERED("IA", NoorGreen, Icons.Default.Psychology)
}

data class Camera(
    val id: String = "",
    val location: String = "",
    val status: CameraStatus = CameraStatus.ONLINE,
    val associatedStreetlight: String = "",
    val type: CameraType = CameraType.DOME,
    val resolution: String = "1080p",
    val nightVision: Boolean = true,
    val installDate: String = "",
    val lastMaintenance: String = "",
    val recordingEnabled: Boolean = true,
    val motionDetection: Boolean = true,
    val zone: String = ""
)