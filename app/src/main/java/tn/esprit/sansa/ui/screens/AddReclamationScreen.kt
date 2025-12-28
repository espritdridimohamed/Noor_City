// AddReclamationScreen.kt – Ajout de réclamation avec bouton retour
package tn.esprit.sansa.ui.screens

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

// Palette Noor
private val NoorBlue = Color(0xFF1E40AF)
private val NoorGreen = Color(0xFF10B981)
private val NoorAmber = Color(0xFFF59E0B)
private val NoorRed = Color(0xFFEF4444)
private val NoorPurple = Color(0xFF8B5CF6)

// SUPPRESSION DE LA REDÉCLARATION - ReclamationPriority est déjà défini dans ReclamationsScreen.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReclamationScreen(
    onAddSuccess: () -> Unit = {},
    onBackPressed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var location by remember { mutableStateOf("") }
    var streetlightId by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var reportedBy by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(ReclamationPriority.MEDIUM) }

    // Suggestions intelligentes pour la description
    val descriptionSuggestions = listOf(
        "Lampadaire éteint depuis plusieurs jours",
        "Lumière clignotante ou instable",
        "Lampadaire penché ou endommagé",
        "Câblage apparent ou dangereux",
        "Lumière trop faible ou trop forte",
        "Lampadaire allumé en plein jour"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            AddReclamationTopBar(
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

            item {
                Text("ID du lampadaire (optionnel)", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = streetlightId,
                    onValueChange = { streetlightId = it },
                    label = { Text("ID du lampadaire") },
                    leadingIcon = { Icon(Icons.Default.Lightbulb, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorBlue,
                        cursorColor = NoorBlue
                    )
                )
            }

            item {
                Text("Description du problème", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Détails du problème") },
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
                Text("Suggestions rapides :", fontSize = 14.sp, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(descriptionSuggestions) { suggestion ->
                        SuggestionChip(
                            onClick = { description = suggestion },
                            label = { Text(suggestion, fontSize = 12.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = NoorBlue.copy(alpha = 0.1f),
                                labelColor = NoorBlue
                            )
                        )
                    }
                }
            }

            item {
                Text("Votre nom", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = reportedBy,
                    onValueChange = { reportedBy = it },
                    label = { Text("Nom du signalant") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorBlue,
                        cursorColor = NoorBlue
                    )
                )
            }

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
                        ReclamationPriority.entries.forEach { prio ->
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
                    enabled = location.isNotBlank() && description.isNotBlank() && reportedBy.isNotBlank(),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NoorBlue)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("Envoyer la réclamation", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun AddReclamationTopBar(
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
                Text("Signaler un problème", color = Color.White.copy(0.9f), fontSize = 16.sp)
                Text("Aidez-nous à améliorer l'éclairage", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddReclamationPreview() {
    SansaTheme {
        AddReclamationScreen()
    }
}