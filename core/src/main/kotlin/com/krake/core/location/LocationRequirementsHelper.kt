package com.krake.core.location

import android.Manifest
import android.app.Activity
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import com.krake.core.R
import com.krake.core.location.settings.GpsSettingsListener
import com.krake.core.location.settings.GpsSettingsManager
import com.krake.core.permission.PermissionFragment
import com.krake.core.permission.PermissionListener
import com.krake.core.permission.PermissionManager

/**
 * Manager che gestisce la richiesta dei permessi della localizzazione e la successiva richiesta
 * per l'attivazione del GPS.
 *
 * @param activity [Activity] utilizzata per mostrare i messaggi relativi ai permessi e all'attivazione del GPS.
 * @param fragmentManager support [FragmentManager] utilizzato per inserire il [Fragment] che gestirà la richiesta dei permessi.
 * @param permissionListener listener che verrà notificato sui cambiamenti dei permessi.
 * @param gpsSettingsListener listener che verrà notificato sui cambiamenti delle impostazioni del GPS.
 * @constructor crea una nuova istanza di [LocationRequirementsHelper] creando un [PermissionManager] e un [GpsSettingsManager].
 */
class LocationRequirementsHelper private constructor(activity: Activity,
                                                     fragmentManager: FragmentManager,
                                                     permissionListener: PermissionListener?,
                                                     gpsSettingsListener: GpsSettingsListener?) : PermissionListener {

    private var forcingRequest: Boolean = false

    val permissionManager = PermissionManager(activity, fragmentManager)
            .permissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            .permanentlyRefusedMsg(activity.getString(R.string.error_location_permission_refused))

    val gpsSettingsManager = GpsSettingsManager(activity, fragmentManager)

    init {
        permissionListener?.let {
            permissionManager.addListener(it)
        }
        gpsSettingsListener?.let {
            gpsSettingsManager.addListener(it)
        }
        /* Aggiunge quest'instanza di LocationRequirementsHelper come listener
         * per gestire la richiesta del GPS quando arriva la callback dei permessi. */
        permissionManager.addListener(this)
    }

    /**
     * Crea una nuova istanza di [LocationRequirementsHelper] creando un [PermissionManager] e un [GpsSettingsManager].
     *
     * @param activity [Activity] utilizzata per mostrare i messaggi relativi ai permessi e all'attivazione del GPS e
     * dalla quale verrà ricavato il support [FragmentManager].
     * @param permissionListener listener che verrà notificato sui cambiamenti dei permessi.
     * @param gpsSettingsListener listener che verrà notificato sui cambiamenti delle impostazioni del GPS.
     */
    constructor(activity: FragmentActivity, permissionListener: PermissionListener? = null, gpsSettingsListener: GpsSettingsListener? = null) :
            this(activity, activity.supportFragmentManager, permissionListener, gpsSettingsListener)

    /**
     * Crea una nuova istanza di [LocationRequirementsHelper] creando un [PermissionManager] e un [GpsSettingsManager].
     *
     * @param fragment [Fragment] dal quale vengono ricavati l'[Activity] utilizzata per mostrare i messaggi relativi
     * ai permessi e all'attivazione del GPS e il child [FragmentManager].
     * @param permissionListener listener che verrà notificato sui cambiamenti dei permessi.
     * @param gpsSettingsListener listener che verrà notificato sui cambiamenti delle impostazioni del GPS.
     */
    constructor(fragment: Fragment, permissionListener: PermissionListener? = null, gpsSettingsListener: GpsSettingsListener? = null) :
            this(fragment.activity ?: throw IllegalArgumentException("The activity mustn't be null."),
                    fragment.childFragmentManager, permissionListener, gpsSettingsListener)

    /**
     * Crea il [PermissionManager] che istanzierà un [PermissionFragment].
     */
    fun create() {
        permissionManager.create()
    }

    /**
     * Richiede i permessi, nel caso in cui siano stati accettati, richiede l'attivazione al GPS.
     *
     * @param force true se si vuole forzare la richiesta del GPS ogni volta, altrimenti, la richiesta
     * verrà spedita solo una volta per tutta la durata della sezione.
     */
    fun request(force: Boolean = false) {
        forcingRequest = force
        permissionManager.request()
    }

    override fun onPermissionsHandled(acceptedPermissions: Array<out String>)
    {
        if (PermissionManager.containLocationPermissions(acceptedPermissions))
        {
            // Se i permessi della localizzazione sono stati accettati, viene richiesto l'accesso al GPS.
            gpsSettingsManager.request(forcingRequest)
        }
    }
}