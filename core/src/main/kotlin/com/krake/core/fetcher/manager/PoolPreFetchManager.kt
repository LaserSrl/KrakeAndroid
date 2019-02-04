package com.krake.core.fetcher.manager

import android.util.ArrayMap
import com.krake.core.fetcher.content.FetchableContent
import com.krake.core.fetcher.observer.PreFetchObserver
import com.krake.core.thread.AsyncTask
import com.krake.core.thread.async

/**
 * Implementation of [PreFetchManager] used to launch the pre-fetching requests in parallel.
 * The pre-fetch is done on another thread and the observers will be notified when
 * the last request finishes.
 * If a pre-fetching request fails, the manager will proceed to the next element that is waiting
 * in the pool.
 */
class PoolPreFetchManager : PreFetchManager {

    private val observers = mutableSetOf<PreFetchObserver>()

    private val requestPool = ArrayMap<FetchableContent, AsyncTask<*>>()
    private val completedContent = mutableListOf<FetchableContent>()
    private val failedContent = mutableListOf<FetchableContent>()

    override fun preFetch(contents: Array<FetchableContent>) {
        val hasObservers = observers.isNotEmpty()

        // Reset the previous contents when other pre-fetchable contents are added.
        completedContent.clear()
        failedContent.clear()
        requestPool.values.forEach { it.cancel() }
        requestPool.clear()

        val removeFromChainAndNotify = { content: FetchableContent ->
            // Remove the request with the same content.
            requestPool.remove(content)
            if (requestPool.isEmpty()) {
                // Notify that the prefetch was completed on all observers.
                observers.forEach {
                    it.onPreFetchCompleted(completedContent.toTypedArray(), failedContent.toTypedArray())
                }
            }
        }

        contents.forEach { content ->
            val requestBuilder = async {
                // Fetch the content.
                content.fetch()
            }

            val request = if (hasObservers) {
                val req = requestBuilder.completed {
                    // Update the contents' status.
                    failedContent.remove(content)
                    completedContent.add(content)

                    removeFromChainAndNotify(content)
                }.error {
                    // Update the contents' status.
                    completedContent.remove(content)
                    failedContent.add(content)

                    removeFromChainAndNotify(content)
                }.build()

                // Add the current request to the chain.
                requestPool.put(content, req)
                req
            } else {
                requestBuilder.build()
            }

            // Load the request.
            request.load()
        }
    }

    override fun registerObserver(observer: PreFetchObserver) {
        observers.add(observer)
    }

    override fun unregisterObserver(observer: PreFetchObserver) {
        observers.remove(observer)
    }
}