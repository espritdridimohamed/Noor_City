// StreetlightsMapScreen.kt — Version corrigée et alignée style moderne (Décembre 2025)
package tn.esprit.sansa.ui.screens

import android.os.Build
import android.view.ViewGroup
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
import tn.esprit.sansa.ui.components.SwipeToDeleteContainer
import tn.esprit.sansa.ui.components.EmptyState

// Palette Noor
private val NoorBlue = Color(0xFF1E40AF)
private val NoorGreen = Color(0xFF10B981)
private val NoorAmber = Color(0xFFF59E0B)
private val NoorRed = Color(0xFFEF4444)

// Modèles pour les lampadaires
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
    var showAddStreetlight by remember { mutableStateOf(false) }

    if (showAddStreetlight) {
        // Suppose que cet écran existe ailleurs dans le projet
        AddStreetlightScreen(
            onAddSuccess = { showAddStreetlight = false },
            onBackPressed = { showAddStreetlight = false }
        )
    } else {
        StreetlightsMapMainScreen(
            modifier = modifier,
            onNavigateToAddStreetlight = { showAddStreetlight = true }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StreetlightsMapMainScreen(
    modifier: Modifier = Modifier,
    onNavigateToAddStreetlight: () -> Unit
) {
    val streetlightsList = remember { mutableStateListOf(*mockStreetlights.toTypedArray()) }
    var showTutorial by rememberSaveable { mutableStateOf(true) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<StreetlightStatus?>(null) }
    var mapExpanded by remember { mutableStateOf(true) }

    val filteredStreetlights = remember(streetlightsList.size, searchQuery, selectedStatus) {
        streetlightsList.filter { light ->
            val matchesSearch = searchQuery.isEmpty() ||
                    light.id.contains(searchQuery, ignoreCase = true) ||
                    light.location.contains(searchQuery, ignoreCase = true) ||
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
            FloatingActionButton(
                onClick = onNavigateToAddStreetlight,
                containerColor = NoorBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nouveau lampadaire")
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
                    InteractiveMap(filteredStreetlights = filteredStreetlights)
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
                        SwipeToDeleteContainer(
                            item = light,
                            onDelete = { streetlightsList.remove(light) }
                        ) { item ->
                            StreetlightCard(streetlight = item)
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
private fun InteractiveMap(filteredStreetlights: List<Streetlight>) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    settings.safeBrowsingEnabled = true
                }
                // Pour une vraie carte → utiliser Mapbox, Google Maps SDK, ou OSM avec Leaflet
                loadUrl("https://www.openstreetmap.org")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

@Composable
private fun StreetlightCard(streetlight: Streetlight) {
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
                    value = streetlight.address.take(15) + if (streetlight.address.length > 15) "..." else "",
                    label = "Adresse",
                    modifier = Modifier.weight(1f)
                )
                ModernStatItem(
                    value = "${streetlight.powerConsumption} W",
                    label = "Conso",
                    modifier = Modifier.weight(1f)
                )
                ModernStatItem(
                    value = streetlight.location,
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
                        icon = Icons.Default.TypeSpecimen,
                        label = "Type ampoule",
                        value = streetlight.bulbType.displayName
                    )

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