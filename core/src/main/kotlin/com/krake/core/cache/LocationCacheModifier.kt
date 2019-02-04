package com.krake.core.cache

/**
 * Allows to change cache settings related to "around me" functionality.
 * It must be implemented by the [CacheManager] to implement a custom cache handling
 * of "around me" related sections.
 */
interface LocationCacheModifier {

    /**
     * Add a Krake path to the cache manager.
     * The newly added path could be used to apply cache settings related to "around me" functionality.
     *
     * @param path relative path to add.
     */
    fun addLocationPath(path: String)
}