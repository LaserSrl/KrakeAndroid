package com.krake.core.fetcher.content

import android.content.Context
import android.os.Trace
import android.util.Log
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer

/**
 * Implementation of [FetchableContent] that will pre-fetch the Google maps' packages.
 *
 * @param context application [Context] used to pre-fetch the maps' packages.
 */
class MapFetchableContent(private val context: Context) : FetchableContent {

    companion object {
        private val TAG = MapFetchableContent::class.java.simpleName
    }

    override fun fetch() {
        Trace.beginSection("preFetchMapPackages")
        Log.d(TAG, "pre-fetching the map packages...")
        // Initialize the map's components.
        MapsInitializer.initialize(context)

        // Create a fake MapView.
        val mapView = MapView(context)
        try {
            // Force the map creation.
            // It will throw exception due to map behavior.
            mapView.onCreate(null)
        } catch (ignored: Exception) {
            // The exception is ignored because it will be thrown every time.
        }
        Log.d(TAG, "pre-fetching finished.")
        Trace.endSection()
    }
}