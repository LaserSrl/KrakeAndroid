package com.krake.beacons.altbeacon

import com.krake.beacons.Beacon
import java.util.*

class AltBeaconBeacon
internal constructor(private val beacon: org.altbeacon.beacon.Beacon) : Beacon
{

    override val uuid: UUID
        get() = beacon.id1.toUuid()
    override val major: Int
        get() = beacon.id2.toInt()
    override val minor: Int
        get() = beacon.id3.toInt()
    override val measuredPower: Int
        get() = beacon.txPower
    override val rssi: Int
        get() = beacon.rssi
    override val original: Any
        get() = beacon

}