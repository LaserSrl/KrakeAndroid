package com.krake.youtube

import android.net.Uri
import com.krake.youtube.model.YoutubeVideo

object YoutubeVideoUtils {
    @JvmStatic
    fun extractVideoIdentifier(video: YoutubeVideo): String {
        val youtubeUri = Uri.parse(video.videoUrlValue)
        val identifier = youtubeUri.getQueryParameter("v")
        if (identifier == null) {
            val segments = youtubeUri.pathSegments
            return segments[segments.size - 1]
        }

        return identifier
    }
}