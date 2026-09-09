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

sealed interface WeatherUIState {
    data object Loading : WeatherUIState
    data class Success(
        val cityName: String,
        val currentWeather: CurrentWeatherUI,
        val hourlyForecast: List<HourlyItem>,
        val dailyForecast: List<DailyItem>,
        val weatherCode: Int,
        val isDay: Boolean,
        val formattedDate: String
    ) : WeatherUIState
    data class PermissionDenied(val reason: String = "No pudimos acceder a tu ubicación") : WeatherUIState
    data class Error(val message: String) : WeatherUIState
}

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WeatherRepository()
    private val prefs = application.getSharedPreferences("weather_app_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow<WeatherUIState>(WeatherUIState.Loading)
    val uiState: StateFlow<WeatherUIState> = _uiState.asStateFlow()

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
                        sunsetTime = currentWeather.sunset
                    )
                }

                val finalCurrentWeather = currentWeather.copy(
                    recommendation = bioclimaticRecommendation,
                    advice = bioclimaticRecommendation.toClothingAdvice()
                )

                prefs.edit()
                    .putString("cached_lat", lat.toString())
                    .putString("cached_lon", lon.toString())
                    .putString("cached_name", cityName)
                    .apply()

                _uiState.value = WeatherUIState.Success(
                    cityName = cityName,
                    currentWeather = finalCurrentWeather,
                    hourlyForecast = forecast.first,
                    dailyForecast = forecast.second,
                    weatherCode = finalCurrentWeather.weatherCode,
                    isDay = finalCurrentWeather.isDay,
                    formattedDate = WeatherUtils.getFormattedCurrentDate()
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
                        sourceBadge = sourceBadge
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
}
