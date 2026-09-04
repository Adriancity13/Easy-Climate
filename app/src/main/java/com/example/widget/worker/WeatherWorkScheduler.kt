package com.example.widget.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WeatherWorkScheduler {

    private const val PERIODIC_WORK_NAME = "weather_widget_periodic_update"
    private const val ONE_TIME_WORK_NAME = "weather_widget_immediate_update"

    /**
     * Schedules periodic background update (every 1 hour) with NetworkType.CONNECTED constraint.
     * Guaranteed to persist across app closes and reboots.
     */
    fun schedulePeriodicWeatherUpdate(context: Context, intervalHours: Long = 1) {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicWorkRequest = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(
                intervalHours.coerceAtLeast(1),
                TimeUnit.HOURS,
                15,
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )
            Log.d("WeatherWorkScheduler", "Periodic weather work scheduled ($intervalHours hr interval).")
        } catch (e: Exception) {
            Log.e("WeatherWorkScheduler", "Failed to schedule periodic weather update: ${e.message}", e)
        }
    }

    /**
     * Triggers a single one-time background update if connected to internet.
     */
    fun enqueueOneTimeSync(context: Context) {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val oneTimeRequest = OneTimeWorkRequestBuilder<WeatherUpdateWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                oneTimeRequest
            )
            Log.d("WeatherWorkScheduler", "One-time weather update enqueued.")
        } catch (e: Exception) {
            Log.e("WeatherWorkScheduler", "Failed to enqueue one-time weather update: ${e.message}", e)
        }
    }

    /**
     * Immediately triggers background update for newly detected location (threshold 300-500m).
     * Resolves new barrio and updates home screen widget instantly without waiting for the 1-hour cycle.
     */
    fun enqueueLocationUpdateSync(context: Context, newLat: Double, newLon: Double) {
        try {
            val inputData = androidx.work.workDataOf(
                "new_lat" to newLat,
                "new_lon" to newLon,
                "force_location_sync" to true
            )
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val locationRequest = OneTimeWorkRequestBuilder<WeatherUpdateWorker>()
                .setInputData(inputData)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "weather_location_change_sync",
                ExistingWorkPolicy.REPLACE,
                locationRequest
            )
            Log.d("WeatherWorkScheduler", "Immediate location change sync enqueued for ($newLat, $newLon)")
        } catch (e: Exception) {
            Log.e("WeatherWorkScheduler", "Failed to enqueue location change sync: ${e.message}", e)
        }
    }
}
