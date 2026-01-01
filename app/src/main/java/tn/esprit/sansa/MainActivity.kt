package tn.esprit.sansa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import tn.esprit.sansa.ui.navigation.AppNavigationWithModernBar
import tn.esprit.sansa.ui.theme.SansaTheme
import tn.esprit.sansa.ui.viewmodels.SettingsViewModel
import tn.esprit.sansa.ui.utils.SansaLocalization
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            SansaTheme(
                darkTheme = settingsViewModel.isDarkMode,
                highContrast = settingsViewModel.isHighContrast,
                textSize = settingsViewModel.textSize
            ) {
                SansaLocalization(language = settingsViewModel.selectedLanguage) {
                    AppNavigationWithModernBar(
                        modifier = Modifier.fillMaxSize(),
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}