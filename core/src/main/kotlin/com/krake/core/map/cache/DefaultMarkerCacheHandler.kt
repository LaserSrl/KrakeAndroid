package com.krake.core.map.cache

import android.support.v4.util.ArrayMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.Marker
import com.krake.core.cache.CacheHandler

/**
 * Implementazione di default di [CacheHandler] per la gestione dei [Marker] che utilizza un
 * [ArrayMap] per salvare i valori in RAM.
 * La chiave è una stringa utilizzata come identificativo per l'icona.
 * Il valore è il [BitmapDescriptor] che rappresenta l'icona del [Marker].
 */
class DefaultMarkerCacheHandler : CacheHandler<String, BitmapDescriptor> {
    private val markerPool: ArrayMap<String, BitmapDescriptor> = ArrayMap()

    override fun saveInCache(key: String, value: BitmapDescriptor) {
        markerPool.put(key, value)
    }

    override fun loadFromCache(key: String): BitmapDescriptor? {
        return markerPool[key]
    }

    override fun removeFromCache(key: String) {
        markerPool.remove(key)
    }
}