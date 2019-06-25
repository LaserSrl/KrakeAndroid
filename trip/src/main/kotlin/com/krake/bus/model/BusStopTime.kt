package com.krake.bus.model

/**
 * Created by joel on 08/03/17.
 */

interface BusStopTime {

    val realtimeArrival: Long?

    val departureDelay: Long?

    val timePoint: Boolean?

    val arrivalDelay: Long?

    val scheduledArrival: Long?

    val realtimeDeparture: Long?

    val scheduledDeparture: Long?

    val tripId: String?

    fun isLastStop(): Boolean {

        return false
    }

}
