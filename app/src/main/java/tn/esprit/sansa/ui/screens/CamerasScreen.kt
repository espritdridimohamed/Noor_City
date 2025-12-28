// CamerasScreen.kt — Version corrigée et complète (compile sans erreur)
package tn.esprit.sansa.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.sansa.ui.theme.SansaTheme
import tn.esprit.sansa.models.* // Import des modèles et couleurs Noor

private val mockCameras = listOf(
    Camera(
        id = "CAM001",
        location = "Avenue Habib Bourguiba - Intersection 1",
        status = CameraStatus.ONLINE,
        associatedStreetlight = "L001",
        type = CameraType.PTZ,
        resolution = "4K (3840x2160)",
        nightVision = true,
        installDate = "01/07/2024",
        lastMaintenance = "11/12/2024",
        recordingEnabled = true,
        motionDetection = true,
        zone = "Centre-ville"
    ),
    Camera(
        id = "CAM002",
        location = "Rue de la Liberté - Entrée principale",
        status = CameraStatus.RECORDING,
        associatedStreetlight = "L003",
        type = CameraType.DOME,
        resolution = "1080p (1920x1080)",
        nightVision = true,
        installDate = "15/08/2024",
        lastMaintenance = "26/11/2024",
        recordingEnabled = true,
        motionDetection = true,
        zone = "Zone A"
    ),
    Camera(
        id = "CAM003",
        location = "Boulevard du 7 Novembre - Parking",
        status = CameraStatus.OFFLINE,
        associatedStreetlight = "L005",
        type = CameraType.BULLET,
        resolution = "1080p (1920x1080)",
        nightVision = true,
        installDate = "10/06/2024",
        lastMaintenance = "26/10/2024",
        recordingEnabled = false,
        motionDetection = false,
        zone = "Zone B"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CamerasScreen(
    modifier: Modifier = Modifier,
    onNavigateToAddCamera: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<CameraStatus?>(null) }
    var selectedType by remember { mutableStateOf<CameraType?>(null) }

    val filteredCameras = remember(searchQuery, selectedStatus, selectedType) {
        mockCameras.filter { camera ->
            val matchesSearch = searchQuery.isEmpty() ||
                    camera.id.contains(searchQuery, ignoreCase = true) ||
                    camera.location.contains(searchQuery, ignoreCase = true) ||
                    camera.associatedStreetlight.contains(searchQuery, ignoreCase = true)
            val matchesStatus = selectedStatus == null || camera.status == selectedStatus
            val matchesType = selectedType == null || camera.type == selectedType
            matchesSearch && matchesStatus && matchesType
        }.sortedBy { it.location }
    }

    val stats = remember(mockCameras) {
        mapOf(
            "Total" to mockCameras.size,
            "En ligne" to mockCameras.count { it.status == CameraStatus.ONLINE || it.status == CameraStatus.RECORDING },
            "Hors service" to mockCameras.count { it.status == CameraStatus.OFFLINE || it.status == CameraStatus.ERROR }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { CamerasTopBarModern(stats = stats) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddCamera,
                containerColor = NoorBlue,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ajouter une nouvelle caméra"
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            item { CameraSearchBar(query = searchQuery, onQueryChange = { searchQuery = it }) }

            item {
                Text("Filtrer par statut", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(12.dp))
                CameraStatusFilters(
                    selectedStatus = selectedStatus,
                    onStatusSelected = { selectedStatus = if (selectedStatus == it) null else it }
                )
            }

            item {
                Text("Filtrer par type de caméra", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(12.dp))
                CameraTypeFilters(
                    selectedType = selectedType,
                    onTypeSelected = { selectedType = if (selectedType == it) null else it }
                )
            }

            item {
                Text(
                    text = "${filteredCameras.size} caméra${if (filteredCameras.size != 1) "s" else ""} trouvé${if (filteredCameras.size != 1) "es" else "e"}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(filteredCameras) { camera ->
                CameraCard(camera = camera)
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun CamerasTopBarModern(stats: Map<String, Int>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(NoorBlue.copy(alpha = 0.95f), NoorBlue.copy(alpha = 0.65f))
                )
            )
            .padding(horizontal = 20.dp, vertical = 28.dp)  // ← Réduit significativement
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Caméras de surveillance",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        letterSpacing = 0.4.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Sécurité en temps réel",
                        color = Color.White,
                        fontSize = 26.sp,                    // ← réduit de 32 → 26
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.6).sp
                    )
                }
                IconButton(onClick = { /* TODO: Refresh */ }) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Actualiser",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))  // ← réduit de 32 → 24

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                stats.forEach { (label, value) ->
                    QuickStatCardCompact(
                        value = value.toString(),
                        label = label,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// Carte de stats plus compacte (même style que pour Sensors)
@Composable
private fun QuickStatCardCompact(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.22f)
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = Color.White,
                fontSize = 24.sp,                  // ← réduit de 28 → 24
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.8).sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 11.sp,
                letterSpacing = 0.3.sp
            )
        }
    }
}

@Composable
private fun QuickStatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = label, color = Color.White.copy(0.9f), fontSize = 12.sp)
        }
    }
}

@Composable
private fun CameraSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Rechercher par ID, emplacement ou lampadaire...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Effacer")
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NoorBlue,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            cursorColor = NoorBlue
        )
    )
}

@Composable
private fun CameraStatusFilters(
    selectedStatus: CameraStatus?,
    onStatusSelected: (CameraStatus) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        CameraStatus.entries.forEach { status ->
            val isSelected = selectedStatus == status
            FilterChip(
                onClick = { onStatusSelected(status) },
                label = { Text(status.displayName) },
                selected = isSelected,
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = status.color,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = status.color
                )
            )
        }
    }
}

@Composable
private fun CameraTypeFilters(
    selectedType: CameraType?,
    onTypeSelected: (CameraType) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        CameraType.entries.forEach { type ->
            val isSelected = selectedType == type
            FilterChip(
                onClick = { onTypeSelected(type) },
                label = { Text(type.displayName) },
                selected = isSelected,
                leadingIcon = {
                    Icon(type.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = type.color,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = type.color
                )
            )
        }
    }
}

@Composable
private fun CameraCard(camera: Camera) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val elevation by animateDpAsState(if (pressed) 16.dp else 8.dp)
    val offsetY by animateDpAsState(if (pressed) (-6).dp else 0.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = offsetY)
            .shadow(elevation, RoundedCornerShape(28.dp))
            .clickable(interactionSource = interactionSource, indication = null) { expanded = !expanded },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        listOf(camera.status.color.copy(0.1f), MaterialTheme.colorScheme.surface)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(camera.type.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            camera.type.icon,
                            contentDescription = null,
                            tint = camera.type.color,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(camera.location, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Badge(containerColor = camera.status.color) {
                                Text(camera.status.displayName, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.width(8.dp))
                            Badge(containerColor = camera.type.color.copy(alpha = 0.2f)) {
                                Text(camera.type.displayName, fontSize = 10.sp, color = camera.type.color, fontWeight = FontWeight.Medium)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            camera.id,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(value = camera.resolution, label = "Résolution", modifier = Modifier.weight(1f))
                    StatItem(value = if (camera.nightVision) "Oui" else "Non", label = "Vision nocturne", modifier = Modifier.weight(1f))
                    StatItem(value = camera.lastMaintenance, label = "Dernière maintenance", modifier = Modifier.weight(1f))
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                        InfoRow(icon = Icons.Default.LocationOn, label = "Emplacement", value = camera.location)

                        Spacer(Modifier.height(12.dp))

                        InfoRow(icon = Icons.Default.Lightbulb, label = "Lampadaire associé", value = camera.associatedStreetlight)

                        Spacer(Modifier.height(12.dp))

                        InfoRow(icon = Icons.Default.CalendarMonth, label = "Date d'installation", value = camera.installDate)

                        Spacer(Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { /* TODO */ }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Settings, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Configurer")
                            }
                            Button(
                                onClick = { /* TODO */ },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = NoorCyan)
                            ) {
                                Icon(Icons.Default.Videocam, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Visualiser")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Preview(showBackground = true, name = "Mode Clair")
@Composable
private fun PreviewCamerasLight() {
    SansaTheme(darkTheme = false) {
        CamerasScreen()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Mode Sombre")
@Composable
private fun PreviewCamerasDark() {
    SansaTheme(darkTheme = true) {
        CamerasScreen()
    }
}