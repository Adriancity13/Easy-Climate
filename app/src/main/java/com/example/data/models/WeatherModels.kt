package com.example.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenMeteoWeatherResponse(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val current: CurrentWeatherData? = null,
    val hourly: HourlyWeatherData? = null,
    val daily: DailyWeatherData? = null
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherData(
    val time: String? = null,
    @Json(name = "temperature_2m") val temperature2m: Double? = null,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Int? = null,
    @Json(name = "apparent_temperature") val apparentTemperature: Double? = null,
    @Json(name = "precipitation_probability") val precipitationProbability: Int? = null,
    @Json(name = "weather_code") val weatherCode: Int? = null,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double? = null,
    @Json(name = "is_day") val isDay: Int? = 1
)

@JsonClass(generateAdapter = true)
data class HourlyWeatherData(
    val time: List<String>? = null,
    @Json(name = "temperature_2m") val temperature2m: List<Double>? = null,
    @Json(name = "weather_code") val weatherCode: List<Int>? = null,
    @Json(name = "is_day") val isDay: List<Int>? = null,
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int>? = null,
    @Json(name = "wind_speed_10m") val windSpeed10m: List<Double>? = null
)

@JsonClass(generateAdapter = true)
data class DailyWeatherData(
    val time: List<String>? = null,
    @Json(name = "weather_code") val weatherCode: List<Int>? = null,
    @Json(name = "temperature_2m_max") val temperature2mMax: List<Double>? = null,
    @Json(name = "temperature_2m_min") val temperature2mMin: List<Double>? = null,
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int>? = null,
    @Json(name = "wind_speed_10m_max") val windSpeed10mMax: List<Double>? = null,
    @Json(name = "apparent_temperature_max") val apparentTemperatureMax: List<Double>? = null,
    val sunrise: List<String>? = null,
    val sunset: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class GeocodingSearchResponse(
    val results: List<GeocodingCityItem>? = null
)

@JsonClass(generateAdapter = true)
data class GeocodingCityItem(
    val id: Long? = null,
    val name: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val country: String? = null,
    val admin1: String? = null
) {
    val fullDisplayName: String
        get() = buildString {
            append(name ?: "")
            if (!admin1.isNullOrBlank()) append(", $admin1")
            if (!country.isNullOrBlank()) append(", $country")
        }
}

@JsonClass(generateAdapter = true)
data class ReverseGeocodeResponse(
    val city: String? = null,
    val locality: String? = null,
    val principalSubdivision: String? = null,
    val countryName: String? = null
) {
    val displayName: String
        get() {
            val main = if (!city.isNullOrBlank()) city else if (!locality.isNullOrBlank()) locality else principalSubdivision
            return if (main != null) {
                if (!principalSubdivision.isNullOrBlank() && principalSubdivision != main) {
                    "$main, $principalSubdivision"
                } else if (!countryName.isNullOrBlank()) {
                    "$main, $countryName"
                } else {
                    main
                }
            } else {
                "Tu Ubicación"
            }
        }
}

// UI State Models
data class AdviceModifier(
    val icon: String,
    val title: String,
    val text: String
)

data class ClothingAdvice(
    val baseAdvice: String,
    val modifiers: List<AdviceModifier> = emptyList()
)

data class HourlyItem(
    val rawTime: String,
    val label: String,
    val temp: Int,
    val weatherCode: Int,
    val isDay: Boolean,
    val rainProb: Int
)

data class DailyItem(
    val dateStr: String,
    val dayName: String,
    val weatherCode: Int,
    val maxTemp: Int,
    val minTemp: Int,
    val rainProb: Int,
    val windSpeed: Int,
    val feelsLikeMax: Int,
    val description: String,
    val iconType: String,
    val sunrise: String = "--:--",
    val sunset: String = "--:--",
    val daylightDuration: String = "",
    val advice: ClothingAdvice,
    val hourlyList: List<HourlyItem>
)

data class CurrentWeatherUI(
    val temp: Int,
    val feelsLike: Double,
    val conditionDesc: String,
    val iconType: String,
    val humidity: Int,
    val windSpeed: Int,
    val rainProb: Int,
    val weatherCode: Int,
    val isDay: Boolean,
    val sunrise: String = "--:--",
    val sunset: String = "--:--",
    val daylightDuration: String = "",
    val advice: ClothingAdvice
)

data class LocationCache(
    val latitude: Double,
    val longitude: Double,
    val name: String
)
