package com.krake.bus.provider

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.krake.bus.model.BusPassage
import com.krake.core.map.MarkerConfiguration
import com.krake.core.map.MarkerKeyProvider
import kotlin.coroutines.CoroutineContext

interface BusMovementProvider {
    /**
     * number of seconds for the refresh of the bus position
     */
    fun getRefreshPeriod(context: Context): Int

    /**
     * provide the bus marker configuration
     */
    fun provideBusMarkerConfiguration(context: Context, busPassage: BusPassage): BusMovementMarkerConfiguration

    /**
     * provide the current bus location
     */
    suspend fun provideCurrentBusPosition(context: Context, busPassage: BusPassage): LatLng
}

interface BusMovementMarkerConfiguration : MarkerConfiguration, MarkerKeyProvider