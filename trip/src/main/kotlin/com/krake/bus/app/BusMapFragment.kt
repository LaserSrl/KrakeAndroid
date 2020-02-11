package com.krake.bus.app

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import com.krake.bus.map.MapModifier
import com.krake.bus.model.BusCenterItem
import com.krake.bus.model.BusStop
import com.krake.bus.provider.PinDropListener
import com.krake.core.app.ContentItemMapModelFragment
import com.krake.core.map.MarkerCreator
import com.krake.core.model.ContentItemWithLocation
import com.krake.trip.R


/**
 * Created by antoniolig on 11/04/2017.
 */
open class BusMapFragment : ContentItemMapModelFragment(),
        GoogleMap.OnMarkerDragListener,
        MapModifier {

    var googleMap: GoogleMap? = null
    var lastCircle: Circle? = null
    var coordinatesListener: PinDropListener? = null
    private var currentBusMarker: Marker? = null
    private var lastRadius: Double = 0.0

    override fun onAttach(activity: Context)
    {
        super.onAttach(activity)
        if (activity is PinDropListener) {
            coordinatesListener = activity
        } else {
            throw RuntimeException("Your Activity must implement ${PinDropListener::class.java.simpleName}")
        }
    }

    override fun onDetach() {
        super.onDetach()
        coordinatesListener = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mMapManager.getMapAsync { googleMap ->
            this.googleMap = googleMap
            googleMap.setOnMarkerDragListener(this)
        }
    }

    override fun drawCircle(center: LatLng, radius: Double, drawMarker: Boolean) {
        val googleMap = googleMap
        val activity = activity
        if (googleMap != null && activity != null) {
            lastRadius = radius
            val circleOptions = CircleOptions()
                    .center(center)
                    .radius(radius)
                    .fillColor(ContextCompat.getColor(activity, R.color.otp_map_circle_fill_color))
                    .strokeColor(ContextCompat.getColor(activity, R.color.otp_map_circle_stroke_color))
                    .strokeWidth(resources.getDimensionPixelSize(R.dimen.otp_map_circle_stroke_width).toFloat())

            lastCircle?.remove()
            lastCircle = googleMap.addCircle(circleOptions)

            if (drawMarker) {
                currentBusMarker?.remove()
                val item = BusCenterItem(center)
                val options = MarkerCreator.shared.createMarker(activity, item)
                options.draggable(true)
                currentBusMarker = googleMap.addMarker(options)
            }
        }
    }

    override fun loadItemsInMap(googleMap: GoogleMap, lazyList: List<ContentItemWithLocation>, cacheValid: Boolean)
    {
        lazyList.filterIsInstance(BusStop::class.java).forEach {
            it.isMainStop = true
        }
        super.loadItemsInMap(googleMap, lazyList, cacheValid)
        lastCircle?.center?.let {
            val latLngBounds = LatLngBounds.Builder().include(SphericalUtil.computeOffset(it, lastRadius, 0.0))
                    .include(SphericalUtil.computeOffset(it, lastRadius, 90.0))
                    .include(SphericalUtil.computeOffset(it, lastRadius, 180.0))
                    .include(SphericalUtil.computeOffset(it, lastRadius, 270.0)).build()

            val mapPadding = resources.getDimensionPixelSize(R.dimen.map_padding)
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, mapPadding)
            googleMap.setOnMapLoadedCallback {
                // Animate the camera when the map is loaded.
                googleMap.animateCamera(cameraUpdate)
            }
        }
    }

    override fun onMarkerDragStart(marker: Marker) {
        // Empty method.
    }

    override fun onMarkerDragEnd(marker: Marker) {
        coordinatesListener!!.onPinDropped(marker.position)
    }

    override fun onMarkerDrag(marker: Marker) {
        drawCircle(marker.position, lastRadius)
    }
}