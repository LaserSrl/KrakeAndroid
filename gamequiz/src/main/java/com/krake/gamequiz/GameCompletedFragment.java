package com.krake.gamequiz;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.leaderboard.ScoreSubmissionData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.krake.core.OrchardError;
import com.krake.core.contacts.ContactInfo;
import com.krake.core.contacts.ContactInfoManager;
import com.krake.core.network.RemoteClient;
import com.krake.core.network.RemoteRequest;
import com.krake.core.network.RemoteResponse;
import com.krake.core.util.LayoutUtils;
import com.krake.gamequiz.model.QuizGame;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

import java.text.NumberFormat;


/**
 * Fragment che mostra il punteggio totale alla fine del gioco.
 * In base al tipo di gioco {@link #ARG_GAME_TYPE} il comportamento finale del fragment sarà diverso.
 */
public class GameCompletedFragment extends Fragment implements View.OnClickListener,
        DialogInterface.OnClickListener, Function2<RemoteResponse, OrchardError, Unit> {
    /**
     * Int: punteggio totale dell'utente
     */
    private static final String ARG_POINTS = "points";
    /**
     * Long: identificativo del gioco utilizzato per
     */
    private static final String ARG_GAME_ID = "gameIdentifier";
    /**
     * String: identitificativo della classifica google play per caricare la classifica dei punteggi
     */
    private static final String ARG_LEADERBOARD = "leaderboard";
    /**
     * String: tipo del gioco associato i valorei ammessi sono solo {@link QuizGame#GAME_TYPE_COMPETITION} {@link QuizGame#GAME_TYPE_NO_RANK}
     */
    private static final String ARG_GAME_TYPE = "GameType";

    private static final int SHOW_LEADERBOARD_REQUEST = 123;
    private static int RC_SIGN_IN = 9001;
    private long mGameIdentifier;
    private int mPoints;
    private PlayGameActivity mListener;
    private Button mPartecipateButton;
    private EditText mAlertDialogTextView;
    private String mLeaderboardIdentifier;

    private ProgressBar mProgress;
    private boolean mEndPrizeEnabled;

    public GameCompletedFragment() {
        // Required empty public constructor
    }

    /**
     * MEtodo per creare l'istanza del fragment
     *
     * @param points         punti totalizzati dall'utente
     * @param gameIdentifier identificatico del gioco in orchard
     * @param leaderBoard    identificativo della classifica da collegare al gioco.
     * @param gameType       tipologia del gioco
     * @return istanza configurata con gli argument corretti
     */
    public static GameCompletedFragment newInstance(int points, long gameIdentifier, @NonNull String leaderBoard, @NonNull String gameType) {
        GameCompletedFragment fragment = new GameCompletedFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POINTS, points);
        args.putString(ARG_LEADERBOARD, leaderBoard);
        args.putLong(ARG_GAME_ID, gameIdentifier);
        args.putString(ARG_GAME_TYPE, gameType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPoints = getArguments().getInt(ARG_POINTS);
            mGameIdentifier = getArguments().getLong(ARG_GAME_ID);
            mLeaderboardIdentifier = getArguments().getString(ARG_LEADERBOARD);
            mEndPrizeEnabled = getArguments().getString(ARG_GAME_TYPE).equals(QuizGame.GAME_TYPE_COMPETITION);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_completed, container, false);

        LayoutUtils.attachScrollingBehavior(view);

        TextView mTotalPointsTextView = view.findViewById(R.id.pointsTextView);

        mTotalPointsTextView.setText(NumberFormat.getIntegerInstance().format(mPoints));

        mPartecipateButton = view.findViewById(R.id.partecipateButton);
        mPartecipateButton.setOnClickListener(this);
        if (!mEndPrizeEnabled) {
            mPartecipateButton.setText(R.string.close);
        }
        mProgress = view.findViewById(android.R.id.progress);

        return view;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mListener = (PlayGameActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        if (view == mPartecipateButton) {
            if (mEndPrizeEnabled) {
                ContactInfo userInfo = ContactInfoManager.Companion.readUserInfo(getActivity());

                String telephone = null;

                if (userInfo != null)
                    telephone = userInfo.getTelephone();

                showNumberDialog(getString(R.string.message_enter_numer_to_partecipate), telephone);
            }
            else
                getActivity().finish();
        }
    }

    private void showNumberDialog(String message, String telephoneNumber) {

        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_telephone_number, null);

        mAlertDialogTextView = dialogView.findViewById(R.id.telephoneNumberEditText);

        if (!TextUtils.isEmpty(telephoneNumber))
            mAlertDialogTextView.setText(telephoneNumber);

        new AlertDialog.Builder(getActivity())
                .setView(dialogView)
                .setMessage(message)
                .setNeutralButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, this)
                .show();
    }


    @Override
    public void onClick(DialogInterface dialogInterface, int i) {

        String originalNumber = mAlertDialogTextView.getText().toString();

        originalNumber = PhoneNumberUtils.stripSeparators(originalNumber);

        try {
            Phonenumber.PhoneNumber number = PhoneNumberUtil.getInstance().parse(originalNumber, mListener.getString(R.string.default_sim_country_code));

            String completeNumber = String.format("+%d%d", number.getCountryCode(), number.getNationalNumber());

            JsonObject parameters = new JsonObject();

            parameters.addProperty("Device", getString(R.string.orchard_mobile_platform));
            parameters.addProperty("ContentIdentifier", mGameIdentifier);
            parameters.addProperty("Point", mPoints);
            parameters.addProperty("UsernameGameCenter", completeNumber);
            parameters.addProperty("Identifier", completeNumber);

            RemoteRequest request = new RemoteRequest(getActivity())
                    .setMethod(RemoteRequest.Method.POST)
                    .setBody(parameters)
                    .setPath(getString(R.string.orchard_api_register_game_points));

            mProgress.setVisibility(View.VISIBLE);
            mPartecipateButton.setEnabled(false);

            RemoteClient.Companion.client(RemoteClient.Mode.LOGGED)
                    .enqueue(request, this);

            ContactInfoManager.Companion.updateUserInfo(getActivity(), new ContactInfo(null, "", completeNumber.substring(1)));

        } catch (NumberParseException e) {

            showNumberDialog(getString(R.string.error_invalid_phone_number), originalNumber);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if (GoogleSignIn.getLastSignedInAccount(getActivity()) == null) {
            signInSilently();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // The signed in account is stored in the result.
                GoogleSignInAccount signedInAccount = result.getSignInAccount();
                sendScore(signedInAccount);
            } else {
                String message = result.getStatus().getStatusMessage();
                if (message == null || message.isEmpty()) {
                    message = getString(R.string.signin_other_error);
                }
                new AlertDialog.Builder(getActivity()).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();
            }
        } else if (requestCode == SHOW_LEADERBOARD_REQUEST) {
            getActivity().finish();
        }
    }

    public void showAlert(int message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
        }
    }

    private void signInSilently() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(getActivity(),
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.silentSignIn().addOnCompleteListener(getActivity(),
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                    }
                });
    }

    @Override
    public Unit invoke(RemoteResponse remoteResponse, OrchardError orchardError) {

        mProgress.setVisibility(View.GONE);
        mPartecipateButton.setEnabled(true);

        if (remoteResponse != null) {
            sendLeaderboardPoints();
        } else {
            Snackbar.make(getView(), orchardError.getUserFriendlyMessage(getActivity()), Snackbar.LENGTH_SHORT).show();
        }
        return null;
    }


    private void sendLeaderboardPoints() {
        GoogleSignInAccount account;

        if ((account = GoogleSignIn.getLastSignedInAccount(getActivity())) != null) {
            sendScore(account);
        } else {
            GoogleSignInClient signInClient = GoogleSignIn.getClient(getActivity(),
                    GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
            Intent intent = signInClient.getSignInIntent();
            startActivityForResult(intent, RC_SIGN_IN);
        }

    }

    private void sendScore(final GoogleSignInAccount account) {
        Games.getLeaderboardsClient(getActivity(), account)
                .submitScoreImmediate(mLeaderboardIdentifier, mPoints)
                .addOnCompleteListener(new OnCompleteListener<ScoreSubmissionData>() {
                    @Override
                    public void onComplete(@NonNull Task<ScoreSubmissionData> task) {
                        Games.getLeaderboardsClient(getActivity(), account)
                                .getLeaderboardIntent(mLeaderboardIdentifier)
                                .addOnCompleteListener(new OnCompleteListener<Intent>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Intent> task) {
                                        if (task.isSuccessful()) {
                                            startActivityForResult(task.getResult(), SHOW_LEADERBOARD_REQUEST);
                                        } else {
                                            getActivity().finish();
                                        }
                                    }
                                });
                    }
                });
    }


}
