package com.krake.core.media.task

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import com.krake.core.extension.asBitmap
import com.krake.core.media.UploadableMediaInfo
import com.krake.core.media.loader.ImageHandler
import com.krake.core.media.watermark.Watermark
import com.krake.core.thread.AsyncTask
import com.krake.core.thread.async

/**
 * Created by antoniolig on 17/11/2017.
 */
class ImageWatermarkTask(context: Context, private var listener: Listener?) {
    private var task: AsyncTask<*>? = null
    private var context: Context? = context

    fun load(watermark: Watermark, mediaInfo: UploadableMediaInfo) {
        task = async {
            val context = this.context ?: return@async

            val mediaUri = mediaInfo.uri
            val displayMetrics = Resources.getSystem().displayMetrics

            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            var thumbNail = ImageHandler.loader<Uri, Bitmap>(context)
                    .from(mediaUri)
                    .size(screenWidth, screenHeight)
                    .asBitmap()
                    .get()

            watermark.localWaterMarkUri?.let { watermarkUri ->
                val watermarkRequest = { width: Int, height: Int ->
                    ImageHandler.loader<Uri, Bitmap>(context)
                            .from(watermarkUri)
                            .size(width, height)
                            .asBitmap()
                }
                var scaledWatermark = watermarkRequest(screenWidth, screenHeight).get()

                val heightWaterMarkPreviewScale = scaledWatermark.height.toFloat() / thumbNail.height

                val widthWaterMarkPreviewScale = scaledWatermark.width.toFloat() / thumbNail.width

                var waterMarkScaleFactor = 1f

                when (watermark.fill) {
                    Watermark.FILL_NONE -> {
                        waterMarkScaleFactor = Math.max(heightWaterMarkPreviewScale, widthWaterMarkPreviewScale)
                        if (waterMarkScaleFactor < 1)
                            waterMarkScaleFactor = 1f
                    }

                    Watermark.FILL_HORIZONTAL -> waterMarkScaleFactor = widthWaterMarkPreviewScale

                    Watermark.FILL_VERTICAL -> waterMarkScaleFactor = heightWaterMarkPreviewScale
                }

                if (waterMarkScaleFactor != 1f) {
                    scaledWatermark.recycle()
                    val newWidth = Math.ceil((scaledWatermark.width / waterMarkScaleFactor).toDouble()).toInt()
                    val newHeight = Math.ceil((scaledWatermark.height / waterMarkScaleFactor).toDouble()).toInt()
                    scaledWatermark = watermarkRequest(newWidth, newHeight).get()
                }
                val composedImage = Bitmap.createBitmap(thumbNail.width, thumbNail.height, Bitmap.Config.ARGB_8888)

                val canvas = Canvas(composedImage)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG or Paint.FILTER_BITMAP_FLAG)
                canvas.drawBitmap(thumbNail, 0f, 0f, paint)

                val position = watermark.position
                val watermarkX = when {
                    position and Watermark.BOTTOM != 0 -> thumbNail.height - scaledWatermark.height
                    position and Watermark.CENTERV != 0 -> (thumbNail.height - scaledWatermark.height) / 2
                    else -> 0
                }.toFloat()

                val watermarkY = when {
                    position and Watermark.RIGHT != 0 -> thumbNail.width - scaledWatermark.width
                    position and Watermark.CENTERH != 0 -> (thumbNail.width - scaledWatermark.width) / 2
                    else -> 0
                }.toFloat()

                canvas.drawBitmap(scaledWatermark, watermarkX, watermarkY, paint)

                if (waterMarkScaleFactor > 1)
                    scaledWatermark.recycle()

                thumbNail.recycle()
                thumbNail = composedImage
            }

            val fos = context.contentResolver.openOutputStream(mediaUri)
            if (fos != null) {
                thumbNail.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.close()
            }
        }.completed {
            listener?.onWatermarkApplied()
        }.build()

        task?.load()
    }

    fun cancel() {
        task?.cancel()
    }

    fun release() {
        cancel()
        context = null
        listener = null
    }

    interface Listener {
        fun onWatermarkApplied()
    }
}