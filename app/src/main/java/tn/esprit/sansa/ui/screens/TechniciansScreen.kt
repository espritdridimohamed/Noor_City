// TechniciansScreen.kt — Interface des techniciens avec design Noor
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.sansa.ui.theme.SansaTheme
import androidx.compose.runtime.saveable.rememberSaveable
import tn.esprit.sansa.ui.components.CoachMarkTooltip
import tn.esprit.sansa.ui.components.SwipeActionsContainer
import tn.esprit.sansa.ui.components.EmptyState
import tn.esprit.sansa.ui.components.StaggeredItem
import tn.esprit.sansa.ui.components.CardSkeleton
import kotlinx.coroutines.delay

import tn.esprit.sansa.ui.theme.*
import tn.esprit.sansa.ui.viewmodels.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import tn.esprit.sansa.ui.screens.models.Technician
import tn.esprit.sansa.ui.screens.models.TechnicianStatus
import tn.esprit.sansa.ui.screens.models.TechnicianSpecialty
// Palette Noor centralisée

// Local definitions removed in favor of shared models in tn.esprit.sansa.ui.screens.models.TechnicianModels.kt

private val mockTechnicians = listOf(
    Technician(
        "TECH001",
        "Ahmed Ben Salem",
        "ahmed.bensalem@sansa.tn",
        TechnicianSpecialty.ELECTRICAL,
        TechnicianStatus.AVAILABLE,
        "+216 98 765 432",
        156,
        98.5f,
        "Jan 2022",
        "Il y a 5 min",
        listOf("ZONE001")
    ),
    Technician(
        "TECH002",
        "Fatma Khelifi",
        "fatma.khelifi@sansa.tn",
        TechnicianSpecialty.NETWORK,
        TechnicianStatus.ON_MISSION,
        "+216 22 345 678",
        203,
        99.2f,
        "Mar 2021",
        "Il y a 2h",
        listOf("ZONE002")
    ),
    Technician(
        "TECH003",
        "Mohamed Trabelsi",
        "mohamed.trabelsi@sansa.tn",
        TechnicianSpecialty.MAINTENANCE,
        TechnicianStatus.BUSY,
        "+216 55 123 456",
        189,
        97.8f,
        "Jun 2022",
        "Il y a 30 min",
         listOf("ZONE001", "ZONE003")
    ),
    Technician(
        "TECH004",
        "Sarra Amri",
        "sarra.amri@sansa.tn",
        TechnicianSpecialty.SURVEILLANCE,
        TechnicianStatus.AVAILABLE,
        "+216 28 901 234",
        142,
        98.9f,
        "Sep 2022",
        "Il y a 1h",
         listOf("ZONE002")
    ),
    Technician(
        "TECH005",
        "Karim Nasri",
        "karim.nasri@sansa.tn",
        TechnicianSpecialty.TECHNICAL_SUPPORT,
        TechnicianStatus.ON_LEAVE,
        "+216 54 567 890",
        98,
        96.5f,
        "Nov 2023",
        "Il y a 2 jours",
         listOf("ZONE003")
    ),
    Technician(
        "TECH006",
        "Leila Gharbi",
        "leila.gharbi@sansa.tn",
        TechnicianSpecialty.ELECTRICAL,
        TechnicianStatus.OFFLINE,
        "+216 99 234 567",
        175,
        99.1f,
        "Feb 2021",
        "Il y a 1 jour",
         listOf("ZONE001")
    )
)


// ... imports ...

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechniciansScreen(
    modifier: Modifier = Modifier,
    onNavigateToAddTechnician: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {
    val realTechnicians by viewModel.technicians.collectAsState()
    
    // Map UserAccount to Technician view model
    val techniciansList = remember(realTechnicians) {
        realTechnicians.map { account ->
            Technician(
                id = account.uid,
                name = account.name,
                email = account.email,
                specialty = TechnicianSpecialty.entries.find { it.displayName == account.specialty } ?: TechnicianSpecialty.MAINTENANCE,
                status = TechnicianStatus.AVAILABLE,
                phone = "+216 -- --- ---",
                interventionsCount = 0,
                successRate = 100f,
                joinDate = "Nouveau",
                lastActivity = "À l'instant"
            )
        }.toMutableStateList()
    }
    
    var showTutorial by rememberSaveable { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.fetchTechnicians()
        delay(800) 
        isLoading = false
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<TechnicianStatus?>(null) }
    var selectedSpecialty by remember { mutableStateOf<TechnicianSpecialty?>(null) }

    val filteredTechnicians = remember(techniciansList.size, searchQuery, selectedStatus, selectedSpecialty) {
        techniciansList.filter { tech ->
            val matchesSearch = searchQuery.isEmpty() ||
                    tech.id.contains(searchQuery, ignoreCase = true) ||
                    tech.name.contains(searchQuery, ignoreCase = true) ||
                    tech.email.contains(searchQuery, ignoreCase = true)
            val matchesStatus = selectedStatus == null || tech.status == selectedStatus
            val matchesSpecialty = selectedSpecialty == null || tech.specialty == selectedSpecialty
            matchesSearch && matchesStatus && matchesSpecialty
        }.sortedBy { it.name }
    }

    val stats = remember(techniciansList.toList()) {
        mapOf(
            "Total" to techniciansList.size,
            "Disponibles" to techniciansList.count { it.status == TechnicianStatus.AVAILABLE },
            "En mission" to techniciansList.count { it.status == TechnicianStatus.ON_MISSION }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TechniciansTopBar(stats = stats, onProfileClick = onNavigateToProfile) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTechnician,
                containerColor = NoorIndigo,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nouveau technicien")
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

            item { TechnicianSearchBar(query = searchQuery, onQueryChange = { searchQuery = it }) }

            item {
                Text("Filtrer par statut", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(12.dp))
                StatusFilters(
                    selectedStatus = selectedStatus,
                    onStatusSelected = { selectedStatus = if (selectedStatus == it) null else it }
                )
            }

            item {
                Text("Filtrer par spécialité", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(12.dp))
                SpecialtyFilters(
                    selectedSpecialty = selectedSpecialty,
                    onSpecialtySelected = { selectedSpecialty = if (selectedSpecialty == it) null else it }
                )
            }

            item {
                Text(
                    text = "${filteredTechnicians.size} technicien${if (filteredTechnicians.size != 1) "s" else ""} trouvé${if (filteredTechnicians.size != 1) "s" else ""}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (isLoading) {
                items(5) {
                    CardSkeleton()
                }
            } else if (filteredTechnicians.isEmpty()) {
                item {
                    EmptyState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        icon = Icons.Default.GroupOff,
                        title = "Aucun technicien trouvé",
                        description = "Modifiez vos filtres ou ajoutez un nouveau technicien.",
                        actionLabel = "Nouveau technicien",
                        onActionClick = onNavigateToAddTechnician,
                        iconColor = NoorIndigo
                    )
                }
            } else {
                itemsIndexed(
                    items = filteredTechnicians,
                    key = { _, technician -> technician.id }
                ) { index, technician ->
                    StaggeredItem(index = index) {
                        Box {
                            SwipeActionsContainer(
                                item = technician,
                                onDelete = { techniciansList.remove(technician) }
                            ) { item ->
                                TechnicianCard(
                                    technician = item,
                                    onNavigateToProfile = onNavigateToProfile
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
}


@Composable
private fun TechniciansTopBar(stats: Map<String, Int>, onProfileClick: () -> Unit) {
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
                        "Équipe technique",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        letterSpacing = 0.4.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Gestion des techniciens",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.6).sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { /* TODO: Refresh */ }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Actualiser",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Mon Profil",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
// ... existing code ...

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
private fun TechnicianSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Rechercher par nom, ID, email...") },
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
private fun StatusFilters(
    selectedStatus: TechnicianStatus?,
    onStatusSelected: (TechnicianStatus) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        TechnicianStatus.entries.forEach { status ->
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
private fun SpecialtyFilters(
    selectedSpecialty: TechnicianSpecialty?,
    onSpecialtySelected: (TechnicianSpecialty) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        TechnicianSpecialty.entries.forEach { specialty ->
            val isSelected = selectedSpecialty == specialty
            FilterChip(
                onClick = { onSpecialtySelected(specialty) },
                label = { Text(specialty.displayName) },
                selected = isSelected,
                leadingIcon = {
                    Icon(specialty.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = specialty.color,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = specialty.color
                )
            )
        }
    }
}

@Composable
private fun TechnicianCard(
    technician: Technician,
    onNavigateToProfile: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    
    // Smoother, more subtle animations
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
        shape = RoundedCornerShape(20.dp), // More modern, less rounded
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .clickable(interactionSource = interactionSource, indication = null) { 
                    onNavigateToProfile()
                }
                .padding(18.dp) // Slightly reduced padding for modern look
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Avatar with subtle background
                Box(
                    modifier = Modifier
                        .size(56.dp) // Slightly smaller
                        .clip(RoundedCornerShape(16.dp)) // Rounded square instead of circle
                        .background(technician.specialty.color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        technician.specialty.icon,
                        contentDescription = null,
                        tint = technician.specialty.color,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    // Name - larger and bolder
                    Text(
                        text = technician.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(Modifier.height(6.dp))
                    
                    // Badges - more compact
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Badge(
                            containerColor = technician.status.color,
                            modifier = Modifier.height(22.dp)
                        ) {
                            Text(
                                text = technician.status.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                        
                        Badge(
                            containerColor = technician.specialty.color.copy(alpha = 0.15f),
                            modifier = Modifier.height(22.dp)
                        ) {
                            Text(
                                text = technician.specialty.displayName,
                                fontSize = 11.sp,
                                color = technician.specialty.color,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // Expand indicator
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Réduire" else "Développer",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            // Stats Row - more compact and modern
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModernStatItem(
                    value = "${technician.interventionsCount}",
                    label = "Interventions",
                    modifier = Modifier.weight(1f)
                )
                ModernStatItem(
                    value = "${technician.successRate}%",
                    label = "Réussite",
                    modifier = Modifier.weight(1f)
                )
                ModernStatItem(
                    value = technician.lastActivity,
                    label = "Activité",
                    modifier = Modifier.weight(1f)
                )
            }

            // Expanded content
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
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = technician.email
                    )

                    Spacer(Modifier.height(10.dp))

                    InfoRow(
                        icon = Icons.Default.Phone,
                        label = "Tél",
                        value = technician.phone
                    )

                    Spacer(Modifier.height(10.dp))

                    InfoRow(
                        icon = Icons.Default.CalendarMonth,
                        label = "Date d'embauche",
                        value = technician.joinDate
                    )

                    Spacer(Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { /* TODO */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Assigner", fontSize = 13.sp)
                        }
                        Button(
                            onClick = { /* TODO */ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = NoorIndigo)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Profil", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

// Modern stat component
@Composable
private fun ModernStatItem(value: String, label: String, modifier: Modifier = Modifier) {
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
private fun StatItem(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
    }
}

@Composable
private fun InfoRow(
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
private fun PreviewTechniciansLight() {
    SansaTheme(darkTheme = false) {
        TechniciansScreen()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Mode Sombre")
@Composable
private fun PreviewTechniciansDark() {
    SansaTheme(darkTheme = true) {
        TechniciansScreen(onNavigateToProfile = {}) // Fixed preview
    }
}
