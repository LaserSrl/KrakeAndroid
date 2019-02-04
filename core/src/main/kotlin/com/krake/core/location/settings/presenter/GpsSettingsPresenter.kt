package com.krake.core.location.settings.presenter

import android.app.Activity

/**
 * Interfaccia che fornisce i metodi per gestire come debbano essere mostrati i messaggi
 * relativi alle impostazioni del GPS sulla UI.
 */
interface GpsSettingsPresenter {

    /**
     * Specifica come deve essere mostrato il messaggio quando è impossibile cambiare le impostazioni del GPS.
     *
     * @param activity [Activity] che ha richiesto l'accensione del GPS e nella quale può essere mostrato il messaggio.
     * @param message messaggio da mostrare.
     */
    fun showSettingsChangeUnavailableMessage(activity: Activity, message: String)
}