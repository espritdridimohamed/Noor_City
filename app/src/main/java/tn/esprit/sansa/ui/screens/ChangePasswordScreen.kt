// ChangePasswordScreen.kt
package tn.esprit.sansa.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import tn.esprit.sansa.ui.theme.NoorBlue
import tn.esprit.sansa.ui.theme.NoorRed
import tn.esprit.sansa.ui.viewmodels.AuthState
import tn.esprit.sansa.ui.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    
    val authState by viewModel.authState.collectAsState()
    val error = (authState as? AuthState.Error)?.message

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Changer le mot de passe") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = NoorBlue
            )
            
            Spacer(Modifier.height(32.dp))
            
            PasswordField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = "Mot de passe actuel",
                isVisible = currentPasswordVisible,
                onVisibilityChange = { currentPasswordVisible = !currentPasswordVisible }
            )
            
            Spacer(Modifier.height(16.dp))
            
            PasswordField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = "Nouveau mot de passe",
                isVisible = newPasswordVisible,
                onVisibilityChange = { newPasswordVisible = !newPasswordVisible }
            )
            
            Spacer(Modifier.height(16.dp))
            
            PasswordField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirmer nouveau mot de passe",
                isVisible = newPasswordVisible, // Share visibility toggle or not? Let's use same for simplicity or hide
                onVisibilityChange = { /* Optional separate toggle */ }
            )

            if (error != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = error,
                    color = NoorRed,
                    fontSize = 14.sp
                )
            }
            
            Spacer(Modifier.height(32.dp))
            
            Button(
                onClick = {
                    if (newPassword == confirmPassword) {
                        if (newPassword.length >= 6) {
                            viewModel.updatePassword(currentPassword, newPassword, onSuccess)
                        } else {
                            // Local validation error could be shown via a snackbar or state
                        }
                    } 
                },
                enabled = currentPassword.isNotEmpty() && newPassword.isNotEmpty() && newPassword == confirmPassword,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NoorBlue)
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Mettre à jour", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isVisible: Boolean,
    onVisibilityChange: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onVisibilityChange) {
                Icon(
                    imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null
                )
            }
        },
        singleLine = true
    )
}
