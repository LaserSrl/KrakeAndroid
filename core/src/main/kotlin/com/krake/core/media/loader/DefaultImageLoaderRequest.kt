package com.krake.core.media.loader

import com.krake.core.thread.async

/**
 * Implementation of [ImageLoader.Request] that handles the [load] and [get] of a resource
 * and notifies the [listeners].
 *
 * @param T the type of the resource that must be loaded.
 */
abstract class DefaultImageLoaderRequest<T> : ImageLoader.Request<T> {

    /**
     * The listeners are stored in a [Set] because the order mustn't be important.
     */
    protected val listeners: MutableSet<ImageLoader.RequestListener<T>> = mutableSetOf()

    override fun addListener(vararg listener: ImageLoader.RequestListener<T>): ImageLoader.Request<T> = apply { listeners.addAll(listener) }

    override fun get(): T = try {
        val resource = obtainResource()
        listeners.forEach { it.onDataLoadSuccess(resource) }
        resource
    } catch (exception: Exception) {
        listeners.forEach { it.onDataLoadFailed() }
        throw RuntimeException("error on load of resource")
    }

    override fun load() {
        async {
            obtainResource()
        }.completed { resource ->
            listeners.forEach { it.onDataLoadSuccess(resource) }
        }.error {
            listeners.forEach { it.onDataLoadFailed() }
        }.load()
    }

    /**
     * Used to specify how the resource must be obtained.
     *
     * @return the resource converted to the type [T].
     */
    abstract fun obtainResource(): T
}