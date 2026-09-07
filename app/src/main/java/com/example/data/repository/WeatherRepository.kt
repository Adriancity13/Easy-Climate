package com.example.data.repository

import com.example.data.api.ApiClient
import com.example.data.models.AirQualityCategory
import com.example.data.models.AirQualityResponse
import com.example.data.models.AirQualityUI
import com.example.data.models.ClothingAdvice
import com.example.data.models.CurrentWeatherUI
import com.example.data.models.DailyItem
import com.example.data.models.GeocodingCityItem
import com.example.data.models.HourlyItem
import com.example.data.models.OpenMeteoWeatherResponse
import com.example.engine.BioclimaticClothingEngine
import com.example.utils.WeatherUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
        val cityNameDeferred = async {
            if (!knownCityName.isNullOrBlank()) {
                knownCityName
            } else {
                resolveCityName(latitude, longitude)
            }
        }

        val forecastDeferred = async {
            ApiClient.openMeteoApi.getForecast(latitude, longitude)
        }

        val airQualityDeferred = async {
            try {
                ApiClient.airQualityApi.getAirQuality(latitude, longitude)
            } catch (_: Exception) {
                null
            }
        }

        val cityName = cityNameDeferred.await()
        val response = forecastDeferred.await()
        val aqResponse = airQualityDeferred.await()

        val airQualityUI = processAirQuality(aqResponse)
        val hourlyAndDaily = processForecast(response)
        val currentWeatherUI = processCurrentWeather(response, hourlyAndDaily.first, airQualityUI)

        Triple(cityName, currentWeatherUI, hourlyAndDaily)
    }

    suspend fun searchCities(query: String): List<GeocodingCityItem> = withContext(Dispatchers.IO) {
        val results = mutableListOf<GeocodingCityItem>()

        // 1. Primary: Micro-local Nominatim search (supports barrios, quarters, suburbs, districts, cities)
        try {
            val nominatimResults = ApiClient.nominatimApi.searchLocations(query = query)
            for (item in nominatimResults) {
                val lat = item.lat?.toDoubleOrNull() ?: continue
                val lon = item.lon?.toDoubleOrNull() ?: continue
                val addr = item.address

                val barrio = addr?.barrioName
                val city = addr?.cityName ?: item.name ?: ""
                val isBarrio = !barrio.isNullOrBlank() && !barrio.equals(city, ignoreCase = true)

                results.add(
                    GeocodingCityItem(
                        id = item.place_id,
                        name = if (city.isNotBlank()) city else (item.name ?: "Ubicación"),
                        latitude = lat,
                        longitude = lon,
                        country = addr?.country,
                        admin1 = addr?.state,
                        barrio = if (isBarrio) barrio else (if (!barrio.isNullOrBlank()) barrio else null),
                        isBarrio = isBarrio
                    )
                )
            }
        } catch (_: Exception) {
            // Fallback to secondary geocoder
        }

        if (results.isNotEmpty()) {
            return@withContext results
        }

        // 2. Secondary Fallback: Open-Meteo geocoding search
        try {
            val res = ApiClient.geocodingApi.searchCity(name = query)
            res.results ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun resolveCityName(lat: Double, lon: Double): String {
        // 1. Primary: Micro-local reverse geocode with Nominatim to extract barrio and city (e.g., "Delicias, Madrid")
        try {
            val res = ApiClient.nominatimApi.reverseGeocode(lat, lon)
            val barrioCity = res.address?.displayBarrioAndCity
            if (!barrioCity.isNullOrBlank() && barrioCity != "Tu Ubicación") {
                return barrioCity
            }
        } catch (_: Exception) {
            // Fallback to secondary reverse geocoding
        }

        // 2. Secondary Fallback: BigDataCloud reverse geocode
        return try {
            val res = ApiClient.reverseGeocodeApi.reverseGeocode(lat, lon)
            res.displayName
        } catch (_: Exception) {
            "Tu Ubicación"
        }
    }

    private fun processAirQuality(response: AirQualityResponse?): AirQualityUI? {
        if (response == null) return null
        val aqiValue = response.current?.europeanAqi
            ?: response.hourly?.europeanAqi?.firstOrNull()
            ?: return null

        val category = when {
            aqiValue <= 20 -> AirQualityCategory.BUENA
            aqiValue <= 40 -> AirQualityCategory.MODERADA
            else -> AirQualityCategory.DEFICIENTE
        }

        return AirQualityUI(aqi = aqiValue, category = category)
    }

    private fun processCurrentWeather(
        response: OpenMeteoWeatherResponse,
        hourlyItems: List<HourlyItem> = emptyList(),
        airQualityUI: AirQualityUI? = null
    ): CurrentWeatherUI {
        val current = response.current ?: throw IllegalStateException("Current weather data is missing")
        val temp = current.temperature2m?.roundToInt() ?: 0
        val feelsLike = current.apparentTemperature ?: temp.toDouble()
        val feelsLikeInt = feelsLike.roundToInt()
        val hum = current.relativeHumidity2m ?: 0
        val wind = current.windSpeed10m?.roundToInt() ?: 0
        val rain = current.precipitationProbability ?: 0
        val wCode = current.weatherCode ?: 0
        val isDay = (current.isDay ?: 1) == 1

        val rawSunrise = response.daily?.sunrise?.firstOrNull()
        val rawSunset = response.daily?.sunset?.firstOrNull()
        val formattedSunrise = WeatherUtils.formatSunTime(rawSunrise)
        val formattedSunset = WeatherUtils.formatSunTime(rawSunset)
        val daylightDuration = WeatherUtils.calculateDaylightDuration(rawSunrise, rawSunset)

        val bioclimaticRec = BioclimaticClothingEngine.calculate(
            currentTemp = current.temperature2m ?: temp.toDouble(),
            currentHumidity = hum,
            currentWindSpeed = current.windSpeed10m ?: wind.toDouble(),
            currentWindGusts = current.windGusts10m,
            currentApparentTemp = feelsLike,
            currentRainProb = rain,
            currentPrecipitation = current.precipitation,
            currentUvIndex = current.uvIndex ?: response.daily?.uvIndexMax?.firstOrNull(),
            currentCloudCover = current.cloudCover,
            isDay = isDay,
            hourlyItems = hourlyItems,
            dailyMaxUv = response.daily?.uvIndexMax?.firstOrNull(),
            dailyPrecipSum = response.daily?.precipitationSum?.firstOrNull(),
            sunsetTime = formattedSunset
        )

        val finalAdvice = bioclimaticRec.toClothingAdvice()

        val desc = WeatherUtils.getDescription(wCode)
        val iconType = WeatherUtils.getIconType(wCode, isDay)

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
            advice = finalAdvice,
            airQuality = airQualityUI,
            recommendation = bioclimaticRec
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
                    val hApparent = hourly.apparentTemperature?.getOrNull(i)
                    val hHumidity = hourly.relativeHumidity2m?.getOrNull(i)
                    val hWindSpeed = hourly.windSpeed10m?.getOrNull(i)
                    val hWindGusts = hourly.windGusts10m?.getOrNull(i)
                    val hPrecip = hourly.precipitation?.getOrNull(i)
                    val hUv = hourly.uvIndex?.getOrNull(i)
                    val hCloud = hourly.cloudCover?.getOrNull(i)

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
                            rainProb = hRain,
                            apparentTemp = hApparent,
                            humidity = hHumidity,
                            windSpeed = hWindSpeed,
                            windGusts = hWindGusts,
                            precipitation = hPrecip,
                            uvIndex = hUv,
                            cloudCover = hCloud
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
                                    rainProb = hRain,
                                    apparentTemp = hourly.apparentTemperature?.getOrNull(h),
                                    humidity = hourly.relativeHumidity2m?.getOrNull(h),
                                    windSpeed = hourly.windSpeed10m?.getOrNull(h),
                                    windGusts = hourly.windGusts10m?.getOrNull(h),
                                    precipitation = hourly.precipitation?.getOrNull(h),
                                    uvIndex = hourly.uvIndex?.getOrNull(h),
                                    cloudCover = hourly.cloudCover?.getOrNull(h)
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
                        hourlyList = dayHourlyList,
                        uvIndexMax = daily.uvIndexMax?.getOrNull(i),
                        precipitationSum = daily.precipitationSum?.getOrNull(i)
                    )
                )
            }
        }

        return Pair(hourlyItems, dailyItems)
    }
}
