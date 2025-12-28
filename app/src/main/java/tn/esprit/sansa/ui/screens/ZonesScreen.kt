// ZonesScreen.kt – Interface des zones avec design Noor et navigation
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
import tn.esprit.sansa.ui.theme.SansaTheme

// Palette Noor
private val NoorBlue = Color(0xFF1E40AF)
private val NoorGreen = Color(0xFF10B981)
private val NoorAmber = Color(0xFFF59E0B)
private val NoorRed = Color(0xFFEF4444)
private val NoorPurple = Color(0xFF8B5CF6)
private val NoorCyan = Color(0xFF06B6D4)
private val NoorIndigo = Color(0xFF6366F1)
private val NoorEmerald = Color(0xFF10B981)

enum class ZoneType(val displayName: String, val color: Color, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    RESIDENTIAL("Résidentielle", NoorBlue, Icons.Default.Home),
    COMMERCIAL("Commerciale", NoorPurple, Icons.Default.Business),
    INDUSTRIAL("Industrielle", NoorCyan, Icons.Default.Factory),
    HISTORICAL("Historique", NoorAmber, Icons.Default.AccountBalance),
    PARK("Parc/Jardin", NoorGreen, Icons.Default.Park),
    DOWNTOWN("Centre-ville", NoorIndigo, Icons.Default.LocationCity),
    HIGHWAY("Route/Autoroute", NoorRed, Icons.Default.LocalShipping),
    MIXED("Mixte", Color(0xFF64748B), Icons.Default.Apps)
}

enum class ZoneStatus(val displayName: String, val color: Color) {
    ACTIVE("Active", NoorGreen),
    MAINTENANCE("En maintenance", NoorAmber),
    INACTIVE("Inactive", Color.Gray),
    PLANNING("En planification", NoorBlue)
}

data class Zone(
    val id: String,
    val name: String,
    val description: String,
    val associatedStreetlights: List<String>,
    val type: ZoneType,
    val status: ZoneStatus,
    val area: Double,
    val population: Int,
    val coordinator: String,
    val activeStreetlights: Int
)

private val mockZones = listOf(
    Zone(
        "Z001",
        "Centre-ville Tunis",
        "Zone principale du centre-ville incluant l'Avenue Habib Bourguiba et ses alentours. Forte densité commerciale et touristique.",
        listOf("L001", "L002", "L003", "L004", "L005", "L006", "L007", "L008", "L009", "L010"),
        ZoneType.DOWNTOWN,
        ZoneStatus.ACTIVE,
        2.5,
        45000,
        "Ahmed Ben Ali",
        10
    ),
    Zone(
        "Z002",
        "Médina Historique",
        "Zone historique protégée avec architecture traditionnelle. Éclairage spécial pour préserver le patrimoine culturel.",
        listOf("L011", "L012", "L013", "L014", "L015"),
        ZoneType.HISTORICAL,
        ZoneStatus.ACTIVE,
        1.8,
        12000,
        "Fatma Gharbi",
        5
    ),
    Zone(
        "Z003",
        "Zone Résidentielle Nord",
        "Quartier résidentiel calme avec écoles et espaces verts. Priorité à l'éclairage de sécurité nocturne.",
        listOf("L016", "L017", "L018", "L019", "L020", "L021", "L022"),
        ZoneType.RESIDENTIAL,
        ZoneStatus.ACTIVE,
        3.2,
        28000,
        "Mohamed Trabelsi",
        7
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZonesScreen(
    modifier: Modifier = Modifier
) {
    // État pour gérer la navigation entre les écrans
    var showAddZone by remember { mutableStateOf(false) }

    if (showAddZone) {
        // Afficher l'écran d'ajout de zone
        AddZoneScreen(
            onAddSuccess = {
                // Retour à l'écran principal après l'ajout
                showAddZone = false
            },
            onBackPressed = {
                // Retour à l'écran principal si l'utilisateur annule
                showAddZone = false
            }
        )
    } else {
        // Afficher l'écran principal des zones
        ZonesMainScreen(
            modifier = modifier,
            onNavigateToAddZone = {
                showAddZone = true
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ZonesMainScreen(
    modifier: Modifier = Modifier,
    onNavigateToAddZone: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<ZoneType?>(null) }
    var selectedStatus by remember { mutableStateOf<ZoneStatus?>(null) }

    val filteredZones = remember(searchQuery, selectedType, selectedStatus) {
        mockZones.filter { zone ->
            val matchesSearch = searchQuery.isEmpty() ||
                    zone.id.contains(searchQuery, ignoreCase = true) ||
                    zone.name.contains(searchQuery, ignoreCase = true) ||
                    zone.description.contains(searchQuery, ignoreCase = true) ||
                    zone.coordinator.contains(searchQuery, ignoreCase = true)
            val matchesType = selectedType == null || zone.type == selectedType
            val matchesStatus = selectedStatus == null || zone.status == selectedStatus
            matchesSearch && matchesType && matchesStatus
        }.sortedBy { it.name }
    }

    val stats = remember(mockZones) {
        mapOf(
            "Total" to mockZones.size,
            "Actives" to mockZones.count { it.status == ZoneStatus.ACTIVE },
            "Lampadaires" to mockZones.sumOf { it.associatedStreetlights.size }
        )
    }

    val totalPopulation = remember(mockZones) {
        mockZones.sumOf { it.population }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { ZonesTopBar(stats = stats, totalPopulation = totalPopulation) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddZone,
                containerColor = NoorIndigo,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nouvelle zone")
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

            item { ZoneSearchBar(query = searchQuery, onQueryChange = { searchQuery = it }) }

            item {
                Text("Filtrer par type", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(12.dp))
                TypeFilters(
                    selectedType = selectedType,
                    onTypeSelected = { selectedType = if (selectedType == it) null else it }
                )
            }

            item {
                Text("Filtrer par statut", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(12.dp))
                StatusFilters(
                    selectedStatus = selectedStatus,
                    onStatusSelected = { selectedStatus = if (selectedStatus == it) null else it }
                )
            }

            item {
                Text(
                    text = "${filteredZones.size} zone${if (filteredZones.size != 1) "s" else ""} trouvée${if (filteredZones.size != 1) "s" else ""}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(filteredZones) { zone ->
                ZoneCard(zone = zone)
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun ZonesTopBar(stats: Map<String, Int>, totalPopulation: Int) {
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
                        "Zones de gestion",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        letterSpacing = 0.4.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Division urbaine",
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
                    value = String.format("%,d", totalPopulation),
                    label = "Population",
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
private fun ZoneSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Rechercher par nom, ID, coordinateur...") },
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
            focusedBorderColor = NoorIndigo,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            cursorColor = NoorIndigo
        )
    )
}

@Composable
private fun TypeFilters(
    selectedType: ZoneType?,
    onTypeSelected: (ZoneType) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        ZoneType.entries.forEach { type ->
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
private fun StatusFilters(
    selectedStatus: ZoneStatus?,
    onStatusSelected: (ZoneStatus) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        ZoneStatus.entries.forEach { status ->
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
private fun ZoneCard(zone: Zone) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val elevation by animateDpAsState(if (pressed) 16.dp else 8.dp)
    val offsetY by animateDpAsState(if (pressed) (-6).dp else 0.dp)

    val coveragePercentage = if (zone.associatedStreetlights.isNotEmpty())
        (zone.activeStreetlights.toFloat() / zone.associatedStreetlights.size * 100).toInt()
    else 0

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
                        listOf(zone.type.color.copy(0.1f), MaterialTheme.colorScheme.surface)
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
                            .background(zone.type.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            zone.type.icon,
                            contentDescription = null,
                            tint = zone.type.color,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(zone.id, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Badge(containerColor = zone.status.color) {
                                Text(zone.status.displayName, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            zone.name,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.9f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(2.dp))
                        Badge(containerColor = zone.type.color.copy(alpha = 0.2f)) {
                            Text(zone.type.displayName, fontSize = 11.sp, color = zone.type.color, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    zone.description,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                    fontSize = 13.sp,
                    maxLines = if (expanded) Int.MAX_VALUE else 2
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = NoorBlue.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = NoorBlue, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.height(4.dp))
                            Text("${zone.associatedStreetlights.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NoorBlue)
                            Text("Lampadaires", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = NoorGreen.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NoorGreen, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.height(4.dp))
                            Text("$coveragePercentage%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NoorGreen)
                            Text("Actifs", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = NoorPurple.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SquareFoot, contentDescription = null, tint = NoorPurple, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.height(4.dp))
                            Text("${zone.area}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NoorPurple)
                            Text("km²", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                        }
                    }
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                        ZoneInfoRow(
                            icon = Icons.Default.Person,
                            label = "Coordinateur",
                            value = zone.coordinator
                        )

                        if (zone.population > 0) {
                            Spacer(Modifier.height(12.dp))
                            ZoneInfoRow(
                                icon = Icons.Default.Groups,
                                label = "Population",
                                value = String.format(java.util.Locale.US, "%,d", zone.population) + " habitants"
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text("Lampadaires associés (${zone.associatedStreetlights.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            zone.associatedStreetlights.forEach { streetlight ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(streetlight, fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp))
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = zone.type.color.copy(alpha = 0.1f),
                                        labelColor = zone.type.color
                                    )
                                )
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
                                colors = ButtonDefaults.buttonColors(containerColor = zone.type.color)
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Détails")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoneInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
private fun PreviewZonesLight() {
    SansaTheme(darkTheme = false) {
        ZonesScreen()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Mode Sombre")
@Composable
private fun PreviewZonesDark() {
    SansaTheme(darkTheme = true) {
        ZonesScreen()
    }
}