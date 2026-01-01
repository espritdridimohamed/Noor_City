package tn.esprit.sansa.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {
    var isDarkMode by mutableStateOf(false)
    var isLocationEnabled by mutableStateOf(true)
    var isPushEnabled by mutableStateOf(true)
    var isBiometryEnabled by mutableStateOf(false)
    var isOfflineMode by mutableStateOf(false)
    var isHighContrast by mutableStateOf(false)
    
    var selectedLanguage by mutableStateOf("Français")
    var refreshInterval by mutableStateOf("5 minutes")
    var textSize by mutableStateOf(100f)
}
