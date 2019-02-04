package com.krake.core.media.loader.glide

import android.content.Context
import com.bumptech.glide.Glide
import com.krake.core.media.loader.ImageLoader
import com.krake.core.media.loader.ImageManager

/**
 * Implementation of [ImageManager] that uses Glide to manage the images.
 *
 * @param context the application's [Context] used to retrieve the shared instance of Glide.
 */
class GlideImageManager(private val context: Context) : ImageManager {

    override fun <From, To> loader(): ImageLoader<From, To> = GlideImageLoader(context)

    override fun clearCache() {
        Glide.get(context).clearDiskCache()
    }
}