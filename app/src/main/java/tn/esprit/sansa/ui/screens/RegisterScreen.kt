package tn.esprit.sansa.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import tn.esprit.sansa.ui.theme.*
import tn.esprit.sansa.ui.viewmodels.AuthState
import tn.esprit.sansa.ui.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackPressed: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    var observedLoading by remember { mutableStateOf(false) }
    
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    val isDarkMode = isSystemInDarkTheme()

    // Theme colors for dark/light mode
    val bgGradientColors = if (isDarkMode) {
        listOf(Color(0xFF0D1B2A), Color(0xFF1B2D42))
    } else {
        listOf(NoorBlue, NoorIndigo)
    }
    
    val cardBgColor = if (isDarkMode) Color(0xFF1A2A3A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.95f)
    val textColorPrimary = if (isDarkMode) Color.White else Color.Black
    val textColorSecondary = if (isDarkMode) Color(0xFFB0B0B0) else Color(0xFF666666)
    val inputBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.5f)
    val inputLabelColor = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color(0xFF1A1A1A)
    val inputTextColor = if (isDarkMode) Color.White else Color.Black
    val inputIconColor = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
    val focusedBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.5f) else NoorBlue
    val focusedLabelColor = if (isDarkMode) Color.White else NoorBlue

    LaunchedEffect(authState) {
        if (authState is AuthState.Loading) {
            observedLoading = true
        }
        if (authState is AuthState.Authenticated && observedLoading) {
            onRegisterSuccess()
        } else if (authState is AuthState.Error) {
            Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(bgGradientColors))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackPressed,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
            
            Text(
                "Créer un compte",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = (-1).sp
            )
            Text(
                "Rejoignez l'aventure NoorCity",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(Modifier.height(48.dp))

            // Glasmorphism card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(32.dp),
                color = cardBgColor,
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nom complet", color = inputLabelColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = inputIconColor) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = focusedBorderColor,
                            unfocusedBorderColor = inputBorderColor,
                            focusedLabelColor = focusedLabelColor,
                            unfocusedLabelColor = inputLabelColor,
                            focusedTextColor = inputTextColor,
                            unfocusedTextColor = inputTextColor
                        )
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Adresse email", color = inputLabelColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Outlined.Email, null, tint = inputIconColor) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = focusedBorderColor,
                            unfocusedBorderColor = inputBorderColor,
                            focusedLabelColor = focusedLabelColor,
                            unfocusedLabelColor = inputLabelColor,
                            focusedTextColor = inputTextColor,
                            unfocusedTextColor = inputTextColor
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Mot de passe", color = inputLabelColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = inputIconColor) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    null,
                                    tint = inputIconColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = focusedBorderColor,
                            unfocusedBorderColor = inputBorderColor,
                            focusedLabelColor = focusedLabelColor,
                            unfocusedLabelColor = inputLabelColor,
                            focusedTextColor = inputTextColor,
                            unfocusedTextColor = inputTextColor
                        )
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmer mot de passe", color = inputLabelColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.VerifiedUser, null, tint = inputIconColor) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = focusedBorderColor,
                            unfocusedBorderColor = inputBorderColor,
                            focusedLabelColor = focusedLabelColor,
                            unfocusedLabelColor = inputLabelColor,
                            focusedTextColor = inputTextColor,
                            unfocusedTextColor = inputTextColor
                        )
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (password == confirmPassword) {
                                viewModel.registerCitizen(name, email, password)
                            } else {
                                Toast.makeText(context, "Mots de passe différents", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NoorBlue),
                        enabled = authState !is AuthState.Loading && name.isNotBlank() && email.isNotBlank() && password.length >= 6
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("S'inscrire", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = "En s'inscrivant, vous acceptez nos Conditions d'utilisation",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}
