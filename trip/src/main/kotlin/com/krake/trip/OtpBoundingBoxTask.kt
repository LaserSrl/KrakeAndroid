package com.krake.trip

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.gson.Gson
import com.krake.core.network.RemoteClient
import com.krake.core.network.RemoteRequest
import com.krake.core.thread.AsyncTask
import com.krake.core.thread.async
import java.util.*

/**
 * Created by antoniolig on 20/04/2017.
 */
class OtpBoundingBoxTask(context: Context, listener: Listener) {
    companion object {
        private const val BOUNDS_BOX_KEY = "bounds"
        private const val DATE_UPDATED_KEY = "dateUpdateBounds"
    }

    private var context: Context? = context
    private var task: AsyncTask<*>? = null
    private var listener: Listener? = listener

    @SuppressLint("ApplySharedPref")
    fun load() {
        cancel()
        task = async {
            val context = this@OtpBoundingBoxTask.context ?: throw NullPointerException("The Context must not be null.")

            val gson = Gson()
            val prefs = context.getSharedPreferences("OTPBounds", Context.MODE_PRIVATE)

            if (prefs.contains(BOUNDS_BOX_KEY)) {
                val dateUpdated = Date(prefs.getLong(DATE_UPDATED_KEY, 0))

                if (dateUpdated.after(Date((-1000 * context.resources.getInteger(R.integer.otp_bounds_cache_validity)).toLong()))) {
                    return@async gson.fromJson<LatLngBounds>(prefs.getString(BOUNDS_BOX_KEY, ""), LatLngBounds::class.java)
                }
            }

            val request = RemoteRequest(context.getString(R.string.open_trip_planner_base_url))
                    .setMethod(RemoteRequest.Method.GET)

            val requestJson = RemoteClient.client(RemoteClient.Mode.DEFAULT)
                    .execute(request)
                    .jsonObject()
            val coordinatesGroup = requestJson!!.getAsJsonObject("polygon").getAsJsonArray("coordinates")
            val builder = LatLngBounds.builder()

            coordinatesGroup
                    .flatMap { it.asJsonArray }
                    .forEach {
                        val coordinates = it.asJsonArray
                        builder.include(LatLng(coordinates[1].asDouble, coordinates[0].asDouble))
                    }

            val bounds = builder.build()

            prefs.edit()
                    .putString(BOUNDS_BOX_KEY, gson.toJson(bounds))
                    .putLong(DATE_UPDATED_KEY, Date().time)
                    .commit()

            bounds
        }.completed {
            listener?.onBoundsLoaded(it)
        }.build()
        task?.load()
    }

    fun cancel() {
        task?.cancel()
    }

    fun release() {
        cancel()
        context = null
        listener = null
    }

    interface Listener {
        fun onBoundsLoaded(bounds: LatLngBounds)
    }
}