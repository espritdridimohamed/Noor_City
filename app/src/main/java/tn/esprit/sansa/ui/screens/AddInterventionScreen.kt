// AddInterventionScreen.kt – Ajout d'intervention avec design Noor
package tn.esprit.sansa.ui.screens

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.sansa.ui.theme.SansaTheme
import tn.esprit.sansa.models.*
import java.text.SimpleDateFormat
import java.util.*

// Palette Noor
private val NoorBlue = Color(0xFF1E40AF)
private val NoorGreen = Color(0xFF10B981)
private val NoorAmber = Color(0xFFF59E0B)
private val NoorRed = Color(0xFFEF4444)
private val NoorPurple = Color(0xFF8B5CF6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInterventionScreen(
    onAddSuccess: () -> Unit = {},
    onBackPressed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var location by remember { mutableStateOf("") }
    var streetlightId by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var technicianName by remember { mutableStateOf("") }
    var assignedBy by remember { mutableStateOf("") }
    var interventionType by remember { mutableStateOf(InterventionType.MAINTENANCE) }
    var priority by remember { mutableStateOf(InterventionPriority.MEDIUM) }
    var estimatedDuration by remember { mutableStateOf("60") }
    var selectedDate by remember { mutableStateOf(Date()) }
    var notes by remember { mutableStateOf("") }

    // Suggestions intelligentes pour la description selon le type
    val descriptionSuggestions = remember(interventionType) {
        when (interventionType) {
            InterventionType.REPAIR -> listOf(
                "Remplacement du ballast défectueux",
                "Réparation du câblage endommagé",
                "Changement de l'ampoule LED",
                "Réparation du support de fixation"
            )
            InterventionType.MAINTENANCE -> listOf(
                "Nettoyage du luminaire",
                "Vérification des connexions électriques",
                "Contrôle de l'état général",
                "Lubrification des éléments mobiles"
            )
            InterventionType.INSPECTION -> listOf(
                "Inspection visuelle de routine",
                "Vérification de la conformité",
                "Contrôle de sécurité",
                "Audit technique complet"
            )
            InterventionType.INSTALLATION -> listOf(
                "Installation d'un nouveau lampadaire",
                "Mise en place du système de contrôle",
                "Installation de capteurs intelligents",
                "Raccordement au réseau électrique"
            )
            InterventionType.REPLACEMENT -> listOf(
                "Remplacement complet du lampadaire",
                "Changement du poteau",
                "Remplacement du système d'éclairage",
                "Mise à niveau vers LED"
            )
            InterventionType.EMERGENCY -> listOf(
                "Intervention d'urgence - Danger immédiat",
                "Réparation urgente suite à incident",
                "Sécurisation d'un équipement dangereux",
                "Intervention suite à signalement critique"
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AddInterventionTopBar(
                onBackPressed = onBackPressed
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type d'intervention
            item {
                Text("Type d'intervention", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(InterventionType.entries) { type ->
                        FilterChip(
                            onClick = { interventionType = type },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        type.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(type.displayName, fontSize = 13.sp)
                                }
                            },
                            selected = interventionType == type,
                            leadingIcon = if (interventionType == type) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
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

            // Localisation
            item {
                Text("Localisation", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Adresse ou emplacement") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorBlue,
                        cursorColor = NoorBlue
                    )
                )
            }

            // ID du lampadaire
            item {
                Text("ID du lampadaire", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = streetlightId,
                    onValueChange = { streetlightId = it },
                    label = { Text("Identifiant du lampadaire") },
                    leadingIcon = { Icon(Icons.Default.Lightbulb, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorBlue,
                        cursorColor = NoorBlue
                    )
                )
            }

            // Description
            item {
                Text("Description de l'intervention", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Détails de l'intervention") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    minLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorBlue,
                        cursorColor = NoorBlue
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text("Suggestions rapides :", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(descriptionSuggestions) { suggestion ->
                        SuggestionChip(
                            onClick = { description = suggestion },
                            label = { Text(suggestion, fontSize = 12.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = interventionType.color.copy(alpha = 0.1f),
                                labelColor = interventionType.color
                            )
                        )
                    }
                }
            }

            // Nom du technicien
            item {
                Text("Technicien assigné", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = technicianName,
                    onValueChange = { technicianName = it },
                    label = { Text("Nom du technicien") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorBlue,
                        cursorColor = NoorBlue
                    )
                )
            }

            // Responsable de l'assignation
            item {
                Text("Assigné par", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = assignedBy,
                    onValueChange = { assignedBy = it },
                    label = { Text("Nom du responsable") },
                    leadingIcon = { Icon(Icons.Default.SupervisorAccount, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorBlue,
                        cursorColor = NoorBlue
                    )
                )
            }

            // Priorité
            item {
                Text("Priorité", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = priority.displayName,
                        onValueChange = {},
                        label = { Text("Priorité") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NoorBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        InterventionPriority.entries.forEach { prio ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(prio.color)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(prio.displayName)
                                    }
                                },
                                onClick = {
                                    priority = prio
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Durée estimée
            item {
                Text("Durée estimée (minutes)", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = estimatedDuration,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            estimatedDuration = it
                        }
                    },
                    label = { Text("Durée en minutes") },
                    leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorBlue,
                        cursorColor = NoorBlue
                    ),
                    supportingText = {
                        if (estimatedDuration.isNotEmpty()) {
                            val minutes = estimatedDuration.toIntOrNull() ?: 0
                            val hours = minutes / 60
                            val remainingMinutes = minutes % 60
                            Text(
                                if (hours > 0) "$hours h $remainingMinutes min" else "$minutes min",
                                color = NoorBlue
                            )
                        }
                    }
                )
            }

            // Notes additionnelles
            item {
                Text("Notes additionnelles (optionnel)", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Informations complémentaires") },
                    leadingIcon = { Icon(Icons.Default.Note, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorBlue,
                        cursorColor = NoorBlue
                    )
                )
            }

            // Bouton de soumission
            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        // TODO: Validation et enregistrement
                        onAddSuccess()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    enabled = location.isNotBlank() &&
                            streetlightId.isNotBlank() &&
                            description.isNotBlank() &&
                            technicianName.isNotBlank() &&
                            assignedBy.isNotBlank() &&
                            estimatedDuration.isNotBlank(),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = interventionType.color)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("Planifier l'intervention", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun AddInterventionTopBar(
    onBackPressed: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(NoorBlue.copy(alpha = 0.9f), NoorBlue.copy(alpha = 0.6f))
                )
            )
            .padding(horizontal = 24.dp, vertical = 56.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBackPressed,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Retour",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("Nouvelle intervention", color = Color.White.copy(0.9f), fontSize = 16.sp)
                Text("Planification technique", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddInterventionScreenPreview() {
    SansaTheme {
        AddInterventionScreen()
    }
}