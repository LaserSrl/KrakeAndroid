package com.krake.surveys.app

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.krake.core.OrchardError
import com.krake.core.Signaler
import com.krake.core.app.AnalyticsApplication
import com.krake.core.app.DateTimePickerFragment
import com.krake.core.app.OrchardDataModelFragment
import com.krake.core.component.annotation.BundleResolvable
import com.krake.core.data.DataModel
import com.krake.core.media.MediaLoadable
import com.krake.core.media.loader.MediaLoader.Companion.with
import com.krake.core.network.RemoteRequest
import com.krake.core.network.RemoteResponse
import com.krake.core.util.LayoutUtils
import com.krake.core.widget.SnackbarUtils
import com.krake.core.widget.SnackbarUtils.createSnackbar
import com.krake.surveys.R
import com.krake.surveys.component.module.SurveyComponentModule
import com.krake.surveys.model.Answer
import com.krake.surveys.model.Question
import com.krake.surveys.model.Questionnaire
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment per il caricamento dei questionari.
 *
 *
 * Il fragment mostra tutte le domande pubbliche dei questionari. Le domande vengono divise in base al [Question.section].
 *
 *
 * Il fragment necessità che i contenttype di orchard siano associati alle corrette interfacce utilizzate dalla libreria:
 *
 *  1. [Questionnaire]. Normalmente assegnata al contenttype Questionnaire
 *  1. [Question]  Normalmente assegnata al contenttype QuestionRecord
 *  1. [Answer] Normalmente assegnata al contenttype AnswerRecord
 *
 *
 *
 * La chiamata per l'invio del questionario è effettuata con una chiamata di API eseguita tramite le
 * [Signaler.invokeAPI] .
 *
 *
 * La chiamata di api è R.string.orchard_api_send_surveys in POST.
 */
class SurveyFragment : OrchardDataModelFragment(),
    View.OnClickListener,
    DateTimePickerFragment.OnDateTimePickerListener,
    AdapterView.OnItemSelectedListener {

    @BundleResolvable
    lateinit var surveyComponentModule: SurveyComponentModule
    private lateinit var mLinear: LinearLayout
    private lateinit var mSendButton: Button
    private lateinit var mProgress: ProgressBar
    private var questionnaire: Questionnaire? = null
    private val mHandler = Handler()
    private var listener: SurveyListener? = null
    private var currentDateEditText: EditText? = null
    private var currentDateFormat: String? = null
    private val answersToSend = mutableListOf<AnswerToSend>()
    private var questionDividerHeight = 0
    private var sectionDividerHeight = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_survey, container, false)
    }

    override fun onAttach(activity: Context) {
        super.onAttach(activity)
        if (activity is SurveyListener) {
            listener = activity
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        LayoutUtils.attachScrollingBehavior(view)
        mLinear = view.findViewById(R.id.surveyQuestionContainer)
        mSendButton = view.findViewById(R.id.sendSurveyButton)
        mSendButton.setOnClickListener(this)
        mProgress = view.findViewById(android.R.id.progress)
        questionDividerHeight = requireActivity().resources.getDimensionPixelSize(R.dimen.surveyQuestionDividerHeight)
        sectionDividerHeight = requireActivity().resources.getDimensionPixelSize(R.dimen.surveySectionDividerHeight)
    }

    override fun onDataModelChanged(dataModel: DataModel?) {
        if (dataModel != null && dataModel.cacheValid) {
            if (dataModel.listData.isNotEmpty()) {
                showWelcomeFirstTime()
                mProgress.visibility = View.GONE
                mSendButton.visibility = View.VISIBLE
                questionnaire = dataModel.listData[0] as Questionnaire
                requireActivity().title = questionnaire!!.titlePartTitle
                loadDataInUI(questionnaire)
            } else {
                AlertDialog.Builder(requireActivity())
                    .setMessage(getString(R.string.error_no_survey_to_answer))
                    .setNeutralButton(
                        android.R.string.ok
                    ) { _, _ -> if (listener != null) listener!!.noSurveyAvailable(this@SurveyFragment) }
                    .setCancelable(false)
                    .show()
            }
        }
    }

    private fun loadDataInUI(questionnaire: Questionnaire?) {
        var index = mLinear.childCount - 2
        while (index >= 0) {
            mLinear.removeView(mLinear.getChildAt(index))
            --index
        }
        val questions: List<Question> = ArrayList(questionnaire!!.questions)
        Collections.sort(questions,
            Comparator { lhs, rhs ->
                val lSection = lhs.section
                val rSection = rhs.section
                if (lSection != null && rSection != null) {
                    val sectionCompare = lSection.compareTo(rSection, ignoreCase = true)
                    return@Comparator if (sectionCompare == 0) lhs.position!!.compareTo(rhs.position!!) else sectionCompare
                } else if (lSection === rSection) return@Comparator 0 else if (lSection == null) return@Comparator -1
                1
            })
        val inflater = requireActivity().layoutInflater
        var lastSection: String? = ""
        for (record in questions) {
            if (record.published) {
                var sectionRecord = record.section
                if (sectionRecord == null) sectionRecord = ""
                if (!sectionRecord.equals(lastSection, ignoreCase = true)) {
                    lastSection = sectionRecord
                    val textView = inflater.inflate(R.layout.survey_section, null) as TextView
                    textView.text = lastSection
                    mLinear.addView(textView, mLinear.childCount - 1)
                    (textView.layoutParams as LinearLayout.LayoutParams).topMargin = sectionDividerHeight

                }
                //add views only if question type is SingleChoice or OpenAnswer (for now)
                val questionType = record.questionType
                if (questionType.equals(Question.Type.SingleChoice, ignoreCase = true) ||
                    questionType.equals(Question.Type.OpenAnswer, ignoreCase = true) ||
                    questionType.equals(Question.Type.MultiChoice, ignoreCase = true)) {

                    val questionView = inflater.inflate(R.layout.survey_question_text_image, null)
                    setQuestionTextAndImage(questionView, record)
                    mLinear.addView(questionView, mLinear.childCount - 1)
                    (questionView.layoutParams as LinearLayout.LayoutParams).topMargin = questionDividerHeight
                    setQuestionViewVisibility(questionView, record)

                    if (questionType.equals(Question.Type.SingleChoice, ignoreCase = true)) {
                        val answers: List<Answer> = record.publishedAnswers.sortedBy { it.position }

                        val size = answers.size
                        var imageInAnswers = false
                        for (answer in answers) {
                            if (!TextUtils.isEmpty(answer.allFiles)) {
                                imageInAnswers = true
                                break
                            }
                        }
                        if (size <= resources.getInteger(R.integer.survey_max_number_of_radio_group_answers) && !imageInAnswers) {
                            val view = inflater.inflate(R.layout.survey_radio_group, null)
                            view.tag = questionType
                            val mChoicesContainer = view.findViewById<RadioGroup>(R.id.surveyRadioGroup)
                            mChoicesContainer.tag = record.identifier

                            for (answer in answers) {
                                val answerButton = inflater.inflate(
                                    R.layout.survey_radio_button,
                                    null
                                ) as RadioButton
                                answerButton.id = answer.identifier.toInt()
                                answerButton.text = answer.answer
                                answerButton.tag = answer.identifier

                                val checked = answersToSend.firstOrNull { it.questionId == answer.identifier } != null
                                if (answerButton.isChecked != checked)
                                    answerButton.isChecked = checked

                                mChoicesContainer.addView(answerButton)
                            }

                            mChoicesContainer.setOnCheckedChangeListener { _, _ -> onChoiceAnswerCheckedChanged() }
                            mLinear.addView(view, mLinear.childCount - 1)
                            setQuestionViewVisibility(view, record)

                        } else {
                            val spinner = inflater.inflate(R.layout.survey_spinner, null) as Spinner
                            spinner.tag = questionType
                            spinner.adapter = AnswerAdapter(
                                activity,
                                R.layout.survey_answer_text_image,
                                android.R.id.text1,
                                answers
                            )
                            val selection = answers.indexOfFirst { answer -> answersToSend.firstOrNull { it.questionId == answer.identifier } != null }
                            if (selection >= 0 && spinner.selectedItemPosition != selection)
                                spinner.setSelection(selection, false)
                            else
                                spinner.setSelection(0, false)

                            spinner.onItemSelectedListener = this
                            mLinear.addView(spinner, mLinear.childCount - 1)
                            setQuestionViewVisibility(spinner, record)

                        }
                    } else if (questionType.equals(Question.Type.OpenAnswer, ignoreCase = true)) {
                        val view = inflater.inflate(R.layout.survey_open_answer, null)
                        view.tag = questionType
                        val editText = view.findViewById<EditText>(R.id.surveyOpenAnswerEditText)
                        editText.tag = record

                        val currentValue = answersToSend.firstOrNull { it.questionId == record.identifier } as? AnswerToSend.TextAnswer
                        editText.setText(currentValue?.text ?: "")

                        when (record.answerTypology) {
                            is Question.AnswerType.Datetime -> buildDateTimeEditText(editText, surveyComponentModule.dateTimeFormat, true)
                            is Question.AnswerType.Date -> buildDateTimeEditText(editText, surveyComponentModule.dateFormat, false)
                            is Question.AnswerType.Url -> editText.inputType = InputType.TYPE_TEXT_VARIATION_URI
                            is Question.AnswerType.Email -> editText.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        }

                        mLinear.addView(view, mLinear.childCount - 1)
                        setQuestionViewVisibility(view, record)

                    } else if (questionType.equals(Question.Type.MultiChoice, ignoreCase = true)) {
                        val answers: List<Answer> = record.publishedAnswers.sortedBy { it.position }

                        for (answer in answers) {
                            val check =
                                inflater.inflate(R.layout.survey_check_box, null) as CheckBox
                            check.text = answer.answer
                            check.id = answer.identifier.toInt()
                            check.tag = answer.identifier
                            check.isChecked = answersToSend.firstOrNull { it.questionId == answer.identifier } != null
                            check.setOnCheckedChangeListener { _, _ -> onChoiceAnswerCheckedChanged() }
                            mLinear.addView(check, mLinear.childCount - 1)
                            setQuestionViewVisibility(check, record)

                            val media = answer.image
                            if (media != null) {
                                val imageView = inflater.inflate(
                                    R.layout.survey_image,
                                    null
                                ) as ImageView
                                with(this, imageView as MediaLoadable)
                                    .mediaPart(media)
                                    .load()
                                mLinear.addView(imageView, mLinear.childCount - 1)
                                setQuestionViewVisibility(imageView, record)

                            }
                        }
                    }
                }
            }
        }
    }

    private fun buildDateTimeEditText(editText: EditText, format: String, withTimePicker: Boolean) {
        editText.isFocusable = false

        editText.setOnClickListener {
            currentDateEditText = it as EditText
            currentDateFormat = format

            val dateToShow = if (currentDateEditText!!.text.isNullOrEmpty()) Date() else SimpleDateFormat(format, Locale.getDefault()).parse(currentDateEditText!!.text.toString())

            DateTimePickerFragment.newInstance(dateToShow, withTimePicker)
                .show(childFragmentManager, null)
        }
    }

    private fun setQuestionTextAndImage(view: View, record: Question) {
        val questionTextView =
            view.findViewById<TextView>(R.id.questionTextView)
        questionTextView.text = record.question
        val questionMedia = record.image
        val imageView =
            view.findViewById<ImageView>(R.id.questionImageView)
        if (questionMedia != null) {
            imageView.visibility = View.VISIBLE
            with(this, imageView as MediaLoadable)
                .mediaPart(questionMedia)
                .load()
        } else {
            imageView.visibility = View.GONE
        }
    }

    override fun onDataLoadingError(orchardError: OrchardError) {}
    override fun onClick(v: View) {
        if (v === mSendButton) {
            sendAnswers()
        } else if (v is CheckedTextView) {
            v.isChecked = !v.isChecked
        }
    }

    private fun buildAnswersToSend() {
        answersToSend.clear()
        for (i in 0 until mLinear.childCount - 1) {
            val view = mLinear.getChildAt(i)
            if (view is RadioGroup) {
                if (view.checkedRadioButtonId != -1) {
                    val selectedButton =
                        view.findViewById<RadioButton>(view.checkedRadioButtonId)
                    answersToSend.add(
                        AnswerToSend.BooleanAnswer(
                            id = selectedButton.tag as Long
                        )
                    )
                } else continue
            } else if (view is Spinner) {
                val selectedItem =
                    view.selectedItem as Answer
                answersToSend.add(
                    AnswerToSend.BooleanAnswer(
                        id = selectedItem.identifier
                    )
                )
            } else if (view is EditText) {
                if (!TextUtils.isEmpty(view.text.toString())) {
                    val record = view.tag as Question
                    answersToSend.add(AnswerToSend.TextAnswer(
                        id = record.identifier,
                        text = view.text.toString(),
                        type = record.answerTypology)
                    )
                } else continue
            } else if (view is CheckBox) {
                if (view.isChecked) {
                    answersToSend.add(
                        AnswerToSend.BooleanAnswer(
                            id = view.getTag().toString().toLong()
                        )
                    )
                }
            }
        }
    }

    private fun checkAndPrepareAnswersJsonToSend(): JsonArray? {
        val answers = JsonArray()

        answersToSend.forEach {
            val answer = JsonObject()

            when (it) {
                is AnswerToSend.BooleanAnswer -> answer.addProperty("Id", it.id)
                is AnswerToSend.TextAnswer -> {

                    when (it.type) {
                        is Question.AnswerType.Email -> {
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(it.text).matches()) {
                                AlertDialog.Builder(requireContext())
                                    .setTitle(R.string.survey_answer_validation_alert_title)
                                    .setMessage(R.string.error_invalid_mail)
                                    .setPositiveButton(android.R.string.ok) { dialog, _ ->
                                        dialog.dismiss()
                                    }
                                    .show()
                                return null
                            }
                        }
                        is Question.AnswerType.Url -> {
                            if (!android.util.Patterns.WEB_URL.matcher(it.text).matches()) {
                                AlertDialog.Builder(requireContext())
                                    .setTitle(R.string.survey_answer_validation_alert_title)
                                    .setMessage(R.string.survey_answer_validation_error_message_url)
                                    .setPositiveButton(android.R.string.ok) { dialog, _ ->
                                        dialog.dismiss()
                                    }
                                    .show()
                                return null
                            }
                        }
                    }

                    answer.addProperty("QuestionRecord_Id", it.id)
                    answer.addProperty("AnswerText", it.text)
                }
            }

            if (answer.entrySet().size > 0) {
                answers.add(answer)
            }
        }

        return answers
    }

    private fun onChoiceAnswerCheckedChanged() {
        buildAnswersToSend()
        loadDataInUI(questionnaire)
    }

    private fun setQuestionViewVisibility(view: View, question: Question) {
        var visible = question.conditionBehaviour == null || question.conditionIdentifiers == null || question.conditionBehaviour == Question.ConditionBehaviour.Hide

        if (question.conditionIdentifiers != null) {
            val conditionRespected = question.conditionIdentifiers!!.all { identifier -> answersToSend.any { it.questionId == identifier } }

            visible = if (conditionRespected) {
                question.conditionBehaviour == Question.ConditionBehaviour.Show
            } else {
                question.conditionBehaviour != Question.ConditionBehaviour.Show
            }
        }

        view.isVisible = visible
    }

    private fun sendAnswers() {
        buildAnswersToSend()
        val answers = checkAndPrepareAnswersJsonToSend() ?: return

        if (answers.size() > 0) {
            val request = RemoteRequest(requireActivity())
                .setPath(surveyComponentModule.sendSurveyApiPath)
                .setMethod(RemoteRequest.Method.POST)
                .setBody(answers)
            updateUiWithSendingAnswers(true)
            Signaler.shared.invokeAPI(
                requireActivity(),
                request,
                true,
                null
            ) { remoteResponse: RemoteResponse?, orchardError: OrchardError? ->
                updateUiWithSendingAnswers(false)
                val activity: Activity? = activity
                if (activity != null) {
                    if (remoteResponse != null) {
                        if (questionnaire != null) {
                            val b = Bundle()
                            b.putString(
                                FirebaseAnalytics.Param.CONTENT_TYPE,
                                questionnaire!!.javaClass.simpleName
                            )
                            b.putString(
                                FirebaseAnalytics.Param.ITEM_ID,
                                questionnaire!!.autoroutePartDisplayAlias
                            )
                            (activity.application as AnalyticsApplication)
                                .logEvent("survey_answered", b)
                        }
                        SnackbarUtils.showCloseSnackbar(
                            mLinear,
                            R.string.thanks_for_taking_survey,
                            mHandler,
                            Runnable { if (listener != null) listener!!.onSurveySent(this@SurveyFragment) })
                    } else {
                        createSnackbar(
                            mLinear,
                            orchardError!!.getUserFriendlyMessage(activity),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
                null
            }
        } else {
            AlertDialog.Builder(requireActivity())
                .setTitle(getString(R.string.error_sending_survey))
                .setMessage(getString(R.string.error_no_questions_answered))
                .setNeutralButton(android.R.string.ok, null)
                .show()
        }
    }

    private fun updateUiWithSendingAnswers(sending: Boolean) {
        mSendButton.isEnabled = !sending
    }

    private fun showWelcomeFirstTime() {
        if (!displayedWelcome && !TextUtils.isEmpty(getString(R.string.survey_welcome))) {
            AlertDialog.Builder(requireActivity())
                .setMessage(R.string.survey_welcome)
                .setNeutralButton(android.R.string.ok, null)
                .show()
            setDisplayedWelcome()
        }
    }

    private fun openPreferences(): SharedPreferences {
        return requireActivity().getSharedPreferences(
            javaClass.simpleName,
            Context.MODE_PRIVATE
        )
    }

    private val displayedWelcome: Boolean
        get() = openPreferences().getBoolean("Displayed", false)

    private fun setDisplayedWelcome() {
        openPreferences().edit().putBoolean("Displayed", true).apply()
    }

    private inner class AnswerAdapter(context: Context?, resource: Int, textViewResourceId: Int, objects: List<Answer>?) :
        ArrayAdapter<Answer?>(context!!, resource, textViewResourceId, objects!!) {

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            setupView(view, getItem(position))
            return view
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            setupView(view, getItem(position))
            return view
        }

        private fun setupView(view: View, answer: Answer?) {
            view.tag = answer!!.identifier
            val textView = view.findViewById<TextView>(android.R.id.text1)
            textView.text = answer.answer
            val media = answer.image
            val imageView = view.findViewById<ImageView>(android.R.id.icon)
            if (media != null) {
                imageView.visibility = View.VISIBLE
                with(this@SurveyFragment, imageView as MediaLoadable)
                    .mediaPart(media)
                    .load()
            } else {
                imageView.visibility = View.GONE
            }
        }
    }

    override fun onDatePicked(startDate: Date, endDate: Date?) {
        val dateFormat = SimpleDateFormat(currentDateFormat!!, Locale.getDefault())
        currentDateEditText!!.setText(dateFormat.format(startDate))
        currentDateEditText = null
        currentDateFormat = null
    }

    override fun onNothingSelected(p0: AdapterView<*>?) { }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        onChoiceAnswerCheckedChanged()
    }

    sealed class AnswerToSend(val questionId: Long) {
        data class BooleanAnswer(val id: Long): AnswerToSend(id)
        data class TextAnswer(val id: Long, val text: String, val type: Question.AnswerType): AnswerToSend(id)
    }
}