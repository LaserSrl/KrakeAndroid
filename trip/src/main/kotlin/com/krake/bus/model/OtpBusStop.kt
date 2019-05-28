package com.krake.bus.model

/**
 * Created by joel on 08/03/17.
 */
class OtpBusStop(val id: String,
                 val code: String,
                 override val name: String,
                 override val lat: Double,
                 override val lon: Double,
                 override val dist: Long?) : BusStop {

    override val stringIdentifier: String
        get() = id
}