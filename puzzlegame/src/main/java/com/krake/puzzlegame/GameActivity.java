package com.krake.puzzlegame;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;

import com.krake.core.app.LoginAndPrivacyActivity;
import com.krake.core.component.base.ComponentManager;
import com.krake.core.component.module.OrchardComponentModule;
import com.krake.surveys.model.Question;


/**
 * Activity che getsice il gioco: mostra il gioco.
 * Prima viene mostrato il gioco del 15 con l'immagine indicato da gioco,
 * poi sara' mostrata la domanda finale
 */
public class GameActivity extends LoginAndPrivacyActivity implements GameTilesFragment.OnGameFinishedListener,
        QuestionaryDetailsFragment.OnQuestionAnsweredListener,
        EndGameFragment.OnCloseGameListener {

    private static final String STATE_GAME_COMPLETED_CORRECTLY = "gameCompletedCorrected";
    private Game mCompletedGame;

    private boolean mCorrectly;

    @Override
    public void onCreate(Bundle savedInstanceState, int layout) {
        super.onCreate(savedInstanceState, layout);

        if (savedInstanceState != null) {
            mCorrectly = savedInstanceState.getBoolean(STATE_GAME_COMPLETED_CORRECTLY);
        }

        updateFragmentUI();
    }

    @Override
    public void onGameFinished(Game game) {
        mCompletedGame = game;

        updateFragmentUI();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        outState.putBoolean(STATE_GAME_COMPLETED_CORRECTLY, mCorrectly);
    }

    private void updateFragmentUI() {
        Fragment mFragment = getSupportFragmentManager().findFragmentById(R.id.activity_layout_coordinator);

        if (mFragment == null || (mCompletedGame == null && mFragment instanceof QuestionaryDetailsFragment)
                || (mCompletedGame != null && mFragment instanceof GameTilesFragment)) {

            if (mCompletedGame == null) {
                mFragment = new GameTilesFragment();
                mFragment.setArguments(getIntent().getExtras());

                getSupportFragmentManager().beginTransaction().replace(R.id.activity_layout_coordinator, mFragment).commit();
            } else {
                Question question = mCompletedGame.question.getQuestion();
                mFragment = new QuestionaryDetailsFragment();

                Bundle args = ComponentManager.createBundle()
                        .from(this)
                        .with(new OrchardComponentModule()
                                .dataClass(question.getClass())
                                .recordIdentifier(question.getIdentifier()))
                        .build();

                mFragment.setArguments(args);
                getSupportFragmentManager().beginTransaction().replace(R.id.activity_layout_coordinator, mFragment).commit();
            }
        }
    }

    @Override
    public void onRequestedShowCompleteImageHelp(Game game) {

    }

    @Override
    public void onTimeElapsed(Game game) {

    }

    @Override
    public void onRequestedTileNumbers(Game game) {

    }

    @Override
    public void userAnsweredCorrecly(boolean correctly) {
        mCorrectly = correctly;
        Fragment mFragment = EndGameFragment.newInstance(correctly);
        getSupportFragmentManager().beginTransaction().replace(R.id.activity_layout_coordinator, mFragment).commit();
    }

    public boolean getCorrectly() {
        return mCorrectly;
    }

    @Override
    public void onCloseGame() {
        finish();
    }

    @Override
    public void changeContentVisibility(boolean visible) {

    }
}