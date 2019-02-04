package com.krake.core.permission.presenter

import android.app.Activity
import com.krake.core.permission.PermissionFragment

/**
 * Interfaccia che fornisce i metodi per gestire come debbano essere mostrati i messaggi relativi ai permessi sulla UI.
 * I messaggi non devono essere visualizzati all'interno del [PermissionFragment] essendo invisibile.
 */
interface PermissionPresenter {

    /**
     * Specifica come deve essere mostrato il messaggio che spiega la ragione dell'utilizzo dei permessi.
     *
     * @param activity [Activity] che ha richiesto i permessi e nella quale può essere mostrato il messaggio.
     * @param permissionFragment istanza attiva di [PermissionFragment] per permettere di effettuare modifiche sui permessi.
     * @param message messaggio da mostrare.
     */
    fun showRationalMessage(activity: Activity, permissionFragment: PermissionFragment, message: String)

    /**
     * Specifica come deve essere mostrato il messaggio quando i permessi sono stati negati in modo permanente.
     *
     * @param activity [Activity] che ha richiesto i permessi e nella quale può essere mostrato il messaggio.
     * @param permissionFragment istanza attiva di [PermissionFragment] per permettere di effettuare modifiche sui permessi.
     * @param message messaggio da mostrare.
     */
    fun showPermanentlyDeniedMessage(activity: Activity, permissionFragment: PermissionFragment, message: String)
}