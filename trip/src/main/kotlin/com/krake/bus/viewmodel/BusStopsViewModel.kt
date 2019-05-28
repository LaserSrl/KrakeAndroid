package com.krake.bus.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.krake.OtpDataRepository
import com.krake.core.thread.AsyncTask
import com.krake.core.thread.async
import com.krake.bus.model.OtpBusStop
import com.krake.bus.model.OtpStopTime
import java.util.*

class BusStopsViewModel : ViewModel() {
    private var busStopsTask: AsyncTask<List<OtpBusStop>>? = null
    private val mutableBusStops = MutableLiveData<List<OtpBusStop>>()
    val busStops: LiveData<List<OtpBusStop>> = mutableBusStops

    private var stopTimesTask: AsyncTask<List<OtpStopTime>>? = null
    private val mutableStopTimes = MutableLiveData<List<OtpStopTime>>()
    val stopTimes: LiveData<List<OtpStopTime>> = mutableStopTimes

    private val mutableStatus = MutableLiveData<Status>()
    val status: LiveData<Status> = mutableStatus

    init {
        mutableStatus.value = Idle
    }

    fun loadStopsByBusRoute(routeId: String) {
        mutableStatus.value = Loading

        busStopsTask = async {
            OtpDataRepository.shared.loadStopsByBusRoute(routeId)
        }.completed {
            mutableBusStops.value = it
            mutableStatus.value = Idle
        }.error {
            mutableStatus.value = Error
        }.build()
        busStopsTask?.load()
    }

    fun loadBusTimesByDate(stop: OtpBusStop, routeId: String, date: Date) {
        mutableStatus.value = Loading

        stopTimesTask = async {
            OtpDataRepository.shared.loadBusTimesByDate(stop, routeId, date)
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