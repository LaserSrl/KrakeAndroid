package com.krake.surveys.component.module

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.krake.core.component.base.ComponentModule
import com.krake.core.component.module.LoginComponentModule
import com.krake.surveys.R
import com.krake.surveys.app.SurveyActivity
import com.krake.surveys.app.SurveyFragment
import com.krake.surveys.model.Question

/**
 * Modulo utilizzato per specificare gli attributi di un'[Activity] che mostra oggetti di tipo [Questionnaire].
 * Utilizzato principalmente da:
 * <ul>
 * <li>[SurveyActivity] e dalle [Activity] che la estendono.</li>
 * <li>[SurveyFragment] e dai [Fragment] che lo estendono.</li>
 * </ul>
 */
class SurveyComponentModule(context: Context) : ComponentModule {
    var analyticsName: String?
        private set

    var sendSurveyApiPath: String
        private set

    var dateFormat: String
        private set

    var dateTimeFormat: String
        private set

    init {
        analyticsName = null
        sendSurveyApiPath = context.getString(R.string.orchard_api_send_surveys)
        dateFormat = "dd/MM/yy"
        dateTimeFormat = "dd/MM/yy HH:mm:ss"
    }

    /**
     * Specifica il testo che verrà mandato ad analytics all'apertura della sezione.
     * DEFAULT: null -> in [SurveyActivity] il testo è uguale a [SurveyActivity.getTitle]
     *
     * @param analyticsName testo in formato [String] da mandare ad analytics.
     */
    fun analyticsName(analyticsName: String) = apply { this.analyticsName = analyticsName }

    /**
     * Specifica il path di Orchard su cui spedire il questionario.
     * DEFAULT: [R.string.orchard_api_send_surveys]
     *
     * @param sendSurveyApiPath path di Orchard su cui spedire un oggetto di tipo [Questionnaire].
     */
    fun sendSurveyApiPath(sendSurveyApiPath: String) = apply { this.sendSurveyApiPath = sendSurveyApiPath }

    /**
     * Specify dateFormat for field with answerType [Question.AnswerType.Date]
     * DEFAULT: dd/MM/yy
     *
     * @param dateFormat date format
     */
    fun dateFormat(dateFormat: String) = apply { this.dateFormat = dateFormat }

    /**
     * Specify dateTimeFormat for field with answerType [Question.AnswerType.Datetime]
     * DEFAULT: dd/MM/yy HH:mm:ss
     *
     * @param dateTimeFormat date time format
     */
    fun dateTimeFormat(dateTimeFormat: String) = apply { this.dateTimeFormat = dateTimeFormat }

    /**
     * Legge il contenuto di un [Bundle] e modifica le sue proprietà.
     *
     * @param context il [Context] utilizzato per leggere il [Bundle].
     * @param bundle container dal quale vengono letti i vari arguments.
     */
    override fun readContent(context: Context, bundle: Bundle) {
        analyticsName = bundle.getString(ARG_ANALYTICS_NAME)
        sendSurveyApiPath = bundle.getString(ARG_SEND_SURVEY_API_PATH, sendSurveyApiPath)
        dateFormat = bundle.getString(ARG_DATE_FORMAT, dateFormat)
        dateTimeFormat = bundle.getString(ARG_DATETIME_FORMAT, dateTimeFormat)
    }

    /**
     * Scrive le proprietà di un modulo su un [Bundle].
     *
     * @param context il [Context] utilizzato per creare il [Bundle] e scrivere su di esso.
     * @return container che contiene le proprietà del modulo corrente.
     */
    override fun writeContent(context: Context): Bundle {
        val bundle = Bundle()
        bundle.putString(ARG_ANALYTICS_NAME, analyticsName)
        bundle.putString(ARG_SEND_SURVEY_API_PATH, sendSurveyApiPath)
        bundle.putString(ARG_DATE_FORMAT, dateFormat)
        bundle.putString(ARG_DATETIME_FORMAT, dateTimeFormat)
        return bundle
    }

    /**
     * Definisce la lista delle classi da cui questo modulo deve dipendere.
     *
     * @return array di dipendenze.
     */
    override fun moduleDependencies(): Array<Class<out ComponentModule>> {
        return arrayOf(LoginComponentModule::class.java)
    }

    companion object {
        private const val ARG_ANALYTICS_NAME = "argAnalyticsName"
        private const val ARG_SEND_SURVEY_API_PATH = "argSendApiPath"
        private const val ARG_DATE_FORMAT = "argDateFormat"
        private const val ARG_DATETIME_FORMAT = "argDateTimeFormat"
    }
}