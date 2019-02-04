package com.krake.contentcreation.model

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.krake.core.map.MarkerConfiguration
import com.krake.core.map.MarkerKeyProvider

/**
 * Created by antoniolig on 17/05/2017.
 */
class LocationSelectionItem(val latLng: LatLng) : MarkerConfiguration, MarkerKeyProvider {

    override fun markerPosition(): LatLng {
        return this.latLng
    }

    override fun markerTitle(): String? {
        return null
    }

    override fun markerSubtitle(): String? {
        return null
    }

    override fun provideMarkerKey(context: Context): String {
        return LocationSelectionItem::class.java.name
    }
}