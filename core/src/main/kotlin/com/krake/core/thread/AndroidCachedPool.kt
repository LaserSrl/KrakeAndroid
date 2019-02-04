package com.krake.core.thread

import kotlinx.coroutines.CoroutineDispatcher
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext

/**
 * Oggetto da utilizzare nelle coroutines per fare il dispatch su un altro thread.
 * I thread vengono cachati e supportano il parallelismo.
 */
@Deprecated("This object is now deprecated.", ReplaceWith("kotlinx.coroutines.experimental.CommonPool"))
object AndroidCachedPool : CoroutineDispatcher() {
    @Volatile
    private var executor: ExecutorService? = null

    private fun createPool(): ExecutorService {
        val threadId = AtomicInteger()
        return Executors.newCachedThreadPool {
            Thread(it, "CommonPool-worker-${threadId.incrementAndGet()}").apply { isDaemon = true }
        }
    }

    @Synchronized
    private fun getOrCreatePoolSync(): ExecutorService =
            executor ?: createPool().also { executor = it }

    override fun dispatch(context: CoroutineContext, block: Runnable) =
            (executor ?: getOrCreatePoolSync()).execute(block)

    override fun toString(): String = "AndroidCachedPool"
}