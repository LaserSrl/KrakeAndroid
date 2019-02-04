package com.krake.trip.component.module

import android.content.Context
import android.os.Bundle
import com.google.gson.Gson
import com.krake.core.component.base.ComponentModule
import com.krake.core.extension.getClass
import com.krake.core.extension.putClass
import com.krake.trip.OpenTripPlanTask
import com.krake.trip.TripPlanRequest
import com.krake.trip.TripPlanViewModel

/**
 * Modulo usato per pianificazione una richiesta di viaggio.
 * @property request la richiesta del viaggio da effettuare
 * @property tripPlanTask in modo di pianificazione del viaggio. Attualmente solo OTP
 * Created by joel on 18/04/17.
 */

class TripPlannerModule : ComponentModule {

    companion object {
        private const val ARG_REQUEST = "Request"
        private const val ARG_TRIP_PLAN = "TripPlan"
    }

    var request: TripPlanRequest = TripPlanRequest()
        private set

    var tripPlanTask: Class<out TripPlanViewModel> = OpenTripPlanTask::class.java
        private set

    fun request(tripPlanRequest: TripPlanRequest) = apply { request = tripPlanRequest }

    fun tripPlanTask(task: Class<out TripPlanViewModel>) = apply { tripPlanTask = task }

    override fun readContent(context: Context, bundle: Bundle) {

        bundle.getString(ARG_REQUEST)?.let {
            request = Gson().fromJson(it, TripPlanRequest::class.java)
        }
        val task: Class<out TripPlanViewModel>? = bundle.getClass(ARG_TRIP_PLAN, null)
        if (task != null)
            tripPlanTask = task
    }

    override fun writeContent(context: Context): Bundle {

        val bundle = Bundle()

        bundle.putString(ARG_REQUEST, Gson().toJson(request))
        bundle.putClass(ARG_TRIP_PLAN, tripPlanTask)

        return bundle
    }


}