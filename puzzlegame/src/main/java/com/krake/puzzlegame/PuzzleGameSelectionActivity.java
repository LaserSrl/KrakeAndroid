package com.krake.puzzlegame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import com.krake.core.PrivacyViewModel;
import com.krake.core.app.ThemableNavigationActivity;
import com.krake.core.cache.CacheManager;
import com.krake.core.component.annotation.BundleResolvable;
import com.krake.core.component.base.ComponentManager;
import com.krake.core.component.module.LoginComponentModule;
import com.krake.core.component.module.OrchardComponentModule;
import com.krake.core.data.DataConnectionModel;
import com.krake.core.data.DataModel;
import com.krake.core.model.ContentItemWithGallery;
import com.krake.core.model.RecordWithIdentifier;
import com.krake.core.model.RecordWithStringIdentifier;
import com.krake.puzzlegame.component.module.PuzzleGameComponentModule;
import com.krake.puzzlegame.model.PuzzleGame;
import io.realm.RealmModel;

import java.util.List;
import java.util.Random;

/**
 * Activity per accogliere l'utente prima di potarlo al puzzle effettivo.
 * L'activity necessità dei pamratri da cui caricare i giochi, presi dalla OrchardServiceDataConnection.
 * <p/>
 * Le classi di dato richiedono alcune interface per permettere al tutto di funzionare correttamente.
 * <ol>
 * <li>{@link PuzzleGame} da assegnare alla classe iniziale per il gioco (normalmente assegnata a QuestionGame)</li>
 * </ol>
 * <p/>
 * Degli n giochi restituiti dalla chiamata sarà mostrato all'utente uno estratto casualmente dall'elenco.
 */
public class PuzzleGameSelectionActivity extends ThemableNavigationActivity
        implements View.OnClickListener, GameTilesFragment.OnGameFinishedListener {
    @BundleResolvable
    public PuzzleGameComponentModule puzzleGameComponentModule;
    @BundleResolvable
    public OrchardComponentModule orchardComponentModule;

    private DataConnectionModel mConnection;
    private ProgressBar mProgress;
    private List<RealmModel> mGames;
    private Button mBeginButton;
    private boolean mStartGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflateMainView(R.layout.activity_game_selection, true);

        mConnection = ViewModelProviders
                .of(this)
                .get(CacheManager.Companion.getShared().getModelKey(orchardComponentModule), DataConnectionModel.class);

        mConnection.configure(orchardComponentModule,
                new LoginComponentModule(),
                ViewModelProviders.of(this).get(PrivacyViewModel.class));

        mConnection.getModel().observe(this, new Observer<DataModel>() {
            @Override
            public void onChanged(@Nullable DataModel dataModel) {
                if (dataModel != null)
                    onDefaultDataLoaded(dataModel);
            }
        });

        mBeginButton = findViewById(R.id.newGameButton);
        mBeginButton.setOnClickListener(this);

        mProgress = findViewById(android.R.id.progress);
    }

    public void onDefaultDataLoaded(@NonNull DataModel dataModel) {
        if (dataModel.getCacheValid()) {
            mGames = dataModel.getListData();

            if (mStartGame && mGames.size() > 0)
                startGame();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mBeginButton) {
            if (mGames != null && mGames.size() > 0) {
                startGame();
            } else {
                mProgress.setVisibility(View.VISIBLE);
                mBeginButton.setEnabled(false);
                mStartGame = true;
            }
        }
    }

    private void startGame() {
        mStartGame = false;
        ContentItemWithGallery game = (ContentItemWithGallery) mGames.get(new Random().nextInt(mGames.size()));

        OrchardComponentModule module = new OrchardComponentModule().dataClass(game.getClass());

        if (game instanceof RecordWithIdentifier)
            module.recordIdentifier(((RecordWithIdentifier) game).getIdentifier());
        else if (game instanceof RecordWithStringIdentifier)
            module.recordStringIdentifier(((RecordWithStringIdentifier) game).getStringIdentifier());

        Intent playIntent = ComponentManager.createIntent()
                .from(this)
                .to(puzzleGameComponentModule.getGameActivity())
                .put(module.writeContent(this))
                .build();

        startActivity(playIntent);

        mProgress.setVisibility(View.GONE);
        mBeginButton.setEnabled(true);
    }

    @Override
    public void onGameFinished(Game game) { /* empty */ }

    @Override
    public void onRequestedShowCompleteImageHelp(Game game) { /* empty */ }

    @Override
    public void onTimeElapsed(Game game) { /* empty */ }

    @Override
    public void onRequestedTileNumbers(Game game) { /* empty */ }
}