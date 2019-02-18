package com.krake.core.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.krake.core.location.GeocoderTask.Listener
import com.krake.core.thread.AsyncTask
import com.krake.core.thread.async

/**
 * Task che permette di caricare un indirizzo a partire da una latitude e una longitudine.
 * Questo task si basa sul [Geocoder] per caricare i dati.
 *
 * @param context [Context] usato per accedere al [Geocoder].
 * @param listener [Listener] per ricevere la callback quando l'indirizzo è stato caricato.
 * @constructor crea un nuovo [GeocoderTask].
 */
class GeocoderTask(context: Context, var listener: Listener?) {
    private var context: Context? = context
    private var task: AsyncTask<*>? = null

    /**
     * Fa partire il caricamento dei dati.
     * Se i dati verranno scaricati con successo il [Listener] verrà notificato con [Listener.onAddressLoaded].
     *
     * @param location punto di partenza.
     */
    fun load(location: Location) {
        load(LatLng(location.latitude, location.longitude))
    }

    /**
     * Fa partire il caricamento dei dati.
     * Se i dati verranno scaricati con successo il [Listener] verrà notificato con [Listener.onAddressLoaded].
     *
     * @param latLng punto di partenza.
     */
    fun load(latLng: LatLng) {
        cancel()
        task = async {
            var address: Address? = null

            context?.let { context ->
                // Carica l'indirizzo solo se il Geocoder è disponibile.
                if (Geocoder.isPresent()) {
                    try {
                        val addresses = Geocoder(context).getFromLocation(latLng.latitude, latLng.longitude, 1)
                        address = addresses.firstOrNull()
                    } catch (exception: Exception) {

                    }
                }
            }
            address
        }.completed {
            if (it != null) {
                listener?.onAddressLoaded(it)
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
     * Cancella il task e rilascia le reference al [Context] e al [Listener] evitando il memory leak.
     */
    fun release() {
        cancel()
        context = null
        listener = null
    }

    /**
     * Listener usato per essere notificati quando i dati sono stati caricati.
     */
    interface Listener {

        /**
         * Callback richiamata quando viene scaricato l'indirizzo associato al punto di partenza.
         *
         * @param address indirizzo scaricato.
         */
        fun onAddressLoaded(address: Address)
    }
}