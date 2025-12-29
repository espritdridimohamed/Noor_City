// ProfileScreen.kt – Interface de profil (Technicien/Citoyen) avec design Noor
package tn.esprit.sansa.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.sansa.ui.theme.SansaTheme

// Palette Noor (locale pour ce fichier)
private val NoorBlue = Color(0xFF1E40AF)
private val NoorGreen = Color(0xFF10B981)
private val NoorAmber = Color(0xFFF59E0B)
private val NoorRed = Color(0xFFEF4444)
private val NoorPurple = Color(0xFF8B5CF6)
private val NoorCyan = Color(0xFF06B6D4)
private val NoorIndigo = Color(0xFF6366F1)

enum class UserRole(val displayName: String, val color: Color) {
    TECHNICIAN("Technicien", NoorBlue),
    CITIZEN("Citoyen", NoorGreen),
    ADMIN("Administrateur", NoorPurple)
}

data class UserProfile(
    val id: String,
    val name: String,
    val role: UserRole,
    val email: String,
    val phone: String,
    val address: String,
    val bio: String,
    val stats: Map<String, String>,
    val joinDate: String,
    val isVerified: Boolean
)

// Mock Data
private val mockTechnicianProfile = UserProfile(
    id = "TECH-2025-001",
    name = "Ahmed Ben Salem",
    role = UserRole.TECHNICIAN,
    email = "ahmed.bensalem@sansa.tn",
    phone = "+216 98 765 432",
    address = "Centre de maintenance Nord, Tunis",
    bio = "Spécialiste en éclairage LED et maintenance préventive. Passionné par les solutions Smart City.",
    stats = mapOf(
        "Interventions" to "156",
        "Réussite" to "98.5%",
        "Heures" to "1,240"
    ),
    joinDate = "Janvier 2022",
    isVerified = true
)

private val mockCitizenProfile = UserProfile(
    id = "CIT-2025-089",
    name = "Sarra Amri",
    role = UserRole.CITIZEN,
    email = "sarra.amri@email.tn",
    phone = "+216 22 345 678",
    address = "15 Rue de Carthage, Marsa",
    bio = "Citoyenne engagée pour l'amélioration de l'éclairage urbain dans mon quartier.",
    stats = mapOf(
        "Signalements" to "12",
        "Impact" to "85%",
        "Points" to "450"
    ),
    joinDate = "Mars 2023",
    isVerified = true
)

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    isTechnician: Boolean = true, // Bascule pour la démo
    onEditProfile: () -> Unit = {},
    onLogOut: () -> Unit = {}
) {
    val profile = if (isTechnician) mockTechnicianProfile else mockCitizenProfile

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { ProfileTopBar() }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header avec Avatar
            item {
                StaggeredEntry(delayMillis = 0) {
                    ProfileHeader(profile)
                }
            }

            // Stats Cards
            item {
                StaggeredEntry(delayMillis = 150) {
                    ProfileStats(profile.stats, profile.role.color)
                }
            }

            // Bio & Info
            item {
                StaggeredEntry(delayMillis = 300) {
                    ProfileInfoSection(profile)
                }
            }

            // Actions
            item {
                StaggeredEntry(delayMillis = 450) {
                    ProfileActions(onEditProfile, onLogOut, profile.role.color)
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun StaggeredEntry(delayMillis: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delayMillis.toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
            animationSpec = tween(500, easing = FastOutSlowInEasing),
            initialOffsetY = { 50 }
        ),
        content = { content() }
    )
}

@Composable
fun AnimatedCounter(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color
) {
    // Nettoyage basique pour extraire le nombre
    val cleanText = text.replace(",", "").replace("%", "").trim()
    val targetValue = cleanText.toFloatOrNull()

    if (targetValue == null) {
        Text(text, style = style, color = color)
        return
    }

    val animatedValue = remember { Animatable(0f) }
    
    LaunchedEffect(targetValue) {
        animatedValue.animateTo(
            targetValue = targetValue,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
    }

    val displayText = remember(animatedValue.value) {
        val value = animatedValue.value
        val intValue = value.toInt()
        
        // Logique de formatage simplifiée pour garder le style original
        var result = if (text.contains(".")) {
             String.format(java.util.Locale.US, "%.1f", value)
        } else {
             intValue.toString()
        }
        
        // Rajouter la virgule si c'était un grand nombre
        if (text.contains(",")) {
             try {
                 result = java.text.NumberFormat.getIntegerInstance(java.util.Locale.US).format(intValue)
             } catch (e: Exception) {}
        }
        
        if (text.endsWith("%")) result += "%"
        result
    }

    Text(
        text = displayText,
        style = style,
        color = color
    )
}

@Composable
private fun ProfileTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* TODO: Back */ }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
            }
            Text(
                "Mon Profil",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { /* TODO: Settings */ }) {
                Icon(Icons.Default.Settings, contentDescription = "Paramètres")
            }
        }
    }
}

@Composable
private fun ProfileHeader(profile: UserProfile) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 24.dp)
    ) {
        Box(
            modifier = Modifier.size(130.dp),
            contentAlignment = Alignment.Center
        ) {
            // Cercle décoratif
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(profile.role.color, profile.role.color.copy(alpha = 0.5f))
                        )
                    )
            )

            // Placeholder Avatar (Ici une icône, remplacer par Image en prod)
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(60.dp)
            )

            // Badge de vérification
            if (profile.isVerified) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-8).dp, y = (-8).dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(NoorBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Vérifié",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = profile.name,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Badge(containerColor = profile.role.color.copy(alpha = 0.15f)) {
                Text(
                    profile.role.displayName.uppercase(),
                    color = profile.role.color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "ID: ${profile.id}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun ProfileStats(stats: Map<String, String>, accentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        stats.forEach { (label, value) ->
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedCounter(
                        text = value,
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = accentColor
                    )
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoSection(profile: UserProfile) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            "À propos",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = profile.bio,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(24.dp))

        Text(
            "Informations",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        InfoItem(Icons.Outlined.Email, "Email", profile.email)
        Spacer(Modifier.height(16.dp))
        InfoItem(Icons.Outlined.Phone, "Téléphone", profile.phone)
        Spacer(Modifier.height(16.dp))
        InfoItem(Icons.Outlined.LocationOn, "Adresse", profile.address)
        Spacer(Modifier.height(16.dp))
        InfoItem(Icons.Outlined.Badge, "Membre depuis", profile.joinDate)
    }
}

@Composable
private fun InfoItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ProfileActions(onEdit: () -> Unit, onLogout: () -> Unit, accentColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Button(
            onClick = onEdit,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Modifier le profil", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NoorRed),
            border = androidx.compose.foundation.BorderStroke(1.dp, NoorRed.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Se déconnecter", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Preview(showBackground = true, name = "Profil Technicien")
@Composable
private fun PreviewProfileTechnician() {
    SansaTheme {
        ProfileScreen(isTechnician = true)
    }
}

@Preview(showBackground = true, name = "Profil Citoyen")
@Composable
private fun PreviewProfileCitizen() {
    SansaTheme {
        ProfileScreen(isTechnician = false)
    }
}
