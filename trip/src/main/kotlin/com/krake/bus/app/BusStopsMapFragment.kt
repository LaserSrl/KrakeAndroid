package com.krake.bus.app

import android.os.Handler
import android.os.Looper
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.krake.bus.component.module.BusComponentModule
import com.krake.bus.model.BusPassage
import com.krake.bus.model.BusPassagesReceiver
import com.krake.bus.model.BusStop
import com.krake.bus.model.BusStopsReceiver
import com.krake.core.app.ContentItemMapModelFragment
import com.krake.core.component.annotation.BundleResolvable
import com.krake.core.data.DataModel
import com.krake.core.map.MarkerCreator
import com.krake.core.model.ContentItemWithLocation
import com.krake.core.model.identifierOrStringIdentifier
import com.krake.core.thread.async
import com.krake.trip.R
import java.util.concurrent.TimeUnit

/**
 * Created by antoniolig on 27/04/2017.
 */
class BusStopsMapFragment : ContentItemMapModelFragment(),
        BusStopsReceiver,
        BusPassagesReceiver, PatternPolylineTask.Listener {

    @BundleResolvable
    lateinit var busComponentModule: BusComponentModule

    private var currentPassage: BusPassage? = null
    private val patternPolylineTask: PatternPolylineTask by lazy { PatternPolylineTask(context ?:
            throw IllegalArgumentException("The context mustn't be null."), this) }
    private var polyline: Polyline? = null
    private var selectedStop: ContentItemWithLocation? = null

    private val busMovementProvider by lazy {
        busComponentModule.busMovementProvider?.newInstance()
    }
    private val busMovementHandler = Handler(Looper.getMainLooper())
    private var currentBusMarker: Marker? = null

    override fun onPassageChosen(passage: BusPassage) {
        currentPassage = passage

        currentPassage?.pattern?.let { patternPolylineTask.loadKML(it.stringIdentifier) }

        polyline?.remove()

        scheduleBusLocationTracking()
    }

    override fun loadItemsInMap(googleMap: GoogleMap, lazyList: List<ContentItemWithLocation>, cacheValid: Boolean)
    {
        val selectedId = orchardComponentModule.recordStringIdentifier
        selectedStop = lazyList.first { it.identifierOrStringIdentifier == selectedId }
        lazyList.filterIsInstance(BusStop::class.java).forEach {
            it.isMainStop = it.identifierOrStringIdentifier == selectedId
        }
        super.loadItemsInMap(googleMap, lazyList, cacheValid)
    }

    override fun onBusStopsReceived(stops: List<BusStop>) {
        var accepted = false
        val stringId = orchardComponentModule.recordStringIdentifier
        if (!stops.isEmpty()) {
            onDataModelChanged(DataModel(stops.filter {
                accepted = accepted || it.identifierOrStringIdentifier == stringId
                accepted
            }, true))
        } else {
            selectedStop?.let {
                onDataModelChanged(DataModel(listOf(it), true))
            }
        }
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

    override fun onResume() {
        super.onResume()
        scheduleBusLocationTracking()
    }

    override fun onPause() {
        super.onPause()
        stopBusLocationTracking()
    }

    private fun scheduleBusLocationTracking() {
        if (busMovementProvider != null && currentPassage != null) {
            val seconds = TimeUnit.SECONDS.toMillis(busMovementProvider!!.getRefreshPeriod().toLong())
            val actionToPerform: () -> Unit = {
                async {
                    busMovementProvider!!.provideCurrentBusPosition(currentPassage!!)
                }.completed { configuration ->
                    mMapManager.getMapAsync {
                        currentBusMarker?.remove()
                        currentBusMarker = it.addMarker(MarkerCreator.shared.createMarker(activity!!, configuration))
                    }
                    scheduleBusLocationTracking()
                }.error {

                }.load()
            }
            busMovementHandler.postDelayed(actionToPerform, seconds)
        } else {
            stopBusLocationTracking()
        }
    }

    private fun stopBusLocationTracking() {
        busMovementHandler.removeCallbacksAndMessages(null)
    }
}