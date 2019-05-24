package com.krake.otp.task

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.krake.core.network.RemoteClient
import com.krake.core.network.RemoteRequest
import com.krake.core.thread.AsyncTask
import com.krake.core.thread.async
import com.krake.trip.R

/**
 * Created by joel on 24/05/17.
 */
class PatternPolylineTask(context: Context, listener: Listener) {
    private var task: AsyncTask<*>? = null
    private var context: Context? = context
    private var listener: Listener? = listener

    fun loadKML(patternID: String) {
        cancel()
        task = async {
            val points = context?.let { context ->
                var points: List<LatLng>? = null
                val url = String.format(context.getString(R.string.bus_pattern_geometry_url_format), context.getString(R.string.open_trip_planner_base_url), patternID)

                val request = RemoteRequest(url)
                    .setMethod(RemoteRequest.Method.GET)

                try {
                    val jsonResult = RemoteClient.client(RemoteClient.Mode.DEFAULT)
                        .execute(request).jsonObject()

                    points = PolyUtil.decode(jsonResult!!.get("points").asString)
                } catch (ignored: Exception) {

                }
                points
            }
            points ?: emptyList()
        }.completed {
            listener?.onPatternPolylineLoaded(patternID, it)
        }.build()
        task?.load()
    }

    fun cancel() {
        task?.cancel()
    }

    fun release() {
        cancel()
        task = null
        context = null
        listener = null
    }

    interface Listener {
        fun onPatternPolylineLoaded(patternID: String, polyline: List<LatLng>)
    }
}