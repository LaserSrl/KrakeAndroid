package com.krake.core.media.task

import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.DrawableRes
import com.krake.core.R
import com.krake.core.media.MediaLoadable
import com.krake.core.media.MediaType
import com.krake.core.media.UploadableMediaInfo
import java.lang.ref.WeakReference

/**
 * [MediaInfoPreviewTask.Viewer] used for load a [Bitmap] into an [ImageView]
 * and handle the visibility of a [ProgressBar] and a container [ViewGroup]
 * if the bitmap passed is null then show a generic placeholder
 */
class ProgressMediaInfoViewer(imageView: ImageView,
                              progressBar: ProgressBar,
                              container: ViewGroup?) : MediaInfoPreviewTask.Viewer {

    private val imageViewRef = WeakReference(imageView)
    private var progressBarRef = WeakReference(progressBar)
    private var containerRef: WeakReference<ViewGroup>? = container?.let { WeakReference(it) }

    @MediaType
    private var mediaType: Int = 0

    override fun onInitLoad(mediaInfo: UploadableMediaInfo) {
        mediaType = mediaInfo.type
        containerRef?.get()?.visibility = View.GONE
        progressBarRef.get()?.visibility = View.VISIBLE
    }

    override fun onLoadSuccess(bitmap: Bitmap) {
        //if the thumbnail is null set the placeholder
        imageViewRef.get()?.setImageBitmap(bitmap)
        finishLoad()
    }

    override fun onLoadError() {
        imageViewRef.get()?.let { imageView ->
            //if the thumbnail is null set the placeholder
            imageView.setImageResource(getPlaceholderResource(imageView, mediaType))
        }
        finishLoad()
    }

    private fun finishLoad() {
        progressBarRef.get()?.visibility = View.GONE
        containerRef?.get()?.visibility = View.VISIBLE
    }

    /**
     * Ottiene il placeholder a partire dal tipo del media e dagli attributi custom dell'ImageView, se presenti
     *
     * @param imageView ImageView in cui verrà settato il placeholder (utilizzata solo per fare delle verifiche su [MediaLoadable]
     * @param mediaType tipo del media
     * @return risorsa del placeholder
     */
    @DrawableRes
    private fun getPlaceholderResource(imageView: ImageView, @MediaType mediaType: Int): Int {
        return if (imageView is MediaLoadable && imageView.showPlaceholder()) {
            when (mediaType) {
                MediaType.IMAGE -> imageView.photoPlaceholder
                MediaType.VIDEO -> imageView.videoPlaceholder
                MediaType.AUDIO -> imageView.audioPlaceholder
                else -> 0
            }
        } else {
            when (mediaType) {
                MediaType.IMAGE -> R.drawable.photo_placeholder
                MediaType.VIDEO -> R.drawable.video_placeholder
                MediaType.AUDIO -> R.drawable.audio_placeholder
                else -> 0
            }
        }
    }
}