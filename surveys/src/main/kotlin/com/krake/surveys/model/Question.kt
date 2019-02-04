package com.krake.surveys.model

import com.krake.core.model.RecordWithIdentifier

/**
 * Created by joel on 08/03/17.
 */
@Suppress("UNCHECKED_CAST")
interface Question : RecordWithIdentifier, AllFileImage {

    val question: String?

    val questionType: String

    val section: String? get() = null

    val answers: List<*>

    val published: Boolean

    val position: Long?

    val publishedAnswers: List<com.krake.surveys.model.Answer>
        get() = (answers as List<com.krake.surveys.model.Answer>).filter { it.published }

    object Type {
        const val SingleChoice = "SingleChoice"

        const val MultiChoice = "MultiChoice"

        const val OpenAnswer = "OpenAnswer"
    }
}