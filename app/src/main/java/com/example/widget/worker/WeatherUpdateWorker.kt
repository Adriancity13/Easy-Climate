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

            val lat = latStr?.toDoubleOrNull() ?: 40.4168
            val lon = lonStr?.toDoubleOrNull() ?: -3.7038
            val fallbackCityName = cachedName ?: "Madrid, España"

            val repository = WeatherRepository()

            val (cityName, currentWeather, forecast) = repository.fetchWeather(lat, lon, fallbackCityName)
            val todayForecast = forecast.second.firstOrNull()
            val tempMax = todayForecast?.maxTemp ?: currentWeather.temp
            val tempMin = todayForecast?.minTemp ?: currentWeather.temp

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
