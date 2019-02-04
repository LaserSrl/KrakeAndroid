package com.krake.core.media.loader

/**
 * The main interface used to manage images.
 * Used to provide a common configuration through the whole app for the image management.
 */
interface ImageManager {

    /**
     * Provides an instance of [ImageLoader] used to load images.
     *
     * @param From the source from which the images will be retrieved.
     * @param To the type the images will be converted into.
     * @return instance of [ImageLoader] used to load images.
     */
    fun <From, To> loader(): ImageLoader<From, To>

    /**
     * Clear the cache of all the images in the app, if any.
     * It can be used to free-up the memory or the disk space.
     */
    fun clearCache()
}