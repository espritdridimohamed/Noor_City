// HomeScreen.kt – VERSION MODERNE CORRIGÉE ET COMPLÈTE (2025 Style)
package tn.esprit.sansa.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import tn.esprit.sansa.ui.theme.SansaTheme

// Couleurs Noor définies au niveau du fichier pour être accessibles partout
private val NoorBlue = Color(0xFF1E40AF)
private val NoorGreen = Color(0xFF10B981)
private val NoorAmber = Color(0xFFF59E0B)
private val NoorRed = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenModern() {

    var isDarkMode by remember { mutableStateOf(false) }

    SansaTheme(darkTheme = isDarkMode) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                                        )
                                    )
                                )
                                .blur(radiusX = 20.dp, radiusY = 20.dp) // Effet glassmorphism
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { isDarkMode = !isDarkMode }
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.align(Alignment.CenterStart)
                            ) {
                                Icon(
                                    imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = "Basculer le thème",
                                    tint = NoorBlue,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "NoorCity",
                                    color = NoorBlue,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            BadgedBox(badge = { Badge { Text("3") } }) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                Text(
                    text = "Tableau de bord",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Lampadaires intelligents",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 4.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // STATUTS MODERNES
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ModernStatusCard(count = 980, label = "En service", color = NoorGreen)
                    ModernStatusCard(count = 37, label = "Maintenance", color = NoorAmber)
                    ModernStatusCard(count = 8, label = "Anomalie", color = NoorRed)
                }

                Spacer(modifier = Modifier.height(48.dp))

                // MENU 2×4 ULTRA MODERNE
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        ModernMenuCard(title = "Lampadaires", icon = Icons.Default.LightMode)
                        ModernMenuCard(title = "Caméras", icon = Icons.Default.Videocam)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        ModernMenuCard(title = "Capteurs", icon = Icons.Default.Sensors)
                        ModernMenuCard(title = "Réclamations", icon = Icons.Default.WarningAmber, badgeCount = 15)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        ModernMenuCard(title = "Interventions", icon = Icons.Default.Build, badgeCount = 7)
                        ModernMenuCard(title = "Zones", icon = Icons.Default.LocationOn)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        ModernMenuCard(title = "Utilisateurs", icon = Icons.Default.People)
                        ModernMenuCard(title = "Support", icon = Icons.Default.HeadsetMic, badgeCount = 4)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.ModernStatusCard(
    count: Int,
    label: String,
    color: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val elevation by animateDpAsState(if (isPressed) 16.dp else 8.dp)

    Card(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(color.copy(0.2f), Color.Transparent),
                        radius = 600f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$count",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RowScope.ModernMenuCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeCount: Int = 0
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateDpAsState(if (isPressed) (-8).dp else 0.dp)

    Card(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .offset(y = scale)
            .shadow(12.dp, RoundedCornerShape(28.dp), clip = false),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        interactionSource = interactionSource,
        onClick = { /* À implémenter : navigation vers l'écran correspondant */ }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.7f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                BadgedBox(
                    badge = {
                        if (badgeCount > 0) {
                            Badge(
                                containerColor = NoorRed,
                                modifier = Modifier.offset(x = 18.dp, y = -18.dp)
                            ) {
                                Text("$badgeCount", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = NoorBlue,
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// PREVIEWS
@Preview(showBackground = true, name = "Mode Clair")
@Composable
fun HomeScreenModernPreview() {
    SansaTheme(darkTheme = false) {
        HomeScreenModern()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Mode Sombre")
@Composable
fun HomeScreenModernDarkPreview() {
    SansaTheme(darkTheme = true) {
        HomeScreenModern()
    }
}