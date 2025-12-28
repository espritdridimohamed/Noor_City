// StreetlightsMapScreen.kt – VERSION AVEC NAVIGATION
package tn.esprit.sansa.ui.screens

import android.os.Build
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import tn.esprit.sansa.ui.theme.SansaTheme
import android.content.res.Configuration

// Palette Noor (définitions locales privées pour ce fichier)
private val NoorBlue = Color(0xFF1E40AF)
private val NoorGreen = Color(0xFF10B981)
private val NoorAmber = Color(0xFFF59E0B)
private val NoorRed = Color(0xFFEF4444)

// Modèles pour les lampadaires (définis ici pour éviter les conflits)
enum class BulbType(val displayName: String) {
    LED("LED"),
    HALOGEN("Halogène"),
    SODIUM("Sodium haute pression"),
    MERCURY("Mercure")
}

enum class StreetlightStatus(val displayName: String, val color: Color) {
    ON("Allumé", NoorGreen),
    OFF("Éteint", Color.Gray),
    MAINTENANCE("Maintenance", NoorAmber),
    ERROR("Défaillance", NoorRed)
}

data class Streetlight(
    val id: String,
    val bulbType: BulbType,
    val status: StreetlightStatus,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val powerConsumption: Double,
    val address: String
)

private val mockStreetlights = listOf(
    Streetlight("L001", BulbType.LED, StreetlightStatus.ON, "Zone A", 36.8065, 10.1815, 45.2, "Avenue Habib Bourguiba"),
    Streetlight("L002", BulbType.LED, StreetlightStatus.ON, "Zone A", 36.8070, 10.1820, 47.8, "Rue de la Liberté"),
    Streetlight("L003", BulbType.HALOGEN, StreetlightStatus.MAINTENANCE, "Zone B", 36.8055, 10.1810, 120.5, "Boulevard du 7 Novembre"),
    Streetlight("L004", BulbType.LED, StreetlightStatus.ON, "Zone B", 36.8060, 10.1825, 43.1, "Place de la République"),
    Streetlight("L005", BulbType.SODIUM, StreetlightStatus.ERROR, "Zone C", 36.8075, 10.1805, 0.0, "Avenue Mohammed V"),
    Streetlight("L006", BulbType.LED, StreetlightStatus.OFF, "Zone C", 36.8050, 10.1830, 0.0, "Rue de Carthage"),
    Streetlight("L007", BulbType.LED, StreetlightStatus.ON, "Zone D", 36.8080, 10.1815, 46.5, "Avenue de France"),
    Streetlight("L008", BulbType.MERCURY, StreetlightStatus.ON, "Zone D", 36.8045, 10.1820, 95.3, "Rue Ibn Khaldoun")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreetlightsMapScreen(
    modifier: Modifier = Modifier
) {
    // État pour gérer la navigation entre les écrans
    var showAddStreetlight by remember { mutableStateOf(false) }

    if (showAddStreetlight) {
        // Afficher l'écran d'ajout de lampadaire
        AddStreetlightScreen(
            onAddSuccess = {
                // Retour à l'écran principal après l'ajout
                showAddStreetlight = false
            },
            onBackPressed = {
                // Retour à l'écran principal si l'utilisateur annule
                showAddStreetlight = false
            }
        )
    } else {
        // Afficher l'écran principal
        StreetlightsMapMainScreen(
            modifier = modifier,
            onNavigateToAddStreetlight = {
                showAddStreetlight = true
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StreetlightsMapMainScreen(
    modifier: Modifier = Modifier,
    onNavigateToAddStreetlight: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<StreetlightStatus?>(null) }
    var mapExpanded by remember { mutableStateOf(true) }

    val filteredStreetlights = remember(searchQuery, selectedStatus) {
        mockStreetlights.filter { light ->
            val matchesSearch = searchQuery.isEmpty() ||
                    light.id.contains(searchQuery, ignoreCase = true) ||
                    light.location.contains(searchQuery, ignoreCase = true) ||
                    light.address.contains(searchQuery, ignoreCase = true)
            val matchesStatus = selectedStatus == null || light.status == selectedStatus
            matchesSearch && matchesStatus
        }
    }

    val stats = remember(mockStreetlights) {
        mapOf(
            "Total" to mockStreetlights.size,
            "Allumés" to mockStreetlights.count { it.status == StreetlightStatus.ON },
            "Hors service" to mockStreetlights.count { it.status == StreetlightStatus.ERROR || it.status == StreetlightStatus.MAINTENANCE }
        )
    }

    val totalConsumption = remember(mockStreetlights) {
        mockStreetlights.filter { it.status == StreetlightStatus.ON }
            .sumOf { it.powerConsumption }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { StreetlightsTopBar(stats = stats, totalConsumption = totalConsumption) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddStreetlight,
                containerColor = NoorBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Nouveau lampadaire", fontWeight = FontWeight.SemiBold)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Carte rétractable
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .heightIn(min = 60.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { mapExpanded = !mapExpanded }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Map,
                                contentDescription = null,
                                tint = NoorBlue,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Carte des lampadaires",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            if (mapExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (mapExpanded) "Réduire" else "Agrandir"
                        )
                    }

                    AnimatedVisibility(
                        visible = mapExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            LeafletMapView(streetlights = filteredStreetlights)
                        }
                    }
                }
            }

            // Liste des lampadaires
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { SearchBar(query = searchQuery, onQueryChange = { searchQuery = it }) }

                item {
                    StatusFilters(
                        selectedStatus = selectedStatus,
                        onStatusSelected = { selectedStatus = if (selectedStatus == it) null else it }
                    )
                }

                item {
                    Text(
                        text = "${filteredStreetlights.size} lampadaire${if (filteredStreetlights.size != 1) "s" else ""} trouvé${if (filteredStreetlights.size != 1) "s" else ""}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                items(filteredStreetlights) { light ->
                    StreetlightCard(streetlight = light)
                }

                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
private fun LeafletMapView(streetlights: List<Streetlight>) {
    val mapHtml = remember(streetlights) {
        buildLeafletHtml(streetlights)
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.allowFileAccess = true
                settings.allowContentAccess = true

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        println("Carte Leaflet chargée avec succès")
                    }

                    @Suppress("DEPRECATION")
                    override fun onReceivedError(
                        view: WebView?,
                        errorCode: Int,
                        description: String?,
                        failingUrl: String?
                    ) {
                        super.onReceivedError(view, errorCode, description, failingUrl)
                        println("Erreur WebView : $description (URL: $failingUrl)")
                    }
                }

                loadDataWithBaseURL(
                    "https://unpkg.com",
                    mapHtml,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(
                "https://unpkg.com",
                mapHtml,
                "text/html",
                "UTF-8",
                null
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(16.dp))
    )
}

private fun buildLeafletHtml(streetlights: List<Streetlight>): String {
    if (streetlights.isEmpty()) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { margin: 0; padding: 0; display: flex; justify-content: center; align-items: center; height: 300px; background: #f3f4f6; font-family: sans-serif; }
                    .message { text-align: center; color: #6b7280; padding: 20px; }
                </style>
            </head>
            <body>
                <div class="message"><p>Aucun lampadaire à afficher</p></div>
            </body>
            </html>
        """.trimIndent()
    }

    val centerLat = streetlights.map { it.latitude }.average()
    val centerLon = streetlights.map { it.longitude }.average()

    val markers = streetlights.joinToString("\n") { light ->
        val color = when (light.status) {
            StreetlightStatus.ON -> "#10B981"
            StreetlightStatus.OFF -> "#6B7280"
            StreetlightStatus.MAINTENANCE -> "#F59E0B"
            StreetlightStatus.ERROR -> "#EF4444"
        }
        """
        L.circleMarker([${light.latitude}, ${light.longitude}], {
            radius: 10,
            fillColor: "$color",
            color: "#ffffff",
            weight: 3,
            opacity: 1,
            fillOpacity: 0.9
        }).addTo(map).bindPopup(`
            <div style="font-family: sans-serif; padding: 8px;">
                <strong style="font-size: 16px; color: #1f2937;">${light.id}</strong><br/>
                <p style="margin: 4px 0; color: #6b7280;">${light.address}</p>
                <p style="margin: 4px 0;"><strong>Statut:</strong> <span style="color: $color;">${light.status.displayName}</span></p>
                <p style="margin: 4px 0;"><strong>Conso:</strong> ${light.powerConsumption}W</p>
            </div>
        `);
        """.trimIndent()
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                body { margin: 0; padding: 0; overflow: hidden; }
                #map { width: 100%; height: 300px; background: #f9fafb; }
                .leaflet-popup-content-wrapper { border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.15); }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                try {
                    var map = L.map('map', {
                        center: [$centerLat, $centerLon],
                        zoom: 14,
                        zoomControl: true,
                        scrollWheelZoom: false,
                        dragging: true,
                        tap: true
                    });

                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        attribution: '© OpenStreetMap contributors',
                        maxZoom: 19
                    }).addTo(map);

                    $markers

                    var bounds = [${streetlights.joinToString(", ") { "[${it.latitude}, ${it.longitude}]" }}];
                    if (bounds.length > 1) {
                        map.fitBounds(bounds, { padding: [30, 30] });
                    }
                } catch (error) {
                    console.error('Erreur initialisation carte:', error);
                    document.body.innerHTML = '<div style="display:flex;justify-content:center;align-items:center;height:300px;color:#ef4444;font-family:sans-serif;">Erreur de chargement de la carte</div>';
                }
            </script>
        </body>
        </html>
    """.trimIndent()
}

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
            .padding(horizontal = 20.dp, vertical = 28.dp)  // ← Réduit de 48 → 28 dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Réseau de lampadaires",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        letterSpacing = 0.4.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Carte intelligente",
                        color = Color.White,
                        fontSize = 26.sp,                    // ← Réduit de 32 → 26
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

            Spacer(Modifier.height(24.dp))  // ← Réduit de 32 → 24

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
                QuickStatCardCompact(
                    value = String.format("%.1f kW", totalConsumption / 1000),
                    label = "Consommation",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// Carte de stats compacte
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
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
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
private fun StatusFilters(
    selectedStatus: StreetlightStatus?,
    onStatusSelected: (StreetlightStatus) -> Unit
) {
    Text("Filtrer par statut", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
    Spacer(Modifier.height(12.dp))
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
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
private fun StreetlightCard(streetlight: Streetlight) {
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
                        listOf(Color.White.copy(0.7f), MaterialTheme.colorScheme.surface)
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
                            .clip(RoundedCornerShape(16.dp))
                            .background(streetlight.status.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = streetlight.status.color,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(streetlight.id, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(streetlight.address, color = MaterialTheme.colorScheme.onSurface.copy(0.7f), fontSize = 14.sp)
                        Text(streetlight.location, color = MaterialTheme.colorScheme.onSurface.copy(0.6f), fontSize = 12.sp)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Badge(containerColor = streetlight.status.color) {
                            Text(streetlight.status.displayName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${streetlight.powerConsumption} W",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (streetlight.status == StreetlightStatus.ON) NoorGreen else Color.Gray
                        )
                    }
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Type d'ampoule", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                                Text(streetlight.bulbType.displayName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Coordonnées", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                                Text("${streetlight.latitude}, ${streetlight.longitude}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { /* TODO */ }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Modifier")
                            }
                            Button(
                                onClick = { /* TODO */ },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = NoorBlue)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Contrôler")
                            }
                        }
                    }
                }
            }
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