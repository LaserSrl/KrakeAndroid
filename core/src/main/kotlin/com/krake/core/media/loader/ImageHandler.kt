package com.krake.core.media.loader

import android.content.Context

/**
 * Helper class used to access to the common [ImageManager] used in the application.
 */
object ImageHandler {

    /**
     * Get the [ImageLoader] related to the [ImageManager] used in the application.
     *
     * @param context parent [Context] used to retrieve the [Context] of the application.
     * @param From the source from which the images will be retrieved.
     * @param To the type the images will be converted into.
     * @return instance of [ImageLoader] used in the application.
     */
    fun <From, To> loader(context: Context): ImageLoader<From, To> = manager(context).loader()

    /**
     * Clears the cache related to the [ImageManager] used in the application.
     *
     * @param context parent [Context] used to retrieve the [Context] of the application.
     */
    fun clearDiskCache(context: Context) {
        manager(context).clearCache()
    }

    /**
     * Retrieves the [ImageManager] from the [Context] of the application.
     *
     * @param context parent [Context] used to retrieve the [Context] of the application.
     * @return instance of [ImageManager] used in the application.
     */
    fun manager(context: Context): ImageManager = (context.applicationContext as? ImageManagerProvider)?.provideManager() ?:
            throw IllegalArgumentException("The application must implement ${ImageManagerProvider::class.java.name}")
}