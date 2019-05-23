package com.krake.bus.provider

import com.krake.bus.model.BusPassage
import com.krake.core.map.MarkerConfiguration
import com.krake.core.map.MarkerKeyProvider

interface BusMovementProvider {
    /**
     * number of seconds for the refresh of the bus position
     */
    fun getRefreshPeriod(): Int

    /**
     * provide the current bus location
     */
    suspend fun provideCurrentBusPosition(busPassage: BusPassage): BusMovementMarkerConfiguration
}

interface BusMovementMarkerConfiguration : MarkerConfiguration, MarkerKeyProvider