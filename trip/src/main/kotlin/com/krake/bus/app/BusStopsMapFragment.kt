package com.krake.bus.app

import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.krake.bus.model.BusPassage
import com.krake.bus.model.BusPassagesReceiver
import com.krake.bus.model.BusStop
import com.krake.bus.model.BusStopsReceiver
import com.krake.core.app.ContentItemMapModelFragment
import com.krake.core.data.DataModel
import com.krake.core.model.ContentItemWithLocation
import com.krake.core.model.identifierOrStringIdentifier
import com.krake.trip.R

/**
 * Created by antoniolig on 27/04/2017.
 */
class BusStopsMapFragment : ContentItemMapModelFragment(),
        BusStopsReceiver,
        BusPassagesReceiver, PatternPolylineTask.Listener {

    private var currentPassage: BusPassage? = null
    private val patternPolylineTask: PatternPolylineTask by lazy { PatternPolylineTask(context ?:
            throw IllegalArgumentException("The context mustn't be null."), this) }
    private var polyline: Polyline? = null

    override fun onPassageChosen(passage: BusPassage) {
        currentPassage = passage

        currentPassage?.pattern?.let { patternPolylineTask.loadKML(it.stringIdentifier) }

        polyline?.remove()
    }

    override fun loadItemsInMap(googleMap: GoogleMap, lazyList: List<ContentItemWithLocation>, cacheValid: Boolean)
    {
        val selectedId = orchardComponentModule.recordStringIdentifier
        lazyList.filterIsInstance(BusStop::class.java).forEach {
            it.isMainStop = it.identifierOrStringIdentifier == selectedId
        }
        super.loadItemsInMap(googleMap, lazyList, cacheValid)
    }

    override fun onBusStopsReceived(stops: List<BusStop>) {
        var accepted = false
        val stringId = orchardComponentModule.recordStringIdentifier
        onDataModelChanged(DataModel(stops.filter {
            accepted = accepted || it.identifierOrStringIdentifier == stringId
            accepted
        }, true))
    }

    override fun onDestroy() {
        super.onDestroy()
        patternPolylineTask.release()
    }

    override fun onPatternPolylineLoaded(patternID: String, polyline: List<LatLng>) {
        if (patternID == currentPassage?.pattern?.stringIdentifier) {
            val line = PolylineOptions()
                    .addAll(polyline)
                    .width(10f)
                    .color(ResourcesCompat.getColor(resources,
                                                    R.color.itinerary_step_color_transit,
                                                    null))

            mMapManager.getMapAsync { this.polyline = it.addPolyline(line) }
        }
    }
}