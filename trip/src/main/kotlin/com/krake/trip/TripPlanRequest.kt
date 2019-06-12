package com.krake.trip

import android.content.Context
import com.krake.core.address.PlaceResult
import java.util.*

/**
 * Classe per gestire una richiesta di pianificazione del viaggio.
 * La pianificazione del viaggio può essere fatta solo se la funzione valida ritorna #isValid
 *
 * @property from da dove parte il viaggio
 * @property to arrivo del viaggio
 * @property travelMode modo di pianificazione del viaggio. Default #TRANSIT
 * @property datePlanChoice indicazione se la data indicata in #dateSelectedForPlan è la data di arrivo o di partenza.
 *  Utile solo per le richieste di tipo #TRANSIT
 * @property dateSelectedForPlan data per la pianificazione
 */
data class TripPlanRequest(var from: PlaceResult? = null,
                           var to: PlaceResult? = null) {

    var travelMode: TravelMode = TravelMode.TRANSIT
    var datePlanChoice: DatePlanChoice = DatePlanChoice.DEPARTURE
    var dateSelectedForPlan = Date()
    var maxWalkDistance: Int = 0

    fun isValid(): Boolean {
        return from?.location != null && to?.location != null
    }

    override fun equals(other: Any?): Boolean {

        if (other is TripPlanRequest) {
            return other.travelMode == this.travelMode && other.from?.equals(this.from) ?: false &&
                    other.to?.equals(this.to) ?: false
        }
        return super.equals(other)
    }
}

enum class TravelMode {
    CAR,
    TRANSIT,
    WALK,
    BICYCLE;

    /**
     * Versione leggibile del modo per viaggiare. Per mostrare le istruzioni all'utente
     * @param context dell'App da cui saranno prese le stringhe per la traduzione

     * @return nome del modo per viaggiare oppure una stringa vuota
     */
    fun instructionDescription(context: Context): String {
        return when (this) {
            CAR -> context.getString(R.string.travel_mode_car)
            TRANSIT -> context.getString(R.string.travle_mode_transit)
            WALK -> context.getString(R.string.travel_mode_walk)
            BICYCLE -> context.getString(R.string.travel_mode_bike)
        }
    }

    fun name(context: Context): String {
        return when (this) {
            CAR -> context.getString(R.string.travel_mode_car_name)
            TRANSIT -> context.getString(R.string.travle_mode_transit_name)
            WALK -> context.getString(R.string.travel_mode_walk_name)
            BICYCLE -> context.getString(R.string.travel_mode_bike_name)
        }
    }

    fun drawableResource(): Int {
        return when (this) {
            BICYCLE -> R.drawable.ic_directions_bike_36dp
            CAR -> R.drawable.ic_directions_car_36dp
            WALK -> R.drawable.ic_directions_walk_36dp
            TRANSIT -> R.drawable.ic_directions_bus_36dp
        }
    }

    companion object {
        fun valueOfOrDefault(value: String, default: TravelMode): TravelMode {
            try {
                return valueOf(value)
            } catch (ignored: Exception) {

            }
            return default
        }
    }
}

enum class VehicleType {
    TRAM,
    SUBWAY,
    METRO_RAIL,
    RAIL,
    BUS,
    FERRY,
    TRANSIT,
    OTHER;


    /**
     * Versione leggibile del modo per viaggiare
     * @param context dell'App da cui saranno prese le stringhe per la traduzione
     * *
     * @return nome del modo per viaggiare oppure una stringa vuota
     */
    fun getVisibleName(context: Context): String {
        when (this) {

            TRANSIT -> return context.getString(R.string.travle_mode_transit)
            TRAM -> return context.getString(R.string.travel_mode_tram)
            BUS -> return context.getString(R.string.travel_mode_bus)
            SUBWAY -> return context.getString(R.string.trip_mode_subway)
            else -> return context.getString(R.string.travle_mode_transit)
        }
    }

    fun drawableResource(): Int {
        val modeDrawable: Int
        when (this) {
            VehicleType.SUBWAY -> modeDrawable = R.drawable.icona_metro
            VehicleType.TRAM -> modeDrawable = R.drawable.icona_tram
            VehicleType.BUS -> modeDrawable = R.drawable.ic_directions_bus_36dp
            else -> modeDrawable = R.drawable.icona_trasporto
        }

        return modeDrawable
    }

    companion object {
        fun valueOfOrDefault(value: String, default: VehicleType): VehicleType {
            try {
                return valueOf(value)
            } catch (ignored: Exception) {

            }
            return default
        }
    }
}

enum class DatePlanChoice {
    DEPARTURE,
    ARRIVAL;

    fun getVisibleName(context: Context): String {
        when (this) {
            DEPARTURE -> return context.getString(R.string.date_start)
            ARRIVAL -> return context.getString(R.string.date_arrival)
        }
    }
}
