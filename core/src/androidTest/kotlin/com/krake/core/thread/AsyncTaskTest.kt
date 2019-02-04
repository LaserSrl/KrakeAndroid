package com.krake.core.thread

import android.support.test.filters.SmallTest
import com.krake.test.extension.lock
import kotlinx.coroutines.delay
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Instrumented test class for [AsyncTask] and [AsyncTask.Builder].
 */
@SmallTest
class AsyncTaskTest {

    @Test
    fun taskCompletedSuccessfully() {
        val lock = lock()

        val number = 5
        async {
            // Do a sum.
            number + number
        }.completed { result ->
            // Check that the sum was done.
            assertEquals(number + number, result)
            // Finish the test unlocking the lock.
            lock.unlock()
        }.load()

        lock.lock()
    }

    @Test
    fun taskCompletedSuccessfullyAvoidErrorBlock() {
        val lock = lock()

        async {
            // Don't execute anything.
        }.error {
            lock.unlock()
        }.load()

        // Give 10ms to invoke the error block, otherwise, it won't be executed.
        val errorBlockIsExecuted = lock.lockWithTimeout(10, TimeUnit.MILLISECONDS)
        assertFalse(errorBlockIsExecuted)
    }

    @Test
    fun taskCaughtException() {
        val lock = lock()

        val exc = RuntimeException()
        async {
            throw exc
        }.error { e ->
            // Check that the thrown exception is the same.
            assertEquals(exc, e)
            // Finish the test unlocking the lock.
            lock.unlock()
        }.load()

        lock.lock()
    }

    @Test
    fun taskCaughtExceptionAvoidCompletedBlock() {
        val lock = lock()

        async {
            throw RuntimeException()
        }.completed {
            lock.unlock()
        }.load()

        // Give 10ms to invoke the completed block, otherwise, it won't be executed.
        val completedBlockIsExecuted = lock.lockWithTimeout(10, TimeUnit.MILLISECONDS)
        assertFalse(completedBlockIsExecuted)
    }

    @Test
    fun taskManageLoading() {
        val startLoadingLock = lock()
        val finishLoadingLock = lock()
        val completedLock = lock()

        val task = async {
            startLoadingLock.unlock()
            // Wait till the lock will be unlocked.
            finishLoadingLock.lock()
        }.completed {
            completedLock.unlock()
        }.build()

        // At the start, the task mustn't load anything.
        assertFalse(task.isLoading)
        // Load the task.
        task.load()
        // Wait till the loading is sent to a background thread.
        startLoadingLock.lock()
        // The background thread is processing now, so the task must be loading.
        assertTrue(task.isLoading)
        // Unlock the waiting on the background thread.
        finishLoadingLock.unlock()
        // Wait till the completed lock is executed.
        completedLock.lock()
        // The task isn't loading anymore.
        assertFalse(task.isLoading)
    }

    @Test
    fun taskManageLoadingOnCancellation() {
        val startLoadingLock = lock()
        val finishLoadingLock = lock()

        var isCallbackCalled = false

        val task = async {
            startLoadingLock.unlock()
            // Wait till the lock will be unlocked.
            finishLoadingLock.lock()
        }.completed {
            isCallbackCalled = true
        }.load()

        // Wait till the loading is sent to a background thread.
        startLoadingLock.lock()
        // The background thread is processing now, so the task must be loading.
        assertTrue(task.isLoading)
        // Cancel the task.
        task.cancel()
        // Unlock the waiting on the background thread and finish the test.
        finishLoadingLock.unlock()

        // The task mustn't call the callback.
        assertFalse(isCallbackCalled)
    }

    @Test
    fun taskTestSuspendCancellation() {
        val startLoadingLock = lock()
        val finishLoadingLock = lock()

        var isCompletedCallbackCalled = false
        var isErrorCallbackCalled = false

        val task = async {
            startLoadingLock.unlock()
            delay(1000)
            finishLoadingLock.lock()
        }.completed {
            isCompletedCallbackCalled = true
        }.error {
            isErrorCallbackCalled = true
        }.load()

        startLoadingLock.lock()
        // The background thread is processing now, so the task must be loading.
        assertTrue(task.isLoading)
        // Cancel the task.
        task.cancel()

        finishLoadingLock.unlock()

        assertFalse(isCompletedCallbackCalled)
        assertFalse(isErrorCallbackCalled)

        // The task mustn't load anything anymore.
        assertFalse(task.isLoading)
    }

    @Test
    fun taskTestComputationCancellation() {
        val startLoadingLock = lock()
        val finishLoadingLock = lock()

        var isCompletedCallbackCalled = false
        var isErrorCallbackCalled = false

        val task = async {
            startLoadingLock.unlock()
            finishLoadingLock.lock()
        }.completed {
            isCompletedCallbackCalled = true
        }.error {
            isErrorCallbackCalled = true
        }.load()

        startLoadingLock.lock()
        // The background thread is processing now, so the task must be loading.
        assertTrue(task.isLoading)
        // Cancel the task.
        task.cancel()

        finishLoadingLock.unlock()

        assertFalse(isCompletedCallbackCalled)
        assertFalse(isErrorCallbackCalled)

        // The task mustn't load anything anymore.
        assertFalse(task.isLoading)
    }
}