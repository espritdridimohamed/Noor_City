// TechniciansScreen.kt — Interface des techniciens avec design Noor
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

enum class TechnicianStatus(val displayName: String, val color: Color) {
    AVAILABLE("Disponible", NoorGreen),
    BUSY("Occupé", NoorAmber),
    ON_MISSION("En mission", NoorBlue),
    OFFLINE("Hors ligne", NoorRed),
    ON_LEAVE("En congé", NoorPurple)
}

enum class TechnicianSpecialty(val displayName: String, val color: Color, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    ELECTRICAL("Électricien", NoorAmber, Icons.Default.ElectricBolt),
    NETWORK("Réseau", NoorCyan, Icons.Default.Router),
    MAINTENANCE("Maintenance", NoorBlue, Icons.Default.Build),
    SURVEILLANCE("Surveillance", NoorPurple, Icons.Default.Videocam),
    TECHNICAL_SUPPORT("Support technique", NoorGreen, Icons.Default.SupportAgent)
}

data class Technician(
    val id: String,
    val name: String,
    val email: String,
    val specialty: TechnicianSpecialty,
    val status: TechnicianStatus,
    val phone: String,
    val interventionsCount: Int,
    val successRate: Float,
    val joinDate: String,
    val lastActivity: String
)

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
        "Il y a 5 min"
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
        "Il y a 2h"
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
        "Il y a 30 min"
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
        "Il y a 1h"
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
        "Il y a 2 jours"
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
        "Il y a 1 jour"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechniciansScreen(
    modifier: Modifier = Modifier,
    onNavigateToAddTechnician: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<TechnicianStatus?>(null) }
    var selectedSpecialty by remember { mutableStateOf<TechnicianSpecialty?>(null) }

    val filteredTechnicians = remember(searchQuery, selectedStatus, selectedSpecialty) {
        mockTechnicians.filter { tech ->
            val matchesSearch = searchQuery.isEmpty() ||
                    tech.id.contains(searchQuery, ignoreCase = true) ||
                    tech.name.contains(searchQuery, ignoreCase = true) ||
                    tech.email.contains(searchQuery, ignoreCase = true)
            val matchesStatus = selectedStatus == null || tech.status == selectedStatus
            val matchesSpecialty = selectedSpecialty == null || tech.specialty == selectedSpecialty
            matchesSearch && matchesStatus && matchesSpecialty
        }.sortedBy { it.name }
    }

    val stats = remember(mockTechnicians) {
        mapOf(
            "Total" to mockTechnicians.size,
            "Disponibles" to mockTechnicians.count { it.status == TechnicianStatus.AVAILABLE },
            "En mission" to mockTechnicians.count { it.status == TechnicianStatus.ON_MISSION }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TechniciansTopBar(stats = stats) },
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

            items(filteredTechnicians) { technician ->
                TechnicianCard(technician = technician)
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun TechniciansTopBar(stats: Map<String, Int>) {
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
                        "Équipe technique",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        letterSpacing = 0.4.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Gestion des techniciens",
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
private fun TechnicianCard(technician: Technician) {
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
                        listOf(technician.status.color.copy(0.1f), MaterialTheme.colorScheme.surface)
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
                            .background(technician.specialty.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            technician.specialty.icon,
                            contentDescription = null,
                            tint = technician.specialty.color,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(technician.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Badge(containerColor = technician.status.color) {
                                Text(technician.status.displayName, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.width(8.dp))
                            Badge(containerColor = technician.specialty.color.copy(alpha = 0.2f)) {
                                Text(technician.specialty.displayName, fontSize = 10.sp, color = technician.specialty.color, fontWeight = FontWeight.Medium)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            technician.id,
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
                        value = "${technician.interventionsCount}",
                        label = "Interventions",
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        value = "${technician.successRate}%",
                        label = "Réussite",
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        value = technician.lastActivity,
                        label = "Dernière activité",
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
                            icon = Icons.Default.Email,
                            label = "Email",
                            value = technician.email
                        )

                        Spacer(Modifier.height(12.dp))

                        InfoRow(
                            icon = Icons.Default.Phone,
                            label = "Téléphone",
                            value = technician.phone
                        )

                        Spacer(Modifier.height(12.dp))

                        InfoRow(
                            icon = Icons.Default.CalendarMonth,
                            label = "Date d'embauche",
                            value = technician.joinDate
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { /* TODO */ }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Assignment, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Assigner")
                            }
                            Button(
                                onClick = { /* TODO */ },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = NoorIndigo)
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Profil")
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
        TechniciansScreen()
    }
}