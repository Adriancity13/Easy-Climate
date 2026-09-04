package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.widget.worker.WeatherWorkScheduler

class WeatherWidgetProvider : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WeatherWorkScheduler.schedulePeriodicWeatherUpdate(context)
        LocationTrackingManager.startLocationTracking(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        WeatherWorkScheduler.schedulePeriodicWeatherUpdate(context)
        LocationTrackingManager.startLocationTracking(context)
        val prefs = context.getSharedPreferences("weather_app_prefs", Context.MODE_PRIVATE)
        val cityName = prefs.getString("cached_name", "Madrid") ?: "Madrid"
        val temp = prefs.getInt("cached_temp", 24)
        val tempMax = prefs.getInt("cached_temp_max", 27)
        val tempMin = prefs.getInt("cached_temp_min", 15)
        val desc = prefs.getString("cached_desc", "Cielo despejado") ?: "Cielo despejado"
        val code = prefs.getInt("cached_code", 0)
        val isDay = prefs.getBoolean("cached_is_day", true)
        val feelsLike = prefs.getInt("cached_feels_like", 24)
        val rainProb = prefs.getInt("cached_rain_prob", 0)
        val windSpeed = prefs.getInt("cached_wind_speed", 12)

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(
                context = context,
                appWidgetManager = appWidgetManager,
                appWidgetId = appWidgetId,
                cityName = cityName,
                temp = temp,
                tempMax = tempMax,
                tempMin = tempMin,
                description = desc,
                weatherCode = code,
                isDay = isDay,
                feelsLike = feelsLike,
                rainProb = rainProb,
                windSpeed = windSpeed
            )
        }
    }

    companion object {
        fun updateAllWidgets(
            context: Context,
            cityName: String,
            temp: Int,
            tempMax: Int,
            tempMin: Int,
            description: String,
            weatherCode: Int,
            isDay: Boolean,
            feelsLike: Int,
            rainProb: Int,
            windSpeed: Int
        ) {
            // Save to prefs for widget persistence
            val prefs = context.getSharedPreferences("weather_app_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("cached_name", cityName)
                .putInt("cached_temp", temp)
                .putInt("cached_temp_max", tempMax)
                .putInt("cached_temp_min", tempMin)
                .putString("cached_desc", description)
                .putInt("cached_code", weatherCode)
                .putBoolean("cached_is_day", isDay)
                .putInt("cached_feels_like", feelsLike)
                .putInt("cached_rain_prob", rainProb)
                .putInt("cached_wind_speed", windSpeed)
                .apply()

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, WeatherWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(
                    context = context,
                    appWidgetManager = appWidgetManager,
                    appWidgetId = appWidgetId,
                    cityName = cityName,
                    temp = temp,
                    tempMax = tempMax,
                    tempMin = tempMin,
                    description = description,
                    weatherCode = weatherCode,
                    isDay = isDay,
                    feelsLike = feelsLike,
                    rainProb = rainProb,
                    windSpeed = windSpeed
                )
            }
        }

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            cityName: String,
            temp: Int,
            tempMax: Int,
            tempMin: Int,
            description: String,
            weatherCode: Int,
            isDay: Boolean,
            feelsLike: Int,
            rainProb: Int,
            windSpeed: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_weather_2x2)

            // Explicitly preserve Barrio and City format (e.g. "Delicias, Madrid" or "Casa de Campo, Madrid")
            val displayName = formatWidgetLocation(cityName)

            views.setTextViewText(R.id.widget_location, displayName)
            views.setTextViewText(R.id.widget_temperature, "$temp°")
            views.setTextViewText(R.id.widget_high_low, "↑$tempMax° ↓$tempMin°")
            views.setTextViewText(R.id.widget_description, description)
            views.setTextViewText(R.id.widget_feels_like, "Sens $feelsLike°")
            views.setTextViewText(R.id.widget_rain_prob, "🌧 $rainProb%")
            views.setTextViewText(R.id.widget_wind, "💨 $windSpeed km/h")

            // Weather icon mapping
            val iconResId = getWidgetIconRes(weatherCode, isDay)
            views.setImageViewResource(R.id.widget_icon, iconResId)

            // Click Intent to open MainActivity
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun getWidgetIconRes(code: Int, isDay: Boolean): Int {
            return when (code) {
                0 -> if (isDay) R.drawable.ic_widget_sun else R.drawable.ic_widget_moon
                1, 2 -> if (isDay) R.drawable.ic_widget_cloud_sun else R.drawable.ic_widget_cloud_moon
                3 -> R.drawable.ic_widget_cloud
                45, 48 -> R.drawable.ic_widget_fog
                51, 53, 55 -> R.drawable.ic_widget_drizzle
                61, 63, 65, 80, 81 -> R.drawable.ic_widget_rain
                71, 73, 75 -> R.drawable.ic_widget_snow
                82, 95, 96, 99 -> R.drawable.ic_widget_storm
                else -> if (isDay) R.drawable.ic_widget_sun else R.drawable.ic_widget_moon
            }
        }

        fun formatWidgetLocation(name: String): String {
            val trimmed = name.trim()
            if (!trimmed.contains(",")) return trimmed
            val parts = trimmed.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            return when {
                parts.size >= 2 -> "${parts[0]}, ${parts[1]}"
                parts.size == 1 -> parts[0]
                else -> trimmed
            }
        }
    }
}
