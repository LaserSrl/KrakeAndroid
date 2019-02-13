package com.krake.core.login;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

/**
 * Classe astratta per gestire i bottoni di login
 */
public abstract class LoginViewManager {
    private final Activity activity;
    private final LoginListener loginFragment;

    public LoginViewManager(@NonNull Activity activity, @NonNull LoginListener loginFragment) {
        this.activity = activity;
        this.loginFragment = loginFragment;
    }

    protected Activity getActivity() {
        return activity;
    }

    protected LoginListener getLoginFragment() {
        return loginFragment;
    }

    /**
     * Ritorna il bottone da inserire nella UI del fragment per la gestione dell'accesso
     * Il bottone sarà solo inserito nella UI del fragment, non verranno impostate listener su di esso
     *
     * @return l'istanza configurata del bottone,
     */
    abstract public View getLoginView();

    /**
     * Connessione per il ritorno dei risultati dalle activity
     *
     * @param requestCode
     * @param resultCode
     * @param data
     * @return true se è stato gestito
     */
    abstract public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data);

    /**
     * Indica al bottone se deve abilitarsi oppure no
     *
     * @param enabled bottone abilitato
     */
    protected abstract void setButtonEnabled(boolean enabled);

    /**
     * Comando per inviare il token di autenticazione su orchard.
     * Ritorna true se il token è disponobile ed è stata inviata la chiamata ad orchard
     * {@link LoginManager}
     *
     * @return true se è stata avviata la login verso orchard
     */
    public abstract boolean sendTokenToOrchard();

    /**
     * Il listener deve eseguire il logout, cancellando eventualmente i token della login.
     */
    abstract public void logout();


    protected String getString(@StringRes int stringRef) {
        return getActivity().getString(stringRef);
    }

    /**
     * Interfaccia che deve implementare la gestione il listener per ricevere le notifiche dei
     * loginmanager
     */
    public interface LoginListener {
        /**
         * Indicazione che il login è stato completato direttamente su Orchard da
         * parte del manager per la gestione della particolare login.
         * Viene utilizzato da {@link com.krake.core.login.orchard.OrchardLoginManager}
         *
         * @param manager il manager del bottone
         */
        void onManagerDidCompleteLogin(LoginViewManager manager);

        /**
         * Indica al listener che i token di autenticazione sono stati recuperati
         * dal gestore di login esterno.
         *
         * @param manager il manager del bottone
         */
        void onManagerTokenAvailable(LoginViewManager manager);
    }
}
