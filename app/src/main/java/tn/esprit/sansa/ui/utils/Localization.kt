package tn.esprit.sansa.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

data class SansaStrings(
    val appTitle: String,
    val home: String,
    val history: String,
    val settings: String,
    val technicians: String,
    val sensors: String,
    val citizens: String,
    val reclamations: String,
    val language: String,
    val darkMode: String,
    val logout: String,
    val account: String,
    val performance: String,
    val refreshInterval: String,
    val clearCache: String,
    val help: String,
    val version: String
)

val FrenchStrings = SansaStrings(
    appTitle = "NoorCity",
    home = "Accueil",
    history = "Historique",
    settings = "Paramètres",
    technicians = "Techniciens",
    sensors = "Capteurs",
    citizens = "Citoyens",
    reclamations = "Réclamations",
    language = "Langue",
    darkMode = "Mode Sombre",
    logout = "Déconnexion",
    account = "Compte",
    performance = "Données et Performance",
    refreshInterval = "Intervalle de rafraîchissement",
    clearCache = "Vider le cache",
    help = "Aide et Support",
    version = "Version de l'application"
)

val EnglishStrings = SansaStrings(
    appTitle = "NoorCity",
    home = "Home",
    history = "History",
    settings = "Settings",
    technicians = "Technicians",
    sensors = "Sensors",
    citizens = "Citizens",
    reclamations = "Claims",
    language = "Language",
    darkMode = "Dark Mode",
    logout = "Logout",
    account = "Account",
    performance = "Data and Performance",
    refreshInterval = "Refresh Interval",
    clearCache = "Clear Cache",
    help = "Help and Support",
    version = "App Version"
)

val ArabicStrings = SansaStrings(
    appTitle = "نور سيتي",
    home = "الرئيسية",
    history = "السجل",
    settings = "الإعدادات",
    technicians = "الفنيين",
    sensors = "المستشعرات",
    citizens = "المواطنين",
    reclamations = "الشكاوى",
    language = "اللغة",
    darkMode = "الوضع الداكن",
    logout = "تسجيل الخروج",
    account = "الحساب",
    performance = "البيانات والأداء",
    refreshInterval = "فاصل التحديث",
    clearCache = "مسح التخزين المؤقت",
    help = "المساعدة والدعم",
    version = "إصدار التطبيق"
)

val LocalSansaStrings = staticCompositionLocalOf { FrenchStrings }

@Composable
fun SansaLocalization(language: String, content: @Composable () -> Unit) {
    val strings = when (language) {
        "English" -> EnglishStrings
        "العربية" -> ArabicStrings
        else -> FrenchStrings
    }
    CompositionLocalProvider(LocalSansaStrings provides strings) {
        content()
    }
}

object Sansa {
    val strings: SansaStrings
        @Composable
        @ReadOnlyComposable
        get() = LocalSansaStrings.current
}
