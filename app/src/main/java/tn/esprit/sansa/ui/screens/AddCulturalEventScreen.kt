// AddCulturalEventScreen.kt – Ajout d'événement culturel avec design Noor simplifié
package tn.esprit.sansa.ui.screens

import tn.esprit.sansa.ui.viewmodels.CulturalEventsViewModel
import tn.esprit.sansa.ui.screens.models.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
import java.text.SimpleDateFormat
import java.util.*

import tn.esprit.sansa.ui.theme.*
// Palette Noor centralisée

private val NoorPink = Color(0xFFEC4899)
private val NoorIndigo = Color(0xFF6366F1)// NoorBlue imported from theme/Color.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCulturalEventScreen(
    viewModel: CulturalEventsViewModel,
    zonesViewModel: tn.esprit.sansa.ui.viewmodels.ZonesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onAddSuccess: () -> Unit = {},
    onBackPressed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var eventName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var ambianceType by remember { mutableStateOf(AmbianceType.FESTIVE) }
    var duration by remember { mutableStateOf("") }
    var attendees by remember { mutableStateOf("") }
    var organizer by remember { mutableStateOf("") }
    var selectedZones by remember { mutableStateOf(setOf<String>()) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var selectedTime by remember { mutableStateOf("18:00") }

    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    // Dynamic Zones from ViewModel
    val zones by zonesViewModel.zones.collectAsStateWithLifecycle()
    val availableZones = remember(zones) { zones.map { it.name } }

    // Suggestions de noms d'événements
    val eventNameSuggestions = listOf(
        "Festival de Musique",
        "Nuit Culturelle",
        "Exposition d'Art",
        "Concert en Plein Air",
        "Spectacle de Danse",
        "Soirée Théâtre"
    )

    // Suggestions d'organisateurs
    val organizerSuggestions = listOf(
        "Ministère de la Culture",
        "Municipalité",
        "Association Culturelle",
        "Centre des Arts",
        "Office du Tourisme"
    )

    // Heures communes
    val commonTimes = listOf("14:00", "16:00", "18:00", "19:00", "20:00", "21:00")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AddEventTopBar(onBackPressed = onBackPressed)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Nom de l'événement
            item {
                SectionTitle("Nom de l'événement")
                OutlinedTextField(
                    value = eventName,
                    onValueChange = { eventName = it },
                    label = { Text("Titre de l'événement") },
                    leadingIcon = { Icon(Icons.Default.Event, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorBlue,
                        cursorColor = NoorBlue
                    )
                )

                if (eventName.isEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Suggestions :", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(eventNameSuggestions) { suggestion ->
                            SuggestionChip(
                                onClick = { eventName = suggestion },
                                label = { Text(suggestion, fontSize = 12.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = NoorBlue.copy(alpha = 0.1f),
                                    labelColor = NoorBlue
                                )
                            )
                        }
                    }
                }
            }

            // Type d'ambiance
            item {
                SectionTitle("Ambiance")
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AmbianceType.entries) { type ->
                        FilterChip(
                            onClick = { ambianceType = type },
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
                            selected = ambianceType == type,
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

            // Date et heure
            item {
                SectionTitle("Date et heure")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Sélecteur de date
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (selectedDate != null) NoorBlue else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (selectedDate != null)
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedDate!!))
                            else "Date",
                            fontSize = 14.sp
                        )
                    }

                    // Sélecteur d'heure
                    OutlinedButton(
                        onClick = { /* TODO: Time picker */ },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = NoorBlue
                        )
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(selectedTime, fontSize = 14.sp)
                    }
                }

                // Heures rapides
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(commonTimes) { time ->
                        SuggestionChip(
                            onClick = { selectedTime = time },
                            label = { Text(time, fontSize = 12.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (selectedTime == time) NoorBlue.copy(0.2f) else MaterialTheme.colorScheme.surface,
                                labelColor = NoorBlue
                            )
                        )
                    }
                }
            }

            // Durée
            item {
                SectionTitle("Durée")
                OutlinedTextField(
                    value = duration,
                    onValueChange = {
                        if (it.isEmpty() || (it.all { char -> char.isDigit() } && it.toIntOrNull()?.let { num -> num <= 24 } == true)) {
                            duration = it
                        }
                    },
                    label = { Text("Durée en heures") },
                    leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorBlue,
                        cursorColor = NoorBlue
                    ),
                    supportingText = {
                        val hours = duration.toIntOrNull() ?: 0
                        if (hours > 0) {
                            Text("$hours heure${if (hours > 1) "s" else ""}", color = NoorGreen)
                        }
                    }
                )
            }

            // Zones
            item {
                SectionTitle("Zones concernées")
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableZones) { zone ->
                        FilterChip(
                            onClick = {
                                selectedZones = if (selectedZones.contains(zone)) {
                                    selectedZones - zone
                                } else {
                                    selectedZones + zone
                                }
                            },
                            label = { Text(zone, fontSize = 13.sp) },
                            selected = selectedZones.contains(zone),
                            leadingIcon = if (selectedZones.contains(zone)) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NoorBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                if (selectedZones.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${selectedZones.size} zone${if (selectedZones.size > 1) "s" else ""} sélectionnée${if (selectedZones.size > 1) "s" else ""}",
                        fontSize = 13.sp,
                        color = NoorGreen
                    )
                }
            }

            // Description
            item {
                SectionTitle("Description")
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description de l'événement") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    minLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorBlue,
                        cursorColor = NoorBlue
                    ),
                    placeholder = { Text("Décrivez votre événement...", fontSize = 14.sp) }
                )
            }

            // Participants estimés
            item {
                SectionTitle("Participants estimés")
                OutlinedTextField(
                    value = attendees,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            attendees = it
                        }
                    },
                    label = { Text("Nombre de participants") },
                    leadingIcon = { Icon(Icons.Default.Groups, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorBlue,
                        cursorColor = NoorBlue
                    ),
                    supportingText = {
                        val count = attendees.toIntOrNull() ?: 0
                        if (count > 0) {
                            Text("${String.format("%,d", count)} personnes", color = NoorGreen)
                        }
                    }
                )
            }

            // Organisateur
            item {
                SectionTitle("Organisateur")
                OutlinedTextField(
                    value = organizer,
                    onValueChange = { organizer = it },
                    label = { Text("Nom de l'organisateur") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorBlue,
                        cursorColor = NoorBlue
                    )
                )

                if (organizer.isEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(organizerSuggestions) { suggestion ->
                            SuggestionChip(
                                onClick = { organizer = suggestion },
                                label = { Text(suggestion, fontSize = 12.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = NoorPurple.copy(alpha = 0.1f),
                                    labelColor = NoorPurple
                                )
                            )
                        }
                    }
                }
            }

            // Résumé et bouton
            item {
                Spacer(Modifier.height(8.dp))

                // Carte de résumé
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = ambianceType.color.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                ambianceType.icon,
                                contentDescription = null,
                                tint = ambianceType.color,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Récapitulatif",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ambianceType.color
                            )
                        }

                        if (eventName.isNotEmpty() || selectedDate != null || selectedZones.isNotEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = ambianceType.color.copy(0.2f))
                            Spacer(Modifier.height(12.dp))
                        }

                        if (eventName.isNotEmpty()) {
                            SummaryItem("📌", "Événement", eventName)
                        }
                        if (selectedDate != null) {
                            val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedDate!!))
                            SummaryItem("📅", "Date", "$dateStr à $selectedTime")
                        }
                        if (duration.isNotEmpty()) {
                            SummaryItem("⏱️", "Durée", "$duration heure${if (duration.toIntOrNull() ?: 0 > 1) "s" else ""}")
                        }
                        if (selectedZones.isNotEmpty()) {
                            SummaryItem("📍", "Zones", "${selectedZones.size} zone${if (selectedZones.size > 1) "s" else ""}")
                        }
                        if (attendees.isNotEmpty()) {
                            SummaryItem("👥", "Participants", String.format("%,d", attendees.toIntOrNull() ?: 0))
                        }
                        if (organizer.isNotEmpty()) {
                            SummaryItem("🏢", "Organisateur", organizer)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Bouton de création
                Button(
                    onClick = {
                        val newEvent = CulturalEvent(
                            id = "EVT_${System.currentTimeMillis()}",
                            name = eventName,
                            dateTime = Date(selectedDate ?: System.currentTimeMillis()),
                            zones = selectedZones.toList(),
                            ambianceType = ambianceType,
                            status = EventStatus.PENDING,
                            duration = duration.toIntOrNull() ?: 2,
                            description = description,
                            attendees = attendees.toIntOrNull() ?: 0,
                            organizer = organizer
                        )
                        viewModel.addEvent(newEvent)
                        onAddSuccess()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    enabled = eventName.isNotBlank() &&
                            selectedDate != null &&
                            duration.isNotBlank() &&
                            selectedZones.isNotEmpty() &&
                            description.isNotBlank() &&
                            organizer.isNotBlank(),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ambianceType.color,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Créer l'événement", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }

        // DatePicker Dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        selectedDate = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Annuler")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun SummaryItem(emoji: String, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 16.sp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun AddEventTopBar(onBackPressed: () -> Unit) {
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
                Text("Nouvel événement", color = Color.White.copy(0.9f), fontSize = 16.sp)
                Text("Configuration culturelle", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
            }

            Icon(
                Icons.Default.Celebration,
                contentDescription = null,
                tint = Color.White.copy(0.8f),
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

// Preview removed to avoid ViewModel instantiation issues
// @Preview(showBackground = true)
// @Composable
// fun AddCulturalEventScreenPreview() {
//     SansaTheme {
//         // AddCulturalEventScreen()
//     }
// }