package com.krake.core.cache

import com.krake.core.component.module.OrchardComponentModule
import com.krake.core.data.DataConnectionModel
import com.krake.core.model.RequestCache

/**
 * Supporto per la gestione della cache, viene interrogato da tutte le [DataConnectionModel] per i caricamenti che includono
 * il parametro [OrchardComponentModule.displayPath].
 */
interface CacheManager {

    companion object
    {
        lateinit var shared: CacheManager
            internal set
    }
    /**
     * Verifica la validità della cache.
     *
     * @param cache  richiesta attuale della cache.
     * @param extras parametri per la nuova richiesta della cache.
     * @return true se la cache è da considerarsi valida, false altrimenti.
     */
    fun isCacheValid(cache: RequestCache, extras: Map<String, String>): Boolean

    /**
     * Converte l'insieme di autoroutePath dell'oggetto di orchard e degli insieme degli altri parametri
     * nella chiave da utilizzare per la cache.
     *
     * @param displayPath corrisponde all'autoroute display path del record di orchard.
     * @param extras      parametri della nuova richiesta da effettuare ad orchard.
     * @return chiave della cache.
     */
    fun getCacheKey(displayPath: String, extras: Map<String, String>): String

    /**
     * Converte l'insieme di autoroutePath dell'oggetto di orchard e degli insieme degli altri parametri
     * nella chiave da utilizzare per la cache.
     *
     * @param displayPath corrisponde all'autoroute display path del record di orchard.
     * @param extras      parametri della nuova richiesta da effettuare ad orchard.
     * @return chiave della cache.
     */
    fun getCacheKey(orchardModule: OrchardComponentModule): String

    /**
     * Ottiene la chiave per avere la dataModelConnection a partire
     * @param orchardModule modulo di orchard
     */
    fun getModelKey(orchardModule: OrchardComponentModule): String

    /**
     * Change the cache validity related to a path.
     *
     * @param path the path added in cache.
     * @param validity the time expressed in minutes after which the cache will be invalidated.
     */
    fun setCacheValidity(path: String, validity: Int)
}