package com.krake.usercontent;

import android.content.Context;
import androidx.annotation.NonNull;

/**
 * Informazioni per creazione di un tab nell'activity per mostrare i contenuti dell'utente
 */
public class UserContentTab {
    private int title;
    private String displayAlias;
    private boolean loginRequired;

    public UserContentTab(int title, @NonNull String displayAlias, boolean loginRequired) {
        this.title = title;
        this.displayAlias = displayAlias;
        this.loginRequired = loginRequired;
    }

    /**
     * Tab di default per la visualizzazione dei contenuti pubblicati di tutti gli utenti.
     * Le impostazioni di default impostano:
     * <ol>
     * <li>title: R.string.reports</li>
     * <li>displayAlias: R.string.orchard_reports_display_alias</li>
     * <li>loginRequired: false</li>
     * </ol>
     *
     * @param context context dell'App
     * @return istanza per mostrare il tab
     */
    public static UserContentTab createAllReportsTab(Context context) {
        return new UserContentTab(R.string.reports, context.getString(R.string.orchard_reports_display_alias), false);
    }

    /**
     * Tab di default per la visualizzazione dei contenuti pubblicati di tutti gli utenti.
     * Le impostazioni di default impostano:
     * <ol>
     * <li>title: R.string.myreports</li>
     * <li>displayAlias: R.string.orchard_user_reports_display_alias</li>
     * <li>loginRequired: true</li>
     * </ol>
     *
     * @param context context dell'App
     * @return istanza per mostrare il tab
     */
    public static UserContentTab createUserReportsTab(Context context) {
        return new UserContentTab(R.string.myreports, context.getString(R.string.orchard_user_reports_display_alias), true);
    }

    public int getTitle() {
        return title;
    }

    public String getDisplayAlias() {
        return displayAlias;
    }

    public boolean isLoginRequired() {
        return loginRequired;
    }
}
