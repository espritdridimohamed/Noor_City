// CitizensScreen.kt — Interface des citoyens avec design Noor (aligné sur style Technicians/Sensors)
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
private val NoorPink = Color(0xFFEC4899)

enum class CitizenStatus(val displayName: String, val color: Color) {
    ACTIVE("Actif", NoorGreen),
    INACTIVE("Inactif", NoorRed),
    NEW("Nouveau", NoorCyan),
    VIP("VIP", NoorPink)
}

data class Citizen(
    val id: String,
    val name: String,
    val email: String,
    val phone: String?,
    val status: CitizenStatus,
    val reclamationsCount: Int,
    val registrationDate: String,
    val lastActivity: String,
    val notificationsEnabled: Boolean,
    val address: String
)

private val mockCitizens = listOf(
    Citizen(
        "CIT001",
        "Youssef Hamdi",
        "youssef.hamdi@email.tn",
        "+216 98 123 456",
        CitizenStatus.ACTIVE,
        12,
        "Jan 2023",
        "Il y a 1h",
        true,
        "Avenue Habib Bourguiba, Tunis"
    ),
    Citizen(
        "CIT002",
        "Amira Slimani",
        "amira.slimani@email.tn",
        null,
        CitizenStatus.ACTIVE,
        8,
        "Mar 2023",
        "Il y a 2 jours",
        false,
        "Rue de la Liberté, La Marsa"
    ),
    Citizen(
        "CIT003",
        "Mehdi Bouazizi",
        "mehdi.bouazizi@email.tn",
        "+216 55 789 012",
        CitizenStatus.VIP,
        34,
        "Sep 2022",
        "Il y a 30 min",
        true,
        "Boulevard du 7 Novembre, Carthage"
    ),
    // Ajoute d'autres mocks si nécessaire
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizensScreen(
    modifier: Modifier = Modifier,
    onNavigateToAddCitizen: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<CitizenStatus?>(null) }

    val filteredCitizens = remember(searchQuery, selectedStatus) {
        mockCitizens.filter { citizen ->
            val matchesSearch = searchQuery.isEmpty() ||
                    citizen.id.contains(searchQuery, ignoreCase = true) ||
                    citizen.name.contains(searchQuery, ignoreCase = true) ||
                    citizen.email.contains(searchQuery, ignoreCase = true)
            val matchesStatus = selectedStatus == null || citizen.status == selectedStatus
            matchesSearch && matchesStatus
        }.sortedBy { it.name }
    }

    val stats = remember(mockCitizens) {
        mapOf(
            "Total" to mockCitizens.size,
            "Actifs" to mockCitizens.count { it.status == CitizenStatus.ACTIVE || it.status == CitizenStatus.VIP },
            "Inactifs" to mockCitizens.count { it.status == CitizenStatus.INACTIVE }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { CitizensTopBarModern(stats = stats) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddCitizen,
                containerColor = NoorBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nouveau citoyen")
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

            item { CitizenSearchBar(query = searchQuery, onQueryChange = { searchQuery = it }) }

            item {
                Text("Filtrer par statut", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(12.dp))
                CitizenStatusFilters(
                    selectedStatus = selectedStatus,
                    onStatusSelected = { selectedStatus = if (selectedStatus == it) null else it }
                )
            }

            item {
                Text(
                    text = "${filteredCitizens.size} citoyen${if (filteredCitizens.size != 1) "s" else ""} trouvé${if (filteredCitizens.size != 1) "s" else ""}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(filteredCitizens) { citizen ->
                CitizenCard(citizen = citizen)
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun CitizensTopBarModern(stats: Map<String, Int>) {
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
                        "Gestion des citoyens",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        letterSpacing = 0.4.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Communauté active",
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

// Carte de stats compacte – même version que pour Sensors & Cameras
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
private fun CitizenSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Rechercher par nom, ID ou email...") },
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
private fun CitizenStatusFilters(
    selectedStatus: CitizenStatus?,
    onStatusSelected: (CitizenStatus) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        CitizenStatus.entries.forEach { status ->
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
private fun CitizenCard(citizen: Citizen) {
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
                        listOf(citizen.status.color.copy(0.1f), MaterialTheme.colorScheme.surface)
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
                            .background(citizen.status.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = citizen.status.color,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(citizen.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Badge(containerColor = citizen.status.color) {
                                Text(citizen.status.displayName, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            citizen.id,
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
                        value = citizen.reclamationsCount.toString(),
                        label = "Réclamations",
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        value = citizen.registrationDate,
                        label = "Inscription",
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        value = citizen.lastActivity,
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
                            value = citizen.email
                        )

                        Spacer(Modifier.height(12.dp))

                        InfoRow(
                            icon = Icons.Default.Phone,
                            label = "Téléphone",
                            value = citizen.phone ?: "Non renseigné"
                        )

                        Spacer(Modifier.height(12.dp))

                        InfoRow(
                            icon = Icons.Default.LocationOn,
                            label = "Adresse",
                            value = citizen.address
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { /* TODO */ }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.History, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Historique")
                            }
                            Button(
                                onClick = { /* TODO */ },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = NoorPink)
                            ) {
                                Icon(Icons.Default.ContactPage, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Contacter")
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
private fun PreviewCitizensLight() {
    SansaTheme(darkTheme = false) {
        CitizensScreen()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Mode Sombre")
@Composable
private fun PreviewCitizensDark() {
    SansaTheme(darkTheme = true) {
        CitizensScreen()
    }
}