package com.krake.surveys.model

import com.krake.core.model.ContentItem
import com.krake.core.model.RecordWithAutoroute
import com.krake.core.model.RecordWithIdentifier
import java.util.*

/**
 * Created by joel on 08/03/17.
 */
@Suppress("UNCHECKED_CAST")
interface Questionnaire : ContentItem, RecordWithAutoroute, RecordWithIdentifier {
    val questions: List<Question>
        get() = (questionnairePart?.questions ?: LinkedList<com.krake.surveys.model.Question>()) as List<Question>

    val questionnairePart: QuestionnairePart?
}

interface QuestionnairePart {
    val questions: List<*>
}