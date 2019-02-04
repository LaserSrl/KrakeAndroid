package com.krake.beacons

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * Generic object that create the default notification channel for the beacon notifications
 */
object BeaconNotificationChannel {

    const val BEACON_NOTIFICATION_CHANNEL = "beaconNotificationChannel"

    @TargetApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(context: Context): NotificationChannel {
        return NotificationChannel(BEACON_NOTIFICATION_CHANNEL, context.getString(R.string.beacon_notification_channel_name), NotificationManager.IMPORTANCE_HIGH)
    }
}

