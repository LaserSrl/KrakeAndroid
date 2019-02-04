package com.krake.core.map

import android.content.Context
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

/**
 * Gestisce la creazione di un [Marker] che potrà essere aggiunto ad una [MapView].
 */
interface MarkerCreator {

    /**
     * Fornisce le opzioni per creare un [Marker].
     *
     * @param context [Context] utilizzato per creare il [Marker].
     * @param configuration configurazione del [Marker].
     * @return opzioni del [Marker] utilizzate per aggiungerlo alla mappa.
     */
    fun createMarker(context: Context, configuration: MarkerConfiguration): MarkerOptions

    companion object
    {
        lateinit var shared: MarkerCreator
    }
}