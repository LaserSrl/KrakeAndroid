package com.krake.core.location.settings

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.*
import com.krake.core.api.GoogleApiClientFactory
import com.krake.core.app.ActivityResultCallback
import com.krake.core.app.ResultManager

/**
 * Support [Fragment] che si occupa di richiedere l'attivazione del GPS.
 */
class GpsSettingsFragment : Fragment(),
        ResultCallback<LocationSettingsResult>,
        ActivityResultCallback,
        GoogleApiClientFactory.ConnectionListener {

    companion object {
        const val FRAGMENT_TAG = "GpsRequestFrag"
        private val TAG = GpsSettingsFragment::class.java.simpleName
        private val ARG_SETTINGS_CHANGE_UNAVAILABLE_MSG = "argSettingsChangeUnavailableMsg"
        private const val REQUEST_CODE_LOCATION_SETTINGS = 742
        private const val OUT_STATE_AVOID_DIALOG = "otsAvoidDialog"
        private const val OUT_STATE_REQUIRED_GPS = "otsRequiredGps"

        /**
         * Crea un nuovo [GpsSettingsFragment] con un [Bundle].
         *
         * @param settingsChangeUnavailableMsg messaggio da mostrare nel caso in cui non sia possibile
         * modificare le impostazioni del GPS.
         */
        fun newInstance(settingsChangeUnavailableMsg: String): GpsSettingsFragment {
            val fragment = GpsSettingsFragment()
            val args = Bundle()
            args.putString(ARG_SETTINGS_CHANGE_UNAVAILABLE_MSG, settingsChangeUnavailableMsg)
            fragment.arguments = args
            return fragment
        }
    }

    var callback: Callback? = null

    private lateinit var settingsChangeUnavailableMsg: String
    var avoidAskingDialog = false
    private var askingGpsAccess = false
    private var forcingRequest = false
    private lateinit var apiClientFactory: GoogleApiClientFactory

    private val locationSettingsRequest: LocationSettingsRequest by lazy {
        val request = LocationRequest()
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        LocationSettingsRequest.Builder()
                .addLocationRequest(request)
                .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        avoidAskingDialog = savedInstanceState?.getBoolean(OUT_STATE_AVOID_DIALOG) ?: false
        askingGpsAccess = savedInstanceState?.getBoolean(OUT_STATE_REQUIRED_GPS) ?: false
        val arguments = arguments ?: throw IllegalArgumentException("The arguments mustn't be null.")
        settingsChangeUnavailableMsg = arguments.getString(ARG_SETTINGS_CHANGE_UNAVAILABLE_MSG)
        // Inizializza il GoogleApiClient con le API di localizzazione.
        val activity = activity ?: throw IllegalArgumentException("The activity mustn't be null.")
        apiClientFactory = GoogleApiClientFactory(activity, this, LocationServices.API)
        // Si registra per ricevere le notifiche dell'onActivityResult()
        (activity as? ResultManager.Provider)?.provideResultManager()?.registerForResult(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(OUT_STATE_AVOID_DIALOG, avoidAskingDialog)
        outState.putBoolean(OUT_STATE_REQUIRED_GPS, askingGpsAccess)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Blocca la ricezione delle notifiche dell'onActivityResult()
        (activity as? ResultManager.Provider)?.provideResultManager()?.unregisterForResult(this)
        // Rilascia il GoogleApiClient.
        apiClientFactory.destroy()
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    override fun onApiClientConnected() {
        Log.d(TAG, "checking location settings")
        LocationServices.SettingsApi.checkLocationSettings(apiClientFactory.apiClient, locationSettingsRequest).setResultCallback(this)
    }

    override fun onResult(result: LocationSettingsResult) {
        val status = result.status
        when (status.statusCode) {
            LocationSettingsStatusCodes.SUCCESS -> {
                // Il GPS è attivo.
                dispatchGpsSettingsAcquired()
            }
            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                // Il GPS può essere attivato.
                if (!askingGpsAccess && (!avoidAskingDialog || forcingRequest)) {
                    avoidAskingDialog = true
                    askingGpsAccess = true
                    try {
                        Log.d(TAG, "starting resolution for GPS settings")
                        /* Mostra il dialog chiamando startResolutionForResult()
                         * e verifica il risultato in onActivityResult() */
                        status.startResolutionForResult(activity, REQUEST_CODE_LOCATION_SETTINGS)
                    } catch (ignored: IntentSender.SendIntentException) {
                        // Ignora l'errore.
                    }
                }
            }
            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                // Il GPS non può essere attivato.
                Log.d(TAG, "settings change is unavailable")
                callback?.showSettingsChangeUnavailableMessage(settingsChangeUnavailableMsg)
                dispatchGpsSettingsUnavailable()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_CODE_LOCATION_SETTINGS)
            return

        askingGpsAccess = false

        Log.d(TAG, "resolving activity result for location settings, result code: $resultCode")
        if (resultCode == Activity.RESULT_OK) {
            // Il GPS è attivo.
            dispatchGpsSettingsAcquired()
        } else {
            dispatchGpsSettingsUnavailable()
        }
    }

    /**
     * Richiede l'attivazione del GPS.
     *
     * @param force true se si vuole forzare la richiesta del GPS ogni volta, altrimenti, la richiesta
     * verrà spedita solo una volta per tutta la durata della sezione.
     */
    fun requireGps(force: Boolean) {
        forcingRequest = force
        apiClientFactory.connect()
    }

    private fun dispatchGpsSettingsAcquired() {
        Log.d(TAG, "GPS settings acquired")
        callback?.onGpsSettingsAcquired()
    }

    private fun dispatchGpsSettingsUnavailable() {
        Log.d(TAG, "GPS settings unavailable")
        callback?.onGpsSettingsUnavailable()
    }

    /**
     * Listener che viene notificato quando ci sono delle modifiche al GPS o c'è bisogno di mostrare un messaggio.
     */
    interface Callback {

        /**
         * Specifica le azioni da effettuare quando l'utilizzo del GPS è stato approvato ed
         * il GPS è stato acceso con successo.
         */
        fun onGpsSettingsAcquired()

        /**
         * il GPS non è stato acceso.
         */
        fun onGpsSettingsUnavailable()

        /**
         * Specifica come deve essere mostrato il messaggio quando è impossibile cambiare le impostazioni del GPS.
         *
         * @param message messaggio da mostrare.
         */
        fun showSettingsChangeUnavailableMessage(message: String)
    }
}