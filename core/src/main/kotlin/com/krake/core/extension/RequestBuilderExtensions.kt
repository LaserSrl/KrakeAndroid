package com.krake.core.extension

import android.annotation.SuppressLint
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.krake.core.media.loader.ImageLoader
import com.krake.core.media.loader.glide.GlideImageLoader

/**
 * Used to add the common configurations to a [RequestBuilder].
 *
 * @param imageLoader instance of [GlideImageLoader] used to retrieve the configurations.
 * @param listeners all the [ImageLoader.Request.Listener] that must be notified.
 * @param options additional [RequestOptions] with some custom attribute. The other common attributes
 * will be added into [options].
 * @return same instance of [RequestBuilder].
 */
@SuppressLint("CheckResult")
internal fun <From, To> RequestBuilder<To>.prepareRequest(imageLoader: GlideImageLoader<From, To>,
                                                          listeners: Set<ImageLoader.RequestListener<To>>,
                                                          options: RequestOptions = RequestOptions()) = apply {

    // Add the resource that must be loaded.
    load(imageLoader.resourceToLoad)

    val cacheStrategy = when (imageLoader.cacheStrategy) {
        ImageLoader.CacheStrategy.WITHOUT_TRANSFORMATION -> DiskCacheStrategy.DATA
        ImageLoader.CacheStrategy.WITH_TRANSFORMATION -> DiskCacheStrategy.RESOURCE
        ImageLoader.CacheStrategy.NONE -> DiskCacheStrategy.NONE
        else -> DiskCacheStrategy.AUTOMATIC
    }
    // Add the cache strategy.
    options.diskCacheStrategy(cacheStrategy)

    // Apply configured RequestOptions.
    apply(options)
}