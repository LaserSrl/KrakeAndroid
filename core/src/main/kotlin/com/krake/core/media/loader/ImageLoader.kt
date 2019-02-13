package com.krake.core.media.loader

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes

/**
 * Generic loader for images
 */
interface ImageLoader<From, To> {

    /**
     * add a [CacheStrategy] to the [ImageLoader]
     */
    fun withCacheStrategy(cacheStrategy: CacheStrategy): ImageLoader<From, To>

    fun from(from: From): ImageLoader<From, To>

    fun size(width: Int, height: Int): ImageLoader<From, To>

    /**
     * obtain a [ViewRequest] for allow the user to load the image into an [ImageView]
     */
    fun intoView(): ViewRequest

    /**
     * with a [RequestProvider], that is similar to an Alterable,
     * tell the [ImageLoader] what kind of resource the user would have,
     * the [RequestProvider] must tell to this [ImageLoader] that kind of resource
     */
    fun asResource(to: ImageLoader.RequestProvider<From, To>): Request<To> = to.provideRequest(this)

    /**
     * Generic request for load a Resource, specified by the [RequestProvider]
     */
    interface Request<T> {

        fun addListener(vararg listener: RequestListener<T>) : Request<T>

        fun load()

        /**
         * load the resource in sync mode
         */
        fun get(): T
    }

    interface ViewRequest {

        fun placeHolder(@DrawableRes placeholder: Int) : ViewRequest

        fun scaleType(scaleType: ImageView.ScaleType) : ViewRequest

        fun animation(animation: Animation) : ViewRequest

        fun addListener(vararg listener: RequestListener<Drawable>) : ViewRequest

        /**
         * final method that load the source into the image view passed
         */
        fun load(imageView: ImageView)
    }

    interface RequestListener<in T> {
        fun onDataLoadSuccess(resource: T?)

        fun onDataLoadFailed()
    }

    /**
     * Provider that alter the [ImageLoader] and set the kind of resource to load
     */
    interface RequestProvider<From, To> {
        fun provideRequest(loader: ImageLoader<From, To>): Request<To>
    }

    enum class CacheStrategy {
        /**
         * never cache the image
         */
        NONE,

        /**
         * cache the image when is load
         */
        WITHOUT_TRANSFORMATION,

        /**
         * cache the image with the transformations, for example with a specific size
         */
        WITH_TRANSFORMATION
    }

    enum class Animation {
        /**
         * without animation
         */
        NONE,

        /**
         * apply a fade animation
         */
        FADE
    }
}