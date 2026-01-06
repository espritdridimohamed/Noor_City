package tn.esprit.sansa.ui.screens.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

import tn.esprit.sansa.ui.theme.*
// Palette Noor centralisée


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
    val zone: String = "",
    val streamUrl: String = "",
    // Blockchain certification fields
    val hasCertificate: Boolean = false,
    val lastCertificateHash: String = "",
    val lastCertificateTimestamp: Long = 0L,
    val certificateCount: Int = 0,
    val blockchainVerified: Boolean = false,
    // AI Alert fields (Crash Detection)
    val isAccidentActive: Boolean = false,
    val alertStatus: String = "NORMAL", // NORMAL, ACCIDENT, OBSTACLE
    // AI Analytics fields (New)
    val detectedPeopleCount: Int = 0,
    val detectedVehicleCount: Int = 0,
    val safetyScore: Int = 100, // 0-100
    val aiDescription: String = "Analyse fluide",
    // AI Diagnostic fields
    val lastAiDiagnostic: String = "Jamais effectué",
    val aiSafetyReport: String = "Aucun rapport disponible"
)

