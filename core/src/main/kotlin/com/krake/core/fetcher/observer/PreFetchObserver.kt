package com.krake.core.fetcher.observer

import com.krake.core.fetcher.content.FetchableContent

/**
 * Used to observe the pre-fetching status.
 */
interface PreFetchObserver {

    /**
     * Notified when the pre-fetching has completed.
     *
     * @param completed the list of successfully pre-fetched [FetchableContent]s.
     * @param failed the list of failed pre-fetched [FetchableContent]s.
     */
    fun onPreFetchCompleted(completed: Array<FetchableContent>, failed: Array<FetchableContent>)
}