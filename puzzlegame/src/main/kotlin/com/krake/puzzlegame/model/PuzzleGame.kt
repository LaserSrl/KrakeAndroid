package com.krake.puzzlegame.model

import com.krake.core.model.ContentItemWithGallery
import com.krake.surveys.model.Question
import com.krake.surveys.model.QuestionnairePart
import java.util.*

/**
 * Created by joel on 08/03/17.
 */

interface PuzzleGame : ContentItemWithGallery {

    val questionnairePart: QuestionnairePart?

    val question: Question? get() = questionnairePart?.questions?.firstOrNull() as? Question

    override val firstPhoto: com.krake.core.model.MediaPart?
        get() {
            val medias = filteredMedias(com.krake.core.model.MediaPart.MIME_TYPE_IMAGE)
            if (medias.size > 0)
                return medias[Random().nextInt(medias.size)]

            return null
        }

}