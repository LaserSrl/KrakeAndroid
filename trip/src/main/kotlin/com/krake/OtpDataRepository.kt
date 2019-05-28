package com.krake

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.maps.android.PolyUtil
import com.krake.core.network.RemoteClient
import com.krake.core.network.RemoteRequest
import com.krake.bus.model.OtpBusRoute
import com.krake.bus.model.OtpBusStop
import com.krake.bus.model.OtpStopTime
import com.krake.trip.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * repository used for fetch data from otp server.
 * this must be initialized in application
 */
class OtpDataRepository private constructor(private val context: Context) {
    private val gson = Gson()
    private val otpBaseUrl = context.getString(R.string.open_trip_planner_base_url)

    suspend fun loadBusRoutes(): List<OtpBusRoute> {
        val url = String.format(context.getString(R.string.bus_routes_path), otpBaseUrl)

        val request = RemoteRequest(url)
            .setMethod(RemoteRequest.Method.GET)

        val jsonResult = RemoteClient.client(RemoteClient.Mode.DEFAULT)
            .execute(request)
            .jsonArray()

        return gson.fromJson(jsonResult, Array<OtpBusRoute>::class.java).toList()
    }

    suspend fun loadStopsByBusRoute(routeId: String): List<OtpBusStop> {
        val url = String.format(context.getString(R.string.bus_stops_by_route_path), otpBaseUrl, routeId)

        val request = RemoteRequest(url)
            .setMethod(RemoteRequest.Method.GET)

        val jsonResult = RemoteClient.client(RemoteClient.Mode.DEFAULT)
            .execute(request)
            .jsonArray()

        return gson.fromJson(jsonResult, Array<OtpBusStop>::class.java).toList()
    }

    suspend fun loadBusTimesByDate(stop: OtpBusStop, routeId: String, date: Date): List<OtpStopTime> {
        val dateFormatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val url = String.format(context.getString(R.string.bus_stoptimes_by_stop_path), otpBaseUrl, stop.id, dateFormatter.format(date))

        val request = RemoteRequest(url)
            .setMethod(RemoteRequest.Method.GET)

        val jsonResult = RemoteClient.client(RemoteClient.Mode.DEFAULT)
            .execute(request)
            .jsonArray()?.filter {
                (it.asJsonObject).get("pattern").asJsonObject.get("id").asString.startsWith(routeId)
            }?.map {
                (it.asJsonObject).get("times").asJsonArray
            }?.flatten()

        return gson.fromJson(gson.toJsonTree(jsonResult), Array<OtpStopTime>::class.java)
            .toList()
            .sortedBy {
                it.scheduledDeparture
            }
    }

    suspend fun loadPatternGeometry(patternID: String): List<LatLng> {
        var points: List<LatLng>? = null
        val url = String.format(context.getString(R.string.bus_pattern_geometry_url_format), otpBaseUrl, patternID)

        val request = RemoteRequest(url)
            .setMethod(RemoteRequest.Method.GET)

        try {
            val jsonResult = RemoteClient.client(RemoteClient.Mode.DEFAULT)
                .execute(request).jsonObject()

            points = PolyUtil.decode(jsonResult!!.get("points").asString)
        } catch (ignored: Exception) {

        }
        return points ?: emptyList()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var shared: OtpDataRepository

        fun create(context: Context) {
            this.shared = OtpDataRepository(context)
        }
    }
}