package com.krake.test.lock

import java.util.concurrent.TimeUnit

/**
 * Used as an abstraction for a synchronized locking mechanism on threads.
 */
interface Lock {

    /**
     * Lock a thread and wait till it will be unlocked or interrupted.
     */
    fun lock()

    /**
     * Lock a thread and wait till it will be unlocked, interrupted or it will reach
     * a certain timeout.
     *
     * @param timeout the maximum time to wait.
     * @param unit the time unit of the [timeout] argument.
     * @return true if the lock was unlocked manually or false if the lock reached the maximum timeout.
     */
    fun lockWithTimeout(timeout: Long, unit: TimeUnit): Boolean

    /**
     * Unlock a thread and re-enable the processing on it.
     */
    fun unlock()
}