package tn.esprit.sansa.ui.screens.models

data class WeatherData(
    val temperature: Double = 0.0,
    val humidity: Int = 0,
    val description: String = "",
    val iconCode: String = "", // e.g., "01d"
    val cityName: String = ""
)

data class AirQualityData(
    val aqi: Int = 0, // Air Quality Index (1-5)
    val co: Double = 0.0,
    val no2: Double = 0.0,
    val o3: Double = 0.0,
    val pm2_5: Double = 0.0,
    val pm10: Double = 0.0
) {
    val statusResId: Int
        get() = when (aqi) {
            1 -> tn.esprit.sansa.R.string.aqi_good
            2 -> tn.esprit.sansa.R.string.aqi_fair
            3 -> tn.esprit.sansa.R.string.aqi_moderate
            4 -> tn.esprit.sansa.R.string.aqi_poor
            5 -> tn.esprit.sansa.R.string.aqi_very_poor
            else -> tn.esprit.sansa.R.string.aqi_unknown
        }
    
    val color: androidx.compose.ui.graphics.Color
        get() = when (aqi) {
            1 -> androidx.compose.ui.graphics.Color(0xFF10B981) // Green
            2 -> androidx.compose.ui.graphics.Color(0xFFFBBF24) // Yellow
            3 -> androidx.compose.ui.graphics.Color(0xFFF97316) // Orange
            4 -> androidx.compose.ui.graphics.Color(0xFFEF4444) // Red
            5 -> androidx.compose.ui.graphics.Color(0xFF7F1D1D) // Dark Red
            else -> androidx.compose.ui.graphics.Color.Gray
        }
}

data class ZoneWeatherInfo(
    val weather: WeatherData? = null,
    val airQuality: AirQualityData? = null
)
