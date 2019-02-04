package com.krake.core.map.manager

import android.os.Bundle
import com.google.android.gms.maps.GoogleMap

/**
 * Used to defines the map behavior related to the application's lifecycle.
 */
interface MapManager {

    /**
     * Creates the map instance.
     *
     * @param savedInstanceState optional [Bundle] with saved state.
     */
    fun onCreate(savedInstanceState: Bundle?)

    /**
     * Defines the behavior of the map when it's started.
     */
    fun onStart()

    /**
     * Defines the behavior of the map when it's started.
     */
    fun onResume()

    /**
     * Defines the behavior of the map when it's resu,ed.
     */
    fun onPause()

    /**
     * Defines the behavior of the map when it's stopped.
     */
    fun onStop()

    /**
     * Defines the behavior of the map when it's destroyed.
     */
    fun onDestroy()

    /**
     * Defines the behavior of the map when it's on low memory.
     */
    fun onLowMemory()

    /**
     * Permits the access to the [GoogleMap] instance asynchronously only when the map is loaded.
     * The implementation could verify the state of the host before obtaining the map instance.
     */
    fun getMapAsync(block: (GoogleMap) -> Unit)
}