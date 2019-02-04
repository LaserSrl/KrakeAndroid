package com.krake.test.lock

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Implementation of [Lock] that uses a [CountDownLatch] with the count down as 1.
 * It's a better implementation for unit tests than the native [Object.wait] and [Object.notify]
 * because it will return the result immediately.
 */
class CountDownLatchLock : CountDownLatch(1), Lock {

    override fun lock() = await()

    override fun lockWithTimeout(timeout: Long, unit: TimeUnit): Boolean = await(timeout, unit)

    override fun unlock() = countDown()
}