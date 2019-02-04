package com.krake.core.map

import android.content.Context
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.krake.core.cache.CacheHandler
import com.krake.core.map.cache.DefaultMarkerCacheHandler

/**
 * Implementazione di default di [MarkerCreator] che gestisce la creazione del [Marker] utilizzando
 * un [CacheHandler] per la cache sulle icone.
 * Per utilizzare questo [MarkerCreator], la classe che implementa [MarkerConfiguration]
 * deve implementare [MarkerKeyProvider].
 */
open class DefaultMarkerCreator(val cacheHandler: CacheHandler<String, BitmapDescriptor> = DefaultMarkerCacheHandler()) : MarkerCreator,
        MarkerInvalidator.Listener {

    override fun createMarker(context: Context, configuration: MarkerConfiguration): MarkerOptions {
        var title = configuration.markerTitle()
        if (title.isNullOrEmpty()) {
            title = null
        }
        var subtitle = configuration.markerSubtitle()
        if (subtitle.isNullOrEmpty()) {
            subtitle = null
        }
        val position = configuration.markerPosition()

        val cacheKey = obtainCacheKey(context, configuration)
        val iconBitmap = obtainIcon(context, cacheKey) {
            configuration.markerIcon(context)
        }

        // Crea le opzioni del Marker
        return MarkerOptions().title(title)
                .snippet(subtitle)
                .position(position)
                .icon(iconBitmap)
    }

    override fun invalidateMarker(context: Context, configuration: MarkerConfiguration) {
        val cacheKey = obtainCacheKey(context, configuration)
        val icon = configuration.markerIcon(context)
        cacheHandler.saveInCache(cacheKey, icon)
    }

    /**
     * Specifica come ottenere l'icona di un [Marker].
     * L'icona viene prima cercata in cache e, se non trovata, viene inizializzata e salvata in cache.
     *
     * @param context [Context] utilizzato per ottenere l'icona.
     * @param bitmapDescriptorInit closure utilizzata per creare il [BitmapDescriptor] nel caso in cui ce ne sia bisogno.
     * @return icona del [Marker].
     */
    protected open fun obtainIcon(context: Context, cacheKey: String, bitmapDescriptorInit: () -> BitmapDescriptor): BitmapDescriptor {
        var iconDescriptor = cacheHandler.loadFromCache(cacheKey)
        if (iconDescriptor == null) {
            // Crea il BitmapDescriptor se non è stato trovato in cache
            iconDescriptor = bitmapDescriptorInit()
            cacheHandler.saveInCache(cacheKey, iconDescriptor)
        }
        return iconDescriptor
    }

    /**
     * Specifica come ottenere la chiave della cache.
     *
     * @param context [Context] utilizzato per ottenere la chiave.
     * @param configuration configurazione del [Marker].
     * @return chiave per trovare il valore in cache.
     */
    protected open fun obtainCacheKey(context: Context, configuration: MarkerConfiguration): String {
        val cacheKey = (configuration as? MarkerKeyProvider)?.provideMarkerKey(context)
        return cacheKey ?: throw IllegalArgumentException("The ${configuration::class.java.simpleName} class " +
                "must implement the method provideMarkerKey() of the class" +
                "${MarkerKeyProvider::class.java.simpleName} to be used by " +
                "the ${DefaultMarkerCreator::class.java.simpleName} class.")
    }
}