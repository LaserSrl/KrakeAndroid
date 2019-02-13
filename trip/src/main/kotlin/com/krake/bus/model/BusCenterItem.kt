package com.krake.bus.model

import android.content.Context
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.krake.core.image.BitmapGenerator
import com.krake.core.map.MarkerConfiguration
import com.krake.core.map.MarkerKeyProvider
import com.krake.core.map.image.DefaultMarkerBitmapGenerator
import com.krake.trip.R

/**
 * Created by antoniolig on 27/04/2017.
 */
class BusCenterItem(val latLng: LatLng) : MarkerConfiguration, MarkerKeyProvider {

    override fun markerPosition(): LatLng = this.latLng

    override fun markerTitle(): String? = null

    override fun markerSubtitle(): String? = null

    override fun markerBitmapGenerator(context: Context): BitmapGenerator {
        return DefaultMarkerBitmapGenerator(context,
                color = obtainColor(context))
    }

    @ColorInt private fun obtainColor(context: Context): Int =
            ContextCompat.getColor(context, R.color.otp_map_center_item_color)

    override fun provideMarkerKey(context: Context): String =
            BusCenterItem::class.java.name + obtainColor(context)
}