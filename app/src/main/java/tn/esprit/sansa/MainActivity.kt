package tn.esprit.sansa

import android.content.Intent
import android.os.Build
import android.util.Base64
import android.util.Log
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
import com.stripe.android.PaymentConfiguration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logFacebookKeyHash()
        
        // Initialisation de Stripe avec la clé fournie par l'utilisateur
        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51SmQcvDxGWBSGImQKuRY1oie8VBWDQD8suP009GNyWFpsQuf9qcstYTiJbBx5Oo8DiIrrq9vS3pu1DJGBy0wA3mT00dHjcpeDB"
        )

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        tn.esprit.sansa.auth.FacebookCallbackHolder.callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun logFacebookKeyHash() {
        try {
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, android.content.pm.PackageManager.PackageInfoFlags.of(android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES.toLong()))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, android.content.pm.PackageManager.GET_SIGNATURES)
            }
            val signatures: Array<android.content.pm.Signature> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // signingInfo can be null; fall back to empty array
                info.signingInfo?.apkContentsSigners ?: emptyArray()
            } else {
                @Suppress("DEPRECATION")
                info.signatures ?: emptyArray()
            }
            for (signature in signatures) {
                val md = java.security.MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val keyHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                Log.d("FBKeyHash", keyHash)
            }
        } catch (e: Exception) {
            Log.e("FBKeyHash", "Failed to compute key hash", e)
        }
    }
}