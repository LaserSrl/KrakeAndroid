package com.krake.core.address

import com.google.android.gms.maps.model.LatLngBounds

/**
 * Created by antoniolig on 03/05/2017.
 */
interface SearchBoundsProvider {
    val searchBounds: LatLngBounds?
}