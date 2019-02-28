package com.krake.core.address

import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.krake.core.address.PlaceIdTask.Listener
import com.krake.core.thread.AsyncTask
import com.krake.core.thread.async
import java.util.*

/**
 * Task che permette di caricare un luogo partendo da un [PlaceResult.placeId].
 * Questo task si bas sulle API [Places] di Google.
 *
 * @param client [PlacesClient] già connesso.
 * @param listener [Listener] per ricevere la callback quando l'indirizzo è stato caricato.
 * @constructor crea un nuovo [PlaceIdTask].
 */
class PlaceIdTask(client: PlacesClient, var listener: Listener?) {
    private var apiClient: PlacesClient? = client
    private var task: AsyncTask<*>? = null

    /**
     * Fa partire il caricamento dei dati.
     * Se i dati verranno scaricati con successo il [Listener] verrà notificato con [Listener.onPlaceLoaded].
     *
     * @param placeId stringa che rappresenta un [PlaceResult.placeId].
     * @param requestId id da associare alla richiesta per supportare richieste multiple.
     */
    @JvmOverloads
    fun load(placeId: String, requestId: Int = 0) {
        cancel()
        task = async {
            var place: Place? = null

            apiClient?.let {
                // Il GoogleApiClient deve essere connesso prima di arrivare qui.
                val placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

                val request = FetchPlaceRequest.builder(placeId, placeFields).build()

                try {
                    place = Tasks.await(it.fetchPlace(request)).place
                } catch (ignored: Exception) {

                }
            }

            place
        }.completed {
            if (it != null) {
                listener?.onPlaceLoaded(requestId, it)
            }
        }.build()
        task?.load()
    }

    /**
     * Cancella il task attivo fermando il caricamento dei dati.
     */
    fun cancel() {
        task?.cancel()
    }

    /**
     * Cancella il task e rilascia le reference al [GoogleApiClient] e al [Listener] evitando il memory leak.
     */
    fun release() {
        cancel()
        apiClient = null
        listener = null
    }

    /**
     * Listener usato per essere notificati quando i dati sono stati caricati.
     */
    interface Listener {

        /**
         * Callback richiamata quando viene scaricato il [Place] associato al [PlaceResult.placeId] di partenza.
         *
         * @param requestId id della richiesta associato all'inizio.
         * @param place [Place] caricato a partire da un [PlaceResult.placeId]
         */
        fun onPlaceLoaded(requestId: Int, place: Place)
    }
}