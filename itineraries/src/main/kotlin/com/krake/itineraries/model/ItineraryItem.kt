package com.krake.itineraries.model

import android.content.Context
import com.krake.core.model.ContentItemWithLocation

/**
 * Created by antoniolig on 17/05/2017.
 */
interface ItineraryItem : ContentItemWithLocation {
    var count: Int?

    override fun markerLabel(context: Context): String? {
        return count?.toString()
    }
}