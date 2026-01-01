// CamerasScreen.kt — Version moderne alignée + corrections complètes (Décembre 2025)
package tn.esprit.sansa.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.sansa.ui.theme.SansaTheme
import tn.esprit.sansa.ui.components.CoachMarkTooltip
import tn.esprit.sansa.ui.components.SwipeToDeleteContainer
import tn.esprit.sansa.ui.components.EmptyState
import tn.esprit.sansa.ui.components.StaggeredItem
import tn.esprit.sansa.ui.screens.models.*
import tn.esprit.sansa.ui.viewmodels.CamerasViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.style.TextAlign
import tn.esprit.sansa.ui.theme.*
// Palette Noor centralisée

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
    role: UserRole? = UserRole.CITIZEN,
    onNavigateToAddCamera: () -> Unit = {},
    viewModel: CamerasViewModel = viewModel()
) {
    val cameras by viewModel.cameras.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showTutorial by rememberSaveable { mutableStateOf(true) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<CameraStatus?>(null) }
    var selectedType by remember { mutableStateOf<CameraType?>(null) }
    var streamingCamera by remember { mutableStateOf<Camera?>(null) }

    val filteredCameras = remember(cameras, searchQuery, selectedStatus, selectedType) {
        cameras.filter { camera ->
            val matchesSearch = searchQuery.isEmpty() ||
                    camera.id.contains(searchQuery, ignoreCase = true) ||
                    camera.location.contains(searchQuery, ignoreCase = true) ||
                    camera.associatedStreetlight.contains(searchQuery, ignoreCase = true)
            val matchesStatus = selectedStatus == null || camera.status == selectedStatus
            val matchesType = selectedType == null || camera.type == selectedType
            matchesSearch && matchesStatus && matchesType
        }.sortedBy { it.location }
    }

    val stats = remember(cameras) {
        mapOf(
            "Total" to cameras.size,
            "En ligne" to cameras.count { it.status == CameraStatus.ONLINE || it.status == CameraStatus.RECORDING },
            "Hors service" to cameras.count { it.status == CameraStatus.OFFLINE || it.status == CameraStatus.ERROR }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { CamerasTopBarModern(stats = stats, onRefresh = { viewModel.refresh() }) },
        floatingActionButton = {
            if (role == UserRole.ADMIN || role == UserRole.TECHNICIAN) {
                FloatingActionButton(
                    onClick = onNavigateToAddCamera,
                    containerColor = NoorBlue,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter une nouvelle caméra")
                }
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
                Text(
                    "Filtrer par statut",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(12.dp))
                CameraStatusFilters(
                    selectedStatus = selectedStatus,
                    onStatusSelected = { selectedStatus = if (selectedStatus == it) null else it }
                )
            }

            item {
                Text(
                    "Filtrer par type de caméra",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
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

            if (filteredCameras.isEmpty()) {
                item {
                    EmptyState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        icon = Icons.Default.VideocamOff,
                        title = "Aucune caméra",
                        description = "Vérifiez vos critères de recherche ou ajoutez-en une.",
                        actionLabel = "Ajouter une caméra",
                        onActionClick = onNavigateToAddCamera,
                        iconColor = NoorBlue
                    )
                }
            } else {
                itemsIndexed(
                    items = filteredCameras,
                    key = { _, camera -> camera.id }
                ) { index, camera ->
                    Box {
                        StaggeredItem(index = index) {
                            SwipeToDeleteContainer(
                                item = camera,
                                onDelete = { viewModel.deleteCamera(camera.id) }
                            ) { item: Camera ->
                                CameraCard(
                                    camera = item,
                                    onViewStream = { streamingCamera = it }
                                )
                            }
                        }

                        if (index == 0 && showTutorial) {
                            CoachMarkTooltip(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 16.dp)
                                    .offset(x = 16.dp, y = 32.dp),
                                text = "Glissez vers la gauche pour supprimer",
                                onDismiss = { showTutorial = false }
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }

        if (streamingCamera != null) {
            CameraStreamDialog(
                camera = streamingCamera!!,
                onDismiss = { streamingCamera = null }
            )
        }
    }
}

@Composable
private fun CamerasTopBarModern(stats: Map<String, Int>, onRefresh: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(NoorBlue.copy(alpha = 0.95f), NoorBlue.copy(alpha = 0.65f))
                )
            )
            .padding(horizontal = 20.dp, vertical = 28.dp)
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
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.6).sp
                    )
                }
                IconButton(onClick = onRefresh) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Actualiser",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

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
                fontSize = 24.sp,
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
private fun CameraCard(
    camera: Camera,
    onViewStream: (Camera) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val elevation by animateDpAsState(
        targetValue = if (pressed) 4.dp else 1.dp,
        animationSpec = tween(200)
    )
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = tween(200)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .clickable(interactionSource = interactionSource, indication = null) {
                    expanded = !expanded
                }
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(camera.type.color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        camera.type.icon,
                        contentDescription = null,
                        tint = camera.type.color,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = camera.location,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Badge(
                            containerColor = camera.status.color,
                            modifier = Modifier.height(22.dp)
                        ) {
                            Text(
                                text = camera.status.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        Badge(
                            containerColor = camera.type.color.copy(alpha = 0.15f),
                            modifier = Modifier.height(22.dp)
                        ) {
                            Text(
                                text = camera.type.displayName,
                                fontSize = 11.sp,
                                color = camera.type.color,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Réduire" else "Développer",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModernStatItem(
                    value = camera.resolution,
                    label = "Résolution",
                    modifier = Modifier.weight(1f)
                )
                ModernStatItem(
                    value = if (camera.nightVision) "Oui" else "Non",
                    label = "Vision nocturne",
                    modifier = Modifier.weight(1f)
                )
                ModernStatItem(
                    value = camera.lastMaintenance,
                    label = "Dern. maintenance",
                    modifier = Modifier.weight(1f)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 14.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    InfoRow(
                        icon = Icons.Default.LocationOn,
                        label = "Emplacement",
                        value = camera.location
                    )

                    Spacer(Modifier.height(10.dp))

                    InfoRow(
                        icon = Icons.Default.Lightbulb,
                        label = "Lampadaire",
                        value = camera.associatedStreetlight
                    )

                    Spacer(Modifier.height(10.dp))

                    InfoRow(
                        icon = Icons.Default.CalendarMonth,
                        label = "Installation",
                        value = camera.installDate
                    )

                    Spacer(Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { /* TODO */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Settings, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Configurer", fontSize = 13.sp)
                        }
                        Button(
                            onClick = { onViewStream(camera) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = NoorCyan),
                            enabled = camera.streamUrl.isNotBlank()
                        ) {
                            Icon(Icons.Default.Videocam, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Visualiser", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernStatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun CameraStreamDialog(
    camera: Camera,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Direct : ${camera.location}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Flux : ${camera.streamUrl}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null)
                    }
                }

                // Stream View
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                webViewClient = WebViewClient()
                                settings.apply {
                                    javaScriptEnabled = true
                                    useWideViewPort = true
                                    loadWithOverviewMode = true
                                    builtInZoomControls = true
                                    displayZoomControls = false
                                }
                                loadUrl(camera.streamUrl)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Overlay Status
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NoorRed)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("LIVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = { /* Screenshot */ }) {
                        Icon(Icons.Default.PhotoCamera, null, tint = NoorBlue)
                    }
                    IconButton(onClick = { /* Record */ }) {
                        Icon(Icons.Default.FiberManualRecord, null, tint = NoorRed)
                    }
                    IconButton(onClick = { /* Fullscreen */ }) {
                        Icon(Icons.Default.Fullscreen, null, tint = NoorBlue)
                    }
                }
            }
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