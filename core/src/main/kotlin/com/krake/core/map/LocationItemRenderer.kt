package com.krake.core.map

import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.krake.core.extension.copyTo
import java.lang.ref.WeakReference

/**
 * Created by joel on 12/04/16.
 */
class LocationItemRenderer(mapFragment: Fragment, map: GoogleMap, clusterManager: ClusterManager<MarkerConfiguration>) :
        DefaultClusterRenderer<MarkerConfiguration>(mapFragment.activity, map, clusterManager) {

    private val mapFragment: WeakReference<Fragment> = WeakReference(mapFragment)

    override fun onBeforeClusterItemRendered(item: MarkerConfiguration, markerOptions: MarkerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions)
        val fragment = mapFragment.get()
        fragment?.activity?.let {
            val options = MarkerCreator.shared.createMarker(it, item)
            options.copyTo(markerOptions)
        }
    }
}