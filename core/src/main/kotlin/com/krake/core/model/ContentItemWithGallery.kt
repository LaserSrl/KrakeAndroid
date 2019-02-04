package com.krake.core.model

/**
 * Created by joel on 22/02/17.
 */
@Suppress("UNCHECKED_CAST")
interface ContentItemWithGallery : ContentItem {

    val firstPhoto get() = filteredMedias(MediaPart.MIME_TYPE_IMAGE).firstOrNull()

    val firstMedia get() = medias.firstOrNull()

    val galleryMediaParts: List<*>

    val medias get() = galleryMediaParts as List<com.krake.core.model.MediaPart>

    fun filteredMedias(mimeTypePrefix: String): List<com.krake.core.model.MediaPart> {
        return medias.filter { it.mimeType?.startsWith(mimeTypePrefix) ?: false }
    }
}