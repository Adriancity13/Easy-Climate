package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.screens.WeatherScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.WeatherViewModel
import com.example.widget.worker.WeatherWorkScheduler

class MainActivity : ComponentActivity() {

  private val weatherViewModel: WeatherViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize background WorkManager periodic task for native widget
    WeatherWorkScheduler.schedulePeriodicWeatherUpdate(this)

    // Optimize for high refresh rate displays (120 Hz / 90 Hz)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
      try {
        val display = display
        val supportedModes = display?.supportedModes
        val maxRefreshMode = supportedModes?.maxByOrNull { it.refreshRate }
        if (maxRefreshMode != null) {
          window.attributes = window.attributes.apply {
            preferredDisplayModeId = maxRefreshMode.modeId
          }
        }
      } catch (_: Exception) {
        // Fallback gracefully on standard refresh rate
      }
    }

    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = androidx.compose.ui.graphics.Color.Transparent
        ) {
          WeatherScreen(viewModel = weatherViewModel)
        }
      }
    }
  }
}

