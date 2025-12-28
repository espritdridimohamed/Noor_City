package tn.esprit.sansa.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.sansa.ui.theme.SansaTheme

private val NoorBlue = Color(0xFF1E40AF)
private val NoorGreen = Color(0xFF10B981)
private val LightBlue = Color(0xFFEFF6FF)
private val LightGreen = Color(0xFFECFDF5)

enum class UserType { CITOYEN, TECHNICIEN }

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit = {},
    onBackToLogin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf(UserType.CITOYEN) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Créer un compte",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Rejoignez notre communauté",
                fontSize = 15.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Sélecteur de type moderne avec cartes
            ModernTypeSelector(
                selectedType = selectedType,
                onTypeSelected = { selectedType = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Formulaire avec animation
            AnimatedContent(
                targetState = selectedType,
                transitionSpec = {
                    (slideInHorizontally { width -> if (targetState == UserType.TECHNICIEN) width else -width } + fadeIn())
                        .togetherWith(slideOutHorizontally { width -> if (targetState == UserType.TECHNICIEN) -width else width } + fadeOut())
                        .using(SizeTransform(clip = false))
                },
                label = "form_animation"
            ) { type ->
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nom complet") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = if (type == UserType.CITOYEN) NoorGreen else NoorBlue
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (type == UserType.CITOYEN) NoorGreen else NoorBlue,
                            focusedLabelColor = if (type == UserType.CITOYEN) NoorGreen else NoorBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = if (type == UserType.CITOYEN) NoorGreen else NoorBlue
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (type == UserType.CITOYEN) NoorGreen else NoorBlue,
                            focusedLabelColor = if (type == UserType.CITOYEN) NoorGreen else NoorBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Mot de passe") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (type == UserType.CITOYEN) NoorGreen else NoorBlue
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = if (type == UserType.CITOYEN) NoorGreen else NoorBlue
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (type == UserType.CITOYEN) NoorGreen else NoorBlue,
                            focusedLabelColor = if (type == UserType.CITOYEN) NoorGreen else NoorBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmer le mot de passe") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = if (type == UserType.CITOYEN) NoorGreen else NoorBlue
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = if (type == UserType.CITOYEN) NoorGreen else NoorBlue
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (type == UserType.CITOYEN) NoorGreen else NoorBlue,
                            focusedLabelColor = if (type == UserType.CITOYEN) NoorGreen else NoorBlue
                        )
                    )

                    // Champ spécialité pour technicien
                    AnimatedVisibility(
                        visible = type == UserType.TECHNICIEN,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = specialty,
                                onValueChange = { specialty = it },
                                label = { Text("Spécialité") },
                                placeholder = { Text("ex: Électricien, Plombier...") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Build,
                                        contentDescription = null,
                                        tint = NoorBlue
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NoorBlue,
                                    focusedLabelColor = NoorBlue
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isLoading = true
                    // TODO: Logique d'inscription
                    onSignUpSuccess()
                    isLoading = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == UserType.CITOYEN) NoorGreen else NoorBlue
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Créer mon compte",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Déjà un compte ? ",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Text(
                    text = "Se connecter",
                    color = if (selectedType == UserType.CITOYEN) NoorGreen else NoorBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onBackToLogin() }
                )
            }
        }
    }
}

@Composable
fun ModernTypeSelector(
    selectedType: UserType,
    onTypeSelected: (UserType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Carte Citoyen
        TypeCard(
            title = "Citoyen",
            description = "Signaler des problèmes",
            icon = Icons.Default.Person,
            isSelected = selectedType == UserType.CITOYEN,
            color = NoorGreen,
            backgroundColor = LightGreen,
            onClick = { onTypeSelected(UserType.CITOYEN) },
            modifier = Modifier.weight(1f)
        )

        // Carte Technicien
        TypeCard(
            title = "Technicien",
            description = "Résoudre des problèmes",
            icon = Icons.Default.Build,
            isSelected = selectedType == UserType.TECHNICIEN,
            color = NoorBlue,
            backgroundColor = LightBlue,
            onClick = { onTypeSelected(UserType.TECHNICIEN) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TypeCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    color: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedBorderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 1.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "border_animation"
    )

    Card(
        modifier = modifier
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) backgroundColor else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = animatedBorderWidth,
            color = if (isSelected) color else Color.LightGray
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) color else Color.Gray,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (isSelected) color else Color.Black
            )

            Text(
                text = description,
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SignUpPreview() {
    SansaTheme {
        SignUpScreen()
    }
}