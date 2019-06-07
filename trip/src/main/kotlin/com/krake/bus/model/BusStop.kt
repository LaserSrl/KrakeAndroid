package com.krake.bus.model

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import com.krake.core.model.ContentItemWithLocation
import com.krake.core.model.MapPart
import com.krake.core.model.RecordWithStringIdentifier
import com.krake.trip.R

/**
 * Created by joel on 08/03/17.
 */
interface BusStop : ContentItemWithLocation, RecordWithStringIdentifier, MapPart {
    val id: String

    var isMainStop: Boolean
        get() = true
        set(value) {
            // Ignore the setter to avoid Realm transactions problems.
            Log.e(BusStop::class.java.simpleName, "to allow the selection on the item, " +
                    "you need to override 'isMainStop' with @Ignore")
        }

    var selectedReferencePassage: BusPassage?
        get() = null
        set(value) {
            // Ignore the setter to avoid Realm transactions problems.
            Log.e(
                BusStop::class.java.simpleName, "to allow the selection on the item, " +
                        "you need to override 'selectedReferencePassage' with @Ignore"
            )
        }


    val dist: Long?

    val lat: Double

    val name: String

    val lon: Double

    override val mapPart: MapPart? get() = this

    override val latitude: Double get() = lat

    override val longitude: Double get() = lon

    override val titlePartTitle: String? get() = name

    override val locationAddress: String? get() = null

    override val locationInfo: String? get() = null

    override fun markerColor(context: Context): Int =
        if (isMainStop) {
            selectedReferencePassage?.pattern?.busRoute?.color ?: super.markerColor(context)
        } else {
            ContextCompat.getColor(context, R.color.otp_map_default_item_color)
        }
}