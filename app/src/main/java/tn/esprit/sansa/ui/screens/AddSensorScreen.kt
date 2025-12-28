// AddSensorScreen.kt – VERSION AMÉLIORÉE AVEC LISTE VERTICALE POUR LES TYPES
package tn.esprit.sansa.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.sansa.ui.theme.SansaTheme

private val NoorBlue = Color(0xFF1E40AF)
private val NoorGreen = Color(0xFF10B981)
private val NoorAmber = Color(0xFFF59E0B)
private val NoorRed = Color(0xFFEF4444)

// Liste fictive de lampadaires
private val mockStreetlights = listOf(
    "L001 - Lampadaire #001 (Rue Principale)",
    "L002 - Lampadaire #002 (Place Centrale)",
    "L003 - Lampadaire #003 (Parc Municipal)",
    "L004 - Lampadaire #004 (Avenue des Lumières)",
    "L005 - Lampadaire #005 (Zone Industrielle)"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSensorScreen(onBack: () -> Unit) {
    var selectedType by remember { mutableStateOf<SensorType?>(null) }
    var selectedLampadaire by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf(SensorStatus.ACTIVE) }
    var currentValue by remember { mutableStateOf("") }
    var batteryLevel by remember { mutableStateOf(80) }
    var notes by remember { mutableStateOf("") }

    var expandedLampadaire by remember { mutableStateOf(false) }

    val isFormValid = selectedType != null &&
            selectedLampadaire.isNotBlank() &&
            currentValue.isNotBlank()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(NoorBlue.copy(alpha = 0.9f), NoorBlue.copy(alpha = 0.6f))
                        )
                    )
                //    .blur(20.dp)
                    .padding(horizontal = 20.dp, vertical = 48.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Ajouter un capteur", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                        Text("Configurez un nouveau capteur intelligent", color = Color.White.copy(0.9f), fontSize = 16.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // Section : Type de capteur (liste verticale)
            Text("Type de capteur", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    SensorType.entries.forEach { type ->
                        val isSelected = selectedType == type
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedType = type }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(type.color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    type.icon,
                                    contentDescription = null,
                                    tint = type.color,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    type.displayName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Unité : ${type.unit}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Sélectionné",
                                    tint = NoorBlue,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        if (type != SensorType.entries.last()) {
                            HorizontalDivider()
                        }
                    }
                }
            }

            // Lampadaire associé (Dropdown)
            ExposedDropdownMenuBox(
                expanded = expandedLampadaire,
                onExpandedChange = { expandedLampadaire = !expandedLampadaire }
            ) {
                OutlinedTextField(
                    value = selectedLampadaire,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Lampadaire associé") },
                    placeholder = { Text("Choisir un lampadaire") },
                    leadingIcon = { Icon(Icons.Default.LightMode, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLampadaire) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorBlue,
                        cursorColor = NoorBlue
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedLampadaire,
                    onDismissRequest = { expandedLampadaire = false }
                ) {
                    mockStreetlights.forEach { lamp ->
                        DropdownMenuItem(
                            text = { Text(lamp) },
                            onClick = {
                                selectedLampadaire = lamp
                                expandedLampadaire = false
                            }
                        )
                    }
                }
            }

            // État initial
            Text("État initial", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(SensorStatus.ACTIVE, SensorStatus.WARNING, SensorStatus.OFFLINE).forEach { status ->
                    val color = when (status) {
                        SensorStatus.ACTIVE -> NoorGreen
                        SensorStatus.WARNING -> NoorAmber
                        else -> Color.Gray
                    }
                    FilterChip(
                        onClick = { selectedStatus = status },
                        label = { Text(when (status) {
                            SensorStatus.ACTIVE -> "Actif"
                            SensorStatus.WARNING -> "Maintenance"
                            else -> "Hors ligne"
                        }) },
                        selected = selectedStatus == status,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = color
                        )
                    )
                }
            }

            // Valeur actuelle
            OutlinedTextField(
                value = currentValue,
                onValueChange = { currentValue = it },
                label = { Text("Valeur actuelle") },
                placeholder = { Text(selectedType?.let { "Ex: 750 ${it.unit}" } ?: "Ex: 750 lux") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NoorBlue,
                    cursorColor = NoorBlue
                )
            )

            // Niveau de batterie
            Text("Niveau de batterie initial", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Column {
                Slider(
                    value = batteryLevel.toFloat(),
                    onValueChange = { batteryLevel = it.toInt() },
                    valueRange = 0f..100f,
                    steps = 99,
                    colors = SliderDefaults.colors(
                        thumbColor = NoorBlue,
                        activeTrackColor = NoorBlue
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0%", color = MaterialTheme.colorScheme.onBackground.copy(0.6f))
                    Text("$batteryLevel%", fontWeight = FontWeight.Bold, color = NoorBlue)
                    Text("100%", color = MaterialTheme.colorScheme.onBackground.copy(0.6f))
                }
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (facultatif)") },
                placeholder = { Text("Ex: Capteur installé à 3,5m, orientation sud...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(24.dp),
                maxLines = 6
            )

            Spacer(Modifier.weight(1f))

            // Boutons
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                    Text("Annuler")
                }
                Button(
                    onClick = {
                        // TODO : Ajouter le capteur à la liste ou base de données
                        onBack()
                    },
                    enabled = isFormValid,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NoorBlue)
                ) {
                    Text("Ajouter le capteur", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// PREVIEWS
@Preview(showBackground = true, name = "Mode Clair")
@Composable
fun AddSensorScreenPreview() {
    SansaTheme(darkTheme = false) {
        AddSensorScreen(onBack = { })
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Mode Sombre")
@Composable
fun AddSensorScreenDarkPreview() {
    SansaTheme(darkTheme = true) {
        AddSensorScreen(onBack = { })
    }
}