package tn.esprit.sansa.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.launch
import tn.esprit.sansa.data.repositories.FirebaseAuthRepository
import tn.esprit.sansa.ui.theme.*

@Composable
fun ForgotPasswordScreen(
    onBackPressed: () -> Unit,
    repository: FirebaseAuthRepository = FirebaseAuthRepository()
) {
    var email by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var isSent by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(NoorBlue, NoorBlue.copy(alpha = 0.8f), Color.White)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
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
            }

            Spacer(Modifier.height(32.dp))
            
            Text(
                "Récupération",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                "Retrouvez l'accès à votre compte",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(Modifier.height(48.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!isSent) {
                        Text(
                            "Entrez votre adresse email pour recevoir un lien de réinitialisation.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            leadingIcon = { Icon(Icons.Default.Email, null, tint = NoorBlue) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NoorBlue,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )

                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick = {
                                if (email.isNotBlank()) {
                                    isSubmitting = true
                                    scope.launch {
                                        val success = repository.resetPassword(email)
                                        isSubmitting = false
                                        if (success) {
                                            isSent = true
                                        } else {
                                            Toast.makeText(context, "Erreur lors de l'envoi de l'email.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NoorBlue),
                            enabled = !isSubmitting && email.isNotBlank()
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Envoyer le lien", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = NoorGreen
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Email envoyé !",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = NoorBlue
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Vérifiez votre boîte de réception pour réinitialiser votre mot de passe.",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Spacer(Modifier.height(32.dp))
                            Button(
                                onClick = onBackPressed,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NoorBlue)
                            ) {
                                Text("Retour à la connexion")
                            }
                        }
                    }
                }
            }
        }
    }
}
