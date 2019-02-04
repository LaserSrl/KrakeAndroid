package com.krake.core.thread

typealias AsyncBlock<T> = suspend () -> T

typealias CompletedBlock<T> = (T) -> Unit

typealias ErrorBlock = (Exception) -> Unit

/**
 * Method used to access easily to the [AsyncTask.Builder] class used to build an [AsyncTask].
 *
 * @param asyncBlock block of code that must be added to the [AsyncTask.Builder].
 */
fun <T> async(asyncBlock: AsyncBlock<T>): AsyncTask.Builder<T> =
        AsyncTask.Builder<T>().background(asyncBlock)
