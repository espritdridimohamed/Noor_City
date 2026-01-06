// ProfileScreen.kt – Interface de profil (Technicien/Citoyen) avec design Noor
package tn.esprit.sansa.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.outlined.Event
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
import tn.esprit.sansa.ui.theme.*
import tn.esprit.sansa.ui.screens.models.*

// Extension pour garder les couleurs dans le profil sans polluer le modèle de données global
val UserRole.color: Color get() = when(this) {
    UserRole.TECHNICIAN -> NoorBlue
    UserRole.CITIZEN -> NoorBlue
    UserRole.ADMIN -> NoorPurple
}

@Composable
fun ProfileScreen(
    account: UserAccount?,
    onEditProfile: () -> Unit = {},
    onLogOut: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onBack: () -> Unit = {},
    onDeleteAccount: () -> Unit = {}
) {
    if (account == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = NoorBlue)
        }
        return
    }

    val isTechnician = account.role == UserRole.TECHNICIAN
    
    // Mocking the extra info that isn't in UserAccount (for demo)
    val bio = if (isTechnician) "Spécialiste en éclairage LED et maintenance préventive." else "Citoyenne engagée pour l'amélioration urbaine."
    val stats = if (isTechnician) mapOf("Interventions" to "156", "Réussite" to "98.5%", "Heures" to "1,240")
                else mapOf("Signalements" to "12", "Impact" to "85%", "Points" to "450")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { ProfileTopBar(onBack = onBack, onSettingsClick = onNavigateToSettings) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header avec Avatar
            item {
                StaggeredEntry(delayMillis = 0) {
                    ProfileHeader(account)
                }
            }

            // Alert pour profil incomplet (Techniciens seulement)
            if (account.role == UserRole.TECHNICIAN && 
                (account.phoneNumber.isBlank() || account.workingZone.isBlank())) {
                item {
                    StaggeredEntry(delayMillis = 100) {
                        ProfileIncompleteAlert(
                            missingPhone = account.phoneNumber.isBlank(),
                            missingZone = account.workingZone.isBlank()
                        )
                    }
                }
            }

            // Stats Cards
            item {
                StaggeredEntry(delayMillis = 150) {
                    ProfileStats(stats, account.role.color)
                }
            }

            // Bio & Info
            item {
                StaggeredEntry(delayMillis = 300) {
                    ProfileInfoSection(account, bio)
                }
            }

            // Actions
            item {
                StaggeredEntry(delayMillis = 450) {
                    ProfileActions(onEditProfile, onLogOut, onDeleteAccount, account.role.color, isTechnician)
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
private fun ProfileTopBar(onBack: () -> Unit, onSettingsClick: () -> Unit) {
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
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
            }
            Text(
                "Mon Profil",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Paramètres")
            }
        }
    }
}

@Composable
private fun ProfileHeader(account: UserAccount) {
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
                            colors = listOf(account.role.color, account.role.color.copy(alpha = 0.5f))
                        )
                    )
            )

            // Placeholder Avatar
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(60.dp)
            )

            // Badge de vérification
            if (account.isVerified) {
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
            text = account.name,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Badge(containerColor = account.role.color.copy(alpha = 0.15f)) {
                Text(
                    account.role.displayName.uppercase(),
                    color = account.role.color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "ID: ${account.uid.take(8)}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun ProfileIncompleteAlert(
    missingPhone: Boolean,
    missingZone: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = NoorAmber.copy(alpha = 0.15f)
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, NoorAmber)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = NoorAmber,
                modifier = Modifier.size(28.dp)
            )
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Profil incomplet",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NoorAmber
                )
                
                Spacer(Modifier.height(4.dp))
                
                val missingItems = buildList {
                    if (missingPhone) add("numéro de téléphone")
                    if (missingZone) add("zone de travail")
                }
                
                Text(
                    text = "Veuillez compléter votre ${missingItems.joinToString(" et ")} pour activer toutes les fonctionnalités.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = "👇 Faites défiler vers le bas pour compléter vos informations",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = NoorAmber
                )
            }
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
private fun ProfileInfoSection(account: UserAccount, bio: String) {
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
            text = bio,
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

        InfoItem(Icons.Outlined.Email, "Email", account.email)
        
        if (account.role == UserRole.TECHNICIAN) {
            Spacer(Modifier.height(16.dp))
            InfoItem(Icons.Outlined.Badge, "Spécialité", account.specialty ?: "Général")
        }

        Spacer(Modifier.height(16.dp))
        InfoItem(
            Icons.Outlined.LocationOn, 
            "Ma Zone", 
            if (account.workingZone.isNotEmpty()) account.workingZone else "Non assignée"
        )

        Spacer(Modifier.height(16.dp))
        InfoItem(
            Icons.Outlined.Phone, 
            "Téléphone", 
            if (account.phoneNumber.isNotEmpty()) account.phoneNumber else "Non renseigné"
        )

        Spacer(Modifier.height(16.dp))
        InfoItem(Icons.Outlined.Event, "Membre depuis", formatDate(account.createdAt))
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.FRENCH)
    return sdf.format(java.util.Date(timestamp))
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
private fun ProfileActions(
    onEdit: () -> Unit, 
    onLogout: () -> Unit, 
    onDelete: () -> Unit, 
    accentColor: Color,
    isTechnician: Boolean
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer le compte", fontWeight = FontWeight.Bold, color = NoorRed) },
            text = { Text("Cette action est irréversible. Toutes vos données seront effacées.") },
            confirmButton = {
                Button(
                    onClick = { 
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NoorRed)
                ) {
                    Text("Confirmer la suppression")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

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
        }

        if (isTechnician) {
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NoorRed),
                border = androidx.compose.foundation.BorderStroke(2.dp, NoorRed.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Supprimer mon compte", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true, name = "Profil Technicien")
@Composable
private fun PreviewProfileTechnician() {
    val mockAdmin = UserAccount("ID1", "Admin User", "admin@noor.tn", UserRole.ADMIN)
    SansaTheme {
        ProfileScreen(account = mockAdmin)
    }
}

@Preview(showBackground = true, name = "Profil Citoyen")
@Composable
private fun PreviewProfileCitizen() {
    val mockCitizen = UserAccount("ID2", "Ahmed Ben Salah", "ahmed@gmail.com", UserRole.CITIZEN)
    SansaTheme {
        ProfileScreen(account = mockCitizen)
    }
}
