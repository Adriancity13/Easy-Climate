package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.models.CurrentWeatherUI
import com.example.ui.components.AtmosphericWeatherBackground
import com.example.ui.components.BioclimaticRecommendationCard
import com.example.ui.components.DailyForecastAccordionList
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPill
import com.example.ui.components.GlassPillColor
import com.example.ui.components.GlassStrongCard
import com.example.ui.components.GlassStrongColor
import com.example.ui.components.HourlyForecastCarousel
import com.example.ui.components.LiveLocationPulse
import com.example.ui.components.WeatherConditionIcon
import com.example.utils.DevToolsTelemetry
import com.example.ui.viewmodel.WeatherUIState
import com.example.ui.viewmodel.WeatherViewModel
import com.example.utils.WeatherUtils

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val isSearchBoxVisible by viewModel.isSearchBoxVisible.collectAsState()
    val isBackgroundLocationGranted by viewModel.isBackgroundLocationGranted.collectAsState()
    var showBgPrompt by remember { mutableStateOf(true) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showDevToolsModal by remember { mutableStateOf(false) }

    // Foreground Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val isGranted = fineGranted || coarseGranted
        viewModel.requestCurrentLocation(isGranted)
    }

    // Background Permission launcher (Android 10+)
    val backgroundPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onBackgroundLocationResult(isGranted)
    }

    // Check if DevTools simulation is active
    val simulationConfig by DevToolsTelemetry.simulationConfig.collectAsState()
    val simHour = if (simulationConfig.isActive) simulationConfig.simulatedHour else null

    // Determine current weather code & isDay for dynamic background
    val (weatherCode, isDay) = when {
        simulationConfig.isActive -> {
            val resolvedIsDay = if (simHour != null) {
                WeatherUtils.isDaytime("07:15", "20:45", simulatedHour = simHour)
            } else {
                simulationConfig.isDay
            }
            Pair(simulationConfig.weatherCode, resolvedIsDay)
        }
        uiState is WeatherUIState.Success -> {
            val s = uiState as WeatherUIState.Success
            val astronomicalIsDay = WeatherUtils.isDaytime(
                s.currentWeather.sunrise,
                s.currentWeather.sunset,
                if (s.isDay) 1 else 0
            )
            Pair(s.weatherCode, astronomicalIsDay)
        }
        else -> {
            Pair(0, WeatherUtils.isDaytimeNow())
        }
    }

    val sunriseStr = (uiState as? WeatherUIState.Success)?.currentWeather?.sunrise
    val sunsetStr = (uiState as? WeatherUIState.Success)?.currentWeather?.sunset

    val isSunrise = remember(uiState, isDay, simHour) {
        if (isDay) {
            WeatherUtils.isSunrisePeriod(
                sunriseIsoOrTime = sunriseStr,
                simulatedHour = simHour
            )
        } else false
    }

    val isSunset = remember(uiState, isDay, simHour, isSunrise) {
        if (isDay && !isSunrise) {
            WeatherUtils.isSunsetPeriod(
                sunsetIsoOrTime = sunsetStr,
                simulatedHour = simHour
            )
        } else false
    }

    val gradientColors = remember(weatherCode, isDay, isSunset, isSunrise) {
        WeatherUtils.getGradient(weatherCode, isDay, isSunset = isSunset, isSunrise = isSunrise)
    }
    val sceneType = remember(weatherCode, isDay) {
        WeatherUtils.getSceneType(weatherCode, isDay)
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Dynamic Atmospheric Animated Background
        AtmosphericWeatherBackground(
            sceneType = sceneType,
            gradientColors = gradientColors,
            isDay = isDay,
            isSunrise = isSunrise
        )

        // Main App Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .widthIn(max = 680.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Header: Date in Spanish
            val dateText = (uiState as? WeatherUIState.Success)?.formattedDate ?: WeatherUtils.getFormattedCurrentDate()
            GlassPill(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .testTag("date_header_pill")
            ) {
                Text(
                    text = dateText,
                    color = Color.White.copy(alpha = 0.90f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }

            // 2. Top Location Bar & Action Buttons
            val locationName = when (val s = uiState) {
                is WeatherUIState.Success -> s.cityName
                is WeatherUIState.Loading -> "Detectando ubicación..."
                is WeatherUIState.PermissionDenied -> "Ubicación desactivada"
                is WeatherUIState.Error -> "El Tiempo"
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Location indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    LiveLocationPulse()
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = locationName,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("location_name_text")
                    )
                }

                // Action Buttons: My Location & Search Toggle
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // My Location Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(GlassPillColor, CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                            .clip(CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                val hasFine = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                                val hasCoarse = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasFine || hasCoarse) {
                                    viewModel.requestCurrentLocation(true)
                                } else {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            }
                            .testTag("my_location_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MyLocation,
                            contentDescription = "Mi ubicación",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Search Toggle Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(GlassPillColor, CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                            .clip(CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.toggleSearchBox()
                            }
                            .testTag("search_toggle_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Buscar ciudad",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 3. Expandable Search Box & Autocomplete Results
            AnimatedVisibility(
                visible = isSearchBoxVisible,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    // Search Bar Input
                    GlassStrongCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Buscar barrio, ciudad o municipio...",
                                        color = Color.White.copy(alpha = 0.65f),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                                    textStyle = TextStyle(
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    cursorBrush = SolidColor(Color.White),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("search_input_field")
                                )
                            }

                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.onSearchQueryChanged("") },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Limpiar búsqueda",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Autocomplete Search Results Dropdown
                    if (searchQuery.trim().length >= 2) {
                        Spacer(modifier = Modifier.height(6.dp))
                        GlassStrongCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(12.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                if (isSearching) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                } else if (searchResults.isNotEmpty()) {
                                    searchResults.forEachIndexed { index, city ->
                                        val hasBarrio = city.isBarrio && !city.barrio.isNullOrBlank()
                                        val primaryTitle = if (hasBarrio) city.barrio!! else (city.name ?: "")
                                        val subtitleParts = buildList {
                                            if (hasBarrio && !city.name.isNullOrBlank() && !city.name.equals(city.barrio, ignoreCase = true)) {
                                                add(city.name)
                                            } else if (!hasBarrio && !city.admin1.isNullOrBlank()) {
                                                add(city.admin1)
                                            }
                                            if (!city.country.isNullOrBlank()) {
                                                add(city.country)
                                            }
                                        }
                                        val secondarySubtitle = subtitleParts.joinToString(" · ")

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    viewModel.selectCity(city)
                                                }
                                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                                .testTag("search_result_item_$index")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = primaryTitle,
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (hasBarrio) {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "Barrio",
                                                            color = Color.White,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                            if (secondarySubtitle.isNotBlank()) {
                                                Text(
                                                    text = secondarySubtitle,
                                                    color = Color.White.copy(alpha = 0.70f),
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }

                                        if (index < searchResults.size - 1) {
                                            HorizontalDivider(
                                                color = Color.White.copy(alpha = 0.08f),
                                                thickness = 1.dp
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "No se encontraron barrios o municipios",
                                        color = Color.White.copy(alpha = 0.70f),
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Optional background location prompt banner for dynamic neighborhood updates
            if (showBgPrompt && !isBackgroundLocationGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NearMe,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Actualización de barrios al moverte",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Actualiza el widget en tiempo real si te desplazas más de 300m.",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .clickable {
                                    backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Activar",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(
                            onClick = { showBgPrompt = false },
                            modifier = Modifier
                                .size(24.dp)
                                .padding(start = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Cerrar sugerencia",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 4. Content Area Based on State
            when (val state = uiState) {
                is WeatherUIState.Loading -> {
                    LoadingContent(modifier = Modifier.weight(1f))
                }
                is WeatherUIState.PermissionDenied -> {
                    PermissionDeniedContent(
                        onRetry = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        onManualSearch = {
                            viewModel.toggleSearchBox()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                is WeatherUIState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = {
                            viewModel.loadWeather(40.4168, -3.7038, "Madrid, España")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                is WeatherUIState.Success -> {
                    MainWeatherContent(
                        weatherUI = state.currentWeather,
                        hourlyList = state.hourlyForecast,
                        dailyList = state.dailyForecast,
                        onDevToolsClicked = { showPinDialog = true },
                        onDurationSelected = { viewModel.setOutingDuration(it) },
                        onSensitivitySelected = { viewModel.setThermalSensitivity(it) },
                        onActivitySelected = { viewModel.setActivityType(it) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Diálogo de PIN de Seguridad para DevTools
        if (showPinDialog) {
            PinSecurityDialog(
                onDismissRequest = { showPinDialog = false },
                onPinSuccess = {
                    showPinDialog = false
                    showDevToolsModal = true
                }
            )
        }

        // Modal Completo de Consola de Administración y Diagnóstico
        if (showDevToolsModal) {
            val currentUi = (uiState as? WeatherUIState.Success)?.currentWeather
            DevToolsAdminModal(
                currentWeatherUI = currentUi,
                viewModel = viewModel,
                onDismissRequest = { showDevToolsModal = false }
            )
        }
    }
}

@Composable
private fun MainWeatherContent(
    weatherUI: CurrentWeatherUI,
    hourlyList: List<com.example.data.models.HourlyItem>,
    dailyList: List<com.example.data.models.DailyItem>,
    onDevToolsClicked: () -> Unit,
    onDurationSelected: (com.example.data.models.OutingDuration) -> Unit = {},
    onSensitivitySelected: (com.example.data.models.ThermalSensitivity) -> Unit = {},
    onActivitySelected: (com.example.data.models.ActivityType) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Hero Weather Card (Flattened single container, no nested matryoshka)
        GlassStrongCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (weatherUI.isFromCache) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Modo sin conexión · Datos en caché${if (!weatherUI.lastUpdatedTime.isNullOrBlank()) " (${weatherUI.lastUpdatedTime})" else ""}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Condition Icon
                WeatherConditionIcon(
                    iconType = weatherUI.iconType,
                    size = 64.dp,
                    tint = Color.White,
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                // Main Temperature
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${weatherUI.temp}",
                        color = Color.White,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-2).sp,
                        modifier = Modifier.testTag("temp_main_text")
                    )
                    Text(
                        text = "°",
                        color = Color.White,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Feels Like
                Text(
                    text = "Sensación ${weatherUI.feelsLike.toInt()}°",
                    color = Color.White.copy(alpha = 0.70f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                // Condition Description
                Text(
                    text = weatherUI.conditionDesc,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                // Recommendation Card (Estrategia de vestimenta & salud con modelo bioclimático)
                if (weatherUI.recommendation != null) {
                    BioclimaticRecommendationCard(
                        recommendation = weatherUI.recommendation,
                        modifier = Modifier.padding(top = 4.dp),
                        onDurationSelected = onDurationSelected,
                        onSensitivitySelected = onSensitivitySelected,
                        onActivitySelected = onActivitySelected
                    )
                } else {
                    // Fallback flat card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ESTRATEGIA DE VESTIMENTA",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Text(
                            text = weatherUI.advice.baseAdvice,
                            color = Color.White.copy(alpha = 0.90f),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // 2. Metrics Row (Lluvia, Viento, Humedad)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Lluvia
            GlassCard(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    WeatherConditionIcon(iconType = "cloud-rain", size = 20.dp, tint = Color.White)
                    Text(
                        text = "${weatherUI.rainProb}%",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Lluvia",
                        color = Color.White.copy(alpha = 0.70f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Viento
            GlassCard(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    WeatherConditionIcon(iconType = "wind", size = 20.dp, tint = Color.White)
                    Text(
                        text = "${weatherUI.windSpeed} km/h",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Viento",
                        color = Color.White.copy(alpha = 0.70f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Humedad
            GlassCard(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    WeatherConditionIcon(iconType = "droplet", size = 20.dp, tint = Color.White)
                    Text(
                        text = "${weatherUI.humidity}%",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Humedad",
                        color = Color.White.copy(alpha = 0.70f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        // 2.3 Indicador Mínimo de Calidad del Aire (AQI Europeo)
        weatherUI.airQuality?.let { aqi ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 14.dp, vertical = 7.dp)
                    .testTag("air_quality_pill"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(aqi.category.color, CircleShape)
                    )
                    Text(
                        text = "Calidad del aire: ${aqi.category.label}",
                        color = Color.White.copy(alpha = 0.90f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 2.5 Sol & Horas de Luz (Amanecer y Atardecer de Hoy)
        if (weatherUI.sunrise.isNotBlank() && weatherUI.sunset.isNotBlank() && weatherUI.sunrise != "--:--") {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sun_cycle_card"),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header: Sol y Horas de Luz
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            WeatherConditionIcon(
                                iconType = "sun",
                                size = 16.dp,
                                tint = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                text = "SOL Y HORAS DE LUZ",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        if (weatherUI.daylightDuration.isNotBlank()) {
                            Text(
                                text = "☀️ ${weatherUI.daylightDuration} sol",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Amanecer y Atardecer en fila limpia
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Amanecer
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WeatherConditionIcon(
                                iconType = "sunrise",
                                size = 20.dp,
                                tint = Color.White.copy(alpha = 0.85f)
                            )
                            Column {
                                Text(
                                    text = "Amanecer",
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal
                                )
                                Text(
                                    text = weatherUI.sunrise,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Atardecer
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WeatherConditionIcon(
                                iconType = "sunset",
                                size = 20.dp,
                                tint = Color.White.copy(alpha = 0.85f)
                            )
                            Column {
                                Text(
                                    text = "Atardecer",
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal
                                )
                                Text(
                                    text = weatherUI.sunset,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Hourly Forecast Carousel
        HourlyForecastCarousel(hourlyItems = hourlyList)

        // 4. 7-Day Forecast Accordions
        DailyForecastAccordionList(dailyItems = dailyList)

        // 5. Version & DevTools Discrete Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 28.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Easy-Climate v1.0.0 · DevTools ⚙️",
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onDevToolsClicked() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("devtools_footer_button")
            )
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.5.dp,
                modifier = Modifier.size(42.dp)
            )
            Text(
                text = "Consultando el clima de tu zona...",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PermissionDeniedContent(
    onRetry: () -> Unit,
    onManualSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(GlassPillColor, CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOff,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = "No pudimos acceder a tu ubicación",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Activa el permiso de ubicación o busca tu ciudad manualmente para ver el clima.",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .background(GlassStrongColor, RoundedCornerShape(999.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.30f), RoundedCornerShape(999.dp))
                    .clip(RoundedCornerShape(999.dp))
                    .clickable { onRetry() }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Reintentar ubicación",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Buscar ciudad manualmente",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { onManualSearch() }
                    .padding(4.dp)
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Text(
                text = "Error al cargar",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Box(
                modifier = Modifier
                    .background(GlassStrongColor, RoundedCornerShape(999.dp))
                    .clickable { onRetry() }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Reintentar",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
