// ReclamationsScreen.kt – Interface des réclamations avec design moderne aligné (Décembre 2025)
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
import java.text.SimpleDateFormat
import java.util.*
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

enum class ReclamationStatus(val displayName: String, val color: Color) {
    PENDING("En attente", NoorAmber),
    IN_PROGRESS("En cours", NoorBlue),
    RESOLVED("Résolue", NoorGreen),
    REJECTED("Rejetée", NoorRed)
}

enum class ReclamationPriority(val displayName: String, val color: Color) {
    LOW("Basse", NoorGreen),
    MEDIUM("Moyenne", NoorAmber),
    HIGH("Haute", NoorRed),
    URGENT("Urgente", NoorPurple)
}

data class Reclamation(
    val id: String,
    val description: String,
    val date: Date,
    val status: ReclamationStatus,
    val streetlightId: String,
    val priority: ReclamationPriority,
    val location: String,
    val reportedBy: String,
    val assignedTo: String?
)

private val mockReclamations = listOf(
    Reclamation(
        "R001",
        "Lampadaire éteint depuis 3 jours",
        Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000),
        ReclamationStatus.IN_PROGRESS,
        "L001",
        ReclamationPriority.HIGH,
        "Avenue Habib Bourguiba",
        "Ahmed Ben Ali",
        "Technicien 1"
    ),
    Reclamation(
        "R002",
        "Lumière clignotante intermittente",
        Date(System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000),
        ReclamationStatus.PENDING,
        "L003",
        ReclamationPriority.MEDIUM,
        "Rue de la Liberté",
        "Fatma Saidi",
        null
    ),
    Reclamation(
        "R003",
        "Câblage endommagé visible",
        Date(System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000),
        ReclamationStatus.RESOLVED,
        "L005",
        ReclamationPriority.URGENT,
        "Boulevard du 7 Novembre",
        "Mohamed Trabelsi",
        "Technicien 2"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReclamationsScreen(
    modifier: Modifier = Modifier
) {
    var showAddReclamation by remember { mutableStateOf(false) }

    if (showAddReclamation) {
        AddReclamationScreen(
            onAddSuccess = { showAddReclamation = false },
            onBackPressed = { showAddReclamation = false }
        )
    } else {
        ReclamationsMainScreen(
            modifier = modifier,
            onNavigateToAddReclamation = { showAddReclamation = true }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReclamationsMainScreen(
    modifier: Modifier = Modifier,
    onNavigateToAddReclamation: () -> Unit
) {
    val reclamationsList = remember { mutableStateListOf(*mockReclamations.toTypedArray()) }
    var showTutorial by rememberSaveable { mutableStateOf(true) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<ReclamationStatus?>(null) }
    var selectedPriority by remember { mutableStateOf<ReclamationPriority?>(null) }

    val filteredReclamations = remember(reclamationsList.size, searchQuery, selectedStatus, selectedPriority) {
        reclamationsList.filter { reclamation ->
            val matchesSearch = searchQuery.isEmpty() ||
                    reclamation.id.contains(searchQuery, ignoreCase = true) ||
                    reclamation.description.contains(searchQuery, ignoreCase = true) ||
                    reclamation.location.contains(searchQuery, ignoreCase = true)
            val matchesStatus = selectedStatus == null || reclamation.status == selectedStatus
            val matchesPriority = selectedPriority == null || reclamation.priority == selectedPriority
            matchesSearch && matchesStatus && matchesPriority
        }.sortedBy { it.date }
    }

    val stats = remember(reclamationsList.toList()) {
        mapOf(
            "Total" to reclamationsList.size,
            "En cours" to reclamationsList.count { it.status == ReclamationStatus.PENDING || it.status == ReclamationStatus.IN_PROGRESS },
            "Résolues" to reclamationsList.count { it.status == ReclamationStatus.RESOLVED }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { ReclamationsTopBarModern(stats = stats) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddReclamation,
                containerColor = NoorBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nouvelle réclamation")
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

            item { ReclamationSearchBar(query = searchQuery, onQueryChange = { searchQuery = it }) }

            item {
                Text(
                    "Filtrer par statut",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(12.dp))
                ReclamationStatusFilters(
                    selectedStatus = selectedStatus,
                    onStatusSelected = { selectedStatus = if (selectedStatus == it) null else it }
                )
            }

            item {
                Text(
                    "Filtrer par priorité",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(12.dp))
                ReclamationPriorityFilters(
                    selectedPriority = selectedPriority,
                    onPrioritySelected = { selectedPriority = if (selectedPriority == it) null else it }
                )
            }

            item {
                Text(
                    "${filteredReclamations.size} réclamation${if (filteredReclamations.size != 1) "s" else ""} trouvé${if (filteredReclamations.size != 1) "es" else "e"}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (filteredReclamations.isEmpty()) {
                item {
                    EmptyState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        icon = Icons.Default.ReportOff,
                        title = "Aucune réclamation",
                        description = "Tout est en ordre pour le moment.",
                        actionLabel = "Nouvelle réclamation",
                        onActionClick = onNavigateToAddReclamation,
                        iconColor = NoorRed
                    )
                }
            } else {
                itemsIndexed(
                    items = filteredReclamations,
                    key = { _, reclamation -> reclamation.id }
                ) { index, reclamation ->
                    Box {
                        SwipeToDeleteContainer(
                            item = reclamation,
                            onDelete = { reclamationsList.remove(reclamation) }
                        ) { item ->
                            ReclamationCard(reclamation = item)
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
private fun ReclamationsTopBarModern(stats: Map<String, Int>) {
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
                        "Réclamations",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        letterSpacing = 0.4.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Gestion des signalements",
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
private fun ReclamationSearchBar(query: String, onQueryChange: (String) -> Unit) {
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
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            cursorColor = NoorBlue
        )
    )
}

@Composable
private fun ReclamationStatusFilters(
    selectedStatus: ReclamationStatus?,
    onStatusSelected: (ReclamationStatus) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        ReclamationStatus.entries.forEach { status ->
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
private fun ReclamationPriorityFilters(
    selectedPriority: ReclamationPriority?,
    onPrioritySelected: (ReclamationPriority) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        ReclamationPriority.entries.forEach { priority ->
            val isSelected = selectedPriority == priority
            FilterChip(
                onClick = { onPrioritySelected(priority) },
                label = { Text(priority.displayName) },
                selected = isSelected,
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = priority.color,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = priority.color
                )
            )
        }
    }
}

@Composable
private fun ReclamationCard(reclamation: Reclamation) {
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
                        .background(reclamation.priority.color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ReportProblem,
                        contentDescription = null,
                        tint = reclamation.priority.color,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reclamation.priority.displayName,
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
                            containerColor = reclamation.status.color,
                            modifier = Modifier.height(22.dp)
                        ) {
                            Text(
                                text = reclamation.status.displayName,
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
                    value = reclamation.reportedBy,
                    label = "Signalé par",
                    modifier = Modifier.weight(1f)
                )
                ModernStatItem(
                    value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(reclamation.date),
                    label = "Date",
                    modifier = Modifier.weight(1f)
                )
                ModernStatItem(
                    value = reclamation.assignedTo ?: "Non assigné",
                    label = "Assigné à",
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
                        label = "Localisation",
                        value = reclamation.location
                    )

                    Spacer(Modifier.height(10.dp))

                    InfoRow(
                        icon = Icons.Default.Description,
                        label = "Description",
                        value = reclamation.description
                    )

                    Spacer(Modifier.height(10.dp))

                    InfoRow(
                        icon = Icons.Default.Lightbulb,
                        label = "Lampadaire",
                        value = reclamation.streetlightId
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
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Traiter", fontSize = 13.sp)
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
fun ReclamationsScreenPreview() {
    SansaTheme(darkTheme = false) {
        ReclamationsScreen()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Mode Sombre")
@Composable
fun ReclamationsScreenDarkPreview() {
    SansaTheme(darkTheme = true) {
        ReclamationsScreen()
    }
}