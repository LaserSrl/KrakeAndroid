package com.krake.core.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.MotionEvent;
import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import com.krake.core.PrivacyStatus;
import com.krake.core.PrivacyViewModel;
import com.krake.core.R;
import com.krake.core.component.annotation.BundleResolvable;
import com.krake.core.component.module.LoginComponentModule;
import com.krake.core.login.AcceptPrivacyFragment;
import com.krake.core.login.LoginFragment;
import com.krake.core.login.LoginManager;
import com.krake.core.login.LoginUserOutput;
import hotchemi.android.rate.AppRate;

import java.util.Set;

/**
 * Activity che si occupa della gestione della privacy e della login durante l'interazione con Orchard.
 * <p/>
 * L'activity crea un binding con {@link OrchardService}, in caso sia richiesta la Login e questa non sia presente viene
 * mostrato il {@link LoginFragment}.
 * Se durante il caricamento di un contenuto di un dei sottofragment che sono parte dell'activity sono
 * restituite delle {@link com.krake.core.model.PolicyText} sono presentate prima di
 * il contenuto dei fragment sarà caricato solamente dopo averle accettate.
 */
public abstract class LoginAndPrivacyActivity
        extends ThemableNavigationActivity
        implements LoginFragment.OnLoginListener, Observer<PrivacyStatus> {
    private static final String LOGIN_FRAGMENT_TAG = "Login";
    private static final String PRIVACY_FRAGMENT_TAG = "Privacy";
    @BundleResolvable
    public LoginComponentModule loginComponentModule = new LoginComponentModule();

    public boolean isLoginRequired() {
        return loginComponentModule.getLoginRequired();
    }

    public void setLoginRequired(boolean loginRequired) {
        loginComponentModule.loginRequired(loginRequired);
    }

    public void showLoginFragment() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(LOGIN_FRAGMENT_TAG) == null) {
            LoginFragment loginFragment = new LoginFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(loginFragment, LOGIN_FRAGMENT_TAG);
            // Avoid the state loss showing the login fragment.
            ft.commitAllowingStateLoss();
        }
    }

    private void showPrivacyFragment() {
        if (!isPrivacyFragmentVisible()) {

            AcceptPrivacyFragment privacyFragment = new AcceptPrivacyFragment();
            try {
                getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack("")
                        .replace(android.R.id.content, privacyFragment, PRIVACY_FRAGMENT_TAG)
                        .commit();
                lockClosedDrawer(true);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!isPrivacyFragmentVisible())
            super.onBackPressed();
    }

    @Override
    @CallSuper
    public void onCreate(Bundle savedInstanceState, int layout) {
        super.onCreate(savedInstanceState, layout);

        ViewModelProviders.of(this)
                .get(PrivacyViewModel.class)
                .getPrivacyStatus()
                .observe(this, this);

        if (getResources().getBoolean(R.bool.app_rate_enable)) {

            String action = getIntent().getAction();
            Set<String> categories = getIntent().getCategories();
            if (action != null && action.equals(Intent.ACTION_MAIN) && categories != null && categories.contains(Intent.CATEGORY_LAUNCHER)) {
                AppRate.with(this)
                        .setInstallDays(getResources().getInteger(R.integer.app_rate_number_of_days))
                        .setLaunchTimes(getResources().getInteger(R.integer.app_rate_number_of_launch))

                        .monitor();

                AppRate.showRateDialogIfMeetsConditions(this);
            }
        }

        LoginManager.shared.getLoggedUser().observe(this, new Observer<LoginUserOutput>() {
            @Override
            public void onChanged(@Nullable LoginUserOutput loginUserOutput) {
                onLoginUserChanged(loginUserOutput);
            }
        });
    }

    protected void onLoginUserChanged(LoginUserOutput loginUserOutput) {
        if (isLoginRequired()) {
            changeContentVisibility(loginUserOutput != null);

            if (loginUserOutput == null) {
                showLoginFragment();
            }
        }
    }

    @Override
    final public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        LoginUserOutput output = LoginManager.shared.getLoggedUser().getValue();
        changeContentVisibility(!isLoginRequired() || output != null);
        onLoginUserChanged(output);
    }

    @Override
    final protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoginUserOutput output = LoginManager.shared.getLoggedUser().getValue();
        changeContentVisibility(!isLoginRequired() || output != null);
        onLoginUserChanged(output);
    }

    /**
     * Bug fix per problemi layout coordinator versione support 23.0.0
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            return false;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        LoginFragment fragment = (LoginFragment) getSupportFragmentManager().findFragmentByTag(LOGIN_FRAGMENT_TAG);

        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onLoginCompleted(LoginFragment loginFragment, boolean success, String s) {
        if (success) {
            LoginFragment mLoginFragment = (LoginFragment) getSupportFragmentManager().findFragmentByTag(LOGIN_FRAGMENT_TAG);
            if (mLoginFragment != null) {
                mLoginFragment.dismiss();
            }
        } else {
            new AlertDialog.Builder(this).setTitle(getString(R.string.authentication_failed))
                    .setMessage(s)
                    .setNeutralButton(android.R.string.ok, null)
                    .show();
        }
    }

    @Override
    public void onLoginCancelled(LoginFragment fragment) {
        if (isLoginRequired()) {
            finish();
            if (getResources().getBoolean(R.bool.is_tablet))
                overridePendingTransition(0, 0);
        }
    }

    private void hidePrivacyFragment() {
        if (isPrivacyFragmentVisible()) {
            lockClosedDrawer(false);
            getSupportFragmentManager().popBackStack();
        }
    }

    /**
     * Indicazione all'activity che lo stato del suo contenuto potrebbe essere cambiato
     * Possono arrivare più chiamate consecutive senza che vi sia effettiva variazione
     * del valore visible.
     *
     * @param visible indica se il contenuto dell'activity deve essere mostrato (Login effettuato)
     *                oppure no (Login mancante)
     */
    public abstract void changeContentVisibility(boolean visible);

    public boolean isPrivacyFragmentVisible() {
        return getSupportFragmentManager().findFragmentByTag(PRIVACY_FRAGMENT_TAG) != null;
    }

    @Override
    public void onChanged(@Nullable PrivacyStatus privacyStatus) {
        if (privacyStatus != null) {
            if (privacyStatus.equals(PrivacyStatus.PENDING)) {
                showPrivacyFragment();
            } else if (privacyStatus.equals(PrivacyStatus.ACCEPTED)) {
                hidePrivacyFragment();
            } else if (privacyStatus.equals(PrivacyStatus.REFUSED)) {
                hidePrivacyFragment();
                finish();
            } else if (privacyStatus.equals(PrivacyStatus.FAILED)) {
                new AlertDialog.Builder(LoginAndPrivacyActivity.this)
                        .setMessage(getString(R.string.error_privacy_acceptance_failed))
                        .setNeutralButton(android.R.string.ok, null)
                        .show();
            }
        }
    }
}
