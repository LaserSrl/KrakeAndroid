package com.krake.bus.provider

import com.google.android.gms.maps.model.LatLng

/**
 * Created by antoniolig on 13/04/2017.
 */
interface PinDropListener {
    fun onPinDropped(latLng: LatLng)
}