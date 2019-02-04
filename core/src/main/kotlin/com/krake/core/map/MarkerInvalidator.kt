package com.krake.core.map

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.model.Marker
import java.util.*

/**
 * Classe che permette di invalidare le configurazioni di un [Marker].
 */
class MarkerInvalidator {
    companion object {
        private val TAG = MarkerInvalidator::class.java.simpleName
    }

    private val listeners = LinkedList<Listener>()

    /**
     * Registra un [MarkerInvalidator.Listener] per ricevere la callback per invalidare un [Marker].
     *
     * @param listener [MarkerInvalidator.Listener] da registrare.
     */
    fun registerInvalidator(listener: Listener) {
        Log.d(TAG, "Registering ${listener::class.java.simpleName} as invalidator listener")
        listeners.add(listener)
    }

    /**
     * Elimina la registrazione di un [MarkerInvalidator.Listener] per non ricevere più
     * la callback per invalidare un [Marker].
     *
     * @param listener [MarkerInvalidator.Listener] che verrà eliminato.
     */
    fun unregisterInvalidator(listener: Listener) {
        Log.d(TAG, "Unregistering ${listener::class.java.simpleName} as invalidator listener")
        listeners.remove(listener)
    }

    /**
     * Richiama su tutti i [MarkerInvalidator.Listener] registrati la callback per invalidare il [Marker]
     * con le nuove configurazioni.
     *
     * @param context [Context] usato per invalidare il [Marker].
     * @param configuration configurazione del [Marker].
     */
    fun invalidate(context: Context, configuration: MarkerConfiguration) {
        listeners.forEach {
            Log.d(TAG, "invalidating markers of ${it::class.java.simpleName}")
            it.invalidateMarker(context, configuration)
        }
    }

    /**
     * Interfaccia usata per ricevere la callback per invalidare il [Marker].
     */
    interface Listener {

        /**
         * Specifica le azioni da effettuare per invalidare un [Marker].
         *
         * @param context [Context] usato per invalidare il [Marker].
         * @param configuration configurazione del [Marker].
         */
        fun invalidateMarker(context: Context, configuration: MarkerConfiguration)
    }

    /**
     * Interfaccia usata per fornire un'istanza valida di [MarkerInvalidator] sul quale registrare
     * i [MarkerInvalidator.Listener].
     */
    interface Provider {

        /**
         * Fornisce un'istanza di [MarkerInvalidator] per registrare i listeners e far partire
         * l'invalidazione dei [Marker].
         *
         * @return istanza di [MarkerInvalidator].
         */
        fun provideMarkerInvalidator(): MarkerInvalidator
    }
}