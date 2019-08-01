package com.krake.bus.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.krake.OtpDataRepository
import com.krake.bus.model.BusStop
import com.krake.bus.model.OtpStopTime
import com.krake.core.thread.AsyncTask
import com.krake.core.thread.async
import java.util.*

class BusStopsViewModel : ViewModel() {
    private var busStopsTask: AsyncTask<List<BusStop>>? = null
    private val mutableBusStops = MutableLiveData<List<BusStop>>()
    val busStops: LiveData<List<BusStop>> = mutableBusStops

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
            mutableBusStops.value = it.sortedBy { it.name }
            mutableStatus.value = Idle
        }.error {
            mutableStatus.value = Error
        }.build()
        busStopsTask?.load()
    }

    fun loadBusTimesByDate(stop: BusStop, routeId: String, date: Date) {
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