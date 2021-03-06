package com.krake.bus

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.gson.Gson
import com.krake.bus.app.BusStopGeofenceManager
import com.krake.bus.app.GeofencePref
import com.krake.core.gcm.OrchardContentNotifier
import com.krake.trip.R

class BusStopGeofenceReceiver : BroadcastReceiver() {

    private val TAG = "BusStopReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {

            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences.firstOrNull()

            if (triggeringGeofences != null) {

                val prefs = context.applicationContext.getSharedPreferences(
                    BusStopGeofenceManager.GeofencePrefName,
                    Context.MODE_PRIVATE
                )

                val json = prefs.getString(triggeringGeofences.requestId, null)

                if (!json.isNullOrEmpty()) {

                    val geofence = Gson().fromJson(json, GeofencePref::class.java)
                    BusStopGeofenceManager(context, object : BusStopGeofenceManager.Listener {
                        override fun geofenceChanged(identifier: String, enabled: Boolean, success: Boolean) {
                        }
                    })
                        .removeGeofence(triggeringGeofences.requestId)

                    OrchardContentNotifier.showNotification(
                        context, String.format(context.getString(R.string.geofence_arriving_bus), geofence.name),
                        null,
                        null,
                        null,
                        null
                    )

                }
            }

        }
    }

}
