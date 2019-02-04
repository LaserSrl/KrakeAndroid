package com.krake.core.model

import com.krake.core.media.MediaType
import java.util.*

/**
 * Created by joel on 28/02/17.
 */

interface MediaPart {
    val mimeType: String?
    val fileName: String?
    val title: String?
    val folderPath: String?
    val logicalType: String?
    val mediaUrl: String?

    @MediaType
    val mediaType: Int
        get() {
            if (mimeType != null) {
                val mimeType = mimeType!!.toLowerCase(Locale.US)
                when {
                    mimeType.startsWith(MediaPart.MIME_TYPE_IMAGE) -> return MediaType.IMAGE
                    mimeType.startsWith(MediaPart.MIME_TYPE_AUDIO) -> return MediaType.AUDIO
                    mimeType.startsWith(MediaPart.MIME_TYPE_VIDEO) || mimeType.startsWith(MediaPart.MIME_TYPE_TEXT_HTML) -> return MediaType.VIDEO
                }
            }
            return MediaType.IMAGE
        }

    companion object {
        const val MIME_TYPE_IMAGE = "image"
        const val MIME_TYPE_VIDEO = "video"
        const val MIME_TYPE_TEXT_HTML = "text/html"
        const val MIME_TYPE_AUDIO = "audio"
    }
}
