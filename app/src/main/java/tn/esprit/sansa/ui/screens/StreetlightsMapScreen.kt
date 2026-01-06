// StreetlightsMapScreen.kt — Version corrigée et alignée style moderne (Décembre 2025)
package tn.esprit.sansa.ui.screens

import androidx.compose.ui.graphics.toArgb
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.ui.viewinterop.AndroidView
import tn.esprit.sansa.ui.theme.SansaTheme
import android.content.res.Configuration
import androidx.compose.runtime.saveable.rememberSaveable
import tn.esprit.sansa.ui.components.CoachMarkTooltip
import tn.esprit.sansa.ui.components.EmptyState
import tn.esprit.sansa.ui.components.NoorChargeDialog

import tn.esprit.sansa.ui.screens.models.*
import tn.esprit.sansa.ui.viewmodels.StreetlightsViewModel
import tn.esprit.sansa.ui.viewmodels.CamerasViewModel
import tn.esprit.sansa.ui.components.SwipeActionsContainer
import tn.esprit.sansa.ui.viewmodels.NoorChargeViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import tn.esprit.sansa.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreetlightsMapScreen(
    viewModel: StreetlightsViewModel = viewModel(),
    camerasViewModel: CamerasViewModel = viewModel(),
    role: UserRole? = UserRole.CITIZEN,
    modifier: Modifier = Modifier,
    onNavigateToAddStreetlight: () -> Unit = {},
    onNavigateToEditStreetlight: (String) -> Unit = {}
) {
    val streetlightsList by viewModel.streetlights.collectAsState()
    val camerasList by camerasViewModel.cameras.collectAsState()
    val zones by viewModel.zones.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showTutorial by rememberSaveable { mutableStateOf(true) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<StreetlightStatus?>(null) }
    var mapExpanded by remember { mutableStateOf(true) }
    var chargingStreetlight by remember { mutableStateOf<Streetlight?>(null) }

    val filteredStreetlights = remember(streetlightsList.size, searchQuery, selectedStatus) {
        streetlightsList.filter { light ->
            val matchesSearch = searchQuery.isEmpty() ||
                    light.id.contains(searchQuery, ignoreCase = true) ||
                    light.zoneId.contains(searchQuery, ignoreCase = true) ||
                    light.address.contains(searchQuery, ignoreCase = true)
            val matchesStatus = selectedStatus == null || light.status == selectedStatus
            matchesSearch && matchesStatus
        }
    }

    val stats = remember(streetlightsList) {
        mapOf(
            "Total" to streetlightsList.size,
            "Allumés" to streetlightsList.count { it.status == StreetlightStatus.ON },
            "Hors service" to streetlightsList.count { it.status == StreetlightStatus.ERROR || it.status == StreetlightStatus.MAINTENANCE }
        )
    }

    val totalConsumption = remember(streetlightsList) {
        streetlightsList.filter { it.status == StreetlightStatus.ON }
            .sumOf { it.powerConsumption }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { StreetlightsTopBar(stats = stats, totalConsumption = totalConsumption) },
        floatingActionButton = {
            if (role == UserRole.ADMIN || role == UserRole.TECHNICIAN) {
                FloatingActionButton(
                    onClick = onNavigateToAddStreetlight,
                    containerColor = NoorBlue,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nouveau lampadaire")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StreetlightSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Filtrer par statut",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(12.dp))
                StatusFilters(
                    selectedStatus = selectedStatus,
                    onStatusSelected = { selectedStatus = if (selectedStatus == it) null else it }
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
                AnimatedVisibility(
                    visible = mapExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    InteractiveMap(
                        filteredStreetlights = filteredStreetlights,
                        cameras = camerasList,
                        zones = zones,
                        onCharge = { id ->
                            val light = streetlightsList.find { it.id == id }
                            if (light != null) chargingStreetlight = light
                        }
                    )
                }
            }

            item {
                Button(
                    onClick = { mapExpanded = !mapExpanded },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NoorBlue)
                ) {
                    Text(if (mapExpanded) "Masquer la carte" else "Afficher la carte")
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    "${filteredStreetlights.size} lampadaire${if (filteredStreetlights.size != 1) "s" else ""}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            if (filteredStreetlights.isEmpty()) {
                item {
                    EmptyState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        icon = Icons.Default.Lightbulb,
                        title = "Aucun lampadaire trouvé",
                        description = "Vérifiez vos filtres ou ajoutez un nouveau lampadaire.",
                        actionLabel = "Nouveau lampadaire",
                        onActionClick = onNavigateToAddStreetlight,
                        iconColor = NoorAmber
                    )
                }
            } else {
                itemsIndexed(
                    items = filteredStreetlights,
                    key = { _, light -> light.id }
                ) { index, light ->
                    Box {
                        tn.esprit.sansa.ui.components.StaggeredItem(index = index) {
                            SwipeActionsContainer(
                                item = light,
                                onDelete = { viewModel.deleteStreetlight(it.id) },
                                onEdit = if (role == UserRole.ADMIN) { { onNavigateToEditStreetlight(it.id) } } else null
                            ) { item ->
                                val zoneName = zones.find { it.id == item.zoneId }?.name ?: item.zoneId
                                StreetlightCard(
                                    streetlight = item, 
                                    zoneName = zoneName,
                                    onCharge = { chargingStreetlight = it }
                                )
                            }
                        }

                        if (index == 0 && showTutorial) {
                            CoachMarkTooltip(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 16.dp)
                                    .offset(x = 16.dp, y = 32.dp),
                                text = "Glissez pour modifier (droite) ou supprimer (gauche)",
                                onDismiss = { showTutorial = false }
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }

    if (chargingStreetlight != null) {
        NoorChargeDialog(
            streetlight = chargingStreetlight!!,
            onDismiss = { chargingStreetlight = null }
        )
    }
}

// ────────────────────────────────────────────────────────────────────────────────
//  TopBar, SearchBar, Filters, Map, Card, etc.
// ────────────────────────────────────────────────────────────────────────────────

@Composable
private fun StreetlightsTopBar(stats: Map<String, Int>, totalConsumption: Double) {
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
                        "Carte des lampadaires",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        letterSpacing = 0.4.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Gestion intelligente",
                        color = Color.White,
                        fontSize = 26.sp,
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

            Spacer(Modifier.height(16.dp))

            QuickStatCardCompact(
                value = "%.1f kW".format(totalConsumption / 1000),
                label = "Consommation totale"
            )
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
private fun StreetlightSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Rechercher par ID, zone ou adresse...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Effacer")
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
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
private fun StatusFilters(
    selectedStatus: StreetlightStatus?,
    onStatusSelected: (StreetlightStatus) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        StreetlightStatus.entries.forEach { status ->
            val isSelected = selectedStatus == status
            FilterChip(
                onClick = { onStatusSelected(status) },
                label = { Text(status.displayName) },
                selected = isSelected,
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
private fun InteractiveMap(
    filteredStreetlights: List<Streetlight>, 
    cameras: List<Camera>, 
    zones: List<Zone>,
    onCharge: (String) -> Unit = {}
) {
    val markersJson = remember(filteredStreetlights, cameras, zones) {
        val streetlightMarkers = filteredStreetlights.map { light ->
            val zoneName = zones.find { it.id == light.zoneId }?.name ?: light.zoneId
            val safeAddress = light.address.replace("'", "\\'")
            val safeZoneName = zoneName.replace("'", "\\'")
            "{type: 'light', lat: ${light.latitude}, lng: ${light.longitude}, name: '${light.id}', status: '${light.status.displayName}', address: '$safeAddress', zone: '$safeZoneName', hasCharger: ${light.hasCharger}, color: '${String.format("#%06X", (0xFFFFFF and light.status.color.toArgb()))}'}"
        }
        
        val cameraMarkers = cameras.filter { it.associatedStreetlight.isNotBlank() }.mapNotNull { cam ->
            val linkedLight = filteredStreetlights.find { it.id == cam.associatedStreetlight }
            if (linkedLight != null) {
                val safeZone = cam.zone.replace("'", "\\'")
                // On décale légèrement la caméra par rapport au lampadaire s'ils sont au même endroit
                val lat = linkedLight.latitude + 0.0001
                val lng = linkedLight.longitude + 0.0001
                "{type: 'camera', lat: $lat, lng: $lng, name: '${cam.id}', status: '${cam.alertStatus}', isAccident: ${cam.isAccidentActive}, address: '${cam.location}', zone: '$safeZone', color: '${if (cam.isAccidentActive) "#FF3B30" else "#5856D6"}'}"
            } else null
        }

        (streetlightMarkers + cameraMarkers).joinToString(",")
    }

    val html = """
        <!DOCTYPE html>
        <html>
        <head>
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                #map { height: 100vh; width: 100vw; margin: 0; padding: 0; border-radius: 20px; }
                .marker-icon {
                    width: 20px; height: 20px;
                    border-radius: 50%;
                    border: 2px solid white;
                    box-shadow: 0 0 5px rgba(0,0,0,0.3);
                    display: flex; align-items: center; justify-content: center;
                    font-size: 12px;
                }
                .camera-icon {
                    width: 24px; height: 24px;
                    border-radius: 50%;
                    display: flex; align-items: center; justify-content: center;
                    color: white; font-size: 14px;
                    border: 2px solid white;
                    box-shadow: 0 0 8px rgba(0,0,0,0.4);
                }
                .blinking {
                    animation: blinker 1s linear infinite;
                }
                @keyframes blinker {
                    50% { opacity: 0.3; background-color: red; transform: scale(1.2); }
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', {zoomControl: false}).setView([36.8065, 10.1815], 14);
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);

                var markers = [$markersJson];
                var group = L.featureGroup();
                
                markers.forEach(function(m) {
                    var iconHtml = '';
                    if (m.type === 'light') {
                        var innerContent = m.hasCharger ? '⚡' : '';
                        iconHtml = '<div class="marker-icon" style="background-color: ' + m.color + '">' + innerContent + '</div>';
                    } else {
                        // Icone Caméra avec pulsation si accident
                        var blinkClass = m.isAccident ? 'blinking' : '';
                        iconHtml = '<div class="camera-icon ' + blinkClass + '" style="background-color: ' + m.color + '">📸</div>';
                    }

                    var icon = L.divIcon({
                        className: 'custom-div-icon',
                        html: iconHtml,
                        iconSize: [24, 24],
                        iconAnchor: [12, 12]
                    });

                    var marker = L.marker([m.lat, m.lng], {icon: icon}).addTo(map);
                    
                    var chargerIcon = m.hasCharger ? ' ⚡' : '';
                    var popupHtml = "<b>" + m.name + "</b> (" + (m.type === 'light' ? '💡' : '📹') + chargerIcon + ")<br>" + 
                                   "<i>" + m.zone + "</i><br>" +
                                   "<hr style='margin: 5px 0'>" +
                                   "📍 " + m.address + "<br>" +
                                   "🚦 Statut: " + m.status;
                    
                    if (m.hasCharger) {
                        popupHtml += "<br><button onclick=\"Android.onChargeRequested('" + m.name + "')\" style='margin-top:8px; width:100%; padding:6px; background:#34C759; color:white; border:none; border-radius:8px; font-weight:bold; cursor:pointer'>⚡ Recharger</button>";
                    }

                    if (m.isAccident) {
                        popupHtml += "<br><b style='color:red'>🚨 ACCIDENT IA DÉTECTÉ</b>";
                    }

                    marker.bindPopup(popupHtml);
                    group.addLayer(marker);
                });

                if (markers.length > 0) {
                    map.fitBounds(group.getBounds().pad(0.2));
                }
            </script>
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onChargeRequested(id: String) {
                        onCharge(id)
                    }
                }, "Android")
                loadDataWithBaseURL("https://osm.org", html, "text/html", "UTF-8", null)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        update = { webView ->
            webView.loadDataWithBaseURL("https://osm.org", html, "text/html", "UTF-8", null)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StreetlightCard(
    streetlight: Streetlight, 
    zoneName: String,
    onCharge: (Streetlight) -> Unit = {}
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
                        .background(streetlight.status.color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = streetlight.status.color,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = streetlight.id,
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
                            containerColor = streetlight.status.color,
                            modifier = Modifier.height(22.dp)
                        ) {
                            Text(
                                text = streetlight.status.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        if (streetlight.hasCharger) {
                            Spacer(Modifier.width(8.dp))
                            Badge(
                                containerColor = NoorBlue,
                                modifier = Modifier.height(22.dp)
                            ) {
                                Icon(
                                    Icons.Default.ElectricBolt,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "CHARGE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
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
                    value = streetlight.bulbType.displayName,
                    label = "Ampoule",
                    modifier = Modifier.weight(1f)
                )
                ModernStatItem(
                    value = "${streetlight.powerConsumption} W",
                    label = "Conso",
                    modifier = Modifier.weight(1f)
                )
                ModernStatItem(
                    value = zoneName,
                    label = "Zone",
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
                        icon = Icons.Default.Home,
                        label = "Emplacement",
                        value = "Voir sur la carte au-dessus"
                    )

                    if (streetlight.hasCharger) {
                        Spacer(Modifier.height(10.dp))
                        InfoRow(
                            icon = Icons.Default.ElectricBolt,
                            label = "Noor Charge",
                            value = "Borne de recharge disponible"
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    InfoRow(
                        icon = Icons.Default.LocationOn,
                        label = "Coordonnées",
                        value = "${streetlight.latitude}, ${streetlight.longitude}"
                    )

                    Spacer(Modifier.height(10.dp))

                    InfoRow(
                        icon = Icons.Default.Power,
                        label = "Consommation",
                        value = "${streetlight.powerConsumption} W"
                    )

                    Spacer(Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { /* TODO */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Modifier", fontSize = 13.sp)
                        }

                        if (streetlight.hasCharger) {
                            Button(
                                onClick = { onCharge(streetlight) },
                                modifier = Modifier.weight(1.2f),
                                colors = ButtonDefaults.buttonColors(containerColor = NoorGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.ElectricBolt, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Recharger", fontSize = 13.sp)
                            }
                        }

                        Button(
                            onClick = { /* TODO */ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = NoorBlue)
                        ) {
                            Icon(Icons.Default.Settings, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Contrôler", fontSize = 13.sp)
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

@Preview(showBackground = true, name = "Mode Clair")
@Composable
fun StreetlightsMapScreenPreview() {
    SansaTheme(darkTheme = false) {
        StreetlightsMapScreen()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Mode Sombre")
@Composable
fun StreetlightsMapScreenDarkPreview() {
    SansaTheme(darkTheme = true) {
        StreetlightsMapScreen()
    }
}