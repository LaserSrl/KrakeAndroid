package com.krake.core.map

import android.content.Context
import com.google.android.gms.maps.model.Marker

/**
 * Interfaccia che viene utilizzata per identificare un [Marker].
 */
interface MarkerKeyProvider {

    /**
     * Fornisce la chiave utilizzata per identificare l'icona di un [Marker].
     * Questa chiave viene utilizzata non come valore univoco del [Marker] ma come identificativo per
     * capire quanto due [Marker] sono rappresentati con la stessa icona.
     *
     * @param context [Context] utilizzato per creare la chiave.
     * @return chiave che identifica l'icona del [Marker].
     */
    fun provideMarkerKey(context: Context): String
}