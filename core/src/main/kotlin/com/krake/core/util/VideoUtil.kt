package com.krake.core.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri

/**
 * Utility class used to manage the videos.
 */
object VideoUtil {

    /**
     * Extracts the thumbnail as a [Bitmap] from a video.
     *
     * @param context the [Context] used to retrieve the video.
     * @param videoUri the [Uri] used to find the video on the disk.
     * @param timeMs the optional time position (expressed in milliseconds) where
     * the frame of the thumbnail will be retrieved.
     * @return the [Bitmap] that contains the information of the thumbnail.
     */
    fun extractThumbnail(context: Context, videoUri: Uri, timeMs: Long = -1): Bitmap? {
        var bitmap: Bitmap? = null
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, videoUri)
            bitmap = retriever.getFrameAtTime(timeMs)
        } catch (ignored: RuntimeException) {
            // In this case the file could be corrupted.
        } finally {
            try {
                retriever.release()
            } catch (ignored: RuntimeException) {
                // The failing of the retriever's releasing is ignored.
            }
        }
        return bitmap
    }
}