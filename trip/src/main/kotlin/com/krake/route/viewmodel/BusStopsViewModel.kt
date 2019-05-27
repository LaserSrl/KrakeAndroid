package com.krake.route.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.krake.core.network.RemoteClient
import com.krake.core.network.RemoteRequest
import com.krake.core.thread.AsyncTask
import com.krake.core.thread.async
import com.krake.route.model.OtpBusRoute
import com.krake.route.model.OtpBusStop
import com.krake.route.model.OtpStopTime
import com.krake.trip.R
import java.text.SimpleDateFormat
import java.util.*

class BusStopsViewModel : ViewModel() {
    private val gson = Gson()

    private var busStopsTask: AsyncTask<List<OtpBusStop>>? = null
    private val mutableBusStops = MutableLiveData<List<OtpBusStop>>()
    val busStops: LiveData<List<OtpBusStop>> = mutableBusStops

    private var stopTimesTask: AsyncTask<List<OtpStopTime>>? = null
    private val mutableStopTimes = MutableLiveData<List<OtpStopTime>>()
    val stopTimes: LiveData<List<OtpStopTime>> = mutableStopTimes

    private val mutableStatus = MutableLiveData<Status>()
    val status: LiveData<Status> = mutableStatus

    private val dateFormatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    init {
        mutableStatus.value = Idle
    }

    fun loadStopsByBusRoute(context: Context, routeId: String) {
        val url = String.format(context.getString(R.string.bus_stops_by_route_path), context.getString(R.string.open_trip_planner_base_url), routeId)

        mutableStatus.value = Loading

        busStopsTask = async {
            val request = RemoteRequest(url)
                .setMethod(RemoteRequest.Method.GET)

            val jsonResult = RemoteClient.client(RemoteClient.Mode.DEFAULT)
                .execute(request)
                .jsonArray()

            gson.fromJson(jsonResult, Array<OtpBusStop>::class.java).toList()

        }.completed {
            mutableBusStops.value = it
            mutableStatus.value = Idle
        }.error {
            mutableStatus.value = Error
        }.build()
        busStopsTask?.load()
    }

    fun loadBusTimesByDate(context: Context, stop: OtpBusStop, routeId: String, date: Date) {
        val url = String.format(context.getString(R.string.bus_stoptimes_by_stop_path), context.getString(R.string.open_trip_planner_base_url), stop.id, dateFormatter.format(date))

        mutableStatus.value = Loading

        stopTimesTask = async {
            val request = RemoteRequest(url)
                .setMethod(RemoteRequest.Method.GET)

            val jsonResult = RemoteClient.client(RemoteClient.Mode.DEFAULT)
                .execute(request)
                .jsonArray()?.filter {
                    (it.asJsonObject).get("pattern").asJsonObject.get("id").asString.startsWith(routeId)
                }?.map {
                    (it.asJsonObject).get("times").asJsonArray
                }?.flatten()

            gson.fromJson(gson.toJsonTree(jsonResult), Array<OtpStopTime>::class.java)
                .toList()
                .sortedBy {
                    it.scheduledDeparture
                }

        }.completed {
            mutableStopTimes.value = it
            mutableStatus.value = Idle
        }.error {
            mutableStatus.value = Error
        }.build()
        stopTimesTask?.load()
    }

    override fun onCleared() {
        super.onCleared()
        busStopsTask?.cancel()
        stopTimesTask?.cancel()
    }
}