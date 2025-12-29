// ZonesScreen.kt – Interface des zones avec design moderne aligné (Décembre 2025)
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
import androidx.compose.runtime.saveable.rememberSaveable
import tn.esprit.sansa.ui.components.CoachMarkTooltip
import tn.esprit.sansa.ui.components.SwipeToDeleteContainer
import tn.esprit.sansa.ui.components.EmptyState

// Palette Noor
private val NoorBlue = Color(0xFF1E40AF)
private val NoorGreen = Color(0xFF10B981)
private val NoorAmber = Color(0xFFF59E0B)
private val NoorRed = Color(0xFFEF4444)
private val NoorPurple = Color(0xFF8B5CF6)
private val NoorCyan = Color(0xFF06B6D4)
private val NoorIndigo = Color(0xFF6366F1)
private val NoorEmerald = Color(0xFF10B981)

enum class ZoneType(val displayName: String, val color: Color, val icon: ImageVector) {
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
    var showAddZone by remember { mutableStateOf(false) }

    if (showAddZone) {
        AddZoneScreen(
            onAddSuccess = { showAddZone = false },
            onBackPressed = { showAddZone = false }
        )
    } else {
        ZonesMainScreen(
            modifier = modifier,
            onNavigateToAddZone = { showAddZone = true }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ZonesMainScreen(
    modifier: Modifier = Modifier,
    onNavigateToAddZone: () -> Unit
) {
    val zonesList = remember { mutableStateListOf(*mockZones.toTypedArray()) }
    var showTutorial by rememberSaveable { mutableStateOf(true) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<ZoneType?>(null) }
    var selectedStatus by remember { mutableStateOf<ZoneStatus?>(null) }

    val filteredZones = remember(zonesList.size, searchQuery, selectedType, selectedStatus) {
        zonesList.filter { zone ->
            val matchesSearch = searchQuery.isEmpty() ||
                    zone.id.contains(searchQuery, ignoreCase = true) ||
                    zone.name.contains(searchQuery, ignoreCase = true) ||
                    zone.description.contains(searchQuery, ignoreCase = true)
            val matchesType = selectedType == null || zone.type == selectedType
            val matchesStatus = selectedStatus == null || zone.status == selectedStatus
            matchesSearch && matchesType && matchesStatus
        }.sortedBy { it.name }
    }

    val stats = remember(zonesList.toList()) {
        mapOf(
            "Total" to zonesList.size,
            "Actives" to zonesList.count { it.status == ZoneStatus.ACTIVE },
            "En maintenance" to zonesList.count { it.status == ZoneStatus.MAINTENANCE }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { ZonesTopBarModern(stats = stats) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddZone,
                containerColor = NoorBlue,
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
                Text(
                    "Filtrer par type",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(12.dp))
                ZoneTypeFilters(
                    selectedType = selectedType,
                    onTypeSelected = { selectedType = if (selectedType == it) null else it }
                )
            }

            item {
                Text(
                    "Filtrer par statut",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(12.dp))
                ZoneStatusFilters(
                    selectedStatus = selectedStatus,
                    onStatusSelected = { selectedStatus = if (selectedStatus == it) null else it }
                )
            }

            item {
                Text(
                    "${filteredZones.size} zone${if (filteredZones.size != 1) "s" else ""} trouvé${if (filteredZones.size != 1) "es" else "e"}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (filteredZones.isEmpty()) {
                item {
                    EmptyState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        icon = Icons.Default.LocationCity,
                        title = "Aucune zone trouvée",
                        description = "Vérifiez vos filtres ou ajoutez une nouvelle zone.",
                        actionLabel = "Nouvelle zone",
                        onActionClick = onNavigateToAddZone,
                        iconColor = NoorIndigo
                    )
                }
            } else {
                itemsIndexed(
                    items = filteredZones,
                    key = { _, zone -> zone.id }
                ) { index, zone ->
                    Box {
                        SwipeToDeleteContainer(
                            item = zone,
                            onDelete = { zonesList.remove(zone) }
                        ) { item ->
                            ZoneCard(zone = item)
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

@Composable
private fun ZonesTopBarModern(stats: Map<String, Int>) {
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
                        "Gestion des zones",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        letterSpacing = 0.4.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Organisation territoriale",
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
private fun ZoneSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Rechercher par ID, nom ou description...") },
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
private fun ZoneTypeFilters(
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
private fun ZoneStatusFilters(
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
                        .background(zone.type.color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        zone.type.icon,
                        contentDescription = null,
                        tint = zone.type.color,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = zone.name,
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
                            containerColor = zone.status.color,
                            modifier = Modifier.height(22.dp)
                        ) {
                            Text(
                                text = zone.status.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        Badge(
                            containerColor = zone.type.color.copy(alpha = 0.15f),
                            modifier = Modifier.height(22.dp)
                        ) {
                            Text(
                                text = zone.type.displayName,
                                fontSize = 11.sp,
                                color = zone.type.color,
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
                    value = "${zone.associatedStreetlights.size}",
                    label = "Lampadaires",
                    modifier = Modifier.weight(1f)
                )
                ModernStatItem(
                    value = "${zone.activeStreetlights}",
                    label = "Actifs",
                    modifier = Modifier.weight(1f)
                )
                ModernStatItem(
                    value = "${zone.area} km²",
                    label = "Superficie",
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
                        icon = Icons.Default.Description,
                        label = "Description",
                        value = zone.description
                    )

                    Spacer(Modifier.height(10.dp))

                    InfoRow(
                        icon = Icons.Default.Person,
                        label = "Coordinateur",
                        value = zone.coordinator
                    )

                    Spacer(Modifier.height(10.dp))

                    InfoRow(
                        icon = Icons.Default.Groups,
                        label = "Population",
                        value = "${zone.population}"
                    )

                    Spacer(Modifier.height(10.dp))

                    Text("Lampadaires associés", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        zone.associatedStreetlights.forEach { light ->
                            AssistChip(
                                onClick = { /* TODO */ },
                                label = { Text(light, fontSize = 12.sp) }
                            )
                        }
                    }

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
                            colors = ButtonDefaults.buttonColors(containerColor = zone.type.color)
                        ) {
                            Icon(Icons.Default.Visibility, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Détails", fontSize = 13.sp)
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