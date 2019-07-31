package com.krake.surveys.app;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.krake.core.OrchardError;
import com.krake.core.Signaler;
import com.krake.core.app.AnalyticsApplication;
import com.krake.core.app.OrchardDataModelFragment;
import com.krake.core.component.annotation.BundleResolvable;
import com.krake.core.data.DataModel;
import com.krake.core.media.MediaLoadable;
import com.krake.core.media.loader.MediaLoader;
import com.krake.core.model.MediaPart;
import com.krake.core.network.RemoteRequest;
import com.krake.core.network.RemoteResponse;
import com.krake.core.util.LayoutUtils;
import com.krake.core.widget.SnackbarUtils;
import com.krake.surveys.R;
import com.krake.surveys.component.module.SurveyComponentModule;
import com.krake.surveys.model.Answer;
import com.krake.surveys.model.Question;
import com.krake.surveys.model.Questionnaire;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Fragment per il caricamento dei questionari.
 * <p/>
 * Il fragment mostra tutte le domande pubbliche dei questionari. Le domande vengono divise in base al {@link Question#getSection()}.
 * <p/>
 * Il fragment necessità che i contenttype di orchard siano associati alle corrette interfacce utilizzate dalla libreria:
 * <ol>
 * <li>{@link Questionnaire}. Normalmente assegnata al contenttype Questionnaire </li>
 * <li>{@link Question}  Normalmente assegnata al contenttype QuestionRecord </li>
 * <li>{@link Answer} Normalmente assegnata al contenttype AnswerRecord</li>
 * </ol>
 * <p/>
 * La chiamata per l'invio del questionario è effettuata con una chiamata di API eseguita tramite le
 * {@link Signaler#invokeAPI(Context, RemoteRequest, boolean, Object, Function2)} .
 * <p/>
 * La chiamata di api è R.string.orchard_api_send_surveys in POST.
 */
public class SurveyFragment extends OrchardDataModelFragment implements View.OnClickListener {
    @BundleResolvable
    public SurveyComponentModule surveyComponentModule;
    private LinearLayout mLinear;
    private Button mSendButton;
    private ProgressBar mProgress;
    private Questionnaire questionnaire;
    private Handler mHandler = new Handler();
    private SurveyListener listener;

    public SurveyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_survey, container, false);
    }


    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof SurveyListener) {
            listener = (SurveyListener) context;
        }
    }

    public void onDetach() {
        super.onDetach();

        listener = null;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LayoutUtils.attachScrollingBehavior(view);

        mLinear = view.findViewById(R.id.surveyQuestionContainer);
        mSendButton = view.findViewById(R.id.sendSurveyButton);
        mSendButton.setOnClickListener(this);
        mProgress = view.findViewById(android.R.id.progress);
    }

    @Override
    public void onDataModelChanged(@org.jetbrains.annotations.Nullable DataModel dataModel) {
        if (dataModel != null && dataModel.getCacheValid()) {
            if (dataModel.getListData().size() > 0) {
                showWelcomeFirstTime();
                mProgress.setVisibility(View.GONE);
                mSendButton.setVisibility(View.VISIBLE);
                questionnaire = (Questionnaire) dataModel.getListData().get(0);
                getActivity().setTitle(questionnaire.getTitlePartTitle());

                loadDataInUI(questionnaire);
            } else {
                new AlertDialog.Builder(getActivity())
                        .setMessage(getString(R.string.error_no_survey_to_answer))
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (listener != null)
                                    listener.noSurveyAvailable(SurveyFragment.this);
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        }
    }

    private void loadDataInUI(Questionnaire questionnaire) {

        int index = mLinear.getChildCount() - 2;
        for (; index >= 0; --index) {
            mLinear.removeView(mLinear.getChildAt(index));
        }

        List<Question> questions = new ArrayList<>(questionnaire.getQuestions());

        Collections.sort(questions, new Comparator<Question>() {
            @Override
            public int compare(Question lhs, Question rhs) {

                String lSection = lhs.getSection();
                String rSection = rhs.getSection();
                if (lSection != null && rSection != null) {
                    int sectionCompare = lSection.compareToIgnoreCase(rSection);
                    if (sectionCompare == 0)
                        return lhs.getPosition().compareTo(rhs.getPosition());

                    return sectionCompare;
                } else if (lSection == rSection)
                    return 0;
                else if (lSection == null)
                    return -1;

                return 1;
            }
        });

        LayoutInflater inflater = getActivity().getLayoutInflater();

        String lastSection = "";

        for (Question record : questions) {
            if (record.getPublished()) {
                String sectionRecord = record.getSection();
                if (sectionRecord == null)
                    sectionRecord = "";
                if (!sectionRecord.equalsIgnoreCase(lastSection)) {
                    lastSection = sectionRecord;

                    TextView textView = (TextView) inflater.inflate(R.layout.survey_section, null);

                    textView.setText(lastSection);

                    mLinear.addView(textView, mLinear.getChildCount() - 1);
                }

                //add views only if question type is SingleChoice or OpenAnswer (for now)
                String questionType = record.getQuestionType();
                if (questionType.equalsIgnoreCase(Question.Type.SingleChoice) ||
                        questionType.equalsIgnoreCase(Question.Type.OpenAnswer) ||
                        questionType.equalsIgnoreCase(Question.Type.MultiChoice)) {

                    View questionView = inflater.inflate(R.layout.survey_question_text_image, null);
                    setQuestionTextAndImage(questionView, record);
                    mLinear.addView(questionView, mLinear.getChildCount() - 1);

                    if (questionType.equalsIgnoreCase(Question.Type.SingleChoice)) {

                        List<Answer> answers = new ArrayList<>(record.getPublishedAnswers());

                        Collections.sort(answers, new Comparator<Answer>() {
                            @Override
                            public int compare(Answer lhs, Answer rhs) {
                                return lhs.getPosition().compareTo(rhs.getPosition());
                            }
                        });

                        int size = answers.size();

                        boolean imageInAnswers = false;

                        for (Answer answer : answers) {
                            if (!TextUtils.isEmpty(answer.getAllFiles())) {
                                imageInAnswers = true;
                                break;
                            }
                        }

                        if (size <= getResources().getInteger(R.integer.survey_max_number_of_radio_group_answers) && !imageInAnswers) {
                            View view = inflater.inflate(R.layout.survey_radio_group, null);
                            view.setTag(questionType);

                            RadioGroup mChoicesContainer = view.findViewById(R.id.surveyRadioGroup);
                            mChoicesContainer.setTag(record.getIdentifier());

                            for (Answer answer : answers) {
                                RadioButton answerButton = (RadioButton) inflater.inflate(R.layout.survey_radio_button, null);

                                answerButton.setId((int) answer.getIdentifier());
                                answerButton.setText(answer.getAnswer());
                                answerButton.setTag(answer.getIdentifier());

                                mChoicesContainer.addView(answerButton);
                            }

                            mLinear.addView(view, mLinear.getChildCount() - 1);
                        } else {
                            Spinner spinner = (Spinner) inflater.inflate(R.layout.survey_spinner, null);

                            spinner.setTag(questionType);

                            spinner.setAdapter(new AnswerAdapter(getActivity(), R.layout.survey_answer_text_image, android.R.id.text1, answers));

                            mLinear.addView(spinner, mLinear.getChildCount() - 1);
                        }
                    } else if (questionType.equalsIgnoreCase(Question.Type.OpenAnswer)) {

                        View view = inflater.inflate(R.layout.survey_open_answer, null);
                        view.setTag(questionType);

                        EditText editText = view.findViewById(R.id.surveyOpenAnswerEditText);
                        editText.setTag(record.getIdentifier());

                        mLinear.addView(view, mLinear.getChildCount() - 1);
                    } else if (questionType.equalsIgnoreCase(Question.Type.MultiChoice)) {

                        List<Answer> answers = new ArrayList<>(record.getPublishedAnswers());

                        Collections.sort(answers, new Comparator<Answer>() {
                            @Override
                            public int compare(Answer lhs, Answer rhs) {
                                return lhs.getPosition().compareTo(rhs.getPosition());
                            }
                        });


                        for (Answer answer : answers) {
                            CheckBox check = (CheckBox) inflater.inflate(R.layout.survey_check_box, null);


                            check.setText(answer.getAnswer());
                            check.setId((int) answer.getIdentifier());
                            check.setTag(answer.getIdentifier());

                            mLinear.addView(check, mLinear.getChildCount() - 1);

                            MediaPart media = answer.getImage();
                            if (media != null) {
                                ImageView imageView = (ImageView) inflater.inflate(R.layout.survey_image, null);
                                MediaLoader.Companion.with(this, (MediaLoadable) imageView)
                                        .mediaPart(media)
                                        .load();

                                mLinear.addView(imageView, mLinear.getChildCount() - 1);
                            }
                        }
                    }
                }
            }
        }
    }

    private void setQuestionTextAndImage(View view, Question record) {
        TextView questionTextView = view.findViewById(R.id.questionTextView);
        questionTextView.setText(record.getQuestion());

        MediaPart questionMedia = record.getImage();

        ImageView imageView = view.findViewById(R.id.questionImageView);

        if (questionMedia != null) {
            imageView.setVisibility(View.VISIBLE);
            MediaLoader.Companion.with(this, (MediaLoadable) imageView)
                    .mediaPart(questionMedia)
                    .load();
        } else {
            imageView.setVisibility(View.GONE);
        }

    }

    @Override
    public void onDataLoadingError(@NotNull OrchardError orchardError) {

    }

    @Override
    public void onClick(View v) {
        if (v == mSendButton) {
            sendAnswers();
        } else if (v instanceof CheckedTextView) {
            ((CheckedTextView) v).setChecked(!((CheckedTextView) v).isChecked());
        }
    }

    private void sendAnswers() {

        JsonArray answers = new JsonArray();
        for (int i = 0; i < mLinear.getChildCount() - 1; ++i) {
            View view = mLinear.getChildAt(i);
            JsonObject answer = new JsonObject();

            if (view instanceof RadioGroup) {
                RadioGroup mChoicesContainer = (RadioGroup) view;
                if (mChoicesContainer.getCheckedRadioButtonId() != -1) {
                    RadioButton selectedButton = mChoicesContainer.findViewById(mChoicesContainer.getCheckedRadioButtonId());
                    answer.addProperty("Id", ((Long) selectedButton.getTag()));
                } else
                    continue;
            } else if (view instanceof Spinner) {
                Spinner spinner = (Spinner) view;

                Answer selectedItem = (Answer) spinner.getSelectedItem();

                answer.addProperty("Id", selectedItem.getIdentifier());
            } else if (view instanceof EditText) {

                EditText mChoicesContainer = (EditText) view;
                if (!TextUtils.isEmpty(mChoicesContainer.getText().toString())) {
                    answer.addProperty("QuestionRecord_Id", ((Long) mChoicesContainer.getTag()));
                    answer.addProperty("AnswerText", mChoicesContainer.getText().toString());
                } else
                    continue;
            } else if (view instanceof CheckBox) {
                if (((CheckBox) view).isChecked()) {
                    answer.addProperty("Id", view.getTag().toString());
                }
            }


            if (answer.entrySet().size() > 0) {
                answers.add(answer);
            }
        }

        if (answers.size() > 0) {
            RemoteRequest request = new RemoteRequest(getActivity())
                    .setPath(surveyComponentModule.getSendSurveyApiPath())
                    .setMethod(RemoteRequest.Method.POST)
                    .setBody(answers);

            updateUiWithSendingAnswers(true);
            Signaler.shared.invokeAPI(getActivity(),
                    request,
                    true,
                    null,
                    new Function2<RemoteResponse, OrchardError, Unit>() {
                        @Override
                        public Unit invoke(RemoteResponse remoteResponse, OrchardError orchardError) {
                            updateUiWithSendingAnswers(false);

                            if (remoteResponse != null) {
                                if (questionnaire != null) {
                                    Bundle b = new Bundle();
                                    b.putString(FirebaseAnalytics.Param.CONTENT_TYPE, questionnaire.getClass().getSimpleName());
                                    b.putString(FirebaseAnalytics.Param.ITEM_ID, questionnaire.getAutoroutePartDisplayAlias());

                                    ((AnalyticsApplication) getActivity().getApplication())
                                            .logEvent("survey_answered", b);

                                }
                                SnackbarUtils.showCloseSnackbar(mLinear, R.string.thanks_for_taking_survey,
                                        mHandler, new Runnable() {
                                            @Override
                                            public void run() {
                                                if (listener != null)
                                                    listener.onSurveySent(SurveyFragment.this);
                                            }
                                        });
                            } else {
                                SnackbarUtils.createSnackbar(mLinear, orchardError.getUserFriendlyMessage(getActivity()), Snackbar.LENGTH_LONG).show();
                            }
                            return null;
                        }
                    });

        } else {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.error_sending_survey))
                    .setMessage(getString(R.string.error_no_questions_answered))
                    .setNeutralButton(android.R.string.ok, null)
                    .show();
        }
    }

    private void updateUiWithSendingAnswers(boolean sending) {
        mSendButton.setEnabled(!sending);
    }

    private void showWelcomeFirstTime() {
        if (!getDisplayedWelcome() && !TextUtils.isEmpty(getString(R.string.survey_welcome))) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.survey_welcome)
                    .setNeutralButton(android.R.string.ok, null)
                    .show();
            setDisplayedWelcome();
        }
    }

    private SharedPreferences openPreferences() {
        return getActivity().getSharedPreferences(getClass().getSimpleName(), Context.MODE_PRIVATE);
    }

    private boolean getDisplayedWelcome() {
        return openPreferences().getBoolean("Displayed", false);
    }

    private void setDisplayedWelcome() {
        openPreferences().edit().putBoolean("Displayed", true).apply();
    }


    private class AnswerAdapter extends ArrayAdapter<Answer> {
        public AnswerAdapter(Context context, int resource, int textViewResourceId, List<Answer> objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            setupView(view, getItem(position));
            return view;
        }

        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            setupView(view, getItem(position));
            return view;
        }

        private void setupView(View view, Answer answer) {
            view.setTag(answer.getIdentifier());
            TextView textView = view.findViewById(android.R.id.text1);

            textView.setText(answer.getAnswer());

            MediaPart media = answer.getImage();

            ImageView imageView = view.findViewById(android.R.id.icon);
            if (media != null) {
                imageView.setVisibility(View.VISIBLE);
                MediaLoader.Companion.with(SurveyFragment.this, (MediaLoadable) imageView)
                        .mediaPart(media)
                        .load();
            } else {
                imageView.setVisibility(View.GONE);
            }
        }
    }
}
