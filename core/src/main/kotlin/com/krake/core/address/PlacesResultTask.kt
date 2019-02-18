package com.krake.core.address

import android.content.Context
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.gson.Gson
import com.krake.core.R
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
class PlacesResultTask(context: Context, apiClient: PlacesClient, var listener: Listener?) {
    private var context: Context? = context
    private var apiClient: PlacesClient? = apiClient
    private var task: AsyncTask<MutableList<PlaceResult>?>? = null

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
        typeFilter: TypeFilter = TypeFilter.ADDRESS,
        searchBoundsProvider: SearchBoundsProvider? = null,
        requestId: Int = 0
    ) {
        cancel()
        task = async {
            var places: MutableList<PlaceResult>? = null
            val context = this@PlacesResultTask.context
            val apiClient = this@PlacesResultTask.apiClient
            if (context != null && apiClient != null) {
                // Se non ci sono bounds disponibili, vengono caricati quelli di default.
                val bounds: LatLngBounds = if (searchBoundsProvider?.searchBounds == null) {
                    Gson().fromJson(context.getString(R.string.place_search_default_json), LatLngBounds::class.java)
                } else {
                    searchBoundsProvider.searchBounds!!
                }

                val token = AutocompleteSessionToken.newInstance()
                val convertedBounds = RectangularBounds.newInstance(bounds)
                val request = FindAutocompletePredictionsRequest.builder()
                    .setLocationBias(convertedBounds)
                    .setTypeFilter(typeFilter)
                    .setSessionToken(token)
                    .setQuery(addressName)
                    .build()

                try {
                    val predictions =
                        Tasks.await(apiClient.findAutocompletePredictions(request)).autocompletePredictions

                    places = predictions.map {
                        PlaceResult(context, it)
                    }.toMutableList()
                } catch (e: Exception) {
                    e.toString()
                }
            }
            places

        }.completed {
            if (it != null) {
                // Notifica il listener del corretto scaricamento dei dati.
                listener?.onPlacesResultLoaded(requestId, it)
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
     * Cancella il task e rilascia le reference al [GoogleApiClient], al [Context] e al [Listener] evitando il memory leak.
     */
    fun release() {
        cancel()
        apiClient = null
        context = null
        listener = null
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
        fun onPlacesResultLoaded(requestId: Int, places: MutableList<PlaceResult>)
    }
}