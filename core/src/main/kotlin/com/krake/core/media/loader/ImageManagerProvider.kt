package com.krake.core.media.loader

/**
 * Provides the components related to [ImageManager].
 */
interface ImageManagerProvider {

    /**
     * @return instance of [ImageManager] that must be provided.
     */
    fun provideManager(): ImageManager
}