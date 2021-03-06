package com.krake.surveys.model

import com.krake.core.model.RecordWithIdentifier
import java.util.*

/**
 * Created by joel on 08/03/17.
 */
@Suppress("UNCHECKED_CAST")
interface Question : RecordWithIdentifier, AllFileImage {

    val question: String?

    val questionType: String

    val section: String? get() = null

    val answers: List<*>

    val answerType : String?

    val answerTypology : AnswerType
        get() = AnswerType.parse(answerType)

    val published: Boolean

    val position: Long?

    val publishedAnswers: List<Answer>
        get() = (answers as List<Answer>).filter { it.published }

    val condition: String?

    val conditionType: String?

    val conditionBehaviour: ConditionBehaviour?
        get() = if (conditionType == null) null else ConditionBehaviour.parse(conditionType!!)

    object Type {
        const val SingleChoice = "SingleChoice"

        const val MultiChoice = "MultiChoice"

        const val OpenAnswer = "OpenAnswer"
    }

    sealed class AnswerType {
        object None : AnswerType()
        object Email : AnswerType()
        object Url : AnswerType()
        object Date : AnswerType()
        object Datetime : AnswerType()

        companion object {
            fun parse(type: String?): AnswerType {
                return when (type?.trim()?.toLowerCase(Locale.getDefault())) {
                    "datetime" -> Datetime
                    "date" -> Date
                    "url" -> Url
                    "email" -> Email
                    else -> None
                }
            }
        }
    }

    sealed class ConditionBehaviour {
        object Hide : ConditionBehaviour()
        object Show : ConditionBehaviour()

        companion object {
            fun parse(type: String): ConditionBehaviour {
                return when (type.trim().toLowerCase(Locale.getDefault())) {
                    "show" -> Show
                    else -> Hide
                }
            }
        }
    }
}