// SettingsScreen.kt — Advanced Settings Screen with full feature set
package tn.esprit.sansa.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import tn.esprit.sansa.ui.theme.SansaTheme
import tn.esprit.sansa.ui.viewmodels.SettingsViewModel
import tn.esprit.sansa.ui.utils.Sansa
import tn.esprit.sansa.ui.screens.models.UserAccount
import tn.esprit.sansa.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentUser: UserAccount? = null,
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val strings = Sansa.strings
    var showLanguagePicker by remember { mutableStateOf(false) }
    var showIntervalPicker by remember { mutableStateOf(false) }
    var showCacheSuccess by remember { mutableStateOf(false) }

    if (showLanguagePicker) {
        LanguagePickerDialog(
            current = settingsViewModel.selectedLanguage,
            onDismiss = { showLanguagePicker = false },
            onSelect = { 
                settingsViewModel.selectedLanguage = it
                showLanguagePicker = false
            }
        )
    }

    if (showIntervalPicker) {
        IntervalPickerDialog(
            current = settingsViewModel.refreshInterval,
            onDismiss = { showIntervalPicker = false },
            onSelect = { 
                settingsViewModel.refreshInterval = it
                showIntervalPicker = false
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { SettingsTopBar(onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // -- EN-TÊTE UTILISATEUR --
            SettingsUserHeader(currentUser, onProfileClick)
            
            Spacer(Modifier.height(12.dp))

            // -- SECTION : DONNÉES ET PERFORMANCE --
            SettingsSectionTitle(strings.performance)
            SettingsCard {
                SettingsActionRow(
                    icon = Icons.Outlined.Update,
                    title = strings.refreshInterval,
                    subtitle = settingsViewModel.refreshInterval,
                    onClick = { showIntervalPicker = true }
                )
                SettingsDivider()
                SettingsSwitchRow(
                    icon = Icons.Outlined.CloudOff,
                    title = "Mode Hors-ligne",
                    subtitle = "Télécharger les données critiques",
                    checked = settingsViewModel.isOfflineMode,
                    onCheckedChange = { settingsViewModel.isOfflineMode = it }
                )
                SettingsDivider()
                SettingsActionRow(
                    icon = Icons.Outlined.DeleteOutline,
                    title = strings.clearCache,
                    subtitle = if (showCacheSuccess) "Cache vidé !" else "42.5 MB d'espace utilisé",
                    onClick = { showCacheSuccess = true }
                )
            }

            // -- SECTION : ALERTES INTELLIGENTES --
            SettingsSectionTitle("Alertes Intelligentes")
            SettingsCard {
                SettingsActionRow(
                    icon = Icons.Outlined.AccessTime,
                    title = "Heures de silence (DND)",
                    subtitle = "22:00 - 07:00 active",
                    onClick = { /* TODO: Show DND hours picker */ }
                )
                SettingsDivider()
                SettingsActionRow(
                    icon = Icons.Outlined.BatteryAlert,
                    title = "Seuils d'alerte batterie",
                    subtitle = "Alerte à 20% par défaut",
                    onClick = { /* TODO: Show threshold picker */ }
                )
            }

            // -- SECTION : APPARENCE ET ACCESSIBILITÉ --
            SettingsSectionTitle("Apparence et Accessibilité")
            SettingsCard {
                SettingsSwitchRow(
                    icon = Icons.Outlined.DarkMode,
                    title = strings.darkMode,
                    subtitle = "Basculer entre clair et sombre",
                    checked = settingsViewModel.isDarkMode,
                    onCheckedChange = { settingsViewModel.isDarkMode = it }
                )
                SettingsDivider()
                SettingsActionRow(
                    icon = Icons.Outlined.Language,
                    title = strings.language,
                    subtitle = settingsViewModel.selectedLanguage,
                    onClick = { showLanguagePicker = true }
                )
                SettingsDivider()
                SettingsSwitchRow(
                    icon = Icons.Outlined.Contrast,
                    title = "Contraste élevé",
                    subtitle = "Améliorer la lisibilité",
                    checked = settingsViewModel.isHighContrast,
                    onCheckedChange = { settingsViewModel.isHighContrast = it }
                )
                SettingsDivider()
                SettingsActionRow(
                    icon = Icons.Outlined.Palette,
                    title = "Thème de couleur",
                    subtitle = "Indigo (Noor par défaut)",
                    onClick = { /* TODO: Show color picker */ }
                )
                SettingsDivider()
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SettingsIconBox(Icons.Outlined.TextFormat)
                        Spacer(Modifier.width(16.dp))
                        Text("Taille du texte", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    }
                    Slider(
                        value = settingsViewModel.textSize,
                        onValueChange = { settingsViewModel.textSize = it },
                        valueRange = 80f..140f,
                        modifier = Modifier.padding(top = 8.dp),
                        colors = SliderDefaults.colors(thumbColor = NoorBlue, activeTrackColor = NoorBlue)
                    )
                }
            }

            // -- SECTION : SÉCURITÉ ET LOGS --
            SettingsSectionTitle("Sécurité et Logs")
            SettingsCard {
                SettingsSwitchRow(
                    icon = Icons.Outlined.NotificationsActive,
                    title = "Notifications Push",
                    subtitle = "Alertes de maintenance et messages",
                    checked = settingsViewModel.isPushEnabled,
                    onCheckedChange = { settingsViewModel.isPushEnabled = it }
                )
                SettingsDivider()
                SettingsSwitchRow(
                    icon = Icons.Outlined.LocationOn,
                    title = "Localisation",
                    subtitle = "Utilisé pour la carte des lampadaires",
                    checked = settingsViewModel.isLocationEnabled,
                    onCheckedChange = { settingsViewModel.isLocationEnabled = it }
                )
                SettingsDivider()
                SettingsActionRow(
                    icon = Icons.Outlined.History,
                    title = "Historique d'activité",
                    subtitle = "Voir vos dernières actions",
                    onClick = { /* TODO: Navigate to logs */ }
                )
                SettingsDivider()
                SettingsActionRow(
                    icon = Icons.Outlined.Devices,
                    title = "Appareils connectés",
                    subtitle = "2 sessions actives",
                    onClick = { /* TODO: Manage sessions */ }
                )
                SettingsDivider()
                SettingsSwitchRow(
                    icon = Icons.Outlined.Fingerprint,
                    title = "Accès biométrique",
                    subtitle = "Utiliser FaceID / Empreinte",
                    checked = settingsViewModel.isBiometryEnabled,
                    onCheckedChange = { settingsViewModel.isBiometryEnabled = it }
                )
            }

            // -- SECTION : AIDE ET DIAGNOSTIC --
            SettingsSectionTitle("Aide et Diagnostic")
            SettingsCard {
                SettingsActionRow(
                    icon = Icons.Outlined.RestartAlt,
                    title = "Réinitialiser les guides",
                    subtitle = "Revoir les tutoriels d'accueil",
                    onClick = { /* TODO: Reset tutorial state */ }
                )
                SettingsDivider()
                SettingsActionRow(
                    icon = Icons.Outlined.MedicalServices,
                    title = "Diagnostic du système",
                    subtitle = "Vérifier la connexion et GPS",
                    onClick = { /* TODO: Run diagnostics */ }
                )
            }

            // -- À PROPOS --
            SettingsSectionTitle("À propos")
            SettingsCard {
                SettingsValueRow(
                    icon = Icons.Outlined.Info,
                    title = strings.version,
                    value = "2.8.5-premium"
                )
                SettingsDivider()
                SettingsActionRow(
                    icon = Icons.Outlined.Assignment,
                    title = "Conditions d'utilisation",
                    onClick = { }
                )
            }

            // -- ACTIONS DE COMPTE --
            Spacer(Modifier.height(32.dp))
            LogoutButton(strings.logout, onLogout)
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun LanguagePickerDialog(
    current: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text("Choisir la langue") },
        text = {
            Column {
                listOf("Français", "English", "العربية").forEach { lang ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(lang) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = lang == current, onClick = null)
                        Spacer(Modifier.width(16.dp))
                        Text(lang)
                    }
                }
            }
        }
    )
}

@Composable
private fun IntervalPickerDialog(
    current: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text("Rafraîchissement") },
        text = {
            Column {
                listOf("Temps réel", "5 minutes", "15 minutes", "Jamais").forEach { interval ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(interval) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = interval == current, onClick = null)
                        Spacer(Modifier.width(16.dp))
                        Text(interval)
                    }
                }
            }
        }
    )
}

@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
        }
        Text(
            "Réglages de NoorCity",
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SettingsUserHeader(currentUser: UserAccount?, onProfileClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onProfileClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(NoorBlue, NoorBlue.copy(alpha = 0.5f)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(currentUser?.name ?: "Utilisateur", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(currentUser?.role?.displayName ?: "Rôle inconnu", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                if (currentUser != null && currentUser.isVerified) {
                    Badge(containerColor = NoorGreen.copy(alpha = 0.2f), contentColor = NoorGreen) {
                        Text("COMPTE VÉRIFIÉ", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        letterSpacing = 1.sp
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconBox(icon)
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 16.sp)
            if (subtitle != null) {
                Text(subtitle, color = Color.Gray, fontSize = 13.sp)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
    }
}

@Composable
private fun SettingsValueRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconBox(icon)
        Spacer(Modifier.width(16.dp))
        Text(title, fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Text(value, color = NoorBlue, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconBox(icon)
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 16.sp)
            Text(subtitle, color = Color.Gray, fontSize = 13.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = NoorBlue, checkedTrackColor = NoorBlue.copy(alpha = 0.2f))
        )
    }
}

@Composable
private fun SettingsIconBox(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(NoorBlue.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = NoorBlue, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SettingsDivider() {
    Divider(modifier = Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
}

@Composable
private fun LogoutButton(label: String, onLogout: () -> Unit) {
    Button(
        onClick = onLogout,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NoorRed.copy(alpha = 0.1f), contentColor = NoorRed),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Icon(Icons.Default.Logout, contentDescription = null)
        Spacer(Modifier.width(12.dp))
        Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Preview(showBackground = true, name = "Paramètres Avancés")
@Composable
private fun PreviewSettings() {
    SansaTheme {
        SettingsScreen()
    }
}
