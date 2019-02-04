package com.krake.bus.map

import com.google.android.gms.maps.model.LatLng

/**
 * Created by antoniolig on 02/05/2017.
 */
interface MapModifier {
    fun drawCircle(center: LatLng, radius: Double, drawMarker: Boolean = false)
}