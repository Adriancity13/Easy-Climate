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
import com.example.engine.BioclimaticClothingEngine
import com.example.engine.ClothingRecommendation
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
        val humidity = prefs.getInt("cached_humidity", 50)

        val cachedIcon = prefs.getString("cached_clothing_icon", null)
        val cachedSummary = prefs.getString("cached_clothing_summary", null)
        val cachedSource = prefs.getString("cached_clothing_source", "⚙️ Local") ?: "⚙️ Local"

        val (clothingIcon, clothingSummary) = if (cachedIcon != null && cachedSummary != null) {
            Pair(cachedIcon, cachedSummary)
        } else {
            getConciseClothingSummary(
                temp = temp.toDouble(),
                apparentTemp = feelsLike.toDouble(),
                windSpeed = windSpeed.toDouble(),
                rainProb = rainProb,
                humidity = humidity
            )
        }

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
                windSpeed = windSpeed,
                clothingSummary = clothingSummary,
                clothingIcon = clothingIcon,
                sourceBadge = cachedSource
            )
        }
    }

    companion object {
        fun getConciseClothingSummary(
            temp: Double,
            apparentTemp: Double,
            windSpeed: Double,
            rainProb: Int,
            humidity: Int = 50,
            recommendation: ClothingRecommendation? = null
        ): Pair<String, String> {
            val adv = recommendation?.advancedMetrics
            val isMandatoryChest = adv?.isMandatoryChestProtection ?: (windSpeed >= 18.0 && temp < 18.0)
            val isMucosaRisk = adv?.isRespiratoryMucosaRisk ?: (temp <= 12.0 && humidity < 40)
            val isSunsetRisk = adv?.isSunsetColdSweatRisk ?: false
            val isHighSweat = adv?.isHighSweatRisk ?: (humidity > 70 && temp >= 20.0)

            val icon = when {
                rainProb >= 50 -> "🌧️"
                isMandatoryChest -> "🛡️"
                isMucosaRisk -> "🧣"
                isHighSweat || temp >= 24.0 -> "🎽"
                temp >= 20.0 -> "👕"
                temp >= 14.0 -> "🧥"
                else -> "❄️"
            }

            val summary = when {
                rainProb >= 50 -> "Cortavientos impermeable"
                isMandatoryChest -> "Capa transpirable + cortavientos"
                isMucosaRisk -> "Cortavientos cerrado + braga cuello"
                isSunsetRisk -> "Manga corta + cortavientos modular"
                isHighSweat && temp >= 22.0 -> "Sintético ligero de secado rápido"
                temp >= 26.0 -> "Manga corta ultraligera"
                temp >= 20.0 -> "Manga corta transpirable"
                temp >= 15.0 -> "Manga corta + chaqueta ligera"
                temp >= 10.0 -> "Capa base + cortavientos cerrado"
                else -> "Multicapa invernal + abrigo"
            }

            return Pair(icon, summary)
        }

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
            windSpeed: Int,
            clothingRecommendation: String? = null,
            clothingSummary: String? = null,
            clothingIcon: String? = null,
            sourceBadge: String? = null
        ) {
            val prefs = context.getSharedPreferences("weather_app_prefs", Context.MODE_PRIVATE)
            val humidity = prefs.getInt("cached_humidity", 50)

            val (effectiveIcon, effectiveSummary) = if (clothingIcon != null && clothingSummary != null) {
                Pair(clothingIcon, clothingSummary)
            } else {
                getConciseClothingSummary(
                    temp = temp.toDouble(),
                    apparentTemp = feelsLike.toDouble(),
                    windSpeed = windSpeed.toDouble(),
                    rainProb = rainProb,
                    humidity = humidity
                )
            }

            val effectiveSourceBadge = sourceBadge ?: prefs.getString("cached_clothing_source", "⚙️ Local") ?: "⚙️ Local"
            val effectiveClothing = clothingRecommendation ?: effectiveSummary

            // Save to prefs for widget persistence
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
                .putString("cached_clothing", effectiveClothing)
                .putString("cached_clothing_icon", effectiveIcon)
                .putString("cached_clothing_summary", effectiveSummary)
                .putString("cached_clothing_source", effectiveSourceBadge)
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
                    windSpeed = windSpeed,
                    clothingSummary = effectiveSummary,
                    clothingIcon = effectiveIcon,
                    sourceBadge = effectiveSourceBadge
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
            windSpeed: Int,
            clothingSummary: String,
            clothingIcon: String,
            sourceBadge: String
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_weather_2x2)

            // Explicitly format Location avoiding overflow
            val displayName = formatWidgetLocation(cityName)

            views.setTextViewText(R.id.widget_location, displayName)
            views.setTextViewText(R.id.widget_temperature, "$temp°")
            views.setTextViewText(R.id.widget_high_low, "↑$tempMax° ↓$tempMin°")
            views.setTextViewText(R.id.widget_description, description)
            views.setTextViewText(R.id.widget_feels_like, "Sens. $feelsLike°")
            views.setTextViewText(R.id.widget_rain_prob, "💧 $rainProb%")
            views.setTextViewText(R.id.widget_wind, "💨 $windSpeed km/h")

            // Bioclimatic bottom block: icon, 3-5 word summary, source badge (✨ Groq / ⚙️ Local)
            views.setTextViewText(R.id.widget_clothing_icon, clothingIcon)
            views.setTextViewText(R.id.widget_clothing_recommendation, clothingSummary)
            views.setTextViewText(R.id.widget_source_badge, sourceBadge)

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
