package com.krake.core.text

/**
 * Created by joel on 21/04/17.
 */
class DistanceNumberFormat {

    var prefix: String? = null
    var suffix: String? = null

    fun prefix(prefix: String?) = apply { this.prefix = prefix }

    fun suffix(suffix: String?) = apply { this.suffix = suffix }

    /**
     * Formattazione della distanza

     * @param distanceMeters distanza in metri
     * *
     * @return distanza formattata
     */
    fun formatDistance(distanceMeters: Float): String {
        if (distanceMeters < 1000) {
            return String.format("%s%.0f m%s", prefix ?: "", distanceMeters, suffix ?: "")
        } else {
            return String.format("%s%.1f Km%s", prefix ?: "", distanceMeters / 1000.0f, suffix ?: "")
        }
    }

    companion object {
        @JvmStatic
        val sharedInstance: DistanceNumberFormat by lazy { DistanceNumberFormat() }
    }
}
