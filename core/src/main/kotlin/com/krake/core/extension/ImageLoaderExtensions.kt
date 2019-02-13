package com.krake.core.extension

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.krake.core.media.loader.ImageLoader
import com.krake.core.media.loader.glide.GlideImageLoader
import java.io.File

/**
 * Convenience method used to retrieve the image as [Bitmap].
 *
 * @return the request that will retrieve the [Bitmap].
 */
fun <From> ImageLoader<From, Bitmap>.asBitmap(): ImageLoader.Request<Bitmap> = asResource(GlideImageLoader.BitmapRequestProvider())

/**
 * Convenience method used to retrieve the image as [File].
 *
 * @return the request that will retrieve the [File].
 */
fun <From> ImageLoader<From, File>.asFile(): ImageLoader.Request<File> = asResource(GlideImageLoader.FileRequestProvider())

/**
 * Convenience method used to retrieve the image as [Drawable].
 *
 * @return the request that will retrieve the [Drawable].
 */
fun <From> ImageLoader<From, Drawable>.asDrawable(): ImageLoader.Request<Drawable> = asResource(GlideImageLoader.DrawableRequestProvider())