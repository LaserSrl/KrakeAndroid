package com.krake.core.util

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri

/**
 * [Bitmap] Utility object
 */
@Deprecated("This class is now deprecated", ReplaceWith("ImageLoader or VideoUtil"))
object BitmapUtil {

    /**
     * return a scaled bitmap from a [Uri] passed
     * @param contentResolver the [ContentResolver] used for retrieve the bitmap
     * @param mediaUri the [Uri] of the [Bitmap]
     * @param desiredMaxSize max pixel size with which the bitmap will be scaled
     */
    @Deprecated("This method is now deprecated.", ReplaceWith("com.krake.core.media.loader.ImageLoader"))
    fun getScaledBitmap(contentResolver: ContentResolver, mediaUri: Uri, desiredMaxSize: Int): Bitmap {
        val options = BitmapFactory.Options()
        //with true, the bitmap will be null, but the options will be populated
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(contentResolver.openInputStream(mediaUri), null, options)
        //with false, the bitmap will be generated
        options.inJustDecodeBounds = false

        val maxSize = Math.max(options.outHeight, options.outWidth).toDouble()
        options.inSampleSize = Math.round(Math.ceil(maxSize / desiredMaxSize)).toInt()

        // return the new bitmap with the new options
        return BitmapFactory.decodeStream(contentResolver.openInputStream(mediaUri), null, options)
    }

    /**
     * return a preview of the video with a frame
     * <br></br>
     * can return a bitmap null if the video is corrupted or has a format invalid
     *
     * @param context
     * @param videoUri uri of the video
     * @param desiredMaxPreviewSize desired max size of the bitmap
     * @return the video's frame preview as bitmap
     */
    @Deprecated("This method is now deprecated", ReplaceWith("VideoUtil.extractThumbnail()"))
    fun createVideoThumbnail(context: Context, videoUri: Uri, desiredMaxPreviewSize: Int): Bitmap? {
        var bitmap: Bitmap = VideoUtil.extractThumbnail(context, videoUri) ?: return null

        // se la Bitmap è troppo grande, deve essere scalata
        val width = bitmap.width
        val height = bitmap.height
        val max = Math.max(width, height)
        if (max > desiredMaxPreviewSize) {
            val scale = desiredMaxPreviewSize.toFloat() / max
            val w = Math.round(scale * width)
            val h = Math.round(scale * height)
            bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true)
        }
        return bitmap
    }
}