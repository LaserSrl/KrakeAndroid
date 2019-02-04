package com.krake.itineraries.model

import com.krake.core.model.ContentItem
import com.krake.core.model.ContentItemWithLocation

/**
 * Created by joel on 22/02/17.
 */
@Suppress("UNCHECKED_CAST")
interface Itinerary : ContentItem {
    val puntiDiInteresseContentItems: List<*>

    val locationPoints: List<ContentItemWithLocation> get() = puntiDiInteresseContentItems as List<ContentItemWithLocation>
}