package com.krake.core.media.loader

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.krake.core.R
import com.krake.core.media.ImageOptions
import com.krake.core.media.MediaLoadable
import com.krake.core.media.MediaType
import com.krake.core.model.MediaPart
import java.io.UnsupportedEncodingException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder
import java.util.*

/**
 * Questa classe compie tre azione principali:
 * <ul>
 * <li>Crea la richiesta per il WS per il download dell'immagine</li>
 * <li>Scarica l'immagine</li>
 * <li>Setta l'immagine in un widget che implementa l'interfaccia [MediaLoadable], se presente</li>
 * </ul>
 */
class MediaLoader<T> private constructor(private val context: Context, val mediaLoadable: MediaLoadable) {
    companion object {
        private const val TAG = "MediaLoader"

        fun with(context: Context, mediaLoadable: MediaLoadable) = MediaLoader<Drawable>(context, mediaLoadable)
        fun with(activity: Activity, mediaLoadable: MediaLoadable) = MediaLoader<Drawable>(activity, mediaLoadable)
        fun with(fragment: Fragment, mediaLoadable: MediaLoadable) = MediaLoader<Drawable>(fragment.activity
                ?: throw IllegalArgumentException("The activity mustn't be null"), mediaLoadable)

        fun <T>typedWith(context: Context, mediaLoadable: MediaLoadable): MediaLoader<T> = MediaLoader(context, mediaLoadable)
        fun <T>typedWith(activity: Activity, mediaLoadable: MediaLoadable): MediaLoader<T> = MediaLoader(activity, mediaLoadable)
        fun <T>typedWith(fragment: Fragment, mediaLoadable: MediaLoadable): MediaLoader<T> = MediaLoader(fragment.activity
                ?: throw IllegalArgumentException("The activity mustn't be null"), mediaLoadable)

        fun getAbsoluteMediaURL(context: Context, mediaUrl: String?): String? {
            val externalURL: URL
            try {
                externalURL = URL(mediaUrl)
                return externalURL.toString()
            } catch (ignored: MalformedURLException) {
            }
            try {
                return URL(URL(context.getString(R.string.orchard_base_service_url)), mediaUrl).toString()
            } catch (ignored: MalformedURLException) {
            }
            return null
        }

    }

    private var mediaPart: MediaPart? = null
    private val imageLoader: ImageLoader<String, T> = ImageHandler.loader(context)
    private val listeners = mutableSetOf<ImageLoader.RequestListener<T>>()

    init {
        imageLoader.withCacheStrategy(ImageLoader.CacheStrategy.WITHOUT_TRANSFORMATION)
    }

    fun addListener(vararg listener: ImageLoader.RequestListener<T>) = apply{
        listeners.addAll(listener)
    }

    fun mediaPart(mediaPart: MediaPart) = apply { this.mediaPart = mediaPart }

    fun load() {
        if (mediaLoadable is ImageView) {
            val request = imageLoader.intoView()
            requestWith(imageLoader, context, mediaLoadable, mediaPart, request)?.load(mediaLoadable as ImageView)
        } else {
            Log.e(TAG, "with: your widget cannot be casted to " + ImageView::class.java.canonicalName)
        }
    }

    fun getRequest(): ImageLoader<String, T> {
        requestWith(imageLoader, context, mediaLoadable, mediaPart, null)
        return imageLoader
    }

    /**
     * Crea la richiesta per Glide nel caso in cui l'url sia valido, in caso contrario, setta il placeholder
     *
     * @param manager       manager di Glide
     * @param context       context corrente
     * @param mediaLoadable istanza di MediaLoadable
     * @param mediaPart     [MediaPart] da caricare
     * @return richiesta di Glide
     */
    @SuppressLint("SwitchIntDef")
    private fun requestWith(imageLoader: ImageLoader<String, T>, context: Context, mediaLoadable: MediaLoadable, mediaPart: MediaPart?, request: ImageLoader.ViewRequest?): ImageLoader.ViewRequest? {
        val options = mediaLoadable.options
        val animated = mediaLoadable.isAnimated

        var placeholderIdentifier = 0

        val imageView = if (mediaLoadable is ImageView) mediaLoadable else null

        if (mediaPart != null) {
            if (!TextUtils.isEmpty(mediaPart.mediaUrl) && mediaPart.mediaType == MediaType.IMAGE) {
                val mediaUrl = mediaPart.mediaUrl

                val glidePlaceholder = if (mediaLoadable.showPlaceholder()) mediaLoadable.photoPlaceholder else 0

                var externalURL: URL? = null
                try {
                    externalURL = URL(mediaUrl)
                } catch (ignored: MalformedURLException) {
                }

                if (externalURL == null) {
                    createRequestCreator(imageLoader, context, imageView, options, mediaUrl!!)
                } else {
                    imageLoader.from(externalURL.toString())
                }

                if (imageView != null && request != null) {
                    // salva lo ScaleType dell'immagine
                    val imageScaleType = mediaLoadable.mediaScaleType
                    val placeholderScaleType = mediaLoadable.placeholderScaleType

                    if (mediaLoadable.showPlaceholder() && imageScaleType != placeholderScaleType && imageView.scaleType != placeholderScaleType) {
                        imageView.scaleType = placeholderScaleType
                    }

                    // se lo ScaleType dell'immagine e quello del placeholder sono differenti bisogna iniziare la procedura per la sostituzione dello ScaleType
                    // settando lo scaleType corretto prima di onResourceReady(), si evita il lag
                    request.scaleType(imageScaleType)

                    @Suppress("UNCHECKED_CAST")
                    request.addListener(*listeners.toTypedArray() as Array<out ImageLoader.RequestListener<Drawable>>)
                    // quando lo ScaleType è differente, probabilmente uno o due frame è possibile che vengano saltati (1 frame ogni 16ms).
                    // questo succede perchè deve richiamare i metodi invalidate() e requestLayout() dello scaleType
                    request.addListener(object : ImageLoader.RequestListener<Drawable> {
                        override fun onDataLoadSuccess(resource: Drawable?) {
                            // setta il vecchio scaleType
                            if (imageView.scaleType != imageScaleType) {
                                imageView.scaleType = imageScaleType
                            }
                        }

                        override fun onDataLoadFailed() { }
                    })

                    if (glidePlaceholder > 0) {
                        request.placeHolder(glidePlaceholder)
                    }

                    if (animated) {
                        request.animation(ImageLoader.Animation.FADE)
                    } else {
                        request.animation(ImageLoader.Animation.NONE)
                    }
                }
                return request
            } else if (mediaLoadable.showPlaceholder()) {
                when (mediaPart.mediaType) {
                    MediaType.IMAGE -> placeholderIdentifier = mediaLoadable.photoPlaceholder
                    MediaType.VIDEO -> placeholderIdentifier = mediaLoadable.videoPlaceholder
                    MediaType.AUDIO -> placeholderIdentifier = mediaLoadable.audioPlaceholder
                }
            }
        } else if (mediaLoadable.showPlaceholder()) {
            placeholderIdentifier = mediaLoadable.photoPlaceholder
        }
        if (placeholderIdentifier > 0 && imageView != null) {
            imageView.scaleType = mediaLoadable.placeholderScaleType
            imageView.setImageResource(placeholderIdentifier)
        }
        return null
    }

    /**
     * Crea la richiesta per il WS aggiungendo in querystring tutti parametri stabiliti in app
     *
     * @param manager   manager di Glide
     * @param context   context corrente
     * @param imageView widget di tipo android.widget.ImageView se presente
     * @param options   configurazione da passare al WS
     * @param mediaUrl  url dell'immagine
     * @return richiesta per il WS
     */
    private fun createRequestCreator(imageLoader: ImageLoader<String, T>, context: Context, imageView: ImageView?, options: ImageOptions, mediaUrl: String) {
        var width = options.width
        var height = options.height
        var sizeIsZero = height == 0 || width == 0

        if (sizeIsZero && imageView != null) {
            height = imageView.measuredHeight
            width = imageView.measuredWidth
        }

        sizeIsZero = height == 0 || width == 0

        if (sizeIsZero) {
            val resources = context.resources
            height = resources.getDimensionPixelSize(R.dimen.orchard_image_default_height)
            width = resources.getDimensionPixelSize(R.dimen.orchard_image_default_width)
        }

        val mediaBasePath = Uri.withAppendedPath(Uri.parse(context.getString(R.string.orchard_base_service_url)), context.getString(R.string.orchard_medias_path)).toString()

        try {
            imageLoader.from(String.format(Locale.getDefault(), "%s?Path=%s&Width=%d&Height=%d&Mode=%s&Alignment=%s",
                    mediaBasePath,
                    URLEncoder.encode(mediaUrl, "UTF-8"),
                    width,
                    height,
                    options.mode,
                    options.alignment))

        } catch (ignored: UnsupportedEncodingException) {
        }
    }
}