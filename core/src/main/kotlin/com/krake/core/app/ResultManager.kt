package com.krake.core.app

import android.app.Activity
import android.content.Intent
import android.util.Log

/**
 * Gestisce l'invio di [Activity.onActivityResult] ai listeners registrati.
 * Questo permette di inviare l'onActivityResult() al di fuori delle [Activity] basandosi
 * sull'[Activity] che è attualmente attiva.
 */
class ResultManager {
    companion object {
        private val TAG = ResultManager::class.java.simpleName
    }

    val callbacks = HashSet<ActivityResultCallback>()

    /**
     * Registra un'[ActivityResultCallback] per ricevere la notifica del metodo [Activity.onActivityResult].
     *
     * @param callback listener da notificare.
     */
    fun registerForResult(callback: ActivityResultCallback) {
        Log.d(TAG, "Registering ${callback::class.java.simpleName} as result callback")
        callbacks.add(callback)
    }

    /**
     * Elimina l'[ActivityResultCallback] in ascolto per le notifiche del metodo [Activity.onActivityResult].
     *
     * @param callback listener da eliminare.
     */
    fun unregisterForResult(callback: ActivityResultCallback) {
        Log.d(TAG, "Unregistering ${callback::class.java.simpleName} as result callback")
        callbacks.remove(callback)
    }

    /**
     * Spedisce il risultato del metodo [Activity.onActivityResult] ai listeners registrati.
     */
    fun dispatchActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbacks.forEach {
            Log.d(TAG, "sending onActivityResult callback to ${it::class.java.simpleName}")
            it.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * Interfaccia che fornisce il [ResultManager] da utilizzare.
     */
    interface Provider {

        /**
         * @return [ResultManager] sul quale verranno registrati i listeners da notificare.
         */
        fun provideResultManager(): ResultManager
    }
}