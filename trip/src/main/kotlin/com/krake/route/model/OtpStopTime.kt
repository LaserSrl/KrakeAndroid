package com.krake.route.model

import com.krake.bus.model.BusStopTime
import com.krake.core.model.ContentItem

class OtpStopTime(
    override val realtimeArrival: Long?,
    override val departureDelay: Long?,
    override val timePoint: Boolean?,
    override val arrivalDelay: Long?,
    override val scheduledArrival: Long?,
    override val realtimeDeparture: Long?,
    override val scheduledDeparture: Long?,
    override val tripId: String?
): BusStopTime, ContentItem {
    //TODO
    override val titlePartTitle: String?
        get() = tripId
}