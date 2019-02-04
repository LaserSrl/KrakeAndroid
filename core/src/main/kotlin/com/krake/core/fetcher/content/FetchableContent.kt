package com.krake.core.fetcher.content

/**
 * Used to define a content that can be fetched.
 */
interface FetchableContent {

    /**
     * Start the fetching of the content.
     * The thread in which this method will be executed, must be defined by the executor.
     */
    fun fetch()
}