package tn.esprit.sansa.data.services

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tn.esprit.sansa.ui.screens.models.*

class WeatherService {
    private val client = OkHttpClient()
    private val apiKey = "cf5971880df07a398408c0ac3c70a479"

    suspend fun getWeatherData(lat: Double, lon: Double): WeatherData? = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&units=metric&appid=$apiKey&lang=fr"
            Log.d("WeatherService", "Fetching weather: $url")
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: run {
                Log.e("WeatherService", "Empty response body")
                return@withContext null
            }
            
            Log.d("WeatherService", "Response: $body")
            val json = JSONObject(body)
            if (json.has("cod") && json.getInt("cod") != 200) {
                Log.e("WeatherService", "API Error: ${json.optString("message")}")
                return@withContext null
            }
            val main = json.getJSONObject("main")
            val weatherArray = json.getJSONArray("weather")
            val weather = weatherArray.getJSONObject(0)
            
            WeatherData(
                temperature = main.getDouble("temp"),
                humidity = main.getInt("humidity"),
                description = weather.getString("description"),
                iconCode = weather.getString("icon"),
                cityName = json.getString("name")
            )
        } catch (e: Exception) {
            Log.e("WeatherService", "Error fetching weather", e)
            null
        }
    }

    suspend fun getAirQualityData(lat: Double, lon: Double): AirQualityData? = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.openweathermap.org/data/2.5/air_pollution?lat=$lat&lon=$lon&appid=$apiKey"
            Log.d("WeatherService", "Fetching AQI: $url")
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: run {
                Log.e("WeatherService", "Empty AQI response body")
                return@withContext null
            }
            
            Log.d("WeatherService", "AQI Response: $body")
            val json = JSONObject(body)
            if (!json.has("list")) {
                Log.e("WeatherService", "AQI API Error or Invalid Key: ${json.optString("message")}")
                return@withContext null
            }
            val list = json.getJSONArray("list")
            val first = list.getJSONObject(0)
            val main = first.getJSONObject("main")
            val components = first.getJSONObject("components")
            
            AirQualityData(
                aqi = main.getInt("aqi"),
                co = components.getDouble("co"),
                no2 = components.getDouble("no2"),
                o3 = components.getDouble("o3"),
                pm2_5 = components.getDouble("pm2_5"),
                pm10 = components.getDouble("pm10")
            )
        } catch (e: Exception) {
            Log.e("WeatherService", "Error fetching AQI", e)
            null
        }
    }
}
