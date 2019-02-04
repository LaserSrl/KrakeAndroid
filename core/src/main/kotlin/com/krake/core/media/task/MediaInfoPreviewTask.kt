package com.krake.core.media.task

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.support.annotation.Px
import com.krake.core.extension.asBitmap
import com.krake.core.media.MediaType
import com.krake.core.media.UploadableMediaInfo
import com.krake.core.media.loader.ImageHandler
import com.krake.core.thread.AsyncTask
import com.krake.core.thread.async
import com.krake.core.util.VideoUtil

/**
 * Task that load a scaled bitmap for the preview for an [UploadableMediaInfo]
 */
class MediaInfoPreviewTask(context: Context, viewer: Viewer) {

    companion object {
        @Px
        private const val PREVIEW_IMAGE_SIZE: Int = 700
    }

    private var context: Context? = context
    private var viewer: Viewer? = viewer
    private var task: AsyncTask<Bitmap?>? = null

    fun load(mediaInfo: UploadableMediaInfo) {
        viewer?.onInitLoad(mediaInfo)

        task = async {
            val context = context ?: return@async null
            val mediaUri = mediaInfo.uri ?: return@async null

            when (mediaInfo.type) {
                MediaType.IMAGE -> ImageHandler.loader<Uri, Bitmap>(context)
                        .from(mediaUri)
                        .size(PREVIEW_IMAGE_SIZE, PREVIEW_IMAGE_SIZE)
                        .asBitmap()
                        .get()

                MediaType.VIDEO -> {
                    val videoBitmap = VideoUtil.extractThumbnail(context, mediaUri)
                    if (videoBitmap != null) {
                        ImageHandler.loader<Bitmap, Bitmap>(context)
                                .from(videoBitmap)
                                .size(PREVIEW_IMAGE_SIZE, PREVIEW_IMAGE_SIZE)
                                .asBitmap()
                                .get()
                    } else videoBitmap
                }
                else -> null
            }
        }.completed { bitmap ->
            if (bitmap == null)
                viewer?.onLoadError()
            else
                viewer?.onLoadSuccess(bitmap)
        }.error {
            viewer?.onLoadError()
        }.build()
        task?.load()
    }

    fun cancel() {
        task?.cancel()
    }

    fun release() {
        cancel()
        context = null
        viewer = null
    }

    /**
     * Interface that is used from a [MediaInfoPreviewTask]
     * for delegate the view of a bitmap
     */
    interface Viewer {
        /**
         * call when the task is going to start
         * @param mediaInfo info that is going to be taken for start the task
         */
        fun onInitLoad(mediaInfo: UploadableMediaInfo)

        /**
         * called when the bitmap is available
         * @param bitmap created in the task
         */
        fun onLoadSuccess(bitmap: Bitmap)

        /**
         * called when a generic error occuded in the task
         */
        fun onLoadError()
    }
}