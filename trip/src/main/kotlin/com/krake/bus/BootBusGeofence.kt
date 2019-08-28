package com.krake.bus

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.krake.bus.app.BusStopGeofenceManager
import com.krake.core.permission.PermissionManager

class BootBusGeofence : BroadcastReceiver(), BusStopGeofenceManager.Listener {


    override fun onReceive(context: Context, intent: Intent) {

        Log.e("BOOT", "avviato")
        val geofence = BusStopGeofenceManager(context, this)

        if (PermissionManager.locationPermissionsGranted(context)) {
            geofence.restartAllGeofences()
        } else {
            geofence.removeAllGeofences()
        }
    }

    override fun geofenceChanged(identifier: String, enabled: Boolean, success: Boolean) {
    }

}