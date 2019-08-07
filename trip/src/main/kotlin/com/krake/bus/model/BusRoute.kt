package com.krake.bus.model

import com.krake.core.model.RecordWithStringIdentifier

interface BusRoute: RecordWithStringIdentifier {
    val color: Int

    val mode: RouteMode

    val longName: String
}