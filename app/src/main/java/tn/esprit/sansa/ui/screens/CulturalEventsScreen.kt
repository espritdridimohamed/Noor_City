// CulturalEventsScreen.kt — Version moderne alignée sur Technicians/Sensors/Interventions/Cameras/Citizens (Décembre 2025)
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
import androidx.compose.foundation.lazy.items
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
import tn.esprit.sansa.ui.components.StaggeredItem
import tn.esprit.sansa.ui.screens.models.UserRole

import tn.esprit.sansa.ui.theme.*
import tn.esprit.sansa.ui.screens.models.*
import tn.esprit.sansa.ui.viewmodels.CulturalEventsViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CulturalEventsScreen(
    viewModel: CulturalEventsViewModel,
    modifier: Modifier = Modifier,
    role: UserRole? = UserRole.CITIZEN,
    onNavigateToAddEvent: () -> Unit = {}
) {
    val eventsList by viewModel.events.collectAsStateWithLifecycle()
    val lightingPrograms by viewModel.lightingPrograms.collectAsStateWithLifecycle()

    CulturalEventsContent(
        eventsList = eventsList,
        modifier = modifier,
        lightingPrograms = lightingPrograms, // NEW
        role = role,
        onNavigateToAddEvent = onNavigateToAddEvent,
        onUpdateEventStatus = viewModel::updateEventStatus,
        onDeleteEvent = viewModel::deleteEvent,
        onConfirmProgram = { programId -> viewModel.confirmLightingProgram(programId) } // NEW
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CulturalEventsContent(
    eventsList: List<CulturalEvent>,
    modifier: Modifier = Modifier,
    lightingPrograms: List<LightingProgram> = emptyList(), // NEW
    role: UserRole? = UserRole.CITIZEN,
    onNavigateToAddEvent: () -> Unit = {},
    onUpdateEventStatus: (CulturalEvent, EventStatus) -> Unit = { _, _ -> },
    onDeleteEvent: (CulturalEvent) -> Unit = {},
    onConfirmProgram: (String) -> Unit = {} // NEW
) {
    var showTutorial by rememberSaveable { mutableStateOf(true) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<EventStatus?>(null) }
    var selectedAmbiance by remember { mutableStateOf<AmbianceType?>(null) }

    val filteredEvents = remember(eventsList, searchQuery, selectedStatus, selectedAmbiance) {
        eventsList.filter { event ->
            val matchesSearch = searchQuery.isEmpty() ||
                    event.id.contains(searchQuery, ignoreCase = true) ||
                    event.name.contains(searchQuery, ignoreCase = true) ||
                    event.description.contains(searchQuery, ignoreCase = true)
            val matchesStatus = selectedStatus == null || event.status == selectedStatus
            val matchesAmbiance = selectedAmbiance == null || event.ambianceType == selectedAmbiance
            
            // FILTER: Citizens should NOT see PENDING events in the main list
            val isVisibleForRole = if (role == UserRole.CITIZEN) event.status != EventStatus.PENDING else true

            matchesSearch && matchesStatus && matchesAmbiance && isVisibleForRole
        }.sortedBy { it.dateTime }
    }
    
    // ADMIN ONLY: Pending events management
    val pendingEvents = remember(eventsList) {
        eventsList.filter { it.status == EventStatus.PENDING }
    }
    var showPendingDialog by remember { mutableStateOf(false) }

    // SIMULATED NOTIFICATION FOR CITIZEN (When an event becomes UPCOMING)
    // In a real app, this would be triggered by a backend push. 
    // Here we just check if there are new UPCOMING events that were previously PENDING (mock logic)
    val hasCitizenNotification by remember(eventsList) {
        mutableStateOf(eventsList.any { it.status == EventStatus.UPCOMING && it.id == "EVT_NEW" }) // Mock condition
    }

    // STATE FOR DIALOGS
    var showTechnicianDialog by remember { mutableStateOf(false) }

    // TECHNICIAN NOTIFICATIONS
    val myPrograms = remember(lightingPrograms, role) {
        if (role == UserRole.TECHNICIAN) { // Assuming mock user is TECH001
            lightingPrograms.filter { it.technicianId == "TECH001" }
        } else emptyList()
    }
    val technicianPendingCount = myPrograms.count { it.technicianStatus == TechnicianAssignmentStatus.WAITING }

    val stats = remember(eventsList) {
        mapOf(
            "Total" to eventsList.size,
            "À venir" to eventsList.count { it.status == EventStatus.UPCOMING },
            "En cours" to eventsList.count { it.status == EventStatus.ACTIVE }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { 
            CulturalEventsTopBarModern(
                stats = stats, 
                role = role,
                pendingCount = when(role) {
                    UserRole.ADMIN -> pendingEvents.size + lightingPrograms.count { it.technicianStatus == TechnicianAssignmentStatus.ACCEPTED }
                    UserRole.TECHNICIAN -> technicianPendingCount
                    else -> if (hasCitizenNotification) 1 else 0
                },
                onNotificationClick = { 
                    if (role == UserRole.ADMIN) showPendingDialog = true 
                    else if (role == UserRole.TECHNICIAN) showTechnicianDialog = true
                    else showPendingDialog = true // For citizen demo
                }
            ) 
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddEvent,
                containerColor = NoorBlue,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(8.dp, 12.dp)
            ) {
                Icon(
                    imageVector = if (role == UserRole.ADMIN) Icons.Default.Add else Icons.Default.PostAdd,
                    contentDescription = if (role == UserRole.ADMIN) "Nouvel événement" else "Proposer un événement"
                )
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

            item { EventSearchBar(query = searchQuery, onQueryChange = { searchQuery = it }) }

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

            if (filteredEvents.isEmpty()) {
                item {
                    EmptyState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        icon = Icons.Default.EventBusy,
                        title = "Aucun événement",
                        description = "Il n'y a rien de prévu pour le moment.",
                        actionLabel = "Créer un événement",
                        onActionClick = onNavigateToAddEvent,
                        iconColor = NoorBlue
                    )
                }
            } else {
                itemsIndexed(
                    items = filteredEvents,
                    key = { _, event -> event.id }
                ) { index, event ->
                    StaggeredItem(index = index) {
                        Box {
                            SwipeToDeleteContainer(
                                item = event,
                                onDelete = { onDeleteEvent(event) }
                            ) { item ->
                                CulturalEventCard(
                                    event = item,
                                    role = role,
                                    onApprove = {
                                        onUpdateEventStatus(item, EventStatus.UPCOMING)
                                    },
                                    onReject = {
                                        onUpdateEventStatus(item, EventStatus.CANCELLED)
                                    }
                                )
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
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
        
        if (showPendingDialog && role == UserRole.CITIZEN) {
             AlertDialog(
                onDismissRequest = { showPendingDialog = false },
                title = { Text("Mise à jour") },
                text = { Text("Votre événement 'Festival Lumières' a été approuvé par l'administrateur !") },
                confirmButton = { TextButton(onClick = { showPendingDialog = false }) { Text("Super !") } }
            )
        }
        

        if (showPendingDialog && role == UserRole.ADMIN) {
            val acceptedPrograms = lightingPrograms.filter { it.technicianStatus == TechnicianAssignmentStatus.ACCEPTED }
            PendingEventsDialog(
                events = pendingEvents,
                acceptedPrograms = acceptedPrograms,
                onDismiss = { showPendingDialog = false },
                onApprove = { event ->
                     onUpdateEventStatus(event, EventStatus.UPCOMING)
                     // Removed auto-dialog as per new workflow
                     showPendingDialog = false 
                },
                onReject = { event ->
                    onUpdateEventStatus(event, EventStatus.CANCELLED)
                }
            )
        }


        if (showTechnicianDialog) {
            TechnicianProgramsDialog(
                programs = myPrograms,
                onDismiss = { showTechnicianDialog = false },
                onConfirm = { program ->
                    onConfirmProgram(program.id)
                    // Optional: show success message
                }
            )
        }
}

@Composable
private fun CulturalEventsTopBarModern(
    stats: Map<String, Int>,
    role: UserRole? = UserRole.CITIZEN,
    pendingCount: Int = 0,
    onNotificationClick: () -> Unit = {}
) {
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
                        "Événements culturels",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        letterSpacing = 0.4.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Ambiances illuminées",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.6).sp
                    )
                }
                if ((role == UserRole.ADMIN && pendingCount > 0) || (role == UserRole.CITIZEN && pendingCount > 0)) {
                     BadgedBox(
                        badge = {
                            Badge(
                                containerColor = if (role == UserRole.ADMIN) NoorRed else NoorGreen,
                                contentColor = Color.White
                            ) {
                                Text("$pendingCount") 
                            }
                        }
                    ) {
                        IconButton(onClick = onNotificationClick) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                } else {
                     IconButton(onClick = { /* TODO: Refresh */ }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Actualiser",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
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
private fun CulturalEventCard(
    event: CulturalEvent,
    role: UserRole? = null,
    onApprove: () -> Unit = {},
    onReject: () -> Unit = {}
) {
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
                        .background(event.ambianceType.color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        event.ambianceType.icon,
                        contentDescription = null,
                        tint = event.ambianceType.color,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.name,
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
                            containerColor = event.status.color,
                            modifier = Modifier.height(22.dp)
                        ) {
                            Text(
                                text = event.status.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        Badge(
                            containerColor = event.ambianceType.color.copy(alpha = 0.15f),
                            modifier = Modifier.height(22.dp)
                        ) {
                            Text(
                                text = event.ambianceType.displayName,
                                fontSize = 11.sp,
                                color = event.ambianceType.color,
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
                    value = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(event.dateTime),
                    label = "Date",
                    modifier = Modifier.weight(1f)
                )
                ModernStatItem(
                    value = "${event.duration}h",
                    label = "Durée",
                    modifier = Modifier.weight(1f)
                )
                ModernStatItem(
                    value = event.attendees.toString(),
                    label = "Participants",
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
                        value = event.description
                    )

                    Spacer(Modifier.height(10.dp))

                    InfoRow(
                        icon = Icons.Default.Business,
                        label = "Organisateur",
                        value = event.organizer
                    )

                    Spacer(Modifier.height(10.dp))

                    InfoRow(
                        icon = Icons.Default.LocationOn,
                        label = "Zones",
                        value = event.zones.joinToString(", ")
                    )

                    Spacer(Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (role == UserRole.ADMIN && event.status == EventStatus.PENDING) {
                            Button(
                                onClick = onReject,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = NoorRed)
                            ) {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Refuser", fontSize = 13.sp)
                            }
                            Button(
                                onClick = onApprove,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = NoorGreen)
                            ) {
                                Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Approuver", fontSize = 13.sp)
                            }
                        } else {
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
                                colors = ButtonDefaults.buttonColors(containerColor = event.ambianceType.color)
                            ) {
                                Icon(Icons.Default.Lightbulb, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Configurer", fontSize = 13.sp)
                            }
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
    val mockEvents = listOf(
        CulturalEvent(
            "EVT001", "Festival Lumières", Date(), listOf("Zone A"), 
            AmbianceType.FESTIVE, EventStatus.UPCOMING, 2, "Desc", 100, "Org"
        )
    )
    SansaTheme(darkTheme = false) {
        CulturalEventsContent(eventsList = mockEvents)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Mode Sombre")
@Composable
private fun PreviewCulturalEventsDark() {
    val mockEvents = listOf(
        CulturalEvent(
            "EVT001", "Festival Lumières", Date(), listOf("Zone A"), 
            AmbianceType.FESTIVE, EventStatus.UPCOMING, 2, "Desc", 100, "Org"
        )
    )
    SansaTheme(darkTheme = true) {
        CulturalEventsContent(eventsList = mockEvents)
    }
}

@Composable
private fun PendingEventsDialog(
    events: List<CulturalEvent>,
    acceptedPrograms: List<LightingProgram> = emptyList(),
    onDismiss: () -> Unit,
    onApprove: (CulturalEvent) -> Unit,
    onReject: (CulturalEvent) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Événements en attente") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (events.isNotEmpty()) {
                    item { Text("Événements en attente", fontWeight = FontWeight.Bold, color = NoorOrange) }
                    items(events) { event ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(event.name, fontWeight = FontWeight.Bold)
                                Text(event.organizer, fontSize = 12.sp)
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { onReject(event) },
                                        colors = ButtonDefaults.buttonColors(containerColor = NoorRed),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Refuser")
                                    }
                                    Button(
                                        onClick = { onApprove(event) },
                                        colors = ButtonDefaults.buttonColors(containerColor = NoorGreen),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Approuver")
                                    }
                                }
                            }
                        }
                    }
                }

                if (acceptedPrograms.isNotEmpty()) {
                    item { Text("Validations Techniques", fontWeight = FontWeight.Bold, color = NoorGreen) }
                    items(acceptedPrograms) { program ->
                         Card(
                            colors = CardDefaults.cardColors(containerColor = NoorGreen.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Programme ${program.id}", fontWeight = FontWeight.Bold)
                                    Text("Technicien a confirmé", fontSize = 12.sp, color = NoorGreen)
                                }
                                Icon(Icons.Default.CheckCircle, null, tint = NoorGreen)
                            }
                        }
                    }
                }
                
                if (events.isEmpty() && acceptedPrograms.isEmpty()) {
                    item { Text("Aucune nouvelle notification.") }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fermer") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TechnicianProgramsDialog(
    programs: List<LightingProgram>,
    onDismiss: () -> Unit,
    onConfirm: (LightingProgram) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mes Affectations") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(programs) { program ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(program.id, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Badge(containerColor = if(program.technicianStatus == TechnicianAssignmentStatus.ACCEPTED) NoorGreen else NoorAmber) {
                                    Text(program.technicianStatus?.name ?: "UNKNOWN")
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Assignation Lampadaires: ${program.associatedStreetlights.size}", fontWeight = FontWeight.SemiBold)
                            Text("Lampadaires: ${program.associatedStreetlights.joinToString(", ")}", fontSize = 12.sp)
                            
                            Spacer(Modifier.height(16.dp))
                            if (program.technicianStatus == TechnicianAssignmentStatus.WAITING) {
                                Button(
                                    onClick = { onConfirm(program) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NoorBlue),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Confirmer l'intervention")
                                }
                            }
                        }
                    }
                }
                if (programs.isEmpty()) {
                   item { Text("Aucune affectation en cours.") } 
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fermer") }
        }
    )
}