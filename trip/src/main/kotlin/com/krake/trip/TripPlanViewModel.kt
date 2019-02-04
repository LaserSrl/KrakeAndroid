package com.krake.trip

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.location.Location
import android.text.Spanned
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.krake.core.OrchardError
import com.krake.core.util.PlainAbstractTypeAdapter

/**
 * Classe astratta per raggruppare una richiesta di Pianifcazione del viaggio
 * Permette di cambiare server usato per la pianificazione solo tramite configurazione
 * Created by joel on 19/04/17.
 */
abstract class TripPlanViewModel : ViewModel()
{
    companion object
    {
        fun gson(): Gson
        {
            return GsonBuilder()
                    .registerTypeAdapter(ComplexStep::class.java, PlainAbstractTypeAdapter())
                    .registerTypeAdapter(Spanned::class.java, PlainAbstractTypeAdapter())
                    .registerTypeAdapter(Location::class.java, LocationSerializer())
                    .create()
        }
    }

    protected val mutableTripResult = MutableLiveData<TripResult>()
    val tripResult: LiveData<TripResult> = mutableTripResult

    protected val mutableLoading = MutableLiveData<Boolean>().apply { value = false }
    val loading: LiveData<Boolean> = mutableLoading

    abstract fun planTrip(context: Context, request: TripPlanRequest)
}

class TripResult(val result: TripPlanResult?, val error: OrchardError?)
