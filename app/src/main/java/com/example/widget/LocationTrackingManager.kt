package com.example.widget

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.concurrent.TimeUnit

/**
 * Manages background location updates with FusedLocationProviderClient.
 * Configures a displacement threshold of 300 meters and routes updates
 * to LocationUpdatesBroadcastReceiver even when the app is closed.
 */
object LocationTrackingManager {

    private const val TAG = "LocationTrackingManager"
    private const val MIN_DISPLACEMENT_METERS = 300f // 300m threshold as specified

    private fun getLocationPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, LocationUpdatesBroadcastReceiver::class.java).apply {
            action = LocationUpdatesBroadcastReceiver.ACTION_LOCATION_UPDATE
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(context, 1001, intent, flags)
    }

    fun hasLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    fun hasBackgroundLocationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            hasLocationPermission(context)
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationTracking(context: Context) {
        if (!hasLocationPermission(context)) {
            Log.w(TAG, "Cannot start location tracking: foreground location permission not granted.")
            return
        }

        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                TimeUnit.MINUTES.toMillis(15)
            )
                .setMinUpdateDistanceMeters(MIN_DISPLACEMENT_METERS)
                .setMinUpdateIntervalMillis(TimeUnit.MINUTES.toMillis(5))
                .setMaxUpdateDelayMillis(TimeUnit.MINUTES.toMillis(15))
                .build()

            val pendingIntent = getLocationPendingIntent(context)
            fusedLocationClient.requestLocationUpdates(locationRequest, pendingIntent)
                .addOnSuccessListener {
                    Log.d(TAG, "Background location tracking successfully started with 300m threshold.")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to start location updates: ${e.message}", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in startLocationTracking: ${e.message}", e)
        }
    }

    fun stopLocationTracking(context: Context) {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val pendingIntent = getLocationPendingIntent(context)
            fusedLocationClient.removeLocationUpdates(pendingIntent)
            Log.d(TAG, "Background location tracking stopped.")
        } catch (e: Exception) {
            Log.e(TAG, "Error in stopLocationTracking: ${e.message}", e)
        }
    }
}
