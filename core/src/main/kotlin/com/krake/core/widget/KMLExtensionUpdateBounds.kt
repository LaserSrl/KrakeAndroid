package com.krake.core.widget

import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.data.Geometry
import com.google.maps.android.data.kml.*

/**
 * Created by joel on 06/10/16.
 */
fun KmlLayer.updateBounds(cameraBounds: LatLngBounds.Builder) {
    //Retrieve the first container in the KML layer
    for (container in this.containers) {
        container.updateBounds(cameraBounds)
    }
}

fun KmlContainer.updateBounds(cameraBounds: LatLngBounds.Builder) {
    if (hasContainers()) {
        containers.forEach { it.updateBounds(cameraBounds) }
    }

    if (hasPlacemarks()) {
        placemarks.forEach { it.geometry.updateBounds(cameraBounds) }
    }
}

fun Geometry<*>.updateBounds(cameraBounds: LatLngBounds.Builder) {
    when (this) {
        is KmlPolygon -> outerBoundaryCoordinates.forEach { cameraBounds.include(it) }
        is KmlLineString -> geometryObject.forEach { cameraBounds.include(it) }
        is KmlPoint -> cameraBounds.include(geometryObject)
        is KmlMultiGeometry -> geometryObject.forEach { it.updateBounds(cameraBounds) }
    }
}

