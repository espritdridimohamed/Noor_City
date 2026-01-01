// AddReclamationScreen.kt – Version Premium (Noor Design System)
package tn.esprit.sansa.ui.screens
import androidx.compose.ui.draw.shadow
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import tn.esprit.sansa.ui.theme.SansaTheme
import tn.esprit.sansa.ui.theme.*
import tn.esprit.sansa.ui.components.*
import tn.esprit.sansa.ui.screens.models.*
import tn.esprit.sansa.ui.viewmodels.ReclamationsViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReclamationScreen(
    onAddSuccess: () -> Unit = {},
    onBackPressed: () -> Unit = {},
    viewModel: ReclamationsViewModel = viewModel()
) {
    val context = LocalContext.current
    var location by remember { mutableStateOf("") }
    var streetlightId by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var reportedBy by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(ReclamationPriority.MEDIUM) }
    var isSubmitting by remember { mutableStateOf(false) }

    val descriptionSuggestions = listOf(
        "Lampadaire éteint",
        "Lumière clignotante",
        "Poteau endommagé",
        "Câblage apparent",
        "Éclairage faible",
        "Allumé en plein jour"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AddReclamationTopBar(onBackPressed = onBackPressed)
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }

                // Section Identité
                item {
                    ModernSectionCard(title = "Votre Identité", icon = Icons.Default.Person) {
                        OutlinedTextField(
                            value = reportedBy,
                            onValueChange = { reportedBy = it },
                            label = { Text("Nom complet") },
                            placeholder = { Text("Ex: Ahmed Ben Ali") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NoorBlue,
                                cursorColor = NoorBlue
                            )
                        )
                    }
                }

                // Section Localisation
                item {
                    val streetlights by viewModel.allStreetlights.collectAsState()
                    var showScanner by remember { mutableStateOf(false) }

                    if (showScanner) {
                        ModalBottomSheet(
                            onDismissRequest = { showScanner = false },
                            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxHeight(0.6f)) {
                                QRScannerView(onCodeScanned = { id ->
                                    streetlightId = id
                                    // Auto-fill location if streetlight found
                                    val matched = streetlights.find { it.id == id }
                                    if (matched != null) {
                                        location = matched.address
                                    }
                                    showScanner = false
                                })
                                // Overlay for the scanner
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(40.dp)
                                        .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                )
                            }
                        }
                    }

                    val permissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        if (isGranted) {
                            showScanner = true
                        } else {
                            Toast.makeText(context, "Permission caméra requise pour scanner le QR Code", Toast.LENGTH_SHORT).show()
                        }
                    }

                    ModernSectionCard(title = "Localisation", icon = Icons.Default.LocationOn) {
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Adresse ou Quartier") },
                            placeholder = { Text("Ex: Avenue Habib Bourguiba, Tunis") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NoorBlue,
                                cursorColor = NoorBlue
                            )
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = streetlightId,
                                onValueChange = { streetlightId = it },
                                label = { Text("ID Lampadaire") },
                                placeholder = { Text("Ex: L-001") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NoorBlue,
                                    cursorColor = NoorBlue
                                )
                            )
                            Spacer(Modifier.width(12.dp))
                            FilledIconButton(
                                onClick = { 
                                    when (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)) {
                                        PackageManager.PERMISSION_GRANTED -> showScanner = true
                                        else -> permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                modifier = Modifier.size(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = NoorBlue)
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scanner QR", tint = Color.White)
                            }
                        }
                    }
                }

                // Section Problème
                item {
                    ModernSectionCard(title = "Détails du problème", icon = Icons.Default.Description) {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            placeholder = { Text("Décrivez précisément le souci...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NoorBlue,
                                cursorColor = NoorBlue
                            )
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("Suggestions rapides", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(descriptionSuggestions) { suggestion ->
                                FilterChip(
                                    selected = description == suggestion,
                                    onClick = { description = suggestion },
                                    label = { Text(suggestion, fontSize = 11.sp) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NoorBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // Section Priorité
                item {
                    ModernSectionCard(title = "Niveau de priorité", icon = Icons.Default.PriorityHigh) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ReclamationPriority.entries.forEach { prio ->
                                PrioritySelectorItem(
                                    priority = prio,
                                    isSelected = priority == prio,
                                    onSelect = { priority = prio },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Bouton d'action
                item {
                    Button(
                        onClick = {
                            isSubmitting = true
                            val reclamation = Reclamation(
                                description = description,
                                location = location,
                                reportedBy = reportedBy,
                                streetlightId = streetlightId,
                                priority = priority
                            )
                            viewModel.addReclamation(reclamation) { success ->
                                isSubmitting = false
                                if (success) {
                                    Toast.makeText(context, "Réclamation envoyée avec succès !", Toast.LENGTH_LONG).show()
                                    onAddSuccess()
                                } else {
                                    Toast.makeText(context, "Erreur lors de l'envoi.", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .shadow(8.dp, RoundedCornerShape(32.dp)),
                        enabled = !isSubmitting && description.isNotBlank() && location.isNotBlank() && reportedBy.isNotBlank(),
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NoorBlue,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(Modifier.width(12.dp))
                            Text("Envoyer le signalement", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}


@Composable
fun PrioritySelectorItem(
    priority: ReclamationPriority,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .clickable(onClick = onSelect),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (isSelected) priority.color else priority.color.copy(alpha = 0.15f))
                .border(
                    width = 2.dp,
                    color = if (isSelected) priority.color.copy(alpha = 0.3f) else Color.Transparent,
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                when(priority) {
                    ReclamationPriority.LOW -> Icons.Default.ArrowDownward
                    ReclamationPriority.MEDIUM -> Icons.Default.Remove
                    ReclamationPriority.HIGH -> Icons.Default.ArrowUpward
                    ReclamationPriority.URGENT -> Icons.Default.Warning
                },
                contentDescription = null,
                tint = if (isSelected) Color.White else priority.color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            priority.displayName,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) priority.color else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AddReclamationTopBar(onBackPressed: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(NoorBlue, NoorBlue.copy(alpha = 0.7f))
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBackPressed,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text("Signaler un problème", color = Color.White.copy(0.85f), fontSize = 14.sp)
                Text("Nouvel Incident", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
