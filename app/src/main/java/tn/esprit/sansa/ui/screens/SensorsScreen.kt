// SensorsScreen.kt – VERSION FINALE, STYLE ALIGNÉ SUR TECHNICIANS SCREEN (Décembre 2025)
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

// Palette Noor commune
private val NoorBlue = Color(0xFF1E40AF)
private val NoorGreen = Color(0xFF10B981)
private val NoorAmber = Color(0xFFF59E0B)
private val NoorRed = Color(0xFFEF4444)
private val NoorPurple = Color(0xFF8B5CF6)
private val NoorCyan = Color(0xFF06B6D4)
private val NoorIndigo = Color(0xFF6366F1)

enum class SensorStatus(val displayName: String, val color: Color) {
    ACTIVE("Actif", NoorGreen),
    WARNING("Attention", NoorAmber),
    ERROR("Erreur", NoorRed),
    OFFLINE("Hors ligne", Color.Gray)
}

enum class SensorType(
    val displayName: String,
    val color: Color,
    val icon: ImageVector,
    val unit: String
) {
    LIGHT("Luminosité", NoorAmber, Icons.Default.LightMode, "lux"),
    MOTION("Mouvement", NoorCyan, Icons.Default.Sensors, "dét./h"),
    TEMPERATURE("Température", NoorRed, Icons.Default.Thermostat, "°C"),
    HUMIDITY("Humidité", NoorPurple, Icons.Default.WaterDrop, "%"),
    POWER("Consommation", NoorGreen, Icons.Default.Power, "W")
}

data class Sensor(
    val id: String,
    val type: SensorType,
    val streetlightId: String,
    val streetlightName: String,
    val currentValue: String,
    val status: SensorStatus,
    val lastUpdate: String,
    val batteryLevel: Int
)

private val mockSensors = listOf(
    Sensor("S001", SensorType.LIGHT, "L001", "Lampadaire #001", "746", SensorStatus.ACTIVE, "Il y a 2 min", 92),
    Sensor("S002", SensorType.MOTION, "L001", "Lampadaire #001", "3", SensorStatus.ACTIVE, "Il y a 5 min", 88),
    Sensor("S003", SensorType.TEMPERATURE, "L002", "Lampadaire #002", "22", SensorStatus.WARNING, "Il y a 1 min", 45),
    Sensor("S004", SensorType.LIGHT, "L002", "Lampadaire #002", "593", SensorStatus.ACTIVE, "Il y a 3 min", 95),
    Sensor("S005", SensorType.HUMIDITY, "L003", "Lampadaire #003", "65", SensorStatus.ERROR, "Il y a 45 min", 12),
    Sensor("S006", SensorType.POWER, "L003", "Lampadaire #003", "0", SensorStatus.OFFLINE, "Il y a 2h", 0),
    Sensor("S007", SensorType.TEMPERATURE, "L004", "Lampadaire #004", "24", SensorStatus.ACTIVE, "Il y a 1 min", 78),
    Sensor("S008", SensorType.MOTION, "L005", "Lampadaire #005", "1", SensorStatus.ACTIVE, "Il y a 8 min", 82)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorsScreenModern(
    modifier: Modifier = Modifier,
    onNavigateToAddSensor: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<SensorStatus?>(null) }
    var selectedType by remember { mutableStateOf<SensorType?>(null) }

    val filteredSensors = remember(searchQuery, selectedStatus, selectedType) {
        mockSensors.filter { sensor ->
            val matchesSearch = searchQuery.isEmpty() ||
                    sensor.id.contains(searchQuery, ignoreCase = true) ||
                    sensor.streetlightName.contains(searchQuery, ignoreCase = true) ||
                    sensor.type.displayName.contains(searchQuery, ignoreCase = true)
            val matchesStatus = selectedStatus == null || sensor.status == selectedStatus
            val matchesType = selectedType == null || sensor.type == selectedType
            matchesSearch && matchesStatus && matchesType
        }.sortedBy { it.streetlightName }
    }

    val stats = remember(mockSensors) {
        mapOf(
            "Total" to mockSensors.size,
            "Actifs" to mockSensors.count { it.status == SensorStatus.ACTIVE },
            "En alerte" to mockSensors.count { it.status != SensorStatus.ACTIVE }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { SensorsTopBarModern(stats = stats) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddSensor,
                containerColor = NoorBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Nouveau capteur", fontWeight = FontWeight.SemiBold)
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

            item { SensorSearchBar(query = searchQuery, onQueryChange = { searchQuery = it }) }

            item {
                Text(
                    "Filtrer par statut",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(12.dp))
                StatusFilters(
                    selectedStatus = selectedStatus,
                    onStatusSelected = { selectedStatus = if (selectedStatus == it) null else it }
                )
            }

            item {
                Text(
                    "Filtrer par type",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(12.dp))
                TypeFilters(
                    selectedType = selectedType,
                    onTypeSelected = { selectedType = if (selectedType == it) null else it }
                )
            }

            item {
                Text(
                    text = "${filteredSensors.size} capteur${if (filteredSensors.size != 1) "s" else ""} trouvé${if (filteredSensors.size != 1) "s" else ""}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(filteredSensors) { sensor: Sensor ->
                SensorCard(sensor = sensor)
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun SensorsTopBarModern(stats: Map<String, Int>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(NoorBlue.copy(alpha = 0.95f), NoorBlue.copy(alpha = 0.65f))
                )
            )
            .padding(horizontal = 20.dp, vertical = 28.dp) // ← Réduit de 48dp → 28dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Réseau de capteurs",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,                    // ← réduit
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(4.dp))             // ← espacement plus naturel
                    Text(
                        "Surveillance intelligente",
                        color = Color.White,
                        fontSize = 26.sp,                    // ← réduit de 32 → 26
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp            // ← léger compactage du titre
                    )
                }
                IconButton(onClick = { /* TODO: Refresh */ }) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Actualiser",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)      // ← un peu plus petit
                    )
                }
            }

            Spacer(Modifier.height(24.dp))                    // ← réduit de 32 → 24

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
        shape = RoundedCornerShape(16.dp),              // ← coins un peu plus doux
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.22f)
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp), // ← padding réduit
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
                fontSize = 11.sp,                           // ← un peu plus petit
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
private fun SensorSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Rechercher par ID, lampadaire ou type...") },
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
    selectedStatus: SensorStatus?,
    onStatusSelected: (SensorStatus) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        SensorStatus.entries.forEach { status ->
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
private fun TypeFilters(
    selectedType: SensorType?,
    onTypeSelected: (SensorType) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        SensorType.entries.forEach { type ->
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
private fun SensorCard(sensor: Sensor) {
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
                        listOf(sensor.status.color.copy(0.1f), MaterialTheme.colorScheme.surface)
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
                            .background(sensor.type.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            sensor.type.icon,
                            contentDescription = null,
                            tint = sensor.type.color,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(sensor.type.displayName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Badge(containerColor = sensor.status.color) {
                                Text(sensor.status.displayName, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.width(8.dp))
                            Badge(containerColor = sensor.type.color.copy(alpha = 0.2f)) {
                                Text(sensor.type.displayName, fontSize = 10.sp, color = sensor.type.color, fontWeight = FontWeight.Medium)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            sensor.id,
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
                        value = "${sensor.currentValue} ${sensor.type.unit}",
                        label = "Valeur actuelle",
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        value = "${sensor.batteryLevel}%",
                        label = "Batterie",
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        value = sensor.lastUpdate,
                        label = "Dernière mise à jour",
                        modifier = Modifier.weight(1f)
                    )
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                        InfoRow(
                            icon = Icons.Default.LocationOn,
                            label = "Lampadaire",
                            value = sensor.streetlightName
                        )

                        Spacer(Modifier.height(12.dp))

                        InfoRow(
                            icon = Icons.Default.PowerSettingsNew,
                            label = "Statut",
                            value = sensor.status.displayName
                        )

                        Spacer(Modifier.height(12.dp))

                        InfoRow(
                            icon = Icons.Default.Update,
                            label = "Dernière mise à jour",
                            value = sensor.lastUpdate
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { /* TODO */ }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Calibrer")
                            }
                            Button(
                                onClick = { /* TODO */ },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = NoorBlue)
                            ) {
                                Icon(Icons.Default.BarChart, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Historique")
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
private fun PreviewSensorsLight() {
    SansaTheme(darkTheme = false) {
        SensorsScreenModern()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Mode Sombre")
@Composable
private fun PreviewSensorsDark() {
    SansaTheme(darkTheme = true) {
        SensorsScreenModern()
    }
}