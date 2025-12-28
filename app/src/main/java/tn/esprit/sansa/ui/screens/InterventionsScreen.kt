// InterventionsScreen.kt – Interface des interventions avec design Noor et navigation
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
import tn.esprit.sansa.models.*
import java.text.SimpleDateFormat
import java.util.*

// Palette Noor (définitions locales pour éviter les conflits)
private val NoorBlue = Color(0xFF1E40AF)
private val NoorGreen = Color(0xFF10B981)
private val NoorAmber = Color(0xFFF59E0B)
private val NoorRed = Color(0xFFEF4444)
private val NoorPurple = Color(0xFF8B5CF6)

private val mockInterventions = listOf(
    Intervention(
        id = "INT001",
        streetlightId = "L001",
        technicianName = "Mohamed Trabelsi",
        type = InterventionType.REPAIR,
        date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000)),
        status = InterventionStatus.IN_PROGRESS,
        location = "Avenue Habib Bourguiba, Tunis",
        description = "Remplacement du ballast et réparation du câblage",
        estimatedDuration = 120,
        priority = "Haute"
    ),
    Intervention(
        id = "INT002",
        streetlightId = "L003",
        technicianName = "Fatma K.",
        type = InterventionType.INSPECTION,
        date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
        status = InterventionStatus.SCHEDULED,
        location = "Rue de la Liberté, Sfax",
        description = "Vérification mensuelle des connexions",
        estimatedDuration = 45,
        priority = "Moyenne"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterventionsScreen(
    modifier: Modifier = Modifier
) {
    // État pour gérer la navigation entre les écrans
    var showAddIntervention by remember { mutableStateOf(false) }

    if (showAddIntervention) {
        // Afficher l'écran d'ajout d'intervention
        AddInterventionScreen(
            onAddSuccess = {
                // Retour à l'écran principal après l'ajout
                showAddIntervention = false
            },
            onBackPressed = {
                // Retour à l'écran principal si l'utilisateur annule
                showAddIntervention = false
            }
        )
    } else {
        // Afficher l'écran principal des interventions
        InterventionsMainScreen(
            modifier = modifier,
            onNavigateToAddIntervention = {
                showAddIntervention = true
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InterventionsMainScreen(
    modifier: Modifier = Modifier,
    onNavigateToAddIntervention: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<InterventionStatus?>(null) }

    val filteredInterventions = remember(searchQuery, selectedStatus) {
        mockInterventions.filter { intervention ->
            val matchesSearch = searchQuery.isEmpty() ||
                    intervention.id.contains(searchQuery, ignoreCase = true) ||
                    intervention.description.contains(searchQuery, ignoreCase = true) ||
                    intervention.location.contains(searchQuery, ignoreCase = true)
            val matchesStatus = selectedStatus == null || intervention.status == selectedStatus
            matchesSearch && matchesStatus
        }.sortedByDescending { it.date }
    }

    val stats = remember(mockInterventions) {
        mapOf(
            "Total" to mockInterventions.size,
            "Planifiées" to mockInterventions.count { it.status == InterventionStatus.SCHEDULED },
            "En cours" to mockInterventions.count { it.status == InterventionStatus.IN_PROGRESS }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { InterventionsTopBarModern(stats = stats) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddIntervention,
                containerColor = NoorBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nouvelle intervention")
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

            item { InterventionSearchBar(query = searchQuery, onQueryChange = { searchQuery = it }) }

            item {
                Text("Filtrer par statut", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                InterventionStatusFilters(
                    selectedStatus = selectedStatus,
                    onStatusSelected = { selectedStatus = if (selectedStatus == it) null else it }
                )
            }

            item {
                Text(
                    "${filteredInterventions.size} intervention${if (filteredInterventions.size != 1) "s" else ""}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(filteredInterventions) { intervention ->
                InterventionCard(intervention = intervention)
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun InterventionsTopBarModern(stats: Map<String, Int>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(NoorBlue.copy(alpha = 0.95f), NoorBlue.copy(alpha = 0.65f))
                )
            )
            .padding(horizontal = 20.dp, vertical = 28.dp)  // ← Réduction significative
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Interventions",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        letterSpacing = 0.4.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Gestion technique",
                        color = Color.White,
                        fontSize = 26.sp,                    // ← de 32 → 26 sp
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

            Spacer(Modifier.height(24.dp))  // ← de 32 → 24 dp

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

// Version compacte des statistiques - à réutiliser partout dans l'application
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
                fontSize = 24.sp,                           // ← de 28 → 24 sp
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
private fun InterventionSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Rechercher par ID, emplacement ou description...") },
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
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun InterventionStatusFilters(
    selectedStatus: InterventionStatus?,
    onStatusSelected: (InterventionStatus) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        InterventionStatus.entries.forEach { status ->
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
private fun InterventionCard(intervention: Intervention) {
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
                        listOf(intervention.status.color.copy(0.1f), MaterialTheme.colorScheme.surface)
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
                            .background(intervention.type.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            intervention.type.icon,
                            contentDescription = null,
                            tint = intervention.type.color,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(intervention.type.displayName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Badge(containerColor = intervention.status.color) {
                                Text(intervention.status.displayName, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.width(8.dp))
                            Badge(containerColor = Color.Gray.copy(alpha = 0.2f)) {
                                Text(intervention.priority, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            intervention.id,
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
                        value = intervention.date,
                        label = "Date",
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        value = intervention.location,
                        label = "Emplacement",
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        value = intervention.technicianName,
                        label = "Technicien",
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
                            icon = Icons.Default.Description,
                            label = "Description",
                            value = intervention.description
                        )

                        Spacer(Modifier.height(12.dp))

                        InfoRow(
                            icon = Icons.Default.Lightbulb,
                            label = "ID Lampadaire",
                            value = intervention.streetlightId
                        )

                        Spacer(Modifier.height(12.dp))

                        InfoRow(
                            icon = Icons.Default.Timer,
                            label = "Durée estimée",
                            value = "${intervention.estimatedDuration} min"
                        )

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
                                colors = ButtonDefaults.buttonColors(containerColor = intervention.status.color)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Mettre à jour")
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
fun InterventionsScreenPreview() {
    SansaTheme(darkTheme = false) {
        InterventionsScreen()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Mode Sombre")
@Composable
fun InterventionsScreenDarkPreview() {
    SansaTheme(darkTheme = true) {
        InterventionsScreen()
    }
}