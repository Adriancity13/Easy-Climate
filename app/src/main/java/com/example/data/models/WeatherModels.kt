package com.example.data.models

import androidx.compose.ui.graphics.Color
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
    @Json(name = "precipitation") val precipitation: Double? = null,
    @Json(name = "weather_code") val weatherCode: Int? = null,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double? = null,
    @Json(name = "wind_gusts_10m") val windGusts10m: Double? = null,
    @Json(name = "uv_index") val uvIndex: Double? = null,
    @Json(name = "cloud_cover") val cloudCover: Int? = null,
    @Json(name = "is_day") val isDay: Int? = 1
)

@JsonClass(generateAdapter = true)
data class HourlyWeatherData(
    val time: List<String>? = null,
    @Json(name = "temperature_2m") val temperature2m: List<Double>? = null,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: List<Int>? = null,
    @Json(name = "apparent_temperature") val apparentTemperature: List<Double>? = null,
    @Json(name = "weather_code") val weatherCode: List<Int>? = null,
    @Json(name = "is_day") val isDay: List<Int>? = null,
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int>? = null,
    @Json(name = "precipitation") val precipitation: List<Double>? = null,
    @Json(name = "wind_speed_10m") val windSpeed10m: List<Double>? = null,
    @Json(name = "wind_gusts_10m") val windGusts10m: List<Double>? = null,
    @Json(name = "uv_index") val uvIndex: List<Double>? = null,
    @Json(name = "cloud_cover") val cloudCover: List<Int>? = null
)

@JsonClass(generateAdapter = true)
data class DailyWeatherData(
    val time: List<String>? = null,
    @Json(name = "weather_code") val weatherCode: List<Int>? = null,
    @Json(name = "temperature_2m_max") val temperature2mMax: List<Double>? = null,
    @Json(name = "temperature_2m_min") val temperature2mMin: List<Double>? = null,
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int>? = null,
    @Json(name = "precipitation_sum") val precipitationSum: List<Double>? = null,
    @Json(name = "wind_speed_10m_max") val windSpeed10mMax: List<Double>? = null,
    @Json(name = "apparent_temperature_max") val apparentTemperatureMax: List<Double>? = null,
    @Json(name = "uv_index_max") val uvIndexMax: List<Double>? = null,
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
    val admin1: String? = null,
    val barrio: String? = null,
    val isBarrio: Boolean = false
) {
    val fullDisplayName: String
        get() = buildString {
            if (!barrio.isNullOrBlank()) {
                append(barrio)
                if (!name.isNullOrBlank() && !name.equals(barrio, ignoreCase = true)) {
                    append(", $name")
                }
                if (!country.isNullOrBlank()) append(", $country")
            } else {
                append(name ?: "")
                if (!admin1.isNullOrBlank()) append(", $admin1")
                if (!country.isNullOrBlank()) append(", $country")
            }
        }

    val displayBarrioCity: String
        get() {
            return if (!barrio.isNullOrBlank() && !name.isNullOrBlank() && !barrio.equals(name, ignoreCase = true)) {
                "$barrio, $name"
            } else if (!barrio.isNullOrBlank()) {
                barrio
            } else if (!name.isNullOrBlank()) {
                if (!admin1.isNullOrBlank() && !admin1.equals(name, ignoreCase = true)) "$name, $admin1" else name
            } else {
                "Ubicación"
            }
        }
}

@JsonClass(generateAdapter = true)
data class NominatimSearchResultItem(
    val place_id: Long? = null,
    val lat: String? = null,
    val lon: String? = null,
    @Json(name = "display_name") val displayName: String? = null,
    val name: String? = null,
    val type: String? = null,
    val address: NominatimAddress? = null
)

@JsonClass(generateAdapter = true)
data class NominatimReverseResponse(
    val place_id: Long? = null,
    val lat: String? = null,
    val lon: String? = null,
    @Json(name = "display_name") val displayName: String? = null,
    val name: String? = null,
    val address: NominatimAddress? = null
)

@JsonClass(generateAdapter = true)
data class NominatimAddress(
    val neighbourhood: String? = null,
    val quarter: String? = null,
    val suburb: String? = null,
    @Json(name = "city_district") val cityDistrict: String? = null,
    val district: String? = null,
    val borough: String? = null,
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val municipality: String? = null,
    val county: String? = null,
    val state: String? = null,
    val country: String? = null
) {
    /**
     * Extracts the most specific barrio/neighbourhood/district name
     */
    val barrioName: String?
        get() = neighbourhood ?: quarter ?: suburb ?: cityDistrict ?: district ?: borough

    /**
     * Extracts city/locality name
     */
    val cityName: String?
        get() = city ?: town ?: village ?: municipality ?: county

    /**
     * Combines barrio and city in format "Barrio, Ciudad" (e.g., "Delicias, Madrid" or "Casa de Campo, Madrid")
     */
    val displayBarrioAndCity: String
        get() {
            val b = barrioName
            val c = cityName
            return if (!b.isNullOrBlank() && !c.isNullOrBlank()) {
                if (!b.equals(c, ignoreCase = true)) {
                    "$b, $c"
                } else {
                    if (!state.isNullOrBlank() && !state.equals(c, ignoreCase = true)) "$c, $state" else c
                }
            } else if (!b.isNullOrBlank()) {
                if (!state.isNullOrBlank() && !state.equals(b, ignoreCase = true)) "$b, $state" else b
            } else if (!c.isNullOrBlank()) {
                if (!state.isNullOrBlank() && !state.equals(c, ignoreCase = true)) "$c, $state" else if (!country.isNullOrBlank()) "$c, $country" else c
            } else {
                state ?: country ?: "Tu Ubicación"
            }
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

// Air Quality API Models (Open-Meteo Air Quality)
@JsonClass(generateAdapter = true)
data class AirQualityResponse(
    val current: CurrentAirQualityData? = null,
    val hourly: HourlyAirQualityData? = null
)

@JsonClass(generateAdapter = true)
data class CurrentAirQualityData(
    val time: String? = null,
    @Json(name = "european_aqi") val europeanAqi: Int? = null
)

@JsonClass(generateAdapter = true)
data class HourlyAirQualityData(
    val time: List<String>? = null,
    @Json(name = "european_aqi") val europeanAqi: List<Int>? = null
)

enum class AirQualityCategory(
    val label: String,
    val color: Color
) {
    BUENA("Buena", Color(0xFF34D399)),
    MODERADA("Moderada", Color(0xFFFBBF24)),
    DEFICIENTE("Deficiente", Color(0xFFF87171))
}

data class AirQualityUI(
    val aqi: Int,
    val category: AirQualityCategory
) {
    val statusText: String
        get() = "Calidad del aire: ${category.label}"
}

// UI State Models
data class AdviceModifier(
    val icon: String,
    val title: String,
    val text: String
)

data class ClothingAdvice(
    val baseAdvice: String,
    val modifiers: List<AdviceModifier> = emptyList(),
    val timeSlotAlert: String? = null
)

data class HourlyItem(
    val rawTime: String,
    val label: String,
    val temp: Int,
    val weatherCode: Int,
    val isDay: Boolean,
    val rainProb: Int,
    val apparentTemp: Double? = null,
    val humidity: Int? = null,
    val windSpeed: Double? = null,
    val windGusts: Double? = null,
    val precipitation: Double? = null,
    val uvIndex: Double? = null,
    val cloudCover: Int? = null
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
    val hourlyList: List<HourlyItem>,
    val uvIndexMax: Double? = null,
    val precipitationSum: Double? = null
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
    val advice: ClothingAdvice,
    val airQuality: AirQualityUI? = null,
    val recommendation: com.example.engine.ClothingRecommendation? = null
)

data class LocationCache(
    val latitude: Double,
    val longitude: Double,
    val name: String
)
