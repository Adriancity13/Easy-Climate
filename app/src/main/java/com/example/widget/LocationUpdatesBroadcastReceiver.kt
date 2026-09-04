package com.example.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import com.example.widget.worker.WeatherWorkScheduler
import com.google.android.gms.location.LocationResult

/**
 * Receives background location updates triggered by FusedLocationProviderClient.
 * Calculates displacement against the last cached location: if >= 300 meters,
 * immediately enqueues a background sync to resolve the new barrio and refresh the widget.
 */
class LocationUpdatesBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        Log.d(TAG, "Location broadcast received: ${intent.action}")

        if (!LocationResult.hasResult(intent)) {
            Log.d(TAG, "Intent does not contain LocationResult")
            return
        }

        val locationResult = LocationResult.extractResult(intent) ?: return
        val lastLocation = locationResult.lastLocation ?: return

        val newLat = lastLocation.latitude
        val newLon = lastLocation.longitude
        Log.d(TAG, "Detected location update: ($newLat, $newLon) accuracy: ${lastLocation.accuracy}m")

        val prefs = context.getSharedPreferences("weather_app_prefs", Context.MODE_PRIVATE)
        val cachedLat = prefs.getString("cached_lat", null)?.toDoubleOrNull()
        val cachedLon = prefs.getString("cached_lon", null)?.toDoubleOrNull()

        var significantMove = false
        if (cachedLat == null || cachedLon == null) {
            significantMove = true
        } else {
            val dist = FloatArray(1)
            Location.distanceBetween(cachedLat, cachedLon, newLat, newLon, dist)
            val distanceMeters = dist[0]
            Log.d(TAG, "Displacement since last recorded location: ${distanceMeters}m (Threshold: 300m)")
            if (distanceMeters >= 300f) {
                significantMove = true
            }
        }

        if (significantMove) {
            Log.d(TAG, "Displacement >= 300m! Enqueueing immediate micro-local weather sync for new barrio.")
            WeatherWorkScheduler.enqueueLocationUpdateSync(
                context = context,
                newLat = newLat,
                newLon = newLon
            )
        }
    }

    companion object {
        const val TAG = "LocationUpdatesReceiver"
        const val ACTION_LOCATION_UPDATE = "com.example.LOCATION_UPDATE_ACTION"
    }
}
