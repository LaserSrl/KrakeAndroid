package com.krake.bus.app

import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import java.util.concurrent.TimeUnit
import com.google.android.gms.maps.model.LatLng
import android.os.SystemClock
import android.view.animation.AccelerateDecelerateInterpolator
import com.krake.trip.R


/**
 * Created by antoniolig on 27/04/2017.
 */
class BusStopsMapFragment : ContentItemMapModelFragment(),
        BusStopsReceiver,
        BusPassagesReceiver, PatternPolylineTask.Listener {

    @BundleResolvable
    lateinit var busComponentModule: BusComponentModule

    private var currentPassage: BusPassage? = null
    private val patternPolylineTask: PatternPolylineTask by lazy {
        PatternPolylineTask(this)
    }
    private var polyline: Polyline? = null
    private var selectedStop: ContentItemWithLocation? = null

    private val busMovementProvider by lazy {
        busComponentModule.busMovementProvider?.newInstance()
    }
    private val busMovementRangeMillis
    get() = TimeUnit.SECONDS.toMillis(busMovementProvider!!.getRefreshPeriod(activity!!).toLong())

    private val busMovementHandler = Handler(Looper.getMainLooper())
    private var currentBusMarker: Marker? = null
    private val markerMovementHandler = Handler()
    private var busMovementTask: com.krake.core.thread.AsyncTask<*>? = null

    override fun onPassageChosen(passage: BusPassage) {
        stopBusLocationTracking()

        currentPassage = passage

        currentPassage?.pattern?.let { patternPolylineTask.loadKML(it.stringIdentifier) }

        polyline?.remove()

        scheduleBusLocationTracking(true)
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
            //TODO decomment this when the new feature must be released
            val lineColor = ResourcesCompat.getColor(resources, R.color.itinerary_step_color_transit, null)
//            val lineColor = currentPassage?.pattern?.busRoute?.color ?: ResourcesCompat.getColor(resources, R.color.itinerary_step_color_transit, null)

            val line = PolylineOptions()
                    .addAll(polyline)
                    .width(10f)
                    .color(lineColor)

            mMapManager.getMapAsync { this.polyline = it.addPolyline(line) }
        }
    }

    override fun onResume() {
        super.onResume()
        scheduleBusLocationTracking(true)
    }

    override fun onPause() {
        super.onPause()
        stopBusLocationTracking()
    }

    private fun scheduleBusLocationTracking(startNow: Boolean) {
        if (busMovementProvider != null && currentPassage != null && activity != null) {
            Log.d(TAG, "schedule bus current position retrieve action")

            if (startNow)
                retrieveCurrentBusLocation()

            busMovementHandler.postDelayed(::retrieveCurrentBusLocation, busMovementRangeMillis)
        } else {
            stopBusLocationTracking()
        }
    }

    private fun retrieveCurrentBusLocation() {
        Log.d(TAG, "call to provider")
        busMovementTask = async {
            busMovementProvider!!.provideCurrentBusPosition(activity!!, currentPassage!!)
        }.completed { position ->
            Log.d(TAG, "position retrieved")
            mMapManager.getMapAsync {
                if (currentBusMarker == null) {
                    val configuration = busMovementProvider!!.provideBusMarkerConfiguration(activity!!, currentPassage!!)
                    currentBusMarker = it.addMarker(MarkerCreator.shared.createMarker(activity!!, configuration))
                    currentBusMarker!!.position = position
                } else {
                    moveBusMarker(position)
                }
            }
            scheduleBusLocationTracking(false)
        }.error {
            Log.d(TAG, "error")
            it.printStackTrace()
            scheduleBusLocationTracking(false)
        }.load()
    }

    private fun stopBusLocationTracking() {
        Log.d(TAG, "stop schedule bus current position retrieve action")
        busMovementHandler.removeCallbacksAndMessages(null)
        markerMovementHandler.removeCallbacksAndMessages(null)
        busMovementTask?.cancel()
        busMovementTask = null
        currentBusMarker?.remove()
        currentBusMarker = null
    }

    private fun moveBusMarker(destination: LatLng) {
        val startPosition = currentBusMarker!!.position
        val start = SystemClock.uptimeMillis()
        val interpolator = AccelerateDecelerateInterpolator()
        val durationInMs = busMovementRangeMillis.toFloat()

        markerMovementHandler.post(object : Runnable {
            var elapsed = 0L
            var t = 0f
            var v = 0f

            override fun run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start
                t = elapsed / durationInMs
                v = interpolator.getInterpolation(t)

                val currentPosition = LatLng(
                    startPosition.latitude * (1 - t) + destination.latitude * t,
                    startPosition.longitude * (1 - t) + destination.longitude * t
                )

                currentBusMarker?.position = currentPosition

                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    markerMovementHandler.postDelayed(this, 16)
                } else {
                    currentBusMarker?.isVisible = true
                }
            }
        })
    }

    companion object {
        private val TAG = BusStopsMapFragment::class.java.simpleName
    }
}