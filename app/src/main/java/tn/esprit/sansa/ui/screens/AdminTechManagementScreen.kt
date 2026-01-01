package tn.esprit.sansa.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import tn.esprit.sansa.ui.theme.*
import tn.esprit.sansa.ui.screens.models.*
import tn.esprit.sansa.ui.viewmodels.AuthViewModel
import tn.esprit.sansa.ui.viewmodels.AuthState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.HourglassEmpty

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AdminTechManagementScreen(
    onBackPressed: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val technicians by viewModel.technicians.collectAsState()
    val invitations by viewModel.invitations.collectAsState()
    val context = LocalContext.current
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Techniciens", "Invitations")
    var showInviteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchData()
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Error) {
            Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = { AdminTechTopBar(onBackPressed = onBackPressed) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showInviteDialog = true },
                containerColor = NoorBlue,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, "Inviter") },
                text = { Text("Inviter") },
                shape = RoundedCornerShape(16.dp)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = NoorBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NoorBlue
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    color = if (selectedTab == index) NoorBlue else Color.Gray.copy(alpha = 0.1f),
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = if (index == 0) technicians.size.toString() else invitations.size.toString(),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        color = if (selectedTab == index) Color.White else Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    )
                }
            }

            // List content view
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "TabTransition"
            ) { targetTab ->
                val currentList = if (targetTab == 0) technicians else invitations
                
                if (currentList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                if (targetTab == 0) Icons.Default.Engineering else Icons.Outlined.MailOutline,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray.copy(alpha = 0.3f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                if (targetTab == 0) "Aucun technicien actif" else "Aucune invitation en attente",
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(currentList) { tech ->
                            TechItemCard(tech)
                        }
                    }
                }
            }
        }

        if (showInviteDialog) {
            InviteTechDialog(
                onDismiss = { showInviteDialog = false },
                onInvite = { name, email, specialty ->
                    viewModel.inviteTechnician(name, email, specialty)
                    showInviteDialog = false
                },
                isLoading = authState is AuthState.Loading
            )
        }
    }
}

@Composable
fun TechItemCard(tech: UserAccount) {
    val isActive = !tech.isFirstLogin
    val statusColor = if (isActive) Color(0xFF10B981) else Color(0xFFF59E0B)
    val statusText = if (isActive) "Actif" else "En attente"
    
    val specialtyIcon = when (tech.specialty) {
        "Électricité" -> Icons.Default.ElectricBolt
        "Éclairage Public" -> Icons.Default.Lightbulb
        "Maintenance" -> Icons.Default.Build
        "Réseaux" -> Icons.Default.Cable
        else -> Icons.Default.Engineering
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = NoorBlue.copy(alpha = 0.15f),
                    border = BorderStroke(2.dp, NoorBlue.copy(alpha = 0.3f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            tech.name.take(1).uppercase(),
                            color = NoorBlue,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp
                        )
                    }
                }
                
                Spacer(Modifier.width(16.dp))
                
                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            tech.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                        Spacer(Modifier.width(8.dp))
                        // Badge de statut
                        Surface(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(statusColor, CircleShape)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    statusText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = statusColor
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(6.dp))
                    
                    // Spécialité avec icône
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            specialtyIcon,
                            contentDescription = null,
                            tint = NoorIndigo,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            tech.specialty ?: "Général",
                            color = NoorIndigo,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(Modifier.height(4.dp))
                    
                    // Email
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            tech.email,
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            }
            
            // Date d'invitation / activation
            if (tech.invitedAt != null) {
                Spacer(Modifier.height(12.dp))
                Divider(color = Color.Gray.copy(alpha = 0.1f))
                Spacer(Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (isActive) "Activé le ${formatDate(tech.activatedAt ?: 0)}"
                            else "Invité le ${formatDate(tech.invitedAt)}",
                            fontSize = 12.sp,
                            color = Color.Gray.copy(alpha = 0.8f)
                        )
                    }
                    
                    if (!isActive) {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.HourglassEmpty,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Première connexion attendue",
                                    fontSize = 10.sp,
                                    color = Color(0xFFF59E0B),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper function pour formater les dates
private fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.FRENCH)
    return sdf.format(java.util.Date(timestamp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteTechDialog(
    onDismiss: () -> Unit,
    onInvite: (String, String, String) -> Unit,
    isLoading: Boolean = false
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    val specialties = listOf(
        "Électricité",
        "Éclairage Public",
        "Maintenance",
        "Réseaux",
        "Autre"
    )
    
    var nameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = if (!isLoading) onDismiss else { {} },
        confirmButton = {
            Button(
                onClick = {
                    nameError = name.isBlank()
                    emailError = email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                    
                    if (!nameError && !emailError && specialty.isNotBlank()) {
                        onInvite(name, email, specialty)
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NoorBlue,
                    disabledContainerColor = NoorBlue.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Envoi en cours...", color = Color.White)
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Envoyer l'invitation", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Annuler", color = Color.Gray)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = NoorBlue,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Inviter un technicien",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Nom
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Nom complet") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = NoorBlue)
                    },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Le nom est requis", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorBlue,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                    )
                )
                
                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = false
                    },
                    label = { Text("Email professionnel") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = NoorBlue)
                    },
                    isError = emailError,
                    supportingText = if (emailError) {
                        { Text("Email invalide", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoorBlue,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                    )
                )
                
                // Spécialité (Dropdown)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded && !isLoading }
                ) {
                    OutlinedTextField(
                        value = specialty,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Spécialité") },
                        leadingIcon = {
                            Icon(Icons.Default.Build, contentDescription = null, tint = NoorBlue)
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NoorBlue,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                        )
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        specialties.forEach { spec ->
                            DropdownMenuItem(
                                text = { Text(spec) },
                                onClick = {
                                    specialty = spec
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (specialty == spec) NoorBlue else Color.Transparent
                                    )
                                }
                            )
                        }
                    }
                }
                
                // Info message
                Surface(
                    color = NoorBlue.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = NoorBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Un email avec les identifiants sera envoyé automatiquement",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
fun AdminTechTopBar(onBackPressed: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(NoorBlue)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackPressed) {
                Icon(Icons.Default.ArrowBack, "Retour", tint = Color.White)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Administration", color = Color.White.copy(0.8f), fontSize = 12.sp)
                Text("Gestion Techniciens", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
