package com.krake.core.address

import android.content.Context
import android.os.Handler
import android.os.Message
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.krake.core.address.PlacesResultTask.Listener
import com.krake.core.thread.AsyncTask
import com.krake.core.thread.async

/**
 * Task che permette di caricare una lista di predizioni di luoghi partendo da una stringa.
 * Questo task si bas sulle API [Places] di Google.
 *
 * @param context [Context] usato per creare i [PlaceResult].
 * @param apiClient [PlacesClient] già connesso.
 * @param listener [Listener] per ricevere la callback quando la lista di luoghi è stata caricata.
 * @constructor crea un nuovo [PlacesResultTask].
 */
class PlacesResultTask(
    context: Context,
    var searcher: RemoteAddressSeacher,
    waitToSearch: Boolean,
    var listener: Listener?
) : Handler.Callback {
    private var context: Context? = context
    private var task: AsyncTask<List<PlaceResult>?>? = null
    private var handler: Handler?

    init {
        if (waitToSearch) {
            handler = Handler(this)
        } else {
            handler = null
        }
    }

    /**
     * Fa partire il caricamento dei dati.
     * Se i dati verranno scaricati con successo il [Listener] verrà notificato con [Listener.onPlacesResultLoaded].
     *
     * @param addressName stringa di partenza per caricare le predizioni.
     * @param searchBoundsProvider oggetto che fornisce i [LatLngBounds] di ricerca.
     * @param requestId id da associare alla richiesta per supportare richieste multiple.
     */
    @JvmOverloads
    fun load(
        addressName: String,
        searchBoundsProvider: SearchBoundsProvider? = null,
        requestId: Int = 0
    ) {
        cancel()
        task = async {
            var places: List<PlaceResult>? = null
            val context = this@PlacesResultTask.context
            if (context != null) {
                places = searcher.searchAddress(context, addressName, searchBoundsProvider)
            }
            places
        }.completed {
            if (it != null) {
                // Notifica il listener del corretto scaricamento dei dati.
                listener?.onPlacesResultLoaded(requestId, it)
            }
        }.build()

        if (handler == null)
            task?.load()
        else
            handler?.sendEmptyMessageDelayed(SEARCH_MESSAGE, MESSAGE_DELAY)
    }

    /**
     * Cancella il task attivo fermando il caricamento dei dati.
     */
    fun cancel() {
        handler?.removeMessages(SEARCH_MESSAGE)
        task?.cancel()
    }

    /**
     * Cancella il task e rilascia le reference al [GoogleApiClient], al [Context] e al [Listener] evitando il memory leak.
     */
    fun release() {
        cancel()
        searcher.release()
        context = null
        listener = null
        handler = null
    }

    override fun handleMessage(p0: Message?): Boolean {
        task?.load()
        return true
    }

    companion object {
        private val SEARCH_MESSAGE = 876
        private val MESSAGE_DELAY = 200L
    }

    /**
     * Listener usato per essere notificati quando i dati sono stati caricati.
     */
    interface Listener {

        /**
         * Callback richiamata quando viene scaricata la lista di predizioni.
         *
         * @param requestId id della richiesta associato all'inizio.
         * @param places lista di [PlaceResult] caricati a partire da una stringa.
         */
        fun onPlacesResultLoaded(requestId: Int, places: List<PlaceResult>)
    }
}