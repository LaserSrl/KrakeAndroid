package com.krake.surveys.model

import com.krake.core.media.MediaPartURLWrapper
import com.krake.core.model.MediaPart
import com.krake.core.model.RecordWithIdentifier

/**
 * Created by joel on 08/03/17.
 */
interface Answer : RecordWithIdentifier, AllFileImage {
    val answer: String?

    val correctResponse: Boolean

    val position: Long?

    val published: Boolean
}

interface AllFileImage {
    val allFiles: String? get() = null

    val image: MediaPart? get() {

        if (allFiles.isNullOrEmpty())
            return null
        else
            return MediaPartURLWrapper(allFiles!!)
    }
}