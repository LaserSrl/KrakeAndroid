package com.krake.bus.model

import com.krake.core.model.RecordWithStringIdentifier

/**
 * Created by joel on 08/03/17.
 */
@Suppress("UNCHECKED_CAST")
interface BusPattern : RecordWithStringIdentifier {

    val stopTimesList: List<*>

    val stopTimes get() = stopTimesList as List<BusStopTime>

    val descriptionText: String?

    var busRoute: BusRoute?
}