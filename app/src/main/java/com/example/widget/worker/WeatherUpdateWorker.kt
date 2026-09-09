package com.example.widget.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.api.ApiClient
import com.example.data.repository.WeatherRepository
import com.example.widget.WeatherWidgetProvider
import kotlin.math.roundToInt

class WeatherUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val prefs = applicationContext.getSharedPreferences("weather_app_prefs", Context.MODE_PRIVATE)
            val latStr = prefs.getString("cached_lat", null)
            val lonStr = prefs.getString("cached_lon", null)
            val cachedName = prefs.getString("cached_name", null)

            val overrideLat = inputData.getDouble("new_lat", Double.NaN)
            val overrideLon = inputData.getDouble("new_lon", Double.NaN)
            val isLocationSync = inputData.getBoolean("force_location_sync", false)

            val lat = if (!overrideLat.isNaN()) overrideLat else (latStr?.toDoubleOrNull() ?: 40.4168)
            val lon = if (!overrideLon.isNaN()) overrideLon else (lonStr?.toDoubleOrNull() ?: -3.7038)

            // When moving to a new location or performing a background refresh, resolve the new barrio name fresh
            val knownCityName = if (isLocationSync || latStr == null) null else cachedName

            val repository = WeatherRepository()

            // Restringido: cero llamadas a la API de Groq en ciclo de actualización del Widget
            val (cityName, currentWeather, forecast) = repository.fetchWeather(
                latitude = lat,
                longitude = lon,
                knownCityName = knownCityName,
                skipAi = true
            )
            val todayForecast = forecast.second.firstOrNull()
            val tempMax = todayForecast?.maxTemp ?: currentWeather.temp
            val tempMin = todayForecast?.minTemp ?: currentWeather.temp

            // Climate change detection: check for significant weather alterations
            val prevCode = prefs.getInt("cached_code", -1)
            val prevTemp = prefs.getInt("cached_temp", -999)
            val prevDesc = prefs.getString("cached_desc", "") ?: ""

            val weatherChangedSignificantly = (prevCode != -1 && prevCode != currentWeather.weatherCode) ||
                    (prevTemp != -999 && kotlin.math.abs(prevTemp - currentWeather.temp) >= 3) ||
                    (prevDesc.isNotBlank() && prevDesc != currentWeather.conditionDesc)

            if (weatherChangedSignificantly) {
                Log.d("WeatherUpdateWorker", "Significant weather condition change detected: code $prevCode -> ${currentWeather.weatherCode}, temp $prevTemp -> ${currentWeather.temp}")
            }

            val clothingRecommendation = currentWeather.recommendation?.headline ?: currentWeather.advice.baseAdvice

            val (clothingIcon, clothingSummary) = WeatherWidgetProvider.getConciseClothingSummary(
                temp = currentWeather.temp.toDouble(),
                apparentTemp = currentWeather.feelsLike,
                windSpeed = currentWeather.windSpeed.toDouble(),
                rainProb = currentWeather.rainProb,
                humidity = currentWeather.humidity,
                recommendation = currentWeather.recommendation
            )

            // Save new coordinates and barrio-level city name to cache
            prefs.edit()
                .putString("cached_lat", lat.toString())
                .putString("cached_lon", lon.toString())
                .putString("cached_name", cityName)
                .putInt("cached_temp", currentWeather.temp)
                .putInt("cached_temp_max", tempMax)
                .putInt("cached_temp_min", tempMin)
                .putString("cached_desc", currentWeather.conditionDesc)
                .putInt("cached_code", currentWeather.weatherCode)
                .putBoolean("cached_is_day", currentWeather.isDay)
                .putInt("cached_feels_like", currentWeather.feelsLike.roundToInt())
                .putInt("cached_rain_prob", currentWeather.rainProb)
                .putInt("cached_wind_speed", currentWeather.windSpeed)
                .putInt("cached_humidity", currentWeather.humidity)
                .putString("cached_clothing", clothingRecommendation)
                .putString("cached_clothing_icon", clothingIcon)
                .putString("cached_clothing_summary", clothingSummary)
                .putString("cached_clothing_source", "⚙️ Local")
                .putLong("last_weather_update_time", System.currentTimeMillis())
                .apply()

            WeatherWidgetProvider.updateAllWidgets(
                context = applicationContext,
                cityName = cityName,
                temp = currentWeather.temp,
                tempMax = tempMax,
                tempMin = tempMin,
                description = currentWeather.conditionDesc,
                weatherCode = currentWeather.weatherCode,
                isDay = currentWeather.isDay,
                feelsLike = currentWeather.feelsLike.roundToInt(),
                rainProb = currentWeather.rainProb,
                windSpeed = currentWeather.windSpeed,
                clothingRecommendation = clothingRecommendation,
                clothingSummary = clothingSummary,
                clothingIcon = clothingIcon,
                sourceBadge = "⚙️ Local"
            )

            Log.d("WeatherUpdateWorker", "Background weather widget update succeeded for $cityName: ${currentWeather.temp}°")
            Result.success()
        } catch (e: Exception) {
            Log.e("WeatherUpdateWorker", "Error executing background weather widget update: ${e.message}", e)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
