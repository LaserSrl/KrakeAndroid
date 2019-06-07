package com.krake

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.maps.android.PolyUtil
import com.krake.bus.model.*
import com.krake.core.network.RemoteClient
import com.krake.core.network.RemoteRequest
import com.krake.core.model.identifierOrStringIdentifier
import java.text.SimpleDateFormat
import java.util.*
import com.krake.bus.component.module.BusComponentModule
import com.krake.trip.R


/**
 * repository used for fetch data from otp server.
 * this must be initialized in application
 */
class OtpDataRepository private constructor(private val context: Context, private val busComponentModule: BusComponentModule) {
    private val gson = Gson()
    private val otpBaseUrl = context.getString(R.string.open_trip_planner_base_url)

    suspend fun loadBusRoutes(): List<BusRoute> {
        val url = String.format(context.getString(R.string.bus_routes_path), otpBaseUrl)

        val request = RemoteRequest(url)
            .setMethod(RemoteRequest.Method.GET)

        val jsonResult = RemoteClient.client(RemoteClient.Mode.DEFAULT)
            .execute(request)
            .jsonArray()

        return gson.fromJson(jsonResult, Array<OtpBusRoute>::class.java).toList().sortedBy { it.longName }
    }

    suspend fun loadStopsByBusRoute(routeId: String): List<BusStop> {
        val url = String.format(context.getString(R.string.bus_stops_by_route_path), otpBaseUrl, routeId)

        val request = RemoteRequest(url)
            .setMethod(RemoteRequest.Method.GET)

        val jsonResult = RemoteClient.client(RemoteClient.Mode.DEFAULT)
            .execute(request)
            .jsonArray()

        val clazz = java.lang.reflect.Array.newInstance(busComponentModule.stopItemClass, 0).javaClass

        @Suppress("UNCHECKED_CAST")
        return (gson.fromJson(jsonResult, clazz) as Array<BusStop>).toList()
    }

    suspend fun loadBusTimesByDate(stop: BusStop, routeId: String, date: Date): List<OtpStopTime> {
        val dateFormatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val url = String.format(context.getString(R.string.bus_stoptimes_by_stop_path), otpBaseUrl, stop.id, dateFormatter.format(date))

        val request = RemoteRequest(url)
            .setMethod(RemoteRequest.Method.GET)

        val jsonResult = RemoteClient.client(RemoteClient.Mode.DEFAULT)
            .execute(request)
            .jsonArray()?.distinctBy {
                (it.asJsonObject).get("pattern").asJsonObject.get("id").asString
            }?.filter {
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

        fun create(context: Context, busComponentModule: BusComponentModule) {
            this.shared = OtpDataRepository(context, busComponentModule)
        }
    }
}