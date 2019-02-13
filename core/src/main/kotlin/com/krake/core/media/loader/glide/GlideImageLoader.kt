package com.krake.core.media.loader.glide

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.krake.core.extension.prepareRequest
import com.krake.core.media.loader.DefaultImageLoaderRequest
import com.krake.core.media.loader.ImageLoader
import java.io.File

/**
 * Implementation of [ImageLoader] that uses Glide to load the images.
 *
 * @param context the [Context] used to retrieve the shared instance of Glide.
 * @param From the source from which the images will be retrieved.
 * @param To the type the images will be converted into.
 */
open class GlideImageLoader<From, To>(context: Context) : ImageLoader<From, To> {

    val glide: RequestManager by lazy { Glide.with(context) }

    var resourceToLoad: From? = null
    var cacheStrategy: ImageLoader.CacheStrategy? = null
    var width: Int? = null
    var height: Int? = null


    override fun withCacheStrategy(cacheStrategy: ImageLoader.CacheStrategy): ImageLoader<From, To> = apply { this.cacheStrategy = cacheStrategy }

    override fun from(from: From): ImageLoader<From, To> {
        resourceToLoad = from
        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun intoView(): ImageLoader.ViewRequest = ViewRequest(this as GlideImageLoader<From, Drawable>)

    override fun size(width: Int, height: Int): ImageLoader<From, To> = apply {
        this.width = width
        this.height = height
    }

    /**
     * Implementation of [ImageLoader.Request] that uses Glide to return the resource converted to the type [To].
     *
     * @param imageLoader the current instance of [GlideImageLoader] used to load the resource.
     * @param From the source from which the images will be retrieved.
     * @param To the type the images will be converted into.
     */
    abstract class Request<From, To>(private val imageLoader: GlideImageLoader<From, To>) : DefaultImageLoaderRequest<To>() {

        @SuppressLint("CheckResult")
        override fun obtainResource(): To {
            val request = createRequestBuilder()
                    .prepareRequest(imageLoader, listeners)

            val width = imageLoader.width
            val height = imageLoader.height
            val future = if (width != null && height != null) {
                request.submit(width, height)
            } else {
                request.submit()
            }
            return future.get()
        }

        /**
         * Creates the base configuration for the Glide request.
         *
         * @return the instance of the [RequestBuilder] used to get the resource.
         */
        abstract fun createRequestBuilder(): RequestBuilder<To>
    }

    /**
     * Implementation of [ImageLoader.ViewRequest] that uses Glide to load the resource into an [ImageView].
     *
     */
    class ViewRequest<From>(private val imageLoader: GlideImageLoader<From, Drawable>) : ImageLoader.ViewRequest {

        companion object {
            private val TAG = ViewRequest::class.java.name
        }

        private @DrawableRes
        var placeholder: Int? = null
        private val listeners = mutableSetOf<ImageLoader.RequestListener<Drawable>>()
        private var scaleType: ImageView.ScaleType? = null
        private var animation: ImageLoader.Animation? = null

        override fun placeHolder(placeholder: Int): ImageLoader.ViewRequest = apply { this.placeholder = placeholder }

        override fun scaleType(scaleType: ImageView.ScaleType): ImageLoader.ViewRequest = apply { this.scaleType = scaleType }

        override fun animation(animation: ImageLoader.Animation): ImageLoader.ViewRequest = apply { this.animation = animation }

        override fun addListener(vararg listener: ImageLoader.RequestListener<Drawable>): ImageLoader.ViewRequest = apply { this.listeners.addAll(listener) }

        @SuppressLint("CheckResult")
        override fun load(imageView: ImageView) {
            val request = imageLoader.glide.asDrawable()
            val options = RequestOptions()

            when (animation) {
                ImageLoader.Animation.FADE -> {
                    val crossFadeFactoryBuilder = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true)
                    request.transition(DrawableTransitionOptions.withCrossFade(crossFadeFactoryBuilder))
                }
                else -> options.dontAnimate()
            }

            placeholder?.let {
                options.placeholder(it)
            }

            val width = imageLoader.width
            val height = imageLoader.height
            if (width != null && height != null) {
                options.override(width, height)
            }

            scaleType?.also {
                when (it) {
                    ImageView.ScaleType.CENTER_CROP -> options.centerCrop()
                    ImageView.ScaleType.FIT_CENTER -> options.fitCenter()
                    ImageView.ScaleType.CENTER_INSIDE -> options.centerInside()
                    else -> Log.w(TAG, "The scale type $it is not supported")
                }
            }

            request.prepareRequest(imageLoader, listeners, options)

            if (listeners.isNotEmpty()) {
                // Add the listener that will notify the other listeners.
                request.listener(object : RequestListener<Drawable> {
                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        listeners.forEach { it.onDataLoadSuccess(resource) }
                        return false
                    }

                    override fun onLoadFailed(e: GlideException?, p1: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        listeners.forEach { it.onDataLoadFailed() }
                        return false
                    }
                })
            }

            request.into(imageView)
        }
    }

    abstract class RequestProvider<From, To> : ImageLoader.RequestProvider<From, To> {

        override fun provideRequest(loader: ImageLoader<From, To>): ImageLoader.Request<To> =
                object : Request<From, To>(loader as GlideImageLoader<From, To>) {
                    override fun createRequestBuilder(): RequestBuilder<To> = createRequestBuilder(loader as GlideImageLoader<From, To>)
                }

        abstract fun createRequestBuilder(loader: GlideImageLoader<From, To>): RequestBuilder<To>
    }

    class BitmapRequestProvider<From> : RequestProvider<From, Bitmap>() {
        override fun createRequestBuilder(loader: GlideImageLoader<From, Bitmap>): RequestBuilder<Bitmap> = loader.glide.asBitmap()
    }

    class DrawableRequestProvider<From> : RequestProvider<From, Drawable>() {
        override fun createRequestBuilder(loader: GlideImageLoader<From, Drawable>): RequestBuilder<Drawable> = loader.glide.asDrawable()
    }

    class FileRequestProvider<From> : RequestProvider<From, File>() {
        override fun createRequestBuilder(loader: GlideImageLoader<From, File>): RequestBuilder<File> = loader.glide.asFile()
    }
}