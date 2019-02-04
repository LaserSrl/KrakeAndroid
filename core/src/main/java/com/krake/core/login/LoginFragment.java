package com.krake.core.login;

import android.app.Activity;
import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.krake.core.OrchardError;
import com.krake.core.R;
import com.krake.core.login.orchard.OrchardLoginManager;
import com.krake.core.util.LayoutUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * Fragment per effettuare il login in orchard.
 * <p/>
 * L'activity che lo integra deve implementare il metodo {@link Activity#onActivityResult(int, int, Intent)}
 * e richiamare il corrispondente metodo del fragment.
 * Inoltre èe necessario che l'activity implementi {@link com.krake.core.login.LoginFragment.OnLoginListener}
 * <p/>
 * Questa gestione è già presente nelle activity {@link com.krake.core.app.LoginAndPrivacyActivity}.
 * <p/>
 * E' possibile effettuare la login con diversi servizi, i servizi abilitati per la login devono essere impostati
 * sull'array di string R.array.enabled_login_managers.
 * L'array deve contenere il nome delle classi da utilizzare per il login.
 * Sono diponibili
 * <ol>
 * <li>{@link OrchardLoginManager} per autenticarsi con orchard</li>
 * <li>TwitterLoginManager delle libreria Twitter per accedere con twitter</li>
 * <li>FacebookLoginManager dalla libreria Facebook per accedere con facebook</li>
 * </ol>
 * <ol>
 * <li></li>
 * </ol>
 */
public class LoginFragment extends DialogFragment implements LoginViewManager.LoginListener, Observer<Boolean> {
    private TextView mLoginMessageTextView;

    private boolean mServiceUserLogged = false;

    private OnLoginListener mListener;

    private List<LoginViewManager> mLoginManagers = new ArrayList<>();
    private OrchardLoginManager mOrchardLoginManager;
    private ProgressBar mProgress;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mServiceUserLogged = LoginManager.shared.getLoggedUser().getValue() != null;

        LoginManager.shared.isLogged().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean loginUserOutput) {
                mServiceUserLogged = loginUserOutput;
                if (loginUserOutput) {
                    mListener.onLoginCompleted(LoginFragment.this, true, null);
                }
            }
        });

        LoginManager.shared.getLoginUserError().observe(this, new Observer<OrchardError>() {
            @Override
            public void onChanged(@Nullable OrchardError orchardError) {
                Calendar lastValidDate = Calendar.getInstance();
                lastValidDate.add(Calendar.SECOND, -3);
                if (orchardError != null && orchardError.getDateCreated().after(lastValidDate.getTime())) {
                    for (LoginViewManager buttonManager : mLoginManagers) {
                        buttonManager.logout();
                    }
                    mListener.onLoginCompleted(LoginFragment.this, false, orchardError.getUserFriendlyMessage(getActivity()));
                }
            }
        });

        LoginManager.getShared().isLoginIn().observe(this, this);

        setStyle(DialogFragment.STYLE_NORMAL, R.style.LoginTheme);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getResources().getBoolean(R.bool.is_tablet))
            getDialog().getWindow().getAttributes().windowAnimations = R.style.LoginFragmentAnimation;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.setOnCancelListener(this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_fullscreen_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgress = view.findViewById(android.R.id.progress);
        mLoginMessageTextView = view.findViewById(R.id.loginMessageTextView);
        mLoginMessageTextView.setText(R.string.login_reason_message);

        View loginMainText = view.findViewById(R.id.loginMainText);
        View loginMainImage = view.findViewById(R.id.loginMainImage);

        boolean showLogo = getResources().getBoolean(R.bool.login_show_logo);

        if (showLogo) {
            loginMainImage.setVisibility(View.VISIBLE);
        } else {
            loginMainText.setVisibility(View.VISIBLE);
        }

        String[] managers = getLoginProviders();


        for (String manager : managers) {
            LoginViewManager loginManager = instantiateButtonManager(manager);
            if (loginManager != null) {
                mLoginManagers.add(loginManager);
                insertLoginView(savedInstanceState, loginManager);
            }
        }

        onChanged(LoginManager.getShared().isLoginIn().getValue());
    }

    @NonNull
    protected String[] getLoginProviders() {
        String[] managers = getResources().getStringArray(R.array.enabled_login_managers);

        if (getContext().getApplicationContext() instanceof LoginParametersModification) {
            managers = ((LoginParametersModification) getContext().getApplicationContext()).getLoginProviders();
        }
        return managers;
    }

    @SuppressWarnings("unchecked")
    private LoginViewManager instantiateButtonManager(String className) {
        try {
            Class managerClass = Class.forName(className);
            return (LoginViewManager) managerClass.getConstructor(Activity.class, LoginViewManager.LoginListener.class).newInstance(getActivity(), this);
        } catch (Exception ignored) {
            Log.e("LoginManager", "Button manager not imported correctly: " + className);
        }

        return null;
    }

    private void insertLoginView(Bundle savedInstanceState, LoginViewManager loginManager) {
        View view = loginManager.getLoginView();
        ViewGroup mParent = (ViewGroup) mLoginMessageTextView.getParent();

        if (view.getId() == R.id.orchard_login_container) {
            mParent.addView(view, mParent.indexOfChild(mLoginMessageTextView) + 1);
            mOrchardLoginManager = (OrchardLoginManager) loginManager;
            mOrchardLoginManager.onViewCreated(savedInstanceState, getLoginProviders().length == 1);
        } else {
            mParent.addView(view);
        }

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();

        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        int margins = getResources().getDimensionPixelSize(R.dimen.content_details_internal_padding);
        lp.setMargins(0, margins, 0, margins);

        view.setLayoutParams(lp);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mOrchardLoginManager != null) {
            String userOrMail = mOrchardLoginManager.getUsernameOrMail();
            if (!TextUtils.isEmpty(userOrMail)) {
                outState.putString(OrchardLoginManager.ARG_MAIL_USERNAME, userOrMail);
            }

            String password = mOrchardLoginManager.getPassword();
            if (!TextUtils.isEmpty(password)) {
                outState.putString(OrchardLoginManager.ARG_PASSWORD, password);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnLoginListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnLoginListener");
        }
    }

    @Override
    public void onDestroyView() {
        View view = getView();
        if (view != null) {
            // Hide the keyboard when the view is destroyed to avoid a persistent soft input keyboard.
            LayoutUtils.hideKeyboard(view.getContext(), view);
        }
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        for (LoginViewManager buttonManager : mLoginManagers) {
            buttonManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    //Login fb and service
    private void loginToService() {
        if (!mServiceUserLogged) {
            for (LoginViewManager buttonManager : mLoginManagers) {
                if (buttonManager.sendTokenToOrchard()) {
                    break;
                }
            }
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        mListener.onLoginCancelled(this);
        dismiss();
    }

    @Override
    public void onManagerDidCompleteLogin(LoginViewManager manager) {
        mListener.onLoginCompleted(this, true, null);
    }

    @Override
    public void onManagerTokenAvailable(LoginViewManager manager) {
        loginToService();
    }

    public int getDialogWidth() {
        return getDialog().getWindow().getDecorView().getWidth();
    }

    public int getDialogHeight() {
        return getDialog().getWindow().getDecorView().getHeight();
    }

    @Override
    public void onChanged(@Nullable Boolean aBoolean) {

        for (LoginViewManager buttonManager : mLoginManagers) {
            buttonManager.setButtonEnabled(!aBoolean);
        }

        setCancelable(!aBoolean);

        mProgress.setVisibility(aBoolean ? View.VISIBLE : View.GONE);
    }

    /**
     * Interfaccia che deve implementare l'activity che utilizza il login fragment.
     */
    public interface OnLoginListener {

        /**
         * Il login è stato effettuato, oppure il tentativo non è andato a buon fine.
         * il fragment non viene chiuso in autonomia
         *
         * @param fragment     fragment che ha generato l'evento
         * @param success      indica se il login è andato a buon fine
         * @param errorMessage messaggio di errore in caso il login sia fallito.
         */
        void onLoginCompleted(LoginFragment fragment, boolean success, String errorMessage);

        /**
         * Indicazione che il login è stato annullato dall'utente.
         * Il dialog fragment sarà automaticamente chiuso
         *
         * @param fragment fragment che ha generato l'evento
         */
        void onLoginCancelled(LoginFragment fragment);
    }
}

