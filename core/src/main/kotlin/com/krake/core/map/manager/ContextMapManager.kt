package com.krake.core.map.manager

import android.content.Context
import com.google.android.gms.maps.MapView

/**
 * Implementation of [MapViewManager] that hosts the map instance in a [Context].
 *
 * @param mapViewInit lazy initializer of the [MapView].
 */
class ContextMapManager(context: Context, private val mapViewInit: (Context) -> MapView) : MapViewManager<Context>(context) {

    override fun createMapView(host: Context) = mapViewInit(host)

    override fun canDispatchCallback(host: Context) = true
}