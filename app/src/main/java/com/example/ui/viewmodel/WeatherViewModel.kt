package com.example.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.CurrentWeatherUI
import com.example.data.models.DailyItem
import com.example.data.models.GeocodingCityItem
import com.example.data.models.HourlyItem
import com.example.data.repository.WeatherRepository
import com.example.engine.BioclimaticClothingEngine
import com.example.engine.ClothingRecommendation
import com.example.utils.WeatherUtils
import com.example.widget.LocationTrackingManager
import com.example.widget.WeatherWidgetProvider
import com.example.widget.worker.WeatherWorkScheduler
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.example.utils.ClimateSimulationConfig
import com.example.utils.DevToolsTelemetry
import com.example.engine.BioclimaticMathEngine
import kotlin.math.roundToInt

sealed interface WeatherUIState {
    data object Loading : WeatherUIState
    data class Success(
        val cityName: String,
        val currentWeather: CurrentWeatherUI,
        val hourlyForecast: List<HourlyItem>,
        val dailyForecast: List<DailyItem>,
        val weatherCode: Int,
        val isDay: Boolean,
        val formattedDate: String,
        val isFromCache: Boolean = false,
        val lastUpdatedText: String? = null
    ) : WeatherUIState
    data class PermissionDenied(val reason: String = "No pudimos acceder a tu ubicación") : WeatherUIState
    data class Error(val message: String) : WeatherUIState
}

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WeatherRepository(application)
    private val prefs = application.getSharedPreferences("weather_app_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow<WeatherUIState>(WeatherUIState.Loading)
    val uiState: StateFlow<WeatherUIState> = _uiState.asStateFlow()

    private val _userPreferences = MutableStateFlow(loadUserPreferences())
    val userPreferences: StateFlow<com.example.data.models.UserPreferences> = _userPreferences.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<GeocodingCityItem>>(emptyList())
    val searchResults: StateFlow<List<GeocodingCityItem>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _isSearchBoxVisible = MutableStateFlow(false)
    val isSearchBoxVisible: StateFlow<Boolean> = _isSearchBoxVisible.asStateFlow()

    private val _isBackgroundLocationGranted = MutableStateFlow(
        LocationTrackingManager.hasBackgroundLocationPermission(application)
    )
    val isBackgroundLocationGranted: StateFlow<Boolean> = _isBackgroundLocationGranted.asStateFlow()

    private var searchJob: Job? = null
    private var currentGpsLat: Double? = null
    private var currentGpsLon: Double? = null

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "cached_name" || key == "last_weather_update_time") {
            val cachedLat = prefs.getString("cached_lat", null)?.toDoubleOrNull()
            val cachedLon = prefs.getString("cached_lon", null)?.toDoubleOrNull()
            val cachedName = prefs.getString("cached_name", null)
            val currentState = _uiState.value
            if (cachedLat != null && cachedLon != null && currentState is WeatherUIState.Success && currentState.cityName != cachedName) {
                loadWeather(cachedLat, cachedLon, cachedName)
            }
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
        checkBackgroundLocationPermission()
        restoreCachedLocationOrStart()
    }

    override fun onCleared() {
        super.onCleared()
        prefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    fun checkBackgroundLocationPermission() {
        _isBackgroundLocationGranted.value = LocationTrackingManager.hasBackgroundLocationPermission(getApplication())
        if (_isBackgroundLocationGranted.value || LocationTrackingManager.hasLocationPermission(getApplication())) {
            LocationTrackingManager.startLocationTracking(getApplication())
        }
    }

    fun onBackgroundLocationResult(granted: Boolean) {
        _isBackgroundLocationGranted.value = granted
        if (granted) {
            LocationTrackingManager.startLocationTracking(getApplication())
        }
    }

    private fun restoreCachedLocationOrStart() {
        val cachedLat = prefs.getString("cached_lat", null)?.toDoubleOrNull()
        val cachedLon = prefs.getString("cached_lon", null)?.toDoubleOrNull()
        val cachedName = prefs.getString("cached_name", null)

        if (cachedLat != null && cachedLon != null) {
            loadWeather(cachedLat, cachedLon, cachedName)
        } else {
            // Default to Madrid, Spain (matching the reference app behavior)
            loadWeather(40.4168, -3.7038, "Madrid, España")
        }
    }

    fun loadWeather(lat: Double, lon: Double, knownCityName: String? = null) {
        viewModelScope.launch {
            _uiState.value = WeatherUIState.Loading
            try {
                // Save cache
                val (cityName, currentWeather, forecast) = repository.fetchWeather(lat, lon, knownCityName)
                val todayForecast = forecast.second.firstOrNull()

                // Utiliza la recomendación de la arquitectura híbrida (Gemini Flash + Fallback Kotlin)
                val bioclimaticRecommendation = currentWeather.recommendation ?: withContext(Dispatchers.Default) {
                    BioclimaticClothingEngine.calculate(
                        currentTemp = currentWeather.temp.toDouble(),
                        currentHumidity = currentWeather.humidity,
                        currentWindSpeed = currentWeather.windSpeed.toDouble(),
                        currentWindGusts = forecast.first.firstOrNull()?.windGusts ?: (currentWeather.windSpeed.toDouble() * 1.35),
                        currentApparentTemp = currentWeather.feelsLike,
                        currentRainProb = currentWeather.rainProb,
                        currentPrecipitation = forecast.first.firstOrNull()?.precipitation ?: 0.0,
                        currentUvIndex = todayForecast?.uvIndexMax,
                        currentCloudCover = forecast.first.firstOrNull()?.cloudCover,
                        isDay = currentWeather.isDay,
                        hourlyItems = forecast.first,
                        dailyMaxUv = todayForecast?.uvIndexMax,
                        dailyPrecipSum = todayForecast?.precipitationSum,
                        sunsetTime = currentWeather.sunset,
                        preferences = _userPreferences.value
                    )
                }

                val finalCurrentWeather = currentWeather.copy(
                    recommendation = bioclimaticRecommendation,
                    advice = bioclimaticRecommendation.toClothingAdvice(),
                    rainRisk = bioclimaticRecommendation.rainRisk
                )

                prefs.edit()
                    .putString("cached_lat", lat.toString())
                    .putString("cached_lon", lon.toString())
                    .putString("cached_name", cityName)
                    .putInt("cached_temp", finalCurrentWeather.temp)
                    .apply()

                DevToolsTelemetry.updateCacheMetrics(
                    lastRefreshMs = System.currentTimeMillis(),
                    cachedLat = lat,
                    cachedLon = lon,
                    cachedName = cityName,
                    gpsLat = currentGpsLat ?: lat,
                    gpsLon = currentGpsLon ?: lon,
                    cachedTemp = finalCurrentWeather.temp.toDouble(),
                    currentTemp = finalCurrentWeather.temp.toDouble()
                )

                _uiState.value = WeatherUIState.Success(
                    cityName = cityName,
                    currentWeather = finalCurrentWeather,
                    hourlyForecast = forecast.first,
                    dailyForecast = forecast.second,
                    weatherCode = finalCurrentWeather.weatherCode,
                    isDay = finalCurrentWeather.isDay,
                    formattedDate = WeatherUtils.getFormattedCurrentDate(),
                    isFromCache = finalCurrentWeather.isFromCache,
                    lastUpdatedText = finalCurrentWeather.lastUpdatedTime
                )

                // Sync with Home Screen Widget instantly
                try {
                    val tempMax = todayForecast?.maxTemp ?: finalCurrentWeather.temp
                    val tempMin = todayForecast?.minTemp ?: finalCurrentWeather.temp

                    val rec = finalCurrentWeather.recommendation
                    val isGroq = rec?.source == com.example.engine.RecommendationSource.AI_BIOCLIMATIC ||
                            rec?.source == com.example.engine.RecommendationSource.GEMINI_AI
                    val sourceBadge = if (isGroq) "✨ Groq" else "⚙️ Local"

                    val (clothingIcon, clothingSummary) = WeatherWidgetProvider.getConciseClothingSummary(
                        temp = finalCurrentWeather.temp.toDouble(),
                        apparentTemp = finalCurrentWeather.feelsLike,
                        windSpeed = finalCurrentWeather.windSpeed.toDouble(),
                        rainProb = finalCurrentWeather.rainProb,
                        humidity = finalCurrentWeather.humidity,
                        recommendation = rec
                    )

                    val clothingHeadline = rec?.headline ?: finalCurrentWeather.advice.baseAdvice

                    WeatherWidgetProvider.updateAllWidgets(
                        context = getApplication(),
                        cityName = cityName,
                        temp = finalCurrentWeather.temp,
                        tempMax = tempMax,
                        tempMin = tempMin,
                        description = finalCurrentWeather.conditionDesc,
                        weatherCode = finalCurrentWeather.weatherCode,
                        isDay = finalCurrentWeather.isDay,
                        feelsLike = kotlin.math.round(finalCurrentWeather.feelsLike).toInt(),
                        rainProb = finalCurrentWeather.rainProb,
                        windSpeed = finalCurrentWeather.windSpeed,
                        clothingRecommendation = clothingHeadline,
                        clothingSummary = clothingSummary,
                        clothingIcon = clothingIcon,
                        sourceBadge = sourceBadge,
                        sunrise = finalCurrentWeather.sunrise,
                        sunset = finalCurrentWeather.sunset
                    )

                    // Ensure background periodic worker is scheduled
                    WeatherWorkScheduler.schedulePeriodicWeatherUpdate(getApplication())
                } catch (_: Exception) {
                    // Safe fallback
                }
            } catch (e: Exception) {
                _uiState.value = WeatherUIState.Error(
                    e.localizedMessage ?: "Error al obtener el clima. Por favor, reintenta."
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()

        if (query.trim().length < 2) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            _isSearching.value = true
            delay(300) // Debounce 300ms
            try {
                val results = repository.searchCities(query.trim())
                _searchResults.value = results
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun selectCity(city: GeocodingCityItem) {
        val lat = city.latitude ?: return
        val lon = city.longitude ?: return
        val name = city.displayBarrioCity

        _isSearchBoxVisible.value = false
        _searchQuery.value = ""
        _searchResults.value = emptyList()

        loadWeather(lat, lon, name)
    }

    fun toggleSearchBox() {
        _isSearchBoxVisible.value = !_isSearchBoxVisible.value
        if (!_isSearchBoxVisible.value) {
            _searchQuery.value = ""
            _searchResults.value = emptyList()
        }
    }

    fun onPermissionDenied() {
        _uiState.value = WeatherUIState.PermissionDenied()
    }

    @SuppressLint("MissingPermission")
    fun requestCurrentLocation(hasPermission: Boolean) {
        if (!hasPermission) {
            _uiState.value = WeatherUIState.PermissionDenied()
            return
        }

        checkBackgroundLocationPermission()
        LocationTrackingManager.startLocationTracking(getApplication())

        _uiState.value = WeatherUIState.Loading
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication<Application>())
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentGpsLat = location.latitude
                    currentGpsLon = location.longitude
                    loadWeather(location.latitude, location.longitude)
                } else {
                    // Fallback to Madrid if location is null on device
                    loadWeather(40.4168, -3.7038, "Madrid, España")
                }
            }.addOnFailureListener {
                loadWeather(40.4168, -3.7038, "Madrid, España")
            }
        } catch (e: Exception) {
            loadWeather(40.4168, -3.7038, "Madrid, España")
        }
    }

    fun reloadCurrentLocation() {
        val cachedLat = prefs.getString("cached_lat", null)?.toDoubleOrNull() ?: 40.4168
        val cachedLon = prefs.getString("cached_lon", null)?.toDoubleOrNull() ?: -3.7038
        val cachedName = prefs.getString("cached_name", null)
        loadWeather(cachedLat, cachedLon, cachedName)
    }

    /**
     * Módulo Sandbox / Climate Override:
     * Sobrescribe el clima en tiempo real para verificar renderizado Canvas,
     * estrategia de 3 capas, calzado y alertas bioclimáticas.
     */
    /**
     * Sobrescribe el clima en tiempo real para verificar renderizado Canvas,
     * estrategia de 3 capas, calzado, alertas bioclimáticas y fondo dinámico del Widget.
     */
    fun applyClimateOverride(
        temp: Double,
        humidity: Int,
        windSpeed: Double,
        cloudCover: Int,
        weatherCode: Int,
        isDay: Boolean,
        simulatedHour: Int = 14
    ) {
        viewModelScope.launch {
            val config = ClimateSimulationConfig(
                isActive = true,
                tempC = temp,
                humidity = humidity,
                windSpeedKmH = windSpeed,
                cloudCover = cloudCover,
                weatherCode = weatherCode,
                isDay = isDay,
                simulatedHour = simulatedHour
            )
            DevToolsTelemetry.setSimulation(config)

            val desc = WeatherUtils.getDescription(weatherCode)
            val iconType = WeatherUtils.getIconType(weatherCode, isDay)
            val feelsLike = temp - (windSpeed * 0.12) + (humidity * 0.04)

            val indicators = BioclimaticMathEngine.calculateIndicators(
                temp = temp,
                humidity = humidity,
                windSpeed = windSpeed,
                cloudCover = cloudCover,
                uvIndex = if (isDay) 4.5 else 0.0,
                isDay = isDay,
                hourlyItems = emptyList(),
                apparentTemp = feelsLike,
                sunsetTime = "20:30"
            )

            val recommendation = BioclimaticClothingEngine.calculate(
                currentTemp = temp,
                currentHumidity = humidity,
                currentWindSpeed = windSpeed,
                currentWindGusts = windSpeed * 1.35,
                currentApparentTemp = feelsLike,
                currentRainProb = if (weatherCode in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82, 95, 96, 99)) 85 else 10,
                currentPrecipitation = if (weatherCode in listOf(61, 63, 65, 95)) 4.5 else 0.0,
                currentUvIndex = if (isDay) 4.5 else 0.0,
                currentCloudCover = cloudCover,
                isDay = isDay,
                hourlyItems = emptyList(),
                dailyMaxUv = 5.0,
                dailyPrecipSum = 2.0,
                sunsetTime = "20:30",
                indicators = indicators
            )

            val simulatedWeatherUI = CurrentWeatherUI(
                temp = temp.roundToInt(),
                feelsLike = feelsLike,
                conditionDesc = "$desc (Simulado)",
                iconType = iconType,
                humidity = humidity,
                windSpeed = windSpeed.roundToInt(),
                rainProb = if (weatherCode in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82, 95, 96, 99)) 85 else 10,
                weatherCode = weatherCode,
                isDay = isDay,
                sunrise = "07:15",
                sunset = "20:45",
                daylightDuration = "13h 30m",
                advice = recommendation.toClothingAdvice(),
                airQuality = null,
                recommendation = recommendation
            )

            val simulatedHourly = (0..23).map { h ->
                val hTemp = (temp + (if (h in 12..16) 2 else if (h in 0..6) -3 else 0)).roundToInt()
                HourlyItem(
                    rawTime = String.format(java.util.Locale.US, "2026-09-09T%02d:00", h),
                    label = String.format(java.util.Locale.US, "%02d:00", h),
                    temp = hTemp,
                    weatherCode = weatherCode,
                    isDay = h in 7..20,
                    rainProb = simulatedWeatherUI.rainProb,
                    apparentTemp = feelsLike,
                    humidity = humidity,
                    windSpeed = windSpeed,
                    cloudCover = cloudCover
                )
            }

            val currentState = _uiState.value
            val existingCity = if (currentState is WeatherUIState.Success) {
                currentState.cityName.removeSuffix(" (Sandbox)")
            } else "Madrid"

            _uiState.value = WeatherUIState.Success(
                cityName = "$existingCity (Sandbox)",
                currentWeather = simulatedWeatherUI,
                hourlyForecast = simulatedHourly,
                dailyForecast = if (currentState is WeatherUIState.Success) currentState.dailyForecast else emptyList(),
                weatherCode = weatherCode,
                isDay = isDay,
                formattedDate = WeatherUtils.getFormattedCurrentDate()
            )

            val (clothingIcon, clothingSummary) = WeatherWidgetProvider.getConciseClothingSummary(
                temp = temp,
                apparentTemp = feelsLike,
                windSpeed = windSpeed,
                rainProb = simulatedWeatherUI.rainProb,
                humidity = humidity,
                recommendation = recommendation
            )

            // Sincronizar simulación con el Widget de escritorio
            WeatherWidgetProvider.updateAllWidgets(
                context = getApplication(),
                cityName = "$existingCity (Sandbox)",
                temp = temp.roundToInt(),
                tempMax = (temp + 3).roundToInt(),
                tempMin = (temp - 4).roundToInt(),
                description = "$desc (Simulado)",
                weatherCode = weatherCode,
                isDay = isDay,
                feelsLike = feelsLike.roundToInt(),
                rainProb = simulatedWeatherUI.rainProb,
                windSpeed = windSpeed.roundToInt(),
                clothingRecommendation = recommendation.headline,
                clothingSummary = clothingSummary,
                clothingIcon = clothingIcon,
                sourceBadge = "⚡ Sandbox",
                sunrise = "07:15",
                sunset = "20:45",
                simulatedHour = simulatedHour
            )
        }
    }

    fun resetClimateOverride() {
        DevToolsTelemetry.clearSimulation()
        prefs.edit().remove("sim_hour").apply()
        restoreCachedLocationOrStart()
    }

    fun forceClearCache() {
        prefs.edit()
            .remove("cached_lat")
            .remove("cached_lon")
            .remove("cached_name")
            .remove("cached_temp")
            .remove("cached_temp_max")
            .remove("cached_temp_min")
            .remove("cached_desc")
            .remove("cached_code")
            .remove("cached_is_day")
            .remove("cached_feels_like")
            .remove("cached_rain_prob")
            .remove("cached_wind_speed")
            .remove("cached_humidity")
            .remove("cached_clothing")
            .remove("cached_clothing_icon")
            .remove("cached_clothing_summary")
            .remove("cached_clothing_source")
            .apply()
        DevToolsTelemetry.log("Cache", "Caché de preferencias locales purgada por completo.")
        restoreCachedLocationOrStart()
    }

    fun toggleSimulatedOffline(enable: Boolean) {
        DevToolsTelemetry.setSimulatedOffline(enable)
    }

    private fun loadUserPreferences(): com.example.data.models.UserPreferences {
        val sensStr = prefs.getString("pref_sensitivity", com.example.data.models.ThermalSensitivity.NORMAL.name)
        val durStr = prefs.getString("pref_duration", com.example.data.models.OutingDuration.MEDIUM.name)
        val actStr = prefs.getString("pref_activity", com.example.data.models.ActivityType.WALKING.name)

        val sensitivity = try {
            com.example.data.models.ThermalSensitivity.valueOf(sensStr ?: com.example.data.models.ThermalSensitivity.NORMAL.name)
        } catch (_: Exception) {
            com.example.data.models.ThermalSensitivity.NORMAL
        }

        val duration = try {
            com.example.data.models.OutingDuration.valueOf(durStr ?: com.example.data.models.OutingDuration.MEDIUM.name)
        } catch (_: Exception) {
            com.example.data.models.OutingDuration.MEDIUM
        }

        val activity = try {
            com.example.data.models.ActivityType.valueOf(actStr ?: com.example.data.models.ActivityType.WALKING.name)
        } catch (_: Exception) {
            com.example.data.models.ActivityType.WALKING
        }

        val dep = if (prefs.contains("pref_departure_hour")) prefs.getInt("pref_departure_hour", -1).takeIf { it in 0..23 } else null
        val ret = if (prefs.contains("pref_return_hour")) prefs.getInt("pref_return_hour", -1).takeIf { it in 0..23 } else null

        return com.example.data.models.UserPreferences(
            sensitivity = sensitivity,
            duration = duration,
            activity = activity,
            departureHour = dep,
            returnHour = ret
        )
    }

    fun setScheduledWindow(departureHour: Int?, returnHour: Int?) {
        val updated = _userPreferences.value.copy(
            departureHour = departureHour,
            returnHour = returnHour
        )
        _userPreferences.value = updated
        prefs.edit()
            .apply {
                if (departureHour != null) putInt("pref_departure_hour", departureHour) else remove("pref_departure_hour")
                if (returnHour != null) putInt("pref_return_hour", returnHour) else remove("pref_return_hour")
            }
            .apply()
        recalculateCurrentWeatherWithPreferences()
    }

    fun clearScheduledWindow() {
        setScheduledWindow(null, null)
    }

    fun setOutingDuration(duration: com.example.data.models.OutingDuration) {
        val updated = _userPreferences.value.copy(duration = duration)
        _userPreferences.value = updated
        prefs.edit().putString("pref_duration", duration.name).apply()
        recalculateCurrentWeatherWithPreferences()
    }

    fun setThermalSensitivity(sensitivity: com.example.data.models.ThermalSensitivity) {
        val updated = _userPreferences.value.copy(sensitivity = sensitivity)
        _userPreferences.value = updated
        prefs.edit().putString("pref_sensitivity", sensitivity.name).apply()
        recalculateCurrentWeatherWithPreferences()
    }

    fun setActivityType(activity: com.example.data.models.ActivityType) {
        val updated = _userPreferences.value.copy(activity = activity)
        _userPreferences.value = updated
        prefs.edit().putString("pref_activity", activity.name).apply()
        recalculateCurrentWeatherWithPreferences()
    }

    private fun recalculateCurrentWeatherWithPreferences() {
        val currentState = _uiState.value
        if (currentState is WeatherUIState.Success) {
            val cw = currentState.currentWeather
            val todayForecast = currentState.dailyForecast.firstOrNull()
            val updatedRec = BioclimaticClothingEngine.calculate(
                currentTemp = cw.temp.toDouble(),
                currentHumidity = cw.humidity,
                currentWindSpeed = cw.windSpeed.toDouble(),
                currentWindGusts = currentState.hourlyForecast.firstOrNull()?.windGusts ?: (cw.windSpeed.toDouble() * 1.35),
                currentApparentTemp = cw.feelsLike,
                currentRainProb = cw.rainProb,
                currentPrecipitation = currentState.hourlyForecast.firstOrNull()?.precipitation ?: 0.0,
                currentUvIndex = todayForecast?.uvIndexMax,
                currentCloudCover = currentState.hourlyForecast.firstOrNull()?.cloudCover,
                isDay = cw.isDay,
                hourlyItems = currentState.hourlyForecast,
                dailyMaxUv = todayForecast?.uvIndexMax,
                dailyPrecipSum = todayForecast?.precipitationSum,
                sunsetTime = cw.sunset,
                preferences = _userPreferences.value
            )
            val updatedCw = cw.copy(
                recommendation = updatedRec,
                advice = updatedRec.toClothingAdvice(),
                rainRisk = updatedRec.rainRisk
            )
            _uiState.value = currentState.copy(currentWeather = updatedCw)
        }
    }
}
