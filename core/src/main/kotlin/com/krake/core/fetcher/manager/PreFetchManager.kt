package com.krake.core.fetcher.manager

import com.krake.core.fetcher.content.FetchableContent
import com.krake.core.fetcher.observer.PreFetchObserver

/**
 * Used to handle the pre-fetching of some [FetchableContent].
 * This manager can also notify the pre-fetching status to the registered [PreFetchObserver]s.
 */
interface PreFetchManager {

    /**
     * Pre-fetch a list of content.
     *
     * @param contents the list of content that must be pre-fetched.
     */
    fun preFetch(contents: Array<FetchableContent>)

    /**
     * Register an observer used to observe on the pre-fetching status.
     *
     * @param observer the [PreFetchObserver] that will be registered.
     */
    fun registerObserver(observer: PreFetchObserver)

    /**
     * Unregister an observer that was used to observe on the pre-fetching status.
     *
     * @param observer the [PreFetchObserver] that will be unregistered.
     */
    fun unregisterObserver(observer: PreFetchObserver)
}