// AddStreetlightScreen.kt – Ajout de lampadaire avec design Noor
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStreetlightScreen(
    onAddSuccess: () -> Unit = {},
    onBackPressed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var streetlightId by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var bulbType by remember { mutableStateOf(BulbType.LED) }
    var status by remember { mutableStateOf(StreetlightStatus.OFF) }
    var powerConsumption by remember { mutableStateOf("") }
    var installationDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Suggestions d'adresses communes à Tunis
    val addressSuggestions = listOf(
        "Avenue Habib Bourguiba",
        "Avenue de la Liberté",
        "Avenue Mohammed V",
        "Boulevard du 7 Novembre",
        "Rue de Carthage",
        "Avenue de France"
    )

    // Suggestions de zones
    val zoneSuggestions = listOf(
        "Zone A - Centre Ville",
        "Zone B - Quartier Nord",
        "Zone C - Quartier Sud",
        "Zone D - Quartier Est",
        "Zone E - Quartier Ouest"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AddStreetlightTopBar(
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
            // ID du lampadaire
            item {
                Text("Identifiant", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = streetlightId,
                    onValueChange = { streetlightId = it.uppercase() },
                    label = { Text("ID du lampadaire (ex: L001)") },
                    leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorBlue,
                        cursorColor = NoorBlue
                    ),
                    supportingText = {
                        Text("Format recommandé: L + 3 chiffres", color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                    }
                )
            }

            // Type d'ampoule
            item {
                Text("Type d'ampoule", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(BulbType.entries) { type ->
                        FilterChip(
                            onClick = { bulbType = type },
                            label = {
                                Text(type.displayName, fontSize = 13.sp)
                            },
                            selected = bulbType == type,
                            leadingIcon = if (bulbType == type) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NoorBlue,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = NoorBlue
                            )
                        )
                    }
                }
            }

            // Statut initial
            item {
                Text("Statut initial", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(StreetlightStatus.entries) { currentStatus ->
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

            // Adresse
            item {
                Text("Adresse", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Adresse complète") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
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
                    items(addressSuggestions) { suggestion ->
                        SuggestionChip(
                            onClick = { address = suggestion },
                            label = { Text(suggestion, fontSize = 12.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = NoorBlue.copy(alpha = 0.1f),
                                labelColor = NoorBlue
                            )
                        )
                    }
                }
            }

            // Zone/Localisation
            item {
                Text("Zone / Secteur", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Zone géographique") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorBlue,
                        cursorColor = NoorBlue
                    )
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(zoneSuggestions) { suggestion ->
                        SuggestionChip(
                            onClick = { location = suggestion },
                            label = { Text(suggestion, fontSize = 12.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = NoorGreen.copy(alpha = 0.1f),
                                labelColor = NoorGreen
                            )
                        )
                    }
                }
            }

            // Coordonnées GPS
            item {
                Text("Coordonnées GPS", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = latitude,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^-?\\d*\\.?\\d*$"))) {
                                latitude = it
                            }
                        },
                        label = { Text("Latitude") },
                        leadingIcon = { Icon(Icons.Default.MyLocation, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NoorBlue,
                            cursorColor = NoorBlue
                        ),
                        placeholder = { Text("36.8065", fontSize = 12.sp) }
                    )
                    OutlinedTextField(
                        value = longitude,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^-?\\d*\\.?\\d*$"))) {
                                longitude = it
                            }
                        },
                        label = { Text("Longitude") },
                        leadingIcon = { Icon(Icons.Default.MyLocation, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NoorBlue,
                            cursorColor = NoorBlue
                        ),
                        placeholder = { Text("10.1815", fontSize = 12.sp) }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Tunis centre: ~36.806, 10.181",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                    )
                    TextButton(onClick = { /* TODO: Obtenir position GPS */ }) {
                        Icon(Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Ma position", fontSize = 12.sp)
                    }
                }
            }

            // Consommation électrique
            item {
                Text("Consommation électrique", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = powerConsumption,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            powerConsumption = it
                        }
                    },
                    label = { Text("Puissance (Watts)") },
                    leadingIcon = { Icon(Icons.Default.Power, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorBlue,
                        cursorColor = NoorBlue
                    ),
                    supportingText = {
                        val consumption = powerConsumption.toDoubleOrNull() ?: 0.0
                        val recommendation = when {
                            consumption == 0.0 -> ""
                            consumption < 50 -> "Très efficace (LED)"
                            consumption < 100 -> "Efficace"
                            consumption < 150 -> "Moyen"
                            else -> "Élevé - Envisager LED"
                        }
                        if (recommendation.isNotEmpty()) {
                            Text(
                                recommendation,
                                color = when {
                                    consumption < 50 -> NoorGreen
                                    consumption < 100 -> NoorBlue
                                    consumption < 150 -> NoorAmber
                                    else -> NoorRed
                                }
                            )
                        }
                    }
                )
            }

            // Date d'installation
            item {
                Text("Date d'installation (optionnel)", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = installationDate,
                    onValueChange = { installationDate = it },
                    label = { Text("JJ/MM/AAAA") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("28/12/2024") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorBlue,
                        cursorColor = NoorBlue
                    )
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
                    ),
                    placeholder = { Text("Ex: Proximité d'une école, horaires spéciaux...") }
                )
            }

            // Bouton de soumission
            item {
                Spacer(Modifier.height(8.dp))

                // Résumé avant validation
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NoorBlue.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Résumé", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NoorBlue)
                        Spacer(Modifier.height(8.dp))
                        if (streetlightId.isNotEmpty()) {
                            Text("• ID: $streetlightId", fontSize = 12.sp)
                        }
                        if (address.isNotEmpty()) {
                            Text("• Adresse: $address", fontSize = 12.sp)
                        }
                        Text("• Type: ${bulbType.displayName}", fontSize = 12.sp)
                        Text("• Statut: ${status.displayName}", fontSize = 12.sp)
                        if (powerConsumption.isNotEmpty()) {
                            Text("• Consommation: ${powerConsumption}W", fontSize = 12.sp)
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
                    enabled = streetlightId.isNotBlank() &&
                            address.isNotBlank() &&
                            location.isNotBlank() &&
                            latitude.isNotBlank() &&
                            longitude.isNotBlank() &&
                            powerConsumption.isNotBlank(),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NoorGreen)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("Ajouter le lampadaire", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun AddStreetlightTopBar(
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
                Text("Nouveau lampadaire", color = Color.White.copy(0.9f), fontSize = 16.sp)
                Text("Ajout au réseau", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddStreetlightScreenPreview() {
    SansaTheme {
        AddStreetlightScreen()
    }
}