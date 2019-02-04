package com.krake.core.model

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import java.util.*

/**
 * Created by joel on 28/02/17.
 */

interface MapPart {
    val longitude: Double
    val latitude: Double
    val locationInfo: String?
    val locationAddress: String?

    val isMapValid get() = (latitude.equals(0.0) || longitude.equals(0.0)).not()

    val latLng get() = LatLng(latitude, longitude)

    val mapSourceFileMediaParts: List<*> get() = LinkedList<MediaPart>()

    val kml get() = mapSourceFileMediaParts.filter { (it as MediaPart).mediaUrl?.endsWith("kml") ?: false }.firstOrNull() as? MediaPart

    val location: Location
        get() {
            val location = Location("")

            location.latitude = latitude
            location.longitude = longitude
            return location
        }
}
