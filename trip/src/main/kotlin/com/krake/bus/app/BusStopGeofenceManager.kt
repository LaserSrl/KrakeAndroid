package com.krake.bus.app

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.krake.bus.BusStopGeofenceReceiver
import com.krake.core.location.LocationRequirementsHelper
import com.krake.core.model.*
import com.krake.core.permission.PermissionListener
import com.krake.core.permission.PermissionManager
import com.krake.trip.R
import java.lang.ref.WeakReference

class BusStopGeofenceManager(activity: FragmentActivity, listener: Listener) : PermissionListener {

    private val appContext: Context
    private val geofencingClient: GeofencingClient
    private val radius: Float
    private var locationRequirementsHelper: LocationRequirementsHelper
    private var geofence: Geofence? = null
    private var contentItemForGeofence: ContentItemWithLocation? = null

    private val monitoredGeofences = mutableSetOf<String>()
    private val preferences: SharedPreferences

    private val weakListener = WeakReference(listener)
    private var listener: Listener?
        get() {
            return weakListener.get()
        }
        set(value) {}

    init {
        appContext = activity.applicationContext
        geofencingClient = LocationServices.getGeofencingClient(activity.applicationContext)
        radius = activity.resources.getInteger(R.integer.geofence_radius_meters).toFloat()

        locationRequirementsHelper = LocationRequirementsHelper(activity, this)
        locationRequirementsHelper.permissionManager.rationalMsg(activity.getString(R.string.error_geofence_permission_required))
        locationRequirementsHelper.create()

        val intent = Intent(activity, BusStopGeofenceReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().

        preferences = activity.applicationContext.getSharedPreferences(GeofencePrefName, Context.MODE_PRIVATE)

        monitoredGeofences.addAll(preferences.getStringSet(Monitored, mutableSetOf())!!)
    }

    fun canGeofence(contentItem: ContentItem): ContentItemWithLocation? {

        val mapPart = (contentItem as? ContentItemWithLocation)?.mapPart
        if (mapPart != null && mapPart.isMapValid &&
            (contentItem is RecordWithStringIdentifier || contentItem is RecordWithIdentifier)
        )
            return contentItem
        else
            return null
    }

    fun addGeofence(contentItem: ContentItemWithLocation) {
        val mapPart = contentItem.mapPart
        if (mapPart != null && mapPart.isMapValid &&
            (contentItem is RecordWithStringIdentifier || contentItem is RecordWithIdentifier)
        ) {
            contentItemForGeofence = contentItem
//TODO
            geofence = Geofence.Builder()
                .setRequestId(contentItem.identifierOrStringIdentifier)
                .setCircularRegion(
                    mapPart.latitude,
                    mapPart.longitude,
                    radius
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()
            locationRequirementsHelper.request(false, false)

        }
    }


    @SuppressLint("MissingPermission")
    override fun onPermissionsHandled(acceptedPermissions: Array<out String>) {
        if (PermissionManager.containLocationPermissions(acceptedPermissions)) {
            geofence?.let {

                val pending = PendingIntent.getBroadcast(
                    appContext,
                    0,
                    Intent(appContext, BusStopGeofenceReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT
                )

                geofencingClient.addGeofences(
                    GeofencingRequest.Builder()
                        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                        .addGeofence(geofence)
                        .build(),
                    pending
                )
                    .run {
                        addOnCompleteListener {

                            contentItemForGeofence?.let {
                                saveGeofenceInPrefs(it)
                            }
                            listener?.geofenceChanged(geofence!!.requestId, true, true)
                        }

                        addOnFailureListener {
                            listener?.geofenceChanged(geofence!!.requestId, true, false)
                        }
                    }
            }

        }
    }

    fun removeGeofence(contentItem: ContentItemWithLocation) {
        val mapPart = contentItem.mapPart
        if (mapPart != null && mapPart.isMapValid &&
            (contentItem is RecordWithStringIdentifier || contentItem is RecordWithIdentifier)
        ) {
            removeGeofence(contentItem.identifierOrStringIdentifier)
        }
    }

    private fun removeGeofence(sid: String) {
        geofencingClient.removeGeofences(listOf(sid)).run {
            addOnCompleteListener {
                removeGeofenceFromPrefs(sid)

                listener?.geofenceChanged(sid, false, true)
            }
            addOnFailureListener {
                listener?.geofenceChanged(sid, false, false)
            }
        }
    }

    fun isGeofenceMonitored(contentItem: ContentItemWithLocation): Boolean {
        if ((contentItem is RecordWithStringIdentifier || contentItem is RecordWithIdentifier)) {
            return monitoredGeofences.contains(contentItem.identifierOrStringIdentifier);
        }

        return false
    }

    private fun saveGeofenceInPrefs(contentItem: ContentItemWithLocation) {

        monitoredGeofences.add(geofence!!.requestId)
        preferences.edit {
            putStringSet(Monitored, monitoredGeofences)
            putString(contentItem.identifierOrStringIdentifier, contentItem.titlePartTitle ?: "")
        }
    }

    private fun removeGeofenceFromPrefs(identifier: String) {
        monitoredGeofences.remove(identifier)
        preferences.edit {
            putStringSet(Monitored, monitoredGeofences)
            remove(identifier)
        }
    }

    fun removeAllGeofences() {
        monitoredGeofences.forEach {
            removeGeofence(it)
        }
    }

    companion object {
        val GeofencePrefName = "BusGeofences"
        val Monitored = "Monitored"
    }

    interface Listener {
        fun geofenceChanged(identifier: String, enabled: Boolean, success: Boolean)
    }

}