// LightingProgramsScreen.kt — Version finale : FAB avec icône + uniquement
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

// Palette Noor
private val NoorBlue = Color(0xFF1E40AF)
private val NoorGreen = Color(0xFF10B981)
private val NoorAmber = Color(0xFFF59E0B)
private val NoorRed = Color(0xFFEF4444)
private val NoorPurple = Color(0xFF8B5CF6)
private val NoorCyan = Color(0xFF06B6D4)
private val NoorTeal = Color(0xFF14B8A6)

enum class ProgramStatus(val displayName: String, val color: Color) {
    ACTIVE("Actif", NoorGreen),
    SCHEDULED("Planifié", NoorBlue),
    PAUSED("En pause", NoorAmber),
    INACTIVE("Inactif", Color.Gray)
}

enum class LightingRuleType(val displayName: String, val color: Color, val icon: ImageVector) {
    TIME_BASED("Basé sur l'heure", NoorBlue, Icons.Default.Schedule),
    SENSOR_BASED("Basé sur capteurs", NoorCyan, Icons.Default.Sensors),
    EVENT_BASED("Basé sur événements", NoorPurple, Icons.Default.Event),
    MANUAL("Manuel", NoorAmber, Icons.Default.TouchApp),
    ADAPTIVE("Adaptatif", NoorTeal, Icons.Default.AutoMode),
    EMERGENCY("Urgence", NoorRed, Icons.Default.Emergency)
}

data class LightingRule(
    val type: LightingRuleType,
    val description: String,
    val parameters: String
)

data class LightingProgram(
    val id: String,
    val name: String,
    val rules: List<LightingRule>,
    val associatedStreetlights: List<String>,
    val status: ProgramStatus,
    val createdDate: String,
    val lastModified: String,
    val priority: Int,
    val description: String
)

private val mockPrograms = listOf(
    LightingProgram(
        "PROG001",
        "Éclairage Nocturne Standard",
        listOf(
            LightingRule(LightingRuleType.TIME_BASED, "Activation automatique au crépuscule", "18:00 - 06:00 | Intensité: 100%"),
            LightingRule(LightingRuleType.SENSOR_BASED, "Réduction si absence de mouvement", "Détection mouvement: 30% après 15min")
        ),
        listOf("L001", "L002", "L003", "L004", "L005"),
        ProgramStatus.ACTIVE,
        "01 Jan 2024",
        "15 Déc 2024",
        1,
        "Programme d'éclairage standard pour les zones résidentielles"
    ),
    LightingProgram(
        "PROG002",
        "Mode Économie d'Énergie",
        listOf(
            LightingRule(LightingRuleType.ADAPTIVE, "Ajustement selon luminosité ambiante", "Capteur luminosité: 0-100%"),
            LightingRule(LightingRuleType.TIME_BASED, "Réduction nocturne progressive", "00:00 - 05:00 | Intensité: 50%")
        ),
        listOf("L006", "L007", "L008", "L009"),
        ProgramStatus.ACTIVE,
        "10 Fév 2024",
        "20 Déc 2024",
        2,
        "Optimisation de la consommation énergétique pendant les heures creuses"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightingProgramsScreen(
    modifier: Modifier = Modifier,
    onNavigateToAddProgram: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<ProgramStatus?>(null) }
    var selectedRuleType by remember { mutableStateOf<LightingRuleType?>(null) }

    val filteredPrograms = remember(searchQuery, selectedStatus, selectedRuleType) {
        mockPrograms.filter { program ->
            val matchesSearch = searchQuery.isEmpty() ||
                    program.id.contains(searchQuery, ignoreCase = true) ||
                    program.name.contains(searchQuery, ignoreCase = true) ||
                    program.description.contains(searchQuery, ignoreCase = true)
            val matchesStatus = selectedStatus == null || program.status == selectedStatus
            val matchesRuleType = selectedRuleType == null || program.rules.any { it.type == selectedRuleType }
            matchesSearch && matchesStatus && matchesRuleType
        }.sortedBy { it.name }
    }

    val stats = remember(mockPrograms) {
        mapOf(
            "Total" to mockPrograms.size,
            "Actifs" to mockPrograms.count { it.status == ProgramStatus.ACTIVE },
            "Planifiés" to mockPrograms.count { it.status == ProgramStatus.SCHEDULED }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { LightingProgramsTopBarModern(stats = stats) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddProgram,
                containerColor = NoorBlue,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ajouter un nouveau programme"
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

            item { LightingProgramSearchBar(query = searchQuery, onQueryChange = { searchQuery = it }) }

            item {
                Text("Filtrer par statut", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(12.dp))
                ProgramStatusFilters(
                    selectedStatus = selectedStatus,
                    onStatusSelected = { selectedStatus = if (selectedStatus == it) null else it }
                )
            }

            item {
                Text("Filtrer par type de règle", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(12.dp))
                LightingRuleTypeFilters(
                    selectedRuleType = selectedRuleType,
                    onRuleTypeSelected = { selectedRuleType = if (selectedRuleType == it) null else it }
                )
            }

            item {
                Text(
                    text = "${filteredPrograms.size} programme${if (filteredPrograms.size != 1) "s" else ""} trouvé${if (filteredPrograms.size != 1) "s" else ""}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(filteredPrograms) { program ->
                LightingProgramCard(program = program)
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun LightingProgramsTopBarModern(stats: Map<String, Int>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(NoorBlue.copy(alpha = 0.95f), NoorBlue.copy(alpha = 0.65f))
                )
            )
            .padding(horizontal = 20.dp, vertical = 28.dp)  // ← Réduit (avant : 48.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Programmes d'éclairage",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        letterSpacing = 0.4.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Gestion intelligente",
                        color = Color.White,
                        fontSize = 26.sp,                    // ← Réduit (avant : 32.sp)
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

            Spacer(Modifier.height(24.dp))  // ← Réduit (avant : 32.dp)

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

// Composant de statistiques compact - à réutiliser dans toute l'application
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
                fontSize = 24.sp,                           // ← Réduit (avant : 28.sp)
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
private fun LightingProgramSearchBar(query: String, onQueryChange: (String) -> Unit) {
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
private fun ProgramStatusFilters(
    selectedStatus: ProgramStatus?,
    onStatusSelected: (ProgramStatus) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        ProgramStatus.entries.forEach { status ->
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
private fun LightingRuleTypeFilters(
    selectedRuleType: LightingRuleType?,
    onRuleTypeSelected: (LightingRuleType) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        LightingRuleType.entries.forEach { type ->
            val isSelected = selectedRuleType == type
            FilterChip(
                onClick = { onRuleTypeSelected(type) },
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
private fun LightingProgramCard(program: LightingProgram) {
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
                        listOf(program.status.color.copy(0.1f), MaterialTheme.colorScheme.surface)
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
                            .background(program.rules.firstOrNull()?.type?.color?.copy(alpha = 0.15f) ?: NoorBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AutoMode,
                            contentDescription = null,
                            tint = program.rules.firstOrNull()?.type?.color ?: NoorBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(program.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Badge(containerColor = program.status.color) {
                            Text(program.status.displayName, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            program.id,
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
                    StatItem(value = program.createdDate, label = "Créé le", modifier = Modifier.weight(1f))
                    StatItem(value = program.lastModified, label = "Modifié le", modifier = Modifier.weight(1f))
                    StatItem(value = "${program.priority}/10", label = "Priorité", modifier = Modifier.weight(1f))
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                        InfoRow(icon = Icons.Default.Description, label = "Description", value = program.description)

                        Spacer(Modifier.height(12.dp))

                        InfoRow(icon = Icons.Default.Lightbulb, label = "Lampadaires associés", value = program.associatedStreetlights.joinToString(", "))

                        Spacer(Modifier.height(12.dp))

                        Text("Règles", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        program.rules.forEach { rule ->
                            InfoRow(icon = rule.type.icon, label = rule.type.displayName, value = rule.parameters)
                            Spacer(Modifier.height(4.dp))
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { /* TODO: Éditer */ }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Modifier")
                            }
                            Button(
                                onClick = { /* TODO: Pause */ },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = NoorAmber)
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Pause")
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
private fun PreviewLightingProgramsLight() {
    SansaTheme(darkTheme = false) {
        LightingProgramsScreen()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Mode Sombre")
@Composable
private fun PreviewLightingProgramsDark() {
    SansaTheme(darkTheme = true) {
        LightingProgramsScreen()
    }
}