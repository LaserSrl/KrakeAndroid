package com.krake.gamequiz;

import android.animation.Animator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.krake.core.OrchardError;
import com.krake.core.app.OrchardDataModelFragment;
import com.krake.core.data.DataModel;
import com.krake.core.extension.ImageLoaderExtensionsKt;
import com.krake.core.media.MediaLoadable;
import com.krake.core.media.loader.ImageLoader;
import com.krake.core.media.loader.MediaLoader;
import com.krake.core.util.LayoutUtils;
import com.krake.surveys.model.Answer;
import com.krake.surveys.model.Question;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class QuestionFragment extends OrchardDataModelFragment
        implements View.OnClickListener,
        View.OnTouchListener,
        Handler.Callback,
        ImageLoader.RequestListener<Drawable> {

    private static final String STATE_ANSWERED = "Answred";
    private static final String STATE_BEGIN_TIME = "BeginTime";
    private static final String STATE_FINAL_POINTS = "FinalPoints";

    private static final int MESSAGE_TIME_CHANGED = 347348;
    private ViewGroup mQuestionAndAnswerContainer;
    private ImageView mQuestionImageView;
    private TextView mQuestionText;
    private Question question;
    private View mAnswerContainerView;
    private TextView mAnswerTitleView;
    private TextView mAnswerSubtitleView;
    private PlayGameActivity mListener;
    private List<Answer> questionAnswers;
    private boolean mAnswered = false;
    private ViewGroup mAnswersContainer;
    private float touchDownEventX, touchDownEventY;
    private int actionBarHeight;
    private long mTimerStartedTimeSytemTime;
    private android.os.Handler mTimeHandler = new android.os.Handler(this);
    private int mFinalPoints;

    private TextView mTimeTextView;
    private TextView mPointsTextView;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        mListener = (PlayGameActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TypedValue tv = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }

        if (savedInstanceState != null) {
            mTimerStartedTimeSytemTime = savedInstanceState.getLong(STATE_BEGIN_TIME);
            mAnswered = savedInstanceState.getBoolean(STATE_ANSWERED);
            mFinalPoints = savedInstanceState.getInt(STATE_FINAL_POINTS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_question, container, false);

        LayoutUtils.attachScrollingBehavior(view);

        mQuestionImageView = view.findViewById(R.id.questionImageView);
        mQuestionText = view.findViewById(R.id.questionTextView);
        mQuestionAndAnswerContainer = view.findViewById(R.id.questionAndAnswerContainer);
        mAnswersContainer = view.findViewById(R.id.answersContainerLayout);
        mAnswerContainerView = view.findViewById(R.id.answerResultBackground);
        mAnswerTitleView = view.findViewById(R.id.answerTitleTextView);
        mAnswerSubtitleView = view.findViewById(R.id.answerSubtitleTextView);
        mQuestionAndAnswerContainer.setVisibility(View.INVISIBLE);
        mTimeTextView = view.findViewById(R.id.timeLeftTextView);
        mPointsTextView = view.findViewById(R.id.pointsTextView);

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(STATE_BEGIN_TIME, mTimerStartedTimeSytemTime);
        outState.putBoolean(STATE_ANSWERED, mAnswered);
        outState.putInt(STATE_FINAL_POINTS, mFinalPoints);
    }

    @Override
    public void onDataModelChanged(@org.jetbrains.annotations.Nullable DataModel dataModel) {
        if (dataModel != null && dataModel.getCacheValid()) {
            question = (Question) dataModel.getListData().get(0);
            questionAnswers = question.getPublishedAnswers();

            showQuestionInUI();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPause() {
        super.onPause();

        mTimeHandler.removeMessages(MESSAGE_TIME_CHANGED);
        mQuestionAndAnswerContainer.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!needToDisplayQuestion()) {

            mQuestionAndAnswerContainer.setVisibility(View.VISIBLE);

            if (mTimerStartedTimeSytemTime > 0) {
                if (calculateTimeLeftInMillis(System.nanoTime()) > 0)
                    startTime();
                else
                    mListener.userTimeExpired(this, question);
            }
        }
    }

    @Override
    public void onDataLoadingError(@NotNull OrchardError orchardError) {

    }

    private void showQuestionInUI() {
        if (needToDisplayQuestion()) {
            mQuestionText.setText(question.getQuestion());

            LayoutInflater inflater = getActivity().getLayoutInflater();

            int index = 1;

            for (Answer answer : questionAnswers) {
                ViewGroup view = (ViewGroup) inflater.inflate(R.layout.answer_layout, null);

                TextView tv = view.findViewById(R.id.answerNumberTextView);

                tv.setText(String.format("%d", index));

                tv = view.findViewById(R.id.answerTitleTextView);
                tv.setText(answer.getAnswer());

                View touchView = view.getChildAt(0);

                touchView.setTag(answer);
                touchView.setOnClickListener(this);

                touchView.setOnTouchListener(this);
                mAnswersContainer.addView(view);

                ++index;
            }

            if (TextUtils.isEmpty(question.getAllFiles())) {

                mQuestionImageView.setVisibility(View.GONE);
                showHiddenUI();
            } else {
                mQuestionImageView.setVisibility(View.VISIBLE);

                ImageLoader loader = MediaLoader.Companion.<Drawable>typedWith(getActivity(), (MediaLoadable) mQuestionImageView)
                        .mediaPart(question.getImage())
                        .getRequest();

                ImageLoaderExtensionsKt.asBitmap(loader)
                        .addListener(this)
                        .load();
            }
        }
    }

    private boolean needToDisplayQuestion() {
        return mAnswersContainer.getChildCount() <= 2;
    }

    private void showHiddenUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && mQuestionAndAnswerContainer.isAttachedToWindow()) {
            Animator revealAnimation =
                    ViewAnimationUtils.createCircularReveal(mQuestionAndAnswerContainer,
                            0, 0, 0, Math.max(mQuestionAndAnswerContainer.getWidth(), mQuestionAndAnswerContainer.getHeight()));

            mQuestionAndAnswerContainer.setVisibility(View.VISIBLE);

            revealAnimation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    startTime();
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            revealAnimation.start();

        } else {
            mQuestionAndAnswerContainer.setVisibility(View.VISIBLE);
            startTime();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getTag() instanceof Answer) {
            if (!mAnswered) {
                mAnswered = true;

                stopTime();

                Answer answer = (Answer) view.getTag();

                mFinalPoints = calculatePoints(System.nanoTime());

                mListener.userAnsweredQuestion(this, question, answer);

                animateResponse(answer.getCorrectResponse());

            }
        }
    }

    private void animateResponse(boolean correct) {
        int coloreResource = correct ? R.color.answred_correctly_color : R.color.answred_incorrectly_color;
        mAnswerContainerView.setBackgroundColor(ContextCompat.getColor(getActivity(), coloreResource));

        if (correct) {
            mAnswerSubtitleView.setText(R.string.correct_answer);
            mAnswerTitleView.setText(R.string.correct_answer_title);
        } else {
            mAnswerSubtitleView.setText(R.string.wrong_answer);
            mAnswerTitleView.setText(R.string.wrong_answer_title);
            Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            if (v.hasVibrator())
                v.vibrate(getResources().getInteger(R.integer.wrong_answer_vibration));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && mAnswerContainerView.isAttachedToWindow()) {
            Animator revealAnimation =
                    ViewAnimationUtils.createCircularReveal(mAnswerContainerView,
                            (int) touchDownEventX, (int) touchDownEventY, 0, Math.max(mAnswerContainerView.getWidth(), mAnswerContainerView.getHeight()));

            mAnswerContainerView.setVisibility(View.VISIBLE);

            revealAnimation.start();
        } else {
            mAnswerContainerView.setVisibility(View.VISIBLE);
        }
    }


    //Time management

    private void startTime() {
        long currentTime = System.nanoTime();
        if (mTimerStartedTimeSytemTime == 0)
            mTimerStartedTimeSytemTime = currentTime;

        long timeLeft = calculateTimeLeftInMillis(currentTime);

        if (timeLeft > 0)
            mTimeHandler.sendEmptyMessageDelayed(MESSAGE_TIME_CHANGED, 50);

        updateTimeLeftUI(currentTime);
    }

    private void updateTimeLeftUI(long currentTime) {
        long timeLeft = calculateTimeLeftInMillis(currentTime);

        String time = String.format("%01.2f", timeLeft / 1000.0f);

        mTimeTextView.setText(time);

        int point = calculatePoints(currentTime);

        mPointsTextView.setText(String.format("%d", point));

    }

    private int calculatePoints(long currentTimeInNanos) {
        float points = 1.0f * mListener.getAnswerPoints() / mListener.getAnswerTime() * calculateTimeLeftInMillis(currentTimeInNanos) / 1000;

        return (int) points;
    }

    private long calculateTimeLeftInMillis(long currentTimeInNanos) {
        long elapsed = (currentTimeInNanos - mTimerStartedTimeSytemTime) / 1000000;
        long timeLeft = mListener.getAnswerTime() * 1000 - (Math.abs(elapsed));

        if (timeLeft > 0)
            return timeLeft;

        return 0;
    }

    private void stopTime() {
        mTimeHandler.removeMessages(MESSAGE_TIME_CHANGED);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDownEventX = motionEvent.getRawX();
                touchDownEventY = motionEvent.getRawY() - actionBarHeight;
                break;

        }
        return false;
    }

    @Override
    public boolean handleMessage(Message message) {

        if (!isDetached()) {
            if (MESSAGE_TIME_CHANGED == message.what) {
                long time = System.nanoTime();
                if (mListener != null) {
                    if (calculateTimeLeftInMillis(time) > 0)
                        mTimeHandler.sendEmptyMessageDelayed(MESSAGE_TIME_CHANGED, 50);
                    else {
                        if (mListener != null) {
                            animateResponse(false);

                            mListener.userTimeExpired(this, question);
                        }
                    }

                    updateTimeLeftUI(time);
                }
            }
        }
        return false;
    }

    public int getFinalPoints() {
        return mFinalPoints;
    }

    @Override
    public void onDataLoadSuccess(Drawable resource) {
        if (mListener != null) {
            showHiddenUI();
        }
    }

    @Override
    public void onDataLoadFailed() {

    }
}