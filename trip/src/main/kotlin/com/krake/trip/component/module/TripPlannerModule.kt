package com.krake.trip.component.module

import android.content.Context
import android.os.Bundle
import com.google.gson.Gson
import com.krake.core.component.base.ComponentModule
import com.krake.core.extension.getClass
import com.krake.core.extension.putClass
import com.krake.trip.OpenTripPlanTask
import com.krake.trip.TravelMode
import com.krake.trip.TripPlanRequest
import com.krake.trip.TripPlanViewModel

/**
 * Modulo usato per pianificazione una richiesta di viaggio.
 * @property request la richiesta del viaggio da effettuare
 * @property tripPlanTask in modo di pianificazione del viaggio. Attualmente solo OTP
 * Created by joel on 18/04/17.
 */

class TripPlannerModule : ComponentModule {

    var request: TripPlanRequest = TripPlanRequest()
        private set

    var tripPlanTask: Class<out TripPlanViewModel> = OpenTripPlanTask::class.java
        private set

    var travelModes: List<TravelMode> = listOf(TravelMode.CAR,
            TravelMode.TRANSIT,
            TravelMode.WALK,
            TravelMode.BICYCLE)
        private set

    fun request(tripPlanRequest: TripPlanRequest) = apply { request = tripPlanRequest }

    fun tripPlanTask(task: Class<out TripPlanViewModel>) = apply { tripPlanTask = task }

    fun travelModes(vararg modes: TravelMode) = apply { travelModes = modes.toList() }

    fun initialTravelMode(mode: TravelMode) = apply { request.travelMode = mode }

    override fun readContent(context: Context, bundle: Bundle) {
        bundle.getString(ARG_REQUEST)?.let {
            request = Gson().fromJson(it, TripPlanRequest::class.java)
        }
        val task: Class<out TripPlanViewModel>? = bundle.getClass(ARG_TRIP_PLAN, null)
        if (task != null)
            tripPlanTask = task

        bundle.getStringArray(ARG_TRAVEL_MODES)?.let {
            travelModes = it.map { TravelMode.valueOf(it) }
        }
    }

    override fun writeContent(context: Context): Bundle {
        val bundle = Bundle()

        bundle.putString(ARG_REQUEST, Gson().toJson(request))
        bundle.putClass(ARG_TRIP_PLAN, tripPlanTask)
        bundle.putStringArray(ARG_TRAVEL_MODES, travelModes.map { it.toString() }.toTypedArray())

        return bundle
    }

    companion object {
        private const val ARG_REQUEST = "Request"
        private const val ARG_TRIP_PLAN = "TripPlan"
        private const val ARG_TRAVEL_MODES = "argTravelModes"
    }
}