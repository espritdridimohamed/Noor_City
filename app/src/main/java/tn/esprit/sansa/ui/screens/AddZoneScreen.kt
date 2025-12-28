// AddZoneScreen.kt – Ajout de zone avec design Noor
package tn.esprit.sansa.ui.screens

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.sansa.ui.theme.SansaTheme

// Palette Noor
private val NoorBlue = Color(0xFF1E40AF)
private val NoorGreen = Color(0xFF10B981)
private val NoorAmber = Color(0xFFF59E0B)
private val NoorRed = Color(0xFFEF4444)
private val NoorPurple = Color(0xFF8B5CF6)
private val NoorIndigo = Color(0xFF6366F1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddZoneScreen(
    onAddSuccess: () -> Unit = {},
    onBackPressed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var zoneId by remember { mutableStateOf("") }
    var zoneName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var zoneType by remember { mutableStateOf(ZoneType.RESIDENTIAL) }
    var status by remember { mutableStateOf(ZoneStatus.PLANNING) }
    var area by remember { mutableStateOf("") }
    var population by remember { mutableStateOf("") }
    var coordinator by remember { mutableStateOf("") }
    var streetlightIds by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Suggestions de noms de zones
    val zoneNameSuggestions = listOf(
        "Centre-ville",
        "Zone Résidentielle Nord",
        "Zone Résidentielle Sud",
        "Quartier Commercial",
        "Parc Municipal",
        "Zone Industrielle",
        "Médina Historique",
        "Nouveau Quartier"
    )

    // Suggestions de coordinateurs
    val coordinatorSuggestions = listOf(
        "Ahmed Ben Ali",
        "Fatma Gharbi",
        "Mohamed Trabelsi",
        "Leila Mansour",
        "Karim Saidi",
        "Sami Ben Salem"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AddZoneTopBar(
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
            // ID de la zone
            item {
                Text("Identifiant de la zone", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = zoneId,
                    onValueChange = { zoneId = it.uppercase() },
                    label = { Text("ID de la zone (ex: Z001)") },
                    leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorIndigo,
                        cursorColor = NoorIndigo
                    ),
                    supportingText = {
                        Text("Format: Z + 3 chiffres", color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                    }
                )
            }

            // Type de zone
            item {
                Text("Type de zone", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ZoneType.entries) { type ->
                        FilterChip(
                            onClick = { zoneType = type },
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
                            selected = zoneType == type,
                            leadingIcon = if (zoneType == type) {
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

            // Nom de la zone
            item {
                Text("Nom de la zone", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = zoneName,
                    onValueChange = { zoneName = it },
                    label = { Text("Nom descriptif de la zone") },
                    leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorIndigo,
                        cursorColor = NoorIndigo
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text("Suggestions rapides :", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(zoneNameSuggestions) { suggestion ->
                        SuggestionChip(
                            onClick = { zoneName = suggestion },
                            label = { Text(suggestion, fontSize = 12.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = NoorIndigo.copy(alpha = 0.1f),
                                labelColor = NoorIndigo
                            )
                        )
                    }
                }
            }

            // Description
            item {
                Text("Description de la zone", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description détaillée") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    minLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorIndigo,
                        cursorColor = NoorIndigo
                    ),
                    placeholder = { Text("Ex: Zone principale incluant...", fontSize = 14.sp) }
                )
            }

            // Statut
            item {
                Text("Statut de la zone", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ZoneStatus.entries) { currentStatus ->
                        FilterChip(
                            onClick = { status = currentStatus },
                            label = {
                                Text(currentStatus.displayName, fontSize = 13.sp)
                            },
                            selected = status == currentStatus,
                            leadingIcon = if (status == currentStatus) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = currentStatus.color,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = currentStatus.color
                            )
                        )
                    }
                }
            }

            // Superficie
            item {
                Text("Superficie de la zone", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = area,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            area = it
                        }
                    },
                    label = { Text("Superficie en km²") },
                    leadingIcon = { Icon(Icons.Default.SquareFoot, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorIndigo,
                        cursorColor = NoorIndigo
                    ),
                    supportingText = {
                        val areaValue = area.toDoubleOrNull() ?: 0.0
                        if (areaValue > 0) {
                            Text(
                                "${String.format("%.1f", areaValue * 1000000)} m²",
                                color = NoorIndigo
                            )
                        }
                    }
                )
            }

            // Population
            item {
                Text("Population (optionnel)", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = population,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            population = it
                        }
                    },
                    label = { Text("Nombre d'habitants") },
                    leadingIcon = { Icon(Icons.Default.Groups, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorIndigo,
                        cursorColor = NoorIndigo
                    ),
                    placeholder = { Text("Laisser vide pour zones non-résidentielles") },
                    supportingText = {
                        val popValue = population.toIntOrNull() ?: 0
                        if (popValue > 0) {
                            Text(
                                "${String.format("%,d", popValue)} habitants",
                                color = NoorIndigo
                            )
                        }
                    }
                )
            }

            // Coordinateur
            item {
                Text("Coordinateur de zone", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = coordinator,
                    onValueChange = { coordinator = it },
                    label = { Text("Nom du coordinateur") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorIndigo,
                        cursorColor = NoorIndigo
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text("Suggestions :", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(coordinatorSuggestions) { suggestion ->
                        SuggestionChip(
                            onClick = { coordinator = suggestion },
                            label = { Text(suggestion, fontSize = 12.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = NoorPurple.copy(alpha = 0.1f),
                                labelColor = NoorPurple
                            )
                        )
                    }
                }
            }

            // IDs des lampadaires
            item {
                Text("Lampadaires associés", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = streetlightIds,
                    onValueChange = { streetlightIds = it },
                    label = { Text("IDs des lampadaires (séparés par virgule)") },
                    leadingIcon = { Icon(Icons.Default.Lightbulb, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorIndigo,
                        cursorColor = NoorIndigo
                    ),
                    placeholder = { Text("Ex: L001, L002, L003") },
                    supportingText = {
                        val count = if (streetlightIds.isBlank()) 0
                        else streetlightIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }.size
                        Text("$count lampadaire(s)", color = if (count > 0) NoorGreen else Color.Gray)
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
                        focusedBorderColor = NoorIndigo,
                        cursorColor = NoorIndigo
                    ),
                    placeholder = { Text("Ex: Horaires spéciaux, contraintes particulières...") }
                )
            }

            // Résumé et bouton de soumission
            item {
                Spacer(Modifier.height(8.dp))

                // Carte de résumé
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = zoneType.color.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(zoneType.icon, contentDescription = null, tint = zoneType.color, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Résumé", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = zoneType.color)
                        }
                        Spacer(Modifier.height(12.dp))
                        if (zoneId.isNotEmpty()) {
                            Text("• ID: $zoneId", fontSize = 13.sp)
                        }
                        if (zoneName.isNotEmpty()) {
                            Text("• Nom: $zoneName", fontSize = 13.sp)
                        }
                        Text("• Type: ${zoneType.displayName}", fontSize = 13.sp)
                        Text("• Statut: ${status.displayName}", fontSize = 13.sp)
                        if (area.isNotEmpty()) {
                            Text("• Superficie: ${area} km²", fontSize = 13.sp)
                        }
                        if (population.isNotEmpty()) {
                            Text("• Population: ${String.format("%,d", population.toIntOrNull() ?: 0)} hab.", fontSize = 13.sp)
                        }
                        if (coordinator.isNotEmpty()) {
                            Text("• Coordinateur: $coordinator", fontSize = 13.sp)
                        }
                        val lightCount = if (streetlightIds.isBlank()) 0
                        else streetlightIds.split(",").filter { it.trim().isNotEmpty() }.size
                        if (lightCount > 0) {
                            Text("• Lampadaires: $lightCount", fontSize = 13.sp)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        // TODO: Validation et enregistrement
                        onAddSuccess()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    enabled = zoneId.isNotBlank() &&
                            zoneName.isNotBlank() &&
                            description.isNotBlank() &&
                            area.isNotBlank() &&
                            coordinator.isNotBlank(),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = zoneType.color)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("Créer la zone", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun AddZoneTopBar(
    onBackPressed: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(NoorIndigo.copy(alpha = 0.9f), NoorIndigo.copy(alpha = 0.6f))
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
                Text("Nouvelle zone", color = Color.White.copy(0.9f), fontSize = 16.sp)
                Text("Configuration urbaine", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddZoneScreenPreview() {
    SansaTheme {
        AddZoneScreen()
    }
}