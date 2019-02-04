package com.krake.trip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import com.krake.core.address.PlaceResult
import com.krake.core.component.annotation.BundleResolvable
import com.krake.core.component.base.ComponentManager
import com.krake.core.map.MapZoomSupport
import com.krake.core.view.MapUtils
import com.krake.trip.component.module.RouteModule

/**
 * Fragment che mostra i dati su mappa.
 * La visualizzaizone su mappa è indipendente dal tipo di pianificazione usata
 *
 * Created by joel on 20/04/17.
 */

class RouteMapFragment : SupportMapFragment() {

    private var mDisplayedStepMarker: Marker? = null
    private lateinit var mMapZoomSupport: MapZoomSupport

    @BundleResolvable
    lateinit var routeModule: RouteModule

    override fun onCreate(p0: Bundle?) {
        super.onCreate(p0)
        ComponentManager.resolveArguments(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        mMapZoomSupport = MapZoomSupport(view)

        getMapAsync { googleMap ->
            googleMap.clear()
            mDisplayedStepMarker = null

            MapUtils.styleMap(googleMap, activity!!)

            val steps = routeModule.route.steps
            var options = createOptionsFromPlace(steps.first().from)
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_partenza))
            options.anchor(0.5f, 1f)

            googleMap.addMarker(options)

            steps.forEach {
                val geometry = PolyUtil.decode(it.polyline)

                val line = PolylineOptions()
                line.addAll(geometry)
                line.width(10f)
                line.color(it.stepColor(context!!))
                googleMap.addPolyline(line)
            }

            options = createOptionsFromPlace(steps.last().to)

            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_traguardo))
            options.anchor(0.5f, 1f)
            googleMap.addMarker(options)

            val camera = animateCamera(routeModule.route.steps)

            if (camera != null)
                mMapZoomSupport.updateCamera(camera, googleMap)
        }

        return view
    }

    private fun createOptionsFromPlace(place: PlaceResult): MarkerOptions {
        val options = MarkerOptions()
        options.title(place.name)
        place.location?.let { options.position(LatLng(it.latitude, it.longitude)) }


        return options
    }

    fun zoom(bounds: LatLngBounds) {
        val camera = CameraUpdateFactory.newLatLngBounds(bounds, resources.getDimensionPixelSize(R.dimen.map_margins))
        getMapAsync {
            mMapZoomSupport.updateCamera(camera, it)
        }

    }

    private fun animateCamera(steps: Array<ComplexStep>): CameraUpdate? {
        val bounds = LatLngBounds.Builder()

        var includeAtLeastOnePoint = false
        for (leg in steps) {
            val geometry = PolyUtil.decode(leg.polyline)

            for (i in geometry.indices) {
                bounds.include(geometry[i])
                includeAtLeastOnePoint = true
            }
        }

        if (includeAtLeastOnePoint)
            return CameraUpdateFactory.newLatLngBounds(bounds.build(), resources.getDimensionPixelSize(R.dimen.map_margins))

        return null
    }
}
