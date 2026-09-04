package com.example.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.widget.worker.WeatherWorkScheduler

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("BootCompletedReceiver", "Received broadcast action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            // Re-schedule and ensure background updates are active
            WeatherWorkScheduler.schedulePeriodicWeatherUpdate(context)

            // Re-start background location tracking if permission is granted
            LocationTrackingManager.startLocationTracking(context)
        }
    }
}
