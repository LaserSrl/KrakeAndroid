package com.krake.beacons

import java.util.*

/**
 * Created by joel on 29/06/17.
 */
interface Beacon {

    /**
     * @return Identificativo della region
     */
    val uuid: UUID

    /**
     * Major number
     * @return
     */
    val major: Int

    /**
     * Minor number
     * @return
     */
    val minor: Int

    /**
     * Potenza teorica
     * @return
     */
    val measuredPower: Int

    /**
     * Rssi
     * @return
     */
    val rssi: Int

    /**
     * Beacon original. Presente per permettere alle singole implementazioni di accedere al loro oggetto originale.
     * @return
     */
    val original: Any
}
