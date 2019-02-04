package com.krake.core.map.manager

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.krake.core.permission.PermissionManager
import java.lang.ref.WeakReference

/**
 * Base implementation of [MapManager] that used the Google's [MapView].
 * This abstraction is needed to use different [Host] that will retain a map instance.
 *
 * @param Host type of the object that retains the map instance.
 */
abstract class MapViewManager<in Host>(host: Host) : MapManager {

    private val hostRef = WeakReference<Host>(host)
    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val host = hostRef.get() ?: throw NullPointerException("The host is null and the map can't be created.")

        mapView = createMapView(host)
        mapView.onCreate(savedInstanceState)
        // Access the GoogleMap instance the first time.
        mapView.getMapAsync { googleMap = it }
    }

    override fun onStart() = mapView.onStart()

    override fun onResume() = mapView.onResume()

    override fun onPause() = mapView.onPause()

    override fun onStop() = mapView.onStop()

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        googleMap?.let { googleMap ->
            if (PermissionManager.areGranted(mapView.context,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Disable location monitoring if permissions are granted.
                // This is necessary to avoid a common memory leak in MapView.
                googleMap.isMyLocationEnabled = false
            }
        }
        googleMap = null
        mapView.onDestroy()
    }

    override fun onLowMemory() = mapView.onLowMemory()

    override fun getMapAsync(block: (GoogleMap) -> Unit) = mapView.getMapAsync { map ->
        hostRef.get()?.let { host ->
            if (canDispatchCallback(host)) {
                block(map)
            }
        }
    }

    /**
     * Creates the [MapView] that will show the map.
     *
     * @param host the object that retains the map instance.
     */
    abstract fun createMapView(host: Host): MapView

    /**
     * Verifies that the asynchronous callback of the map can be dispatched.
     *
     * @param host the object that retains the map instance.
     * @return true if the host is in a valid state and the callback can be dispatched.
     */
    abstract fun canDispatchCallback(host: Host): Boolean
}
