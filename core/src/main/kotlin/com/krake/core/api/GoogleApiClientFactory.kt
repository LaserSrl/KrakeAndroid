package com.krake.core.api

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.Api
import com.google.android.gms.common.api.GoogleApiClient
import com.krake.core.api.GoogleApiClientFactory.ConnectionListener
import com.krake.core.app.ActivityResultCallback
import com.krake.core.app.ResultManager
import java.util.*

/**
 * Permette di creare un [GoogleApiClient], gestire le API di Google da utilizzare e
 * notificare i cambiamenti ad un [ConnectionListener].
 *
 * @param activity [Activity] utilizzata per mostrare i messaggi di errore gestiti da Google.
 * @param listener [ConnectionListener] da notificare per i cambiamenti del [GoogleApiClient].
 * @param apis lista di API di Google da aggiungere al [GoogleApiClient].
 * @constructor crea una nuova [GoogleApiClientFactory] per inizializzare e gestire un [GoogleApiClient].
 */
class GoogleApiClientFactory(activity: Activity, var listener: ConnectionListener?, vararg apis: Api<out Api.ApiOptions.NotRequiredOptions>) : GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ActivityResultCallback {

    companion object {
        private val TAG = GoogleApiClientFactory::class.java.simpleName
        private const val REQUEST_CODE_CHECK_PS = 92

        @JvmStatic
        fun defaultConnectionSuspendedResolution(code: Int) {
            Log.w(TAG, "Connection suspended with code: $code")
        }

        @JvmStatic
        fun defaultConnectionFailedResolution(result: ConnectionResult) {
            Log.w(TAG, "Connection failed with result code: ${result.errorCode} and message: ${result.errorMessage ?: " - "}")
        }
    }

    private var activity: Activity? = activity
    var apiClient: GoogleApiClient
        private set

    init {
        val builder = createApiClientBuilder(activity)
        apis.forEach { builder.addApi(it) }
        apiClient = builder.build()
    }

    /**
     * Aggiunge delle API di Google al [GoogleApiClient], anche se è stato creato precedentemente.
     * Nel caso in cui il [GoogleApiClient] è connesso, lo disconnette e lo riconnette successivamente.
     *
     * @param apis lista di API di Google da aggiungere al [GoogleApiClient].
     */
    fun addApis(vararg apis: Api<out Api.ApiOptions.NotRequiredOptions>) {
        val apisToConnect = LinkedList<Api<out Api.ApiOptions.NotRequiredOptions>>()
        apis.forEach {
            if (!apiClient.hasConnectedApi(it)) {
                apisToConnect.add(it)
            }
        }

        val activity = activity
        if (apisToConnect.isNotEmpty() && activity != null) {
            val builder = createApiClientBuilder(activity)
            apisToConnect.forEach {
                builder.addApi(it)
            }
            val wasConnected = apiClient.isConnected || apiClient.isConnecting
            if (wasConnected) {
                disconnect()
            }
            apiClient = builder.build()
            if (wasConnected) {
                connect()
            }
        }
    }

    /**
     * Connette il [GoogleApiClient] nel caso in cui non sia connesso.
     * Il metodo [ConnectionListener.onApiClientConnected] viene richiamato sia nel caso in cui
     * il [GoogleApiClient] fosse connesso in precedenza, sia nel caso in cui la connessione sia
     * dovuta al metodo [GoogleApiClient.connect].
     */
    fun connect() {
        if (apiClient.isConnected) {
            listener?.onApiClientConnected()
            return
        }

        val activity = activity
        if (activity != null && !apiClient.isConnecting) {
            val googleAPI = GoogleApiAvailability.getInstance()
            val result = googleAPI.isGooglePlayServicesAvailable(activity.applicationContext)
            if (result == ConnectionResult.SUCCESS) {
                Log.d(TAG, "Connecting the Google Api Client")
                apiClient.connect()
                return
            }

            // Risolve l'errore presentando un dialog di Google.
            if (googleAPI.isUserResolvableError(result)) {
                (activity as? ResultManager.Provider)?.provideResultManager()?.registerForResult(this)
                Log.d(TAG, "Showing dialog fragment to resolve error related to Play Services")
                googleAPI.showErrorDialogFragment(activity, result, REQUEST_CODE_CHECK_PS)
            }
        }
    }

    /**
     * Disconnette il [GoogleApiClient] nel caso in cui sia connesso.
     */
    fun disconnect() {
        if (apiClient.isConnected || apiClient.isConnecting) {
            Log.d(TAG, "Disconnecting the Google Api Client")
            apiClient.disconnect()
        }
    }

    /**
     * Disconnette il [GoogleApiClient] nel caso in cui sia connesso.
     * Rilascia le reference all'[Activity] e al [ConnectionListener].
     */
    fun destroy() {
        apiClient.unregisterConnectionFailedListener(this)
        apiClient.unregisterConnectionCallbacks(this)
        disconnect()
        (activity as? ResultManager.Provider)?.provideResultManager()?.unregisterForResult(this)
        listener = null
        activity = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_CHECK_PS && resultCode == Activity.RESULT_OK) {
            connect()
        }
    }

    override fun onConnected(bundle: Bundle?) {
        listener?.onApiClientConnected()
    }

    override fun onConnectionSuspended(code: Int) {
        listener?.onApiClientConnectionSuspended(code)
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        listener?.onApiClientConnectionFailed(result)
    }

    private fun createApiClientBuilder(activity: Activity) = GoogleApiClient.Builder(activity.applicationContext)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)

    /**
     * Listener che viene notificato sui cambiamenti del [GoogleApiClient].
     */
    interface ConnectionListener {

        /**
         * Richiamato quando il [GoogleApiClient] viene connesso con successo.
         */
        fun onApiClientConnected()

        /**
         * Richiamato quando la connessione al [GoogleApiClient] viene sospesa.
         *
         * @param code codice per identificare il tipo di sospensione.
         */
        fun onApiClientConnectionSuspended(code: Int) {
            defaultConnectionSuspendedResolution(code)
        }

        /**
         * Richiamato quando la connessione al [GoogleApiClient] fallisce.
         *
         * @param result risultato che identifica il fallimento della connessione.
         */
        fun onApiClientConnectionFailed(result: ConnectionResult) {
            defaultConnectionFailedResolution(result)
        }
    }
}