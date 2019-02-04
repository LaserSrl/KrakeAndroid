package com.krake.gamequiz;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.krake.core.app.ContentItemDetailActivity;
import com.krake.core.app.ContentItemListMapActivity;
import com.krake.core.component.annotation.BundleResolvable;
import com.krake.core.component.base.ComponentManager;
import com.krake.core.component.module.OrchardComponentModule;
import com.krake.core.component.module.ThemableComponentModule;
import com.krake.core.extension.BundleExtensionsKt;
import com.krake.core.model.ContentItem;
import com.krake.core.widget.SnackbarUtils;
import com.krake.gamequiz.component.module.GameQuizComponentModule;
import com.krake.gamequiz.model.QuizGame;

import java.text.DateFormat;
import java.util.Random;


/**
 * Activity base per mostrare l'elenco dei giochi disponibili.
 */
public class GamesActivity extends ContentItemListMapActivity {
    private static final int RC_SIGN_IN = 2334;
    @BundleResolvable
    public GameQuizComponentModule gameQuizComponentModule;
    private String mLeaderBoardToDisplay = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);

        if (gameQuizComponentModule.getHelpDetailBundle() != null) {
            getMenuInflater().inflate(R.menu.game_menu, menu);
        }

        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_show_infos) {
            Intent helpBundle = ComponentManager.createIntent()
                    .from(this)
                    .to(ContentItemDetailActivity.class)
                    .put(helpDetailBundle())
                    .build();
            startActivity(helpBundle);
            return true;
        } else if (item.getItemId() == R.id.action_show_leaderboards) {
            showLeaderboard("");
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Crea il {@link Bundle} che verrà passato alla {@link ContentItemDetailActivity} di help.
     * Utilizzando {@link GameQuizComponentModule#getHelpDetailBundle()} come {@link Bundle} di partenza, viene aggiunto l'up {@link Intent}.
     *
     * @return {@link Bundle} con le proprietà relative al {@link ContentItem} selezionato.
     */
    protected Bundle helpDetailBundle() {
        ThemableComponentModule themableModule = new ThemableComponentModule();
        //noinspection ConstantConditions
        themableModule.readContent(this, gameQuizComponentModule.getHelpDetailBundle());
        themableModule.upIntent(getIntent());

        /* Necessario per evitare delle references circolari che non sono supportate dai Parcelable. */
        Bundle bundle = new Bundle();
        bundle.putAll(gameQuizComponentModule.getHelpDetailBundle());
        BundleExtensionsKt.putModules(bundle, this, themableModule);
        return bundle;
    }

    @Override
    public void onShowContentItemDetails(@NonNull Object sender, @NonNull ContentItem contentItem) {
        final QuizGame game = (QuizGame) contentItem;

        if (game.getState() == QuizGame.Status.ACTIVE) {
            Intent intent = ComponentManager.createIntent()
                    .from(this)
                    .to(PlayGameActivity.class)
                    .with(new OrchardComponentModule()
                            .dataClass(game.getClass())
                            .displayPath(game.getAutoroutePartDisplayAlias())
                            .putExtraParameter(getString(R.string.orchard_query_game_rand), Long.valueOf(new Random().nextLong()).toString())
                            .dataPartFilters("TitlePart,AutoroutePart,Gallery,ActivityPart,GamePart")
                            .deepLevel(13)
                            .noCache())
                    .build();

            startActivity(intent);
        } else if (game.getState() == QuizGame.Status.NO_YET_ACTIVE) {
            DateFormat simpleDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

            SnackbarUtils.createSnackbar(findViewById(R.id.activity_layout_coordinator),
                    String.format("%s %s", getString(R.string.no_yet_available), simpleDateFormat.format(game.getStartDate())),
                    Snackbar.LENGTH_LONG)
                    .show();
        } else {
            SnackbarUtils.createSnackbar(findViewById(R.id.activity_layout_coordinator),
                    getString(R.string.error_game_closed),
                    Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.action_open_play_leaderboard), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showLeaderboard(game.getRankingAndroidIdentifier());
                        }
                    })
                    .show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (GoogleSignIn.getLastSignedInAccount(this) == null) {
            signInSilently();
        }
    }

    private void showLeaderboard(@NonNull String identifier) {

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {


            LeaderboardsClient leaderboardsClient = Games.getLeaderboardsClient(this, account);
            Task<Intent> task;
            if (identifier.isEmpty()) {
                task = leaderboardsClient.getAllLeaderboardsIntent();
            } else {
                task = leaderboardsClient.getLeaderboardIntent(identifier);
            }

            task.addOnSuccessListener(
                            new OnSuccessListener<Intent>() {
                                @Override
                                public void onSuccess(Intent intent) {
                                    startActivityForResult(intent, 1222);
                                }
                            }
                    );
        } else {
            mLeaderBoardToDisplay = identifier;
            GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                    GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
            Intent intent = signInClient.getSignInIntent();
            startActivityForResult(intent, RC_SIGN_IN);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // The signed in account is stored in the result.
                showLeaderboard(mLeaderBoardToDisplay);
            } else {
                Snackbar.make(findViewById(R.id.activity_layout_coordinator), R.string.signin_failure, Snackbar.LENGTH_LONG).show();
            }

            mLeaderBoardToDisplay = null;
        }
    }

    private void signInSilently() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                    }
                });
    }
}
