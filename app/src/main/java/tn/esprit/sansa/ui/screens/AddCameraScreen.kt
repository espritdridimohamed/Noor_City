// AddCameraScreen.kt — Écran d'ajout de caméra avec design Noor premium
package tn.esprit.sansa.ui.screens
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.sansa.ui.theme.SansaTheme
import tn.esprit.sansa.models.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCameraScreen(
    onAddSuccess: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var location by remember { mutableStateOf("") }
    var associatedStreetlight by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(CameraType.DOME) }
    var status by remember { mutableStateOf(CameraStatus.ONLINE) }
    var resolution by remember { mutableStateOf("1080p") }
    var zone by remember { mutableStateOf("") }
    var nightVision by remember { mutableStateOf(true) }
    var recordingEnabled by remember { mutableStateOf(true) }
    var motionDetection by remember { mutableStateOf(true) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AddCameraTopBarModern() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Scroll ou rien */ },
                containerColor = NoorBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Check, contentDescription = "Valider")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(Modifier.height(16.dp)) }

            item {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Emplacement de la caméra") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorBlue,
                        focusedLabelColor = NoorBlue
                    )
                )
            }

            item {
                OutlinedTextField(
                    value = associatedStreetlight,
                    onValueChange = { associatedStreetlight = it },
                    label = { Text("Lampadaire associé (ID)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = zone,
                    onValueChange = { zone = it },
                    label = { Text("Zone") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                )
            }

            item {
                var typeExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = !typeExpanded }) {
                    OutlinedTextField(
                        readOnly = true,
                        value = type.displayName,
                        onValueChange = {},
                        label = { Text("Type de caméra") },
                        leadingIcon = { Icon(type.icon, contentDescription = null, tint = type.color) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = type.color)
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        CameraType.entries.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.displayName) },
                                leadingIcon = { Icon(t.icon, contentDescription = null, tint = t.color) },
                                onClick = { type = t; typeExpanded = false }
                            )
                        }
                    }
                }
            }

            item {
                var statusExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = !statusExpanded }) {
                    OutlinedTextField(
                        readOnly = true,
                        value = status.displayName,
                        onValueChange = {},
                        label = { Text("Statut initial") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = status.color)
                    )
                    ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                        CameraStatus.entries.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.displayName) },
                                onClick = { status = s; statusExpanded = false },
                                modifier = Modifier.background(if (s == status) s.color.copy(0.2f) else Color.Transparent)
                            )
                        }
                    }
                }
            }

            item {
                var resolutionExpanded by remember { mutableStateOf(false) }
                val resolutions = listOf("720p", "1080p", "2K", "4K", "8K")
                ExposedDropdownMenuBox(expanded = resolutionExpanded, onExpandedChange = { resolutionExpanded = !resolutionExpanded }) {
                    OutlinedTextField(
                        readOnly = true,
                        value = resolution,
                        onValueChange = {},
                        label = { Text("Résolution") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = resolutionExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = resolutionExpanded, onDismissRequest = { resolutionExpanded = false }) {
                        resolutions.forEach { res ->
                            DropdownMenuItem(
                                text = { Text(res) },
                                onClick = { resolution = res; resolutionExpanded = false }
                            )
                        }
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = nightVision, onCheckedChange = { nightVision = it })
                    Text("Vision nocturne", fontWeight = FontWeight.Medium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = recordingEnabled, onCheckedChange = { recordingEnabled = it })
                    Text("Enregistrement activé", fontWeight = FontWeight.Medium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = motionDetection, onCheckedChange = { motionDetection = it })
                    Text("Détection de mouvement", fontWeight = FontWeight.Medium)
                }
            }

            item {
                Button(
                    onClick = {
                        // TODO: Sauvegarde réelle
                        onAddSuccess()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = location.isNotBlank() && associatedStreetlight.isNotBlank(),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NoorBlue)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("Valider et ajouter la caméra", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun AddCameraTopBarModern() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(NoorBlue.copy(alpha = 0.9f), NoorBlue.copy(alpha = 0.6f))
                )
            )
            .padding(horizontal = 20.dp, vertical = 48.dp)
    ) {
        Column {
            Text("Nouvelle caméra", color = Color.White.copy(0.9f), fontSize = 16.sp)
            Text("Surveillance intelligente", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddCameraPreview() {
    SansaTheme {
        AddCameraScreen()
    }
}