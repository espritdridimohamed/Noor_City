// ReclamationsScreen.kt – Version Connectée (Décembre 2025)
package tn.esprit.sansa.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import tn.esprit.sansa.ui.theme.SansaTheme
import tn.esprit.sansa.ui.theme.*
import tn.esprit.sansa.ui.components.*
import tn.esprit.sansa.ui.screens.models.*
import tn.esprit.sansa.ui.viewmodels.ReclamationsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReclamationsScreen(
    modifier: Modifier = Modifier,
    viewModel: ReclamationsViewModel = viewModel()
) {
    var showAddReclamation by remember { mutableStateOf(false) }

    if (showAddReclamation) {
        AddReclamationScreen(
            onAddSuccess = { showAddReclamation = false },
            onBackPressed = { showAddReclamation = false },
            viewModel = viewModel
        )
    } else {
        ReclamationsMainScreen(
            modifier = modifier,
            onNavigateToAddReclamation = { showAddReclamation = true },
            viewModel = viewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReclamationsMainScreen(
    modifier: Modifier = Modifier,
    onNavigateToAddReclamation: () -> Unit,
    viewModel: ReclamationsViewModel
) {
    val reclamationsList by viewModel.reclamations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showTutorial by rememberSaveable { mutableStateOf(true) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<ReclamationStatus?>(null) }
    var selectedPriority by remember { mutableStateOf<ReclamationPriority?>(null) }

    val filteredReclamations = remember(reclamationsList, searchQuery, selectedStatus, selectedPriority) {
        reclamationsList.filter { reclamation ->
            val matchesSearch = searchQuery.isEmpty() ||
                    reclamation.id.contains(searchQuery, ignoreCase = true) ||
                    reclamation.description.contains(searchQuery, ignoreCase = true) ||
                    reclamation.location.contains(searchQuery, ignoreCase = true)
            val matchesStatus = selectedStatus == null || reclamation.status == selectedStatus
            val matchesPriority = selectedPriority == null || reclamation.priority == selectedPriority
            matchesSearch && matchesStatus && matchesPriority
        }.sortedByDescending { it.date }
    }

    val stats = remember(reclamationsList) {
        mapOf(
            "Total" to reclamationsList.size,
            "En attente" to reclamationsList.count { it.status == ReclamationStatus.PENDING || it.status == ReclamationStatus.IN_PROGRESS },
            "Résolues" to reclamationsList.count { it.status == ReclamationStatus.RESOLVED }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { ReclamationsTopBarModern(stats = stats, onRefresh = { viewModel.refresh() }) },
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
                Text("Filtrer par statut", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                ReclamationStatusFilters(
                    selectedStatus = selectedStatus,
                    onStatusSelected = { selectedStatus = if (selectedStatus == it) null else it }
                )
            }

            item {
                Text("Filtrer par priorité", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                ReclamationPriorityFilters(
                    selectedPriority = selectedPriority,
                    onPrioritySelected = { selectedPriority = if (selectedPriority == it) null else it }
                )
            }

            if (isLoading) {
                items(5) { CardSkeleton() }
            } else if (filteredReclamations.isEmpty()) {
                item {
                    EmptyState(
                        modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
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
                    StaggeredItem(index = index) {
                        Box {
                            SwipeToDeleteContainer(
                                item = reclamation,
                                onDelete = { viewModel.deleteReclamation(reclamation.id) }
                            ) { item ->
                                ReclamationCard(reclamation = item)
                            }

                            if (index == 0 && showTutorial && reclamationsList.isNotEmpty()) {
                                CoachMarkTooltip(
                                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp).offset(x = 16.dp, y = 32.dp),
                                    text = "Glissez vers la gauche pour supprimer",
                                    onDismiss = { showTutorial = false }
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun ReclamationsTopBarModern(stats: Map<String, Int>, onRefresh: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(NoorBlue, NoorBlue.copy(alpha = 0.7f))
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
                    Text("Signalements Citoyens", color = Color.White.copy(0.85f), fontSize = 14.sp)
                    Text("Gestion Urbaine", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
                }
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualiser", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                stats.forEach { (label, value) ->
                    QuickStatCardCompact(value = value.toString(), label = label, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuickStatCardCompact(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = label, color = Color.White.copy(0.9f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun ReclamationSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Rechercher un signalement...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NoorBlue,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            cursorColor = NoorBlue
        )
    )
}

@Composable
private fun ReclamationStatusFilters(selectedStatus: ReclamationStatus?, onStatusSelected: (ReclamationStatus) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
        ReclamationStatus.entries.forEach { status ->
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onStatusSelected(status) },
                label = { Text(status.displayName) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = status.color,
                    selectedLabelColor = Color.White,
                    labelColor = status.color
                )
            )
        }
    }
}

@Composable
private fun ReclamationPriorityFilters(selectedPriority: ReclamationPriority?, onPrioritySelected: (ReclamationPriority) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
        ReclamationPriority.entries.forEach { priority ->
            FilterChip(
                selected = selectedPriority == priority,
                onClick = { onPrioritySelected(priority) },
                label = { Text(priority.displayName) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = priority.color,
                    selectedLabelColor = Color.White,
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
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f)

    Card(
        modifier = Modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.clickable { expanded = !expanded }.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(12.dp)).background(reclamation.priority.color.copy(0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ReportProblem, null, tint = reclamation.priority.color)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(reclamation.reportedBy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Badge(containerColor = reclamation.status.color) { 
                        Text(reclamation.status.displayName, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp)) 
                    }
                }
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
            }

            Spacer(Modifier.height(14.dp))
            Text(reclamation.description, maxLines = if (expanded) Int.MAX_VALUE else 2, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            AnimatedVisibility(visible = expanded) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    InfoRow(Icons.Default.LocationOn, "Lieu", reclamation.location)
                    InfoRow(Icons.Default.CalendarToday, "Date", SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(reclamation.date)))
                    if (reclamation. streetlightId.isNotBlank()) InfoRow(Icons.Default.Lightbulb, "ID Lampadaire", reclamation.streetlightId)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = NoorBlue)
        Spacer(Modifier.width(8.dp))
        Text("$label: ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 12.sp)
    }
}
