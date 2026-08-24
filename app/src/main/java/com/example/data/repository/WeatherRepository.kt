package com.example.data.repository

import com.example.data.api.ApiClient
import com.example.data.models.ClothingAdvice
import com.example.data.models.CurrentWeatherUI
import com.example.data.models.DailyItem
import com.example.data.models.GeocodingCityItem
import com.example.data.models.HourlyItem
import com.example.data.models.OpenMeteoWeatherResponse
import com.example.utils.WeatherUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class WeatherRepository {

    suspend fun fetchWeather(
        latitude: Double,
        longitude: Double,
        knownCityName: String? = null
    ): Triple<String, CurrentWeatherUI, Pair<List<HourlyItem>, List<DailyItem>>> = withContext(Dispatchers.IO) {
        val cityName = if (!knownCityName.isNullOrBlank()) {
            knownCityName
        } else {
            resolveCityName(latitude, longitude)
        }

        val response = ApiClient.openMeteoApi.getForecast(latitude, longitude)
        val currentWeatherUI = processCurrentWeather(response)
        val hourlyAndDaily = processForecast(response)

        Triple(cityName, currentWeatherUI, hourlyAndDaily)
    }

    suspend fun searchCities(query: String): List<GeocodingCityItem> = withContext(Dispatchers.IO) {
        try {
            val res = ApiClient.geocodingApi.searchCity(name = query)
            res.results ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun resolveCityName(lat: Double, lon: Double): String {
        return try {
            val res = ApiClient.reverseGeocodeApi.reverseGeocode(lat, lon)
            res.displayName
        } catch (e: Exception) {
            "Tu Ubicación"
        }
    }

    private fun processCurrentWeather(response: OpenMeteoWeatherResponse): CurrentWeatherUI {
        val current = response.current ?: throw IllegalStateException("Current weather data is missing")
        val temp = current.temperature2m?.roundToInt() ?: 0
        val feelsLike = current.apparentTemperature ?: temp.toDouble()
        val feelsLikeInt = feelsLike.roundToInt()
        val hum = current.relativeHumidity2m ?: 0
        val wind = current.windSpeed10m?.roundToInt() ?: 0
        val rain = current.precipitationProbability ?: 0
        val wCode = current.weatherCode ?: 0
        val isDay = (current.isDay ?: 1) == 1

        var tempDropNight = 0
        val dailyMin = response.daily?.temperature2mMin?.firstOrNull()
        if (dailyMin != null) {
            tempDropNight = maxOf(0, temp - dailyMin.roundToInt())
        }

        val advice = WeatherUtils.generateClothingAdvice(
            temp = temp,
            feels = feelsLikeInt,
            hum = hum,
            wind = wind,
            rainProb = rain,
            tempDropNight = tempDropNight
        )

        val desc = WeatherUtils.getDescription(wCode)
        val iconType = WeatherUtils.getIconType(wCode, isDay)

        val rawSunrise = response.daily?.sunrise?.firstOrNull()
        val rawSunset = response.daily?.sunset?.firstOrNull()
        val formattedSunrise = WeatherUtils.formatSunTime(rawSunrise)
        val formattedSunset = WeatherUtils.formatSunTime(rawSunset)
        val daylightDuration = WeatherUtils.calculateDaylightDuration(rawSunrise, rawSunset)

        return CurrentWeatherUI(
            temp = temp,
            feelsLike = feelsLike,
            conditionDesc = desc,
            iconType = iconType,
            humidity = hum,
            windSpeed = wind,
            rainProb = rain,
            weatherCode = wCode,
            isDay = isDay,
            sunrise = formattedSunrise,
            sunset = formattedSunset,
            daylightDuration = daylightDuration,
            advice = advice
        )
    }

    private fun processForecast(response: OpenMeteoWeatherResponse): Pair<List<HourlyItem>, List<DailyItem>> {
        val hourlyItems = mutableListOf<HourlyItem>()
        val hourly = response.hourly

        val nowMs = System.currentTimeMillis() - 3600_000 // include current hour

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())

        if (hourly?.time != null && hourly.temperature2m != null && hourly.weatherCode != null) {
            var count = 0
            for (i in hourly.time.indices) {
                if (count >= 24) break
                val timeStr = hourly.time[i]
                val itemDate = try { sdf.parse(timeStr) } catch (e: Exception) { null }
                if (itemDate == null || itemDate.time >= nowMs) {
                    val hTemp = hourly.temperature2m.getOrNull(i)?.roundToInt() ?: 0
                    val hCode = hourly.weatherCode.getOrNull(i) ?: 0
                    val isDayInt = hourly.isDay?.getOrNull(i) ?: 1
                    val hRain = hourly.precipitationProbability?.getOrNull(i) ?: 0

                    val label = if (count == 0) "Ahora" else {
                        try {
                            val hourFormat = SimpleDateFormat("HH:00", Locale.getDefault())
                            hourFormat.format(itemDate ?: Date())
                        } catch (e: Exception) {
                            timeStr.substringAfter("T", "00:00")
                        }
                    }

                    hourlyItems.add(
                        HourlyItem(
                            rawTime = timeStr,
                            label = label,
                            temp = hTemp,
                            weatherCode = hCode,
                            isDay = isDayInt == 1,
                            rainProb = hRain
                        )
                    )
                    count++
                }
            }
        }

        // Daily processing
        val dailyItems = mutableListOf<DailyItem>()
        val daily = response.daily

        if (daily?.time != null && daily.weatherCode != null && daily.temperature2mMax != null && daily.temperature2mMin != null) {
            val limit = minOf(7, daily.time.size)
            for (i in 0 until limit) {
                val dateStr = daily.time[i]
                val dayName = WeatherUtils.getDayNameForIndex(i, dateStr)
                val dCode = daily.weatherCode.getOrNull(i) ?: 0
                val dMax = daily.temperature2mMax.getOrNull(i)?.roundToInt() ?: 0
                val dMin = daily.temperature2mMin.getOrNull(i)?.roundToInt() ?: 0
                val dRain = daily.precipitationProbabilityMax?.getOrNull(i) ?: 0
                val dWind = daily.windSpeed10mMax?.getOrNull(i)?.roundToInt() ?: 10
                val dFeels = daily.apparentTemperatureMax?.getOrNull(i)?.roundToInt() ?: dMax
                val desc = WeatherUtils.getDescription(dCode)
                val iconType = WeatherUtils.getIconType(dCode, true)

                val tempDrop = maxOf(0, dMax - dMin)
                val dayAdvice = WeatherUtils.generateClothingAdvice(
                    temp = dMax,
                    feels = dFeels,
                    hum = 60,
                    wind = dWind,
                    rainProb = dRain,
                    tempDropNight = tempDrop
                )

                val dayRawSunrise = daily.sunrise?.getOrNull(i)
                val dayRawSunset = daily.sunset?.getOrNull(i)
                val daySunrise = WeatherUtils.formatSunTime(dayRawSunrise)
                val daySunset = WeatherUtils.formatSunTime(dayRawSunset)
                val dayDuration = WeatherUtils.calculateDaylightDuration(dayRawSunrise, dayRawSunset)

                // 24-hour items for this specific day
                val dayHourlyList = mutableListOf<HourlyItem>()
                if (hourly?.time != null) {
                    for (h in hourly.time.indices) {
                        val hTime = hourly.time[h]
                        if (hTime.startsWith(dateStr)) {
                            val hTemp = hourly.temperature2m?.getOrNull(h)?.roundToInt() ?: 0
                            val hCode = hourly.weatherCode?.getOrNull(h) ?: 0
                            val isDayInt = hourly.isDay?.getOrNull(h) ?: 1
                            val hRain = hourly.precipitationProbability?.getOrNull(h) ?: 0

                            val hourLabel = try {
                                val itemD = sdf.parse(hTime)
                                val hourFormat = SimpleDateFormat("HH:00", Locale.getDefault())
                                hourFormat.format(itemD ?: Date())
                            } catch (e: Exception) {
                                hTime.substringAfter("T", "00:00")
                            }

                            dayHourlyList.add(
                                HourlyItem(
                                    rawTime = hTime,
                                    label = hourLabel,
                                    temp = hTemp,
                                    weatherCode = hCode,
                                    isDay = isDayInt == 1,
                                    rainProb = hRain
                                )
                            )
                        }
                    }
                }

                dailyItems.add(
                    DailyItem(
                        dateStr = dateStr,
                        dayName = dayName,
                        weatherCode = dCode,
                        maxTemp = dMax,
                        minTemp = dMin,
                        rainProb = dRain,
                        windSpeed = dWind,
                        feelsLikeMax = dFeels,
                        description = desc,
                        iconType = iconType,
                        sunrise = daySunrise,
                        sunset = daySunset,
                        daylightDuration = dayDuration,
                        advice = dayAdvice,
                        hourlyList = dayHourlyList
                    )
                )
            }
        }

        return Pair(hourlyItems, dailyItems)
    }
}
