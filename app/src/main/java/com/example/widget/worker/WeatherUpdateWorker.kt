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

            val (cityName, currentWeather, forecast) = repository.fetchWeather(lat, lon, knownCityName)
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

            // Save new coordinates and barrio-level city name to cache
            prefs.edit()
                .putString("cached_lat", lat.toString())
                .putString("cached_lon", lon.toString())
                .putString("cached_name", cityName)
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
                windSpeed = currentWeather.windSpeed
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
