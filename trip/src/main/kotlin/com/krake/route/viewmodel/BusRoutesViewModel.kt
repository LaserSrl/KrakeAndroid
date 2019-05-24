package com.krake.route.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.krake.core.network.RemoteClient
import com.krake.core.network.RemoteRequest
import com.krake.core.thread.AsyncTask
import com.krake.core.thread.async
import com.krake.route.model.OtpBusRoute
import com.krake.trip.R

class BusRoutesViewModel : ViewModel() {
    private val gson = Gson()

    private var busRoutesTask: AsyncTask<List<OtpBusRoute>>? = null
    private val mutableBusRoutes = MutableLiveData<List<OtpBusRoute>>()
    val busRoutes: LiveData<List<OtpBusRoute>> = mutableBusRoutes

    private val mutableStatus = MutableLiveData<Status>()
    val status: LiveData<Status> = mutableStatus

    init {
        mutableStatus.value = Idle
    }

    fun loadBusRoutes(context: Context) {
        val url = String.format(context.getString(R.string.bus_routes_path), context.getString(R.string.open_trip_planner_base_url))

        mutableStatus.value = Loading

        busRoutesTask = async {
            val request = RemoteRequest(url)
                .setMethod(RemoteRequest.Method.GET)

            val jsonResult = RemoteClient.client(RemoteClient.Mode.DEFAULT)
                .execute(request)
                .jsonArray()

            gson.fromJson(jsonResult, Array<OtpBusRoute>::class.java).toList()

        }.completed {
            mutableBusRoutes.value = it
            mutableStatus.value = Idle
        }.error {
            mutableStatus.value = Error
        }.build()
        busRoutesTask?.load()
    }

    override fun onCleared() {
        super.onCleared()
        busRoutesTask?.cancel()
    }
}