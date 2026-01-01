package tn.esprit.sansa.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import tn.esprit.sansa.data.repositories.FirebaseZonesRepository
import tn.esprit.sansa.ui.theme.NoorBlue
import tn.esprit.sansa.ui.theme.NoorIndigo
import tn.esprit.sansa.ui.viewmodels.AuthViewModel
import tn.esprit.sansa.ui.viewmodels.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechOnboardingScreen(
    onOnboardingComplete: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    zonesRepository: FirebaseZonesRepository = remember { FirebaseZonesRepository() }
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    var phoneNumber by remember { mutableStateOf("") }
    var selectedZone by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val zones by zonesRepository.getZones().collectAsState(initial = emptyList())

    var phoneError by remember { mutableStateOf(false) }
    var zoneError by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated && !(authState as AuthState.Authenticated).user.isFirstLogin) {
            onOnboardingComplete()
        }
        if (authState is AuthState.Error) {
            Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_LONG).show()
            authViewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(NoorBlue, NoorIndigo)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Icon(
                Icons.Outlined.RocketLaunch,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(80.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Bienvenue, ${currentUser?.name ?: "Technicien"} !",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                "Finalisons votre profil pour commencer",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            // Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Info Email/Specialty (Read-only)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = NoorBlue.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Badge, null, tint = NoorBlue, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Spécialité", fontSize = 10.sp, color = Color.Gray)
                                    Text(currentUser?.specialty ?: "Non définie", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Phone Input
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { 
                            phoneNumber = it
                            phoneError = false 
                        },
                        label = { Text("Numéro de téléphone") },
                        placeholder = { Text("Ex: 55 123 456") },
                        leadingIcon = { Icon(Icons.Outlined.Phone, null, tint = NoorBlue) },
                        isError = phoneError,
                        supportingText = if (phoneError) { { Text("Numéro invalide") } } else null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                        )
                    )

                    // Zone selection
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedZone,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Zone de travail") },
                            leadingIcon = { Icon(Icons.Outlined.Map, null, tint = NoorBlue) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(16.dp),
                            isError = zoneError,
                            supportingText = if (zoneError) { { Text("Veuillez choisir une zone") } } else null
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            zones.forEach { zone ->
                                DropdownMenuItem(
                                    text = { Text(zone.name) },
                                    onClick = {
                                        selectedZone = zone.name
                                        expanded = false
                                        zoneError = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val isPhoneValid = phoneNumber.length >= 8
                            val isZoneValid = selectedZone.isNotBlank()
                            
                            phoneError = !isPhoneValid
                            zoneError = !isZoneValid

                            if (isPhoneValid && isZoneValid) {
                                authViewModel.completeOnboarding(phoneNumber, selectedZone)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NoorBlue),
                        enabled = authState !is AuthState.Loading
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Terminer", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}
