package com.krake.bus.model

/**
 * Created by antoniolig on 27/04/2017.
 */
interface BusStopsReceiver {
    fun onBusStopsReceived(stops: List<BusStop>)
}