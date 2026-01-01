package tn.esprit.sansa.ui.screens.models

enum class UserRole(val displayName: String) {
    CITIZEN("Citoyen"),
    TECHNICIAN("Technicien"),
    ADMIN("Administrateur")
}

data class UserAccount(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.CITIZEN,
    val specialty: String? = null, // Only for Technicians
    val profilePicture: String? = null,
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val isFirstLogin: Boolean = true, // Pour suivre la première connexion
    val invitedAt: Long? = null, // Timestamp de l'invitation
    val activatedAt: Long? = null, // Timestamp de l'activation (premier login)
    val phoneNumber: String = "", // Mobile du technicien
    val workingZone: String = "", // Zone assignée
    val coordinates: String = "" // Coordonnées GPS (ex: "36.8065, 10.1815")
)
