package com.krake.beacons

import java.util.*

/**
 * Created by joel on 29/06/17.
 */
interface Region {
    val identifier: String
    val uuid: UUID
    val major: Int?
    val minor: Int?

    val originalRegion: Any
}