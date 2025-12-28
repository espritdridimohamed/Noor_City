// CulturalEventsScreen.kt — Interface des événements culturels avec design Noor
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
import java.text.SimpleDateFormat
import java.util.*

// Palette Noor
private val NoorBlue = Color(0xFF1E40AF)
private val NoorGreen = Color(0xFF10B981)
private val NoorAmber = Color(0xFFF59E0B)
private val NoorRed = Color(0xFFEF4444)
private val NoorPurple = Color(0xFF8B5CF6)
private val NoorPink = Color(0xFFEC4899)
private val NoorIndigo = Color(0xFF6366F1)
private val NoorOrange = Color(0xFFF97316)

enum class AmbianceType(
    val displayName: String,
    val color: Color,
    val icon: ImageVector,
    val description: String
) {
    FESTIVE("Festive", NoorOrange, Icons.Default.Celebration, "Éclairage dynamique et coloré"),
    ROMANTIC("Romantique", NoorPink, Icons.Default.Favorite, "Lumière douce et tamisée"),
    PATRIOTIC("Patriotique", NoorRed, Icons.Default.Flag, "Couleurs nationales"),
    ARTISTIC("Artistique", NoorPurple, Icons.Default.Palette, "Jeu de lumières créatif"),
    MODERN("Moderne", NoorBlue, Icons.Default.AutoAwesome, "Éclairage LED blanc"),
    TRADITIONAL("Traditionnel", NoorAmber, Icons.Default.Mosque, "Lumière chaude traditionnelle"),
    SPORT("Sportif", NoorGreen, Icons.Default.SportsScore, "Éclairage intense et vif"),
    CHRISTMAS("Noël", NoorIndigo, Icons.Default.AcUnit, "Illuminations de fêtes")
}

enum class EventStatus(val displayName: String, val color: Color) {
    UPCOMING("À venir", NoorBlue),
    ACTIVE("En cours", NoorGreen),
    COMPLETED("Terminé", Color.Gray),
    CANCELLED("Annulé", NoorRed)
}

data class CulturalEvent(
    val id: String,
    val name: String,
    val dateTime: Date,
    val zones: List<String>,
    val ambianceType: AmbianceType,
    val status: EventStatus,
    val duration: Int, // en heures
    val description: String,
    val attendees: Int,
    val organizer: String
)

private val mockEvents = listOf(
    CulturalEvent(
        "EVT001",
        "Festival International de Carthage",
        Date(System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000),
        listOf("Zone A", "Zone B", "Centre-ville"),
        AmbianceType.FESTIVE,
        EventStatus.UPCOMING,
        6,
        "Grand festival de musique et d'arts avec spectacles internationaux",
        5000,
        "Ministère de la Culture"
    ),
    CulturalEvent(
        "EVT002",
        "Nuit des Musées",
        Date(System.currentTimeMillis()),
        listOf("Zone historique", "Médina"),
        AmbianceType.ARTISTIC,
        EventStatus.ACTIVE,
        8,
        "Ouverture nocturne exceptionnelle des musées avec éclairage artistique",
        1200,
        "Association des Musées"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CulturalEventsScreen(
    modifier: Modifier = Modifier,
    onNavigateToAddEvent: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<EventStatus?>(null) }
    var selectedAmbiance by remember { mutableStateOf<AmbianceType?>(null) }

    val filteredEvents = remember(searchQuery, selectedStatus, selectedAmbiance) {
        mockEvents.filter { event ->
            val matchesSearch = searchQuery.isEmpty() ||
                    event.id.contains(searchQuery, ignoreCase = true) ||
                    event.name.contains(searchQuery, ignoreCase = true) ||
                    event.description.contains(searchQuery, ignoreCase = true)
            val matchesStatus = selectedStatus == null || event.status == selectedStatus
            val matchesAmbiance = selectedAmbiance == null || event.ambianceType == selectedAmbiance
            matchesSearch && matchesStatus && matchesAmbiance
        }.sortedBy { it.dateTime }
    }

    val stats = remember(mockEvents) {
        mapOf(
            "Total" to mockEvents.size,
            "À venir" to mockEvents.count { it.status == EventStatus.UPCOMING },
            "En cours" to mockEvents.count { it.status == EventStatus.ACTIVE }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { CulturalEventsTopBarModern(stats = stats) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddEvent,
                containerColor = NoorOrange,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(8.dp, 12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter un événement")
                Spacer(Modifier.width(8.dp))
                Text("Nouvel événement", fontWeight = FontWeight.SemiBold)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            item {
                EventSearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
            }

            item {
                Text(
                    "Filtrer par statut",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(12.dp))
                EventStatusFilters(
                    selectedStatus = selectedStatus,
                    onStatusSelected = { selectedStatus = if (selectedStatus == it) null else it }
                )
            }

            item {
                Text(
                    "Filtrer par ambiance",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(12.dp))
                AmbianceTypeFilters(
                    selectedAmbiance = selectedAmbiance,
                    onAmbianceSelected = { selectedAmbiance = if (selectedAmbiance == it) null else it }
                )
            }

            item {
                Text(
                    "${filteredEvents.size} événement${if (filteredEvents.size != 1) "s" else ""} trouvé${if (filteredEvents.size != 1) "s" else ""}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(filteredEvents) { event ->
                CulturalEventCard(event = event)
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun CulturalEventsTopBarModern(stats: Map<String, Int>) {
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
                        "Événements culturels",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        letterSpacing = 0.4.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Programmation artistique",
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

            Spacer(Modifier.height(24.dp))  // ← réduit de 32 → 24 dp

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

// Carte de statistiques compacte (même version utilisée partout)
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
                fontSize = 24.sp,                           // ← réduit de 28 → 24
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
private fun EventSearchBar(query: String, onQueryChange: (String) -> Unit) {
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
private fun EventStatusFilters(
    selectedStatus: EventStatus?,
    onStatusSelected: (EventStatus) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        EventStatus.entries.forEach { status ->
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
private fun AmbianceTypeFilters(
    selectedAmbiance: AmbianceType?,
    onAmbianceSelected: (AmbianceType) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        AmbianceType.entries.forEach { ambiance ->
            val isSelected = selectedAmbiance == ambiance
            FilterChip(
                onClick = { onAmbianceSelected(ambiance) },
                label = { Text(ambiance.displayName) },
                selected = isSelected,
                leadingIcon = { Icon(ambiance.icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ambiance.color,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = ambiance.color
                )
            )
        }
    }
}

@Composable
private fun CulturalEventCard(event: CulturalEvent) {
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
                        listOf(event.status.color.copy(0.1f), MaterialTheme.colorScheme.surface)
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
                            .background(event.ambianceType.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            event.ambianceType.icon,
                            contentDescription = null,
                            tint = event.ambianceType.color,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(event.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Badge(containerColor = event.status.color) {
                                Text(event.status.displayName, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.width(8.dp))
                            Badge(containerColor = event.ambianceType.color.copy(alpha = 0.2f)) {
                                Text(event.ambianceType.displayName, fontSize = 10.sp, color = event.ambianceType.color, fontWeight = FontWeight.Medium)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            event.id,
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
                    StatItem(
                        value = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(event.dateTime),
                        label = "Date",
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(value = "${event.duration}h", label = "Durée", modifier = Modifier.weight(1f))
                    StatItem(value = event.attendees.toString(), label = "Participants", modifier = Modifier.weight(1f))
                }
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        InfoRow(icon = Icons.Default.Description, label = "Description", value = event.description)
                        Spacer(Modifier.height(12.dp))
                        InfoRow(icon = Icons.Default.Business, label = "Organisateur", value = event.organizer)
                        Spacer(Modifier.height(12.dp))
                        InfoRow(icon = Icons.Default.LocationOn, label = "Zones", value = event.zones.joinToString(", "))
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
                                colors = ButtonDefaults.buttonColors(containerColor = event.ambianceType.color)
                            ) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Configurer")
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
private fun PreviewCulturalEventsLight() {
    SansaTheme(darkTheme = false) {
        CulturalEventsScreen()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Mode Sombre")
@Composable
private fun PreviewCulturalEventsDark() {
    SansaTheme(darkTheme = true) {
        CulturalEventsScreen()
    }
}