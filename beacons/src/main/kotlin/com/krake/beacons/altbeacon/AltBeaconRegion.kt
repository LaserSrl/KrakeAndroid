package com.krake.beacons.altbeacon

import com.krake.beacons.Region
import java.util.*

class AltBeaconRegion(private val region: org.altbeacon.beacon.Region) : Region
{
    override val identifier: String
        get() = region.uniqueId
    override val uuid: UUID
        get() = region.id1.toUuid()
    override val major: Int?
        get() = region.id2.toInt()
    override val minor: Int?
        get() = region.id3.toInt()

    override val originalRegion: Any
        get() = region

    override fun equals(other: Any?): Boolean
    {
        if (other is Region)
            return region === other.originalRegion

        return super.equals(other)
    }
}