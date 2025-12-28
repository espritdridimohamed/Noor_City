package tn.esprit.sansa.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.sansa.R
import tn.esprit.sansa.ui.theme.SansaTheme

// Palette Noor (ajustée pour l'écran login)
private val NoorBlue = Color(0xFF1E40AF)
private val NoorGreen = Color(0xFF10B981)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onSignUp: () -> Unit = {},  // ← Ce callback pointe vers SignUpScreen
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)  // Fond blanc pur
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo / Illustration d'accueil
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(800)) + slideInVertically(initialOffsetY = { -100 })
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Logo SANSA",
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .shadow(8.dp, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Bienvenue",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Connectez-vous pour gérer l'éclairage intelligent",
                fontSize = 16.sp,
                color = Color.Black.copy(alpha = 0.65f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Champ Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        tint = NoorBlue
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NoorBlue,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = NoorBlue,
                    cursorColor = NoorBlue,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Champ Mot de passe
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mot de passe") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = NoorBlue
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Masquer" else "Afficher",
                            tint = NoorBlue
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NoorBlue,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = NoorBlue,
                    cursorColor = NoorBlue,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Lien "Mot de passe oublié"
            Text(
                text = "Mot de passe oublié ?",
                fontSize = 14.sp,
                color = NoorBlue,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { onForgotPassword() }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Bouton Connexion
            Button(
                onClick = {
                    isLoading = true
                    // TODO: Remplace par ta vraie logique d'authentification
                    onLoginSuccess()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NoorBlue),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text(
                        "Se connecter",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Lien vers la création de compte (SignUpScreen)
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Pas encore inscrit ? ",
                    color = Color.Black.copy(alpha = 0.7f),
                    fontSize = 15.sp
                )
                Text(
                    text = "Créer un compte",
                    color = NoorGreen,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onSignUp() }  // ← Clic ici ouvre SignUpScreen
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginPreview() {
    SansaTheme {
        LoginScreen(onLoginSuccess = {})
    }
}