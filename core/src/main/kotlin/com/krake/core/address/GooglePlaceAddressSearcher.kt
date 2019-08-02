package com.krake.core.address

import android.content.Context
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.gson.Gson
import com.krake.core.R

class GooglePlaceAddressSearcher(
    private var apiClient: PlacesClient?,
    private val typeFilter: TypeFilter = TypeFilter.ADDRESS
) : RemoteAddressSeacher {

    override fun searchAddress(
        context: Context,
        addressName: String,
        searchBoundsProvider: SearchBoundsProvider?
    ): List<PlaceResult>? {
        val apiClient = apiClient
        if (apiClient != null) {
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
                .setSessionToken(token)
                .setQuery(addressName)
                .build()

            try {
                val predictions =
                    Tasks.await(apiClient.findAutocompletePredictions(request)).autocompletePredictions

                return predictions.map {
                    PlaceResult(context, it)
                }.toMutableList()
            } catch (e: Exception) {
                e.toString()
            }
        }
        return null
    }

    override fun release() {
        apiClient = null
    }
}