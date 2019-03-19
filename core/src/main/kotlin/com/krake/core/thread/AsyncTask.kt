package com.krake.core.thread

import kotlinx.coroutines.*

/**
 * Used to execute a block of code in a background thread.
 */
class AsyncTask<T> private constructor(private val asyncBlock: AsyncBlock<T>,
                                       private val completedBlock: CompletedBlock<T>? = null,
                                       private val errorBlock: ErrorBlock? = null) {

    /**
     * @return true if the background task is currently loading, false otherwise.
     */
    val isLoading: Boolean
        get() = task?.isActive == true

    private val isCancelled: Boolean
        get() = task?.isCancelled == true

    private var task: Deferred<T>? = null

    /**
     * Launch a task that will run on the background thread and will dispatch the result
     * on the UI thread if any of [completedBlock] or [errorBlock] was used.
     */
    fun load() {
        GlobalScope.launch(Dispatchers.Main) {
            task = GlobalScope.async(Dispatchers.Default, CoroutineStart.LAZY) {
                // Invoke the block that must be executed in background.
                asyncBlock()
            }

            val result = try {
                task?.await() ?: throw NullPointerException("The background task can't be null.")
            } catch (ex: Exception) {
                if (ex !is CancellationException)
                    errorBlock?.invoke(ex)
                return@launch
            }

            if (isCancelled)
                return@launch

            completedBlock?.invoke(result)
        }
    }

    /**
     * Cancel the execution of a task without dispatch results on UI thread.
     */
    fun cancel() {
        task?.cancel()
    }

    /**
     * Class used to build an [AsyncTask].
     */
    class Builder<T> {
        private var asyncBlock: AsyncBlock<T>? = null
        private var completedBlock: CompletedBlock<T>? = null
        private var errorBlock: ErrorBlock? = null

        /**
         * Define the block that must be executed on a background thread.
         * The result of this block is passed to a [CompletedBlock] if it's used.
         *
         * @param asyncBlock block executed on the background thread.
         */
        fun background(asyncBlock: AsyncBlock<T>) = apply {
            this.asyncBlock = asyncBlock
        }

        /**
         * Define the block that is called when the [AsyncBlock] is completed without exceptions.
         * This block will run on the UI thread.
         *
         * @param completedBlock block used to manage the result of [AsyncBlock].
         */
        fun completed(completedBlock: CompletedBlock<T>) = apply {
            this.completedBlock = completedBlock
        }

        /**
         * Define the block that is called when an exception was thrown during the
         * execution of the [AsyncBlock].
         * This block will run on the UI thread.
         *
         * @param errorBlock block used to manage an exception thrown by the [AsyncBlock].
         */
        fun error(errorBlock: ErrorBlock) = apply {
            this.errorBlock = errorBlock
        }

        /**
         * Build an [AsyncTask].
         *
         * @return the [AsyncTask] built with the blocks added in this builder.
         */
        fun build(): AsyncTask<T> {
            if (asyncBlock == null)
                throw IllegalArgumentException("You must specify an async block.")

            return AsyncTask(asyncBlock!!, completedBlock, errorBlock)
        }

        /**
         * Method used to create an [AsyncTask] and execute it immediately.
         *
         * @return the [AsyncTask] built with the blocks added in this builder.
         */
        fun load(): AsyncTask<T> {
            val task = build()
            task.load()
            return task
        }
    }
}