package com.krake.bus.app

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.krake.OtpDataRepository
import com.krake.core.network.RemoteClient
import com.krake.core.network.RemoteRequest
import com.krake.core.thread.AsyncTask
import com.krake.core.thread.async
import com.krake.trip.R

/**
 * Created by joel on 24/05/17.
 */
class PatternPolylineTask(listener: Listener) {
    private var task: AsyncTask<*>? = null
    private var listener: Listener? = listener

    fun loadKML(patternID: String) {
        cancel()
        task = async {
            OtpDataRepository.shared.loadPatternGeometry(patternID)
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
        listener = null
    }

    interface Listener {
        fun onPatternPolylineLoaded(patternID: String, polyline: List<LatLng>)
    }
}