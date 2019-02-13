package com.krake.gamequiz;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.krake.core.OrchardError;
import com.krake.core.PrivacyViewModel;
import com.krake.core.app.KrakeApplication;
import com.krake.core.app.LoginAndPrivacyActivity;
import com.krake.core.cache.CacheManager;
import com.krake.core.component.annotation.BundleResolvable;
import com.krake.core.component.base.ComponentManager;
import com.krake.core.component.module.OrchardComponentModule;
import com.krake.core.data.DataConnectionModel;
import com.krake.core.data.DataModel;
import com.krake.core.widget.SnackbarUtils;
import com.krake.gamequiz.model.QuizGame;
import com.krake.surveys.model.Answer;
import com.krake.surveys.model.Question;

import java.text.NumberFormat;
import java.util.List;

/**
 * Activity che mostra un singolo gioco, gestendo il passaggio tra le domande, la gestione del tempo
 * i punti totali e la classifica.
 */
public class PlayGameActivity extends LoginAndPrivacyActivity implements Handler.Callback {

    private static final int SHOW_NEXT_QUESTION_OR_END_GAME_MESSAGE = 123;
    private static final int MSG_CLOSE_VIEW = 124;
    private static final int MSG_SHOW_BEGIN_GAME = 125;

    private static final String STATE_GAME_POINTS = "Points";
    private static final String STATE_CURRENT_QUESTION = "Points";
    @BundleResolvable
    public OrchardComponentModule orchardComponentModule;
    private DataConnectionModel mConnection;
    private QuizGame mGame;
    private List<Question> mSortedQuestions;
    private int currentGameQuestion = -1;
    private int gamePoints;
    private TextView mTotalPointsTextView;
    private Handler mHandler = new Handler(this);

    @Override
    public void onCreate(Bundle savedInstanceState, int layout) {
        super.onCreate(savedInstanceState, layout);

        mTotalPointsTextView = findViewById(R.id.totalPointsLabel);

        if (mTotalPointsTextView == null) {
            AppBarLayout appBar = findViewById(R.id.app_bar_layout);
            getLayoutInflater().inflate(R.layout.partial_total_points, appBar);

            mTotalPointsTextView = findViewById(R.id.totalPointsLabel);
        }

        if (mConnection == null) {
            mConnection = ViewModelProviders.of(this)
                    .get(CacheManager.Companion.getShared().getModelKey(orchardComponentModule),
                            DataConnectionModel.class);
            mConnection.configure(orchardComponentModule,
                    loginComponentModule,
                    ViewModelProviders.of(this).get(PrivacyViewModel.class));

            mConnection.getModel().observe(this, new Observer<DataModel>() {
                @Override
                public void onChanged(@Nullable DataModel dataModel) {
                    if (dataModel != null) {
                        onGameDataChanged(dataModel);
                    }
                }
            });

            mConnection.getDataError().observe(this, new Observer<OrchardError>() {
                @Override
                public void onChanged(@Nullable OrchardError orchardError) {
                    if (orchardError != null) {
                        SnackbarUtils.showCloseSnackbar(mCoordinator,
                                R.string.data_loading_failed, mHandler, MSG_CLOSE_VIEW);
                    }
                }
            });
        }

        if (savedInstanceState != null) {
            gamePoints = savedInstanceState.getInt(STATE_GAME_POINTS);
            currentGameQuestion = savedInstanceState.getInt(STATE_CURRENT_QUESTION);
        }

        updateTotalPointsLabel();
    }

    private void updateTotalPointsLabel() {
        mTotalPointsTextView.setText(NumberFormat.getIntegerInstance().format(gamePoints));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(STATE_GAME_POINTS, gamePoints);
        outState.putInt(STATE_CURRENT_QUESTION, currentGameQuestion);
    }

    @Override
    public void onBackPressed() {
        if (currentGameQuestion < 0 || mSortedQuestions == null || currentGameQuestion >= mSortedQuestions.size()) {
            super.onBackPressed();
        }
    }

    @Override
    public void changeContentVisibility(boolean visible) {

    }

    public void onGameDataChanged(@NonNull DataModel dataModel) {
        if (dataModel.getCacheValid()) {
            mGame = (QuizGame) dataModel.getListData().get(0);

            if (mGame.getQuestionnaire() != null) {
                mSortedQuestions = mGame.getQuestionnaire().getQuestions();
                mHandler.sendEmptyMessage(MSG_SHOW_BEGIN_GAME);
            } else {
                SnackbarUtils.showCloseSnackbar(mCoordinator, R.string.error_game_not_available_yet, mHandler, MSG_CLOSE_VIEW);
            }
        }
    }

    private void showNextQuestion() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.activity_layout_coordinator);

        Question question = mSortedQuestions.get(currentGameQuestion);
        if (currentFragment == null ||
                !(currentFragment instanceof QuestionFragment) ||
                question.getIdentifier() != (currentFragment.getArguments().getLong(OrchardComponentModule.ARG_RECORD_IDENTIFIER))) {
            QuestionFragment fragment = new QuestionFragment();

            Bundle args = ComponentManager.createBundle()
                    .from(this)
                    .with(new OrchardComponentModule()
                            .dataClass(question.getClass())
                            .recordIdentifier(question.getIdentifier()))
                    .build();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().replace(R.id.activity_layout_coordinator, fragment).commit();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case SHOW_NEXT_QUESTION_OR_END_GAME_MESSAGE:
                if (currentGameQuestion < mSortedQuestions.size())
                    showNextQuestion();
                else
                    showEndGame();
                return true;

            case MSG_CLOSE_VIEW:
                finish();
                return true;

            case MSG_SHOW_BEGIN_GAME:
                showBeginGame();
                return true;
        }

        return false;
    }

    private void showBeginGame() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.activity_layout_coordinator);

        if (currentFragment == null || !(currentFragment instanceof BeginGameFragment)) {
            BeginGameFragment fragment = BeginGameFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.activity_layout_coordinator, fragment).commit();
        }
    }

    private void showEndGame() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.activity_layout_coordinator);

        if (currentFragment == null || !(currentFragment instanceof GameCompletedFragment)) {
            if (mGame != null) {
                Bundle b = new Bundle();
                b.putString(FirebaseAnalytics.Param.CONTENT_TYPE, mGame.getClass().getSimpleName());
                b.putString(FirebaseAnalytics.Param.ITEM_ID, mGame.getAutoroutePartDisplayAlias());
                ((KrakeApplication) getApplication()).logEvent("game_completed", b);
            }

            GameCompletedFragment fragment = GameCompletedFragment.newInstance(gamePoints, mGame.getIdentifier(), mGame.getRankingAndroidIdentifier(), mGame.getGameType());
            getSupportFragmentManager().beginTransaction().replace(R.id.activity_layout_coordinator, fragment).commitAllowingStateLoss();
        }
    }

    public long getAnswerTime() {
        if (mGame != null)
            return mGame.getAnswerTime();
        return 0;
    }

    public long getAnswerPoints() {
        if (mGame != null)
            return mGame.getAnswerPoint();
        return 0;
    }

    public void userAnsweredQuestion(QuestionFragment fragment, Question question, Answer answer) {
        ++currentGameQuestion;
        mHandler.sendEmptyMessageDelayed(SHOW_NEXT_QUESTION_OR_END_GAME_MESSAGE, getResources().getInteger(R.integer.answer_delay_time));

        if (answer.getCorrectResponse()) {
            gamePoints += fragment.getFinalPoints();
            updateTotalPointsLabel();
        }
    }

    public void userTimeExpired(QuestionFragment fragment, Question question) {
        if (question.getIdentifier() == mSortedQuestions.get(currentGameQuestion).getIdentifier()) {
            ++currentGameQuestion;
            mHandler.sendEmptyMessageDelayed(SHOW_NEXT_QUESTION_OR_END_GAME_MESSAGE, getResources().getInteger(R.integer.answer_delay_time));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeMessages(SHOW_NEXT_QUESTION_OR_END_GAME_MESSAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activity_layout_coordinator);
        if (fragment != null)
            fragment.onActivityResult(requestCode, resultCode, data);
    }

    protected void beginGame() {
        if (currentGameQuestion < 0) {
            currentGameQuestion = 0;
            showNextQuestion();
        }
    }
}