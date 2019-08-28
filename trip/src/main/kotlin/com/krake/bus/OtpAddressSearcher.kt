package com.krake.bus

import android.content.Context
import android.location.Location
import com.google.gson.JsonObject
import com.krake.core.address.PlaceResult
import com.krake.core.address.RemoteAddressSeacher
import com.krake.core.address.SearchBoundsProvider
import com.krake.core.network.RemoteClient
import com.krake.core.network.RemoteRequest
import com.krake.trip.R

class OtpAddressSearcher(context: Context) : RemoteAddressSeacher {

    val otpBaseUrl = context.getString(R.string.open_trip_planner_base_url)

    override fun searchAddress(
        context: Context,
        addressName: String,
        searchBoundsProvider: SearchBoundsProvider?
    ): List<PlaceResult>? {

        val request = RemoteRequest(otpBaseUrl)
            .setMethod(RemoteRequest.Method.GET)
            .setPath(context.getString(R.string.otp_geocode_path))

        request.queryParameters.apply {
            this["autocomplete"] = "true"
            this["corners"] = "false"
            this["stops"] = "true"
            this["query"] = addressName
        }

        try {
            val jsonResult = RemoteClient.client(RemoteClient.Mode.DEFAULT)
                .execute(request)
                .jsonArray()

            if (jsonResult != null) {

                val places = mutableListOf<PlaceResult>()
                val results = mutableListOf<PlaceResult>()
                for (elemen in jsonResult) {
                    if (elemen is JsonObject) {
                        var description = elemen.get("description").asString
                        if (description.startsWith("stop ", true)) {
                            description = description.substring(5)
                        }

                        val result = PlaceResult(description)
                        result.location = Location("").apply {
                            latitude = elemen.get("lat").asDouble
                            longitude = elemen.get("lng").asDouble
                        }

                        places.add(result)
                    }
                }

                return places

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    override fun release() {

    }

}