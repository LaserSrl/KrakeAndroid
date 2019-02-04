package com.krake.core.map.manager

import android.support.v4.app.Fragment
import com.google.android.gms.maps.MapView

/**
 * Implementation of [MapViewManager] that hosts the map instance in a [Fragment].
 *
 * @param mapViewInit lazy initializer of the [MapView].
 */
class FragmentMapManager(fragment: Fragment, private val mapViewInit: (Fragment) -> MapView) : MapViewManager<Fragment>(fragment) {

    override fun createMapView(host: Fragment) = mapViewInit(host)

    override fun canDispatchCallback(host: Fragment) = host.isAdded
}