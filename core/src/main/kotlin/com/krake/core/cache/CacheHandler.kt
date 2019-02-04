package com.krake.core.cache

/**
 * Interfaccia che permette la gestione della cache di un qualsiasi tipo di valore abbinato ad
 * un qualsiasi tipo di chiave.
 *
 * @param KeyType tipo della chiave utilizzata per trovare il valore.
 * @param ValueType tipo del valore da salvare in cache.
 */
interface CacheHandler<in KeyType, ValueType> {

    /**
     * Salva in cache un valore abbinato ad un chiave.
     *
     * @param key chiave abbinata al valore.
     * @param value valore da salvare in cache.
     */
    fun saveInCache(key: KeyType, value: ValueType)

    /**
     * Ottiene il valore presente in cache abbinato ad una certa chiave.
     *
     * @param key chiave utilizzata per trovare il valore.
     * @return valore abbinato alla chiave oppure null se il valore non è stato trovato.
     */
    fun loadFromCache(key: KeyType): ValueType?

    /**
     * Rimuove un valore presente in cache abbinato ad una certa chiave.
     *
     * @param key chiave utilizzata per trovare il valore.
     */
    fun removeFromCache(key: KeyType)
}