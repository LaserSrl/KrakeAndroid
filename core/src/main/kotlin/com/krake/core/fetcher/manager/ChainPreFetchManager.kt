package com.krake.core.fetcher.manager

import com.krake.core.fetcher.content.FetchableContent
import com.krake.core.fetcher.observer.PreFetchObserver
import com.krake.core.thread.AsyncTask
import com.krake.core.thread.CompletedBlock
import com.krake.core.thread.async

/**
 * Implementation of [PreFetchManager] used to launch the pre-fetching requests
 * in a serial way.
 * The pre-fetch is done on another thread and the observers will be notified when
 * the last request finishes.
 * If a pre-fetching request fails, the manager will proceed to the next element in the chain.
 */
class ChainPreFetchManager : PreFetchManager {

    private val observers = mutableSetOf<PreFetchObserver>()

    private val requestChain = mutableListOf<AsyncTask<*>>()
    private val completedContents = mutableListOf<FetchableContent>()
    private val failedContents = mutableListOf<FetchableContent>()

    override fun preFetch(contents: Array<FetchableContent>) {
        val hasObservers = observers.isNotEmpty()

        // Reset the previous contents when other pre-fetchable contents are added.
        completedContents.clear()
        failedContents.clear()
        requestChain.forEach { it.cancel() }
        requestChain.clear()

        val contentStrategy: (FetchableContent) -> Unit = if (hasObservers) { content ->
            // Fetch the content.
            content.fetch()
            // Update the contents' status.
            failedContents.remove(content)
            completedContents.add(content)
        } else { content ->
            // Fetch the content.
            content.fetch()
        }

        val exceptionStrategy: (FetchableContent) -> Unit = if (hasObservers) { content ->
            // Update the contents' status.
            completedContents.remove(content)
            failedContents.add(content)
        } else { _ -> }

        val completedStrategy: CompletedBlock<Unit> = if (hasObservers) { _ ->
            // Notify that the prefetch was completed on all observers.
            observers.forEach {
                it.onPreFetchCompleted(completedContents.toTypedArray(), failedContents.toTypedArray())
            }
            requestChain.clear()
        } else { _ ->
            requestChain.clear()
        }

        async {
            contents.forEach { content ->
                try {
                    // Fetch the content.
                    contentStrategy(content)
                } catch (ignored: Exception) {
                    exceptionStrategy(content)
                }
            }
        }.completed(completedStrategy).load()
    }

    override fun registerObserver(observer: PreFetchObserver) {
        observers.add(observer)
    }

    override fun unregisterObserver(observer: PreFetchObserver) {
        observers.remove(observer)
    }
}