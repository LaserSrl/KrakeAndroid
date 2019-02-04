package com.krake.core.location.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.LocationManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import com.krake.core.R
import com.krake.core.extension.commitSyncAllowingStateLoss
import com.krake.core.location.settings.presenter.GpsSettingsPresenter
import com.krake.core.location.settings.presenter.SnackbarGpsSettingsPresenter

/**
 * Manager che gestisce la richiesta di attivazione del GPS tramite un builder pattern.
 *
 * @param activity [Activity] utilizzata per mostrare i messaggi relativi alla richiesta di attivazione del GPS.
 * @param fragmentManager support [FragmentManager] utilizzato per inserire il [Fragment] che gestirà la richiesta di attivazione del GPS.
 * @constructor crea un [GpsSettingsManager] per il check e la richiesta di attivazione del GPS.
 */
open class GpsSettingsManager constructor(protected val activity: Activity, protected var fragmentManager: FragmentManager) : GpsSettingsFragment.Callback {

    /**
     * Crea un [GpsSettingsManager] per il check e la richiesta di attivazione del GPS.
     *
     * @param activity [Activity] utilizzata per mostrare i messaggi relativi alla richiesta di attivazione del GPS e
     * dalla quale verrà ricavato il support [FragmentManager].
     */
    constructor(activity: FragmentActivity) : this(activity, activity.supportFragmentManager)

    /**
     * Crea un [GpsSettingsManager] per il check e la richiesta di attivazione del GPS.
     *
     * @param fragment [Fragment] dal quale vengono ricavati l'[Activity] utilizzata per mostrare i messaggi relativi
     * alla richiesta di attivazione del GPS e il child [FragmentManager].
     */
    constructor(fragment: Fragment) : this(fragment.activity ?:
            throw IllegalArgumentException("The activity mustn't be null."), fragment.childFragmentManager)

    protected var gpsFragment: GpsSettingsFragment? = null
        private set
    protected var listeners: MutableSet<GpsSettingsListener> = HashSet()
        private set
    protected var presenter: GpsSettingsPresenter = SnackbarGpsSettingsPresenter()
        private set
    protected var settingsChangeUnavailableMsg: String = activity.getString(R.string.error_gps_blocked)
        private set

    /**
     * Specifica l'oggetto che si occuperà di mostrare i permessi sulla UI.
     * L'oggetto deve implementare [GpsSettingsListener].
     * DEFAULT: [SnackbarGpsSettingsPresenter]
     *
     * @param presenter [GpsSettingsListener] che mostra i permessi sulla UI.
     */
    fun presenter(presenter: GpsSettingsPresenter) = apply { this.presenter = presenter }

    /**
     * Specifica il messaggio che deve essere mostrato quando non è possibile attivare il GPS.
     * DEFAULT: null
     *
     * @param msg messaggio da mostrare oppure null per non mostrare nulla.
     */
    fun settingsChangeUnavailableMsg(msg: String) = apply { settingsChangeUnavailableMsg = msg }

    /**
     * Aggiunge un listener per gli eventi sull'attivazione del GPS.
     *
     * @param listener [GpsSettingsListener] listener da aggiungere.
     */
    fun addListener(listener: GpsSettingsListener) = apply { listeners.add(listener) }

    /**
     * Rimuove un listener registrato per gli eventi sull'attivazione del GPS.
     *
     * @param listener [GpsSettingsListener] listener da rimuovere.
     */
    fun removeListener(listener: GpsSettingsListener) = apply { listeners.remove(listener) }

    /**
     * Rimuove tutti i listener registrati per gli eventi sull'attivazione del GPS.
     */
    fun removeAllListeners() = apply { listeners.clear() }

    /**
     * Crea il [GpsSettingsFragment] e lo inserisce con il [FragmentManager] passato come parametro.
     * Successivamente richiede l'attivazione del GPS.
     *
     * @param force true se si vuole forzare la richiesta del GPS ogni volta, altrimenti, la richiesta
     * verrà spedita solo una volta per tutta la durata della sezione.
     */
    @SuppressLint("CommitTransaction")
    open fun request(force: Boolean = false) {
        val tag = GpsSettingsFragment.FRAGMENT_TAG
        gpsFragment = fragmentManager.findFragmentByTag(tag) as? GpsSettingsFragment
        if (gpsFragment == null) {
            gpsFragment = GpsSettingsFragment.newInstance(settingsChangeUnavailableMsg)
            fragmentManager.beginTransaction()
                    .add(gpsFragment, tag)
                    .commitSyncAllowingStateLoss(fragmentManager)
        }
        gpsFragment?.callback = this
        // Richiede il GPS.
        gpsFragment?.requireGps(force)
    }

    /**
     * Verifica che il GPS sia attivo o meno.
     *
     * @return true se il GPS è attivo.
     */
    fun isActive(): Boolean {
        return (activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onGpsSettingsAcquired() {
        listeners.forEach(GpsSettingsListener::onGpsSettingsAcquired)
    }

    override fun showSettingsChangeUnavailableMessage(message: String) {
        presenter.showSettingsChangeUnavailableMessage(activity, message)
    }
}