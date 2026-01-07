package tn.esprit.sansa.ui.screens
import android.util.Log


import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.lifecycle.viewmodel.compose.viewModel
import tn.esprit.sansa.auth.FacebookCallbackHolder
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import tn.esprit.sansa.R
import tn.esprit.sansa.ui.theme.*
import tn.esprit.sansa.ui.viewmodels.AuthState
import tn.esprit.sansa.ui.viewmodels.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: AuthViewModel = viewModel(),
    allowAutoLogin: Boolean = true
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
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

    // -- GOOGLE SIGN IN CONFIG --
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestIdToken(context.getString(R.string.default_web_client_id)) // This comes from google-services.json
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                account.idToken?.let { viewModel.loginWithGoogle(it) }
            } catch (e: ApiException) {
                Log.e("Auth", "Google sign-in failed", e)
                Toast.makeText(context, "Erreur Google: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // -- FACEBOOK LOGIN CONFIG --
    val callbackManager = FacebookCallbackHolder.callbackManager

    DisposableEffect(Unit) {
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                viewModel.loginWithFacebook(result.accessToken.token)
            }
            override fun onCancel() {
                Toast.makeText(context, "Connexion annulée", Toast.LENGTH_SHORT).show()
            }
            override fun onError(error: FacebookException) {
                Toast.makeText(context, "Erreur Facebook: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
        onDispose {
            LoginManager.getInstance().unregisterCallback(callbackManager)
        }
    }

    var observedLoading by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Loading) {
            observedLoading = true
        }
        if (authState is AuthState.Authenticated) {
            // Only auto-login if allowed OR if it's the result of an explicit action (loading happened)
            if (allowAutoLogin || observedLoading) {
                onLoginSuccess()
            }
        } else if (authState is AuthState.Error) {
            Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(bgGradientColors)
            )
    ) {
        // Decorative floating elements
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(300.dp)
                .offset(x = 100.dp, y = (-50).dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(200.dp)
                .offset(x = (-50).dp, y = 50.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))
            
            // Modern Logo Header
            Surface(
                modifier = Modifier.size(90.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color.White.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Flare,
                        contentDescription = "NoorCity Logo",
                        modifier = Modifier.size(44.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            
            Text(
                "NoorCity",
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = (-1).sp
            )
            Text(
                "Votre ville, plus intelligente que jamais",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))

            // Glassmorphism login card
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
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Se connecter",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColorPrimary,
                        modifier = Modifier.padding(bottom = 24.dp)
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
                            unfocusedTextColor = inputTextColor,
                            focusedPlaceholderColor = textColorSecondary,
                            unfocusedPlaceholderColor = textColorSecondary
                        )
                    )

                    Spacer(Modifier.height(16.dp))

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
                                    tint = inputIconColor
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
                            unfocusedTextColor = inputTextColor,
                            focusedPlaceholderColor = textColorSecondary,
                            unfocusedPlaceholderColor = textColorSecondary
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onNavigateToForgotPassword) {
                            Text("Mot de passe oublié ?", color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else NoorBlue, fontSize = 13.sp)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.loginWithEmail(email, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NoorBlue),
                        enabled = authState !is AuthState.Loading
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Se connecter", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Social/Register section
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Ou continuer avec", color = Color.White.copy(alpha = if (isDarkMode) 0.6f else 0.7f), fontSize = 14.sp)
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.width(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SocialLoginButton(
                        icon = Icons.Default.GTranslate, 
                        onClick = { googleLauncher.launch(googleSignInClient.signInIntent) },
                        modifier = Modifier.weight(1f),
                        isDarkMode = isDarkMode
                    )
                    SocialLoginButton(
                        icon = Icons.Default.Facebook, 
                        onClick = { 
                            LoginManager.getInstance().logInWithReadPermissions(context as androidx.activity.ComponentActivity, listOf("public_profile", "email")) 
                        },
                        modifier = Modifier.weight(1f),
                        isDarkMode = isDarkMode
                    )
                }
                
                Spacer(Modifier.height(40.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Nouveau ici ?", color = Color.White.copy(alpha = if (isDarkMode) 0.7f else 0.8f))
                    TextButton(onClick = onNavigateToRegister) {
                        Text("Créer un compte", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun SocialLoginButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = isSystemInDarkTheme()
) {
    val borderColor = if (isDarkMode) Color.White.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.4f)
    val contentColor = Color.White
    
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor)
    ) {
        Icon(icon, null, modifier = Modifier.size(24.dp))
    }
}
