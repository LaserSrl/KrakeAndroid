package com.krake.bus.model

import com.krake.core.model.ContentItem

class OtpStopTime(
    override val realtimeArrival: Long?,
    override val departureDelay: Long?,
    override val timePoint: Boolean?,
    override val arrivalDelay: Long?,
    override val scheduledArrival: Long?,
    override val realtimeDeparture: Long?,
    override val scheduledDeparture: Long?,
    override val tripId: String?,
    override val realtime: String?,

    val stopIndex: Int?,
    val stopCount: Int?,
    val realtimeState: String?
): BusStopTime, ContentItem {
    override val titlePartTitle: String?
        get() = tripId

    override fun isLastStop(): Boolean {

        if (stopIndex != null && stopCount != null) {
            return stopIndex == stopCount - 1
        }

        return false
    }
}