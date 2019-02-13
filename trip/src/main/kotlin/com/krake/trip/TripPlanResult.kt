package com.krake.trip

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLngBounds
import com.krake.core.address.PlaceResult
import com.krake.core.model.MediaPart
import java.util.*

/**
 * Created by joel on 22/03/17.
 */

class TripPlanResult(val request: TripPlanRequest, val routes: Array<Route>)

enum class Maneuver {
    DEPART,
    RIGHT,
    HARD_RIGHT,
    CIRCLE_COUNTERCLOCKWISE,
    CIRCLE_CLOCKWISE,
    CONTINUE,
    LEFT,
    HARD_LEFT,
    SLIGHTLY_RIGHT,
    SLIGHTLY_LEFT,
    UTURN_RIGHT,
    UTURN_LEFT;

    companion object {
        fun valueOfOrDefault(value: String): Maneuver {
            try {
                return Maneuver.valueOf(value)
            } catch (ignored: Exception) {

            }
            return CONTINUE
        }
    }

    fun drawableResource(): Int {
        val directionDrawable: Int
        when (this) {
            Maneuver.CIRCLE_COUNTERCLOCKWISE, Maneuver.CIRCLE_CLOCKWISE -> directionDrawable = R.drawable.rotonda

            Maneuver.RIGHT, Maneuver.HARD_RIGHT -> directionDrawable = R.drawable.destra_90

            Maneuver.LEFT, Maneuver.HARD_LEFT -> directionDrawable = R.drawable.sinistra_90

            Maneuver.CONTINUE -> directionDrawable = R.drawable.continua_dritto

            Maneuver.DEPART -> directionDrawable = R.drawable.continua_dritto

            Maneuver.SLIGHTLY_LEFT -> directionDrawable = R.drawable.destra_45

            Maneuver.SLIGHTLY_RIGHT -> directionDrawable = R.drawable.sinistra_45

            Maneuver.UTURN_LEFT -> directionDrawable = R.drawable.inversione_u_sx

            Maneuver.UTURN_RIGHT -> directionDrawable = R.drawable.inversione_u_dx
        }

        return directionDrawable
    }
}

/**
 * Contiene i dati di un itinerario.
 * le route contengono dati leggermente diversi in base al modo di viaggio.
 * Se il travel mode è TRANSIT: steps contiene più elementi di tipo #StepGroup o #TransitStep
 * Negli altri casi steps contiene un solo elemento di tipo StepGroup che raggruppa tutti i dati del viaggio
 * @property steps Lista di steps dell'itinerario. Gli steps possono contenere solo classi #StepGroup o #TransitStep
 *
 */
class Route(val startTime: Date, val endTime: Date,
            val duration: Long, val distance: Int,
            val walkDistance: Int,
            val steps: Array<ComplexStep>,
            val bounds: LatLngBounds,
            val warnings: Array<String>? = null,
            val copyright: String? = null)

interface ComplexStep {
    val travelMode: TravelMode
    val from: PlaceResult
    val to: PlaceResult
    val polyline: String
    val duration: Long
    @ColorInt
    fun stepColor(context: Context): Int
}

class SingleStep(val travelMode: TravelMode,
                 val from: PlaceResult,
                 val to: PlaceResult? = null,
                 val duration: Long = 0,
                 val distance: Int,
                 val polyline: String? = null,
                 val instruction: Spanned,
                 val maneuver: Maneuver)

class StepGroup(override val travelMode: TravelMode,
                override val from: PlaceResult,
                override val to: PlaceResult,
                override val polyline: String,
                override val duration: Long,
                val distance: Int,
                val instruction: SpannableString,
                val steps: Array<SingleStep>) : ComplexStep {

    override fun stepColor(context: Context): Int {

        val color: Int

        when (travelMode) {
            TravelMode.BICYCLE -> color = R.color.itinerary_step_color_bike

            TravelMode.CAR -> color = R.color.itinerary_step_color_car

            TravelMode.WALK -> color = R.color.itinerary_step_color_walk

            TravelMode.TRANSIT -> color = R.color.itinerary_step_color_transit
        }

        return ContextCompat.getColor(context, color)
    }


}

class TransitStep(val vehicle: VehicleType,
                  val headsign: String?,
                  override val from: PlaceResult,
                  override val to: PlaceResult,
                  override val polyline: String,
                  val startTime: Date,
                  val endTime: Date,
                  override val duration: Long,
                  val line: TransitLine) : ComplexStep {

    override fun stepColor(context: Context): Int {
        if (line.color != null) {
            return line.color
        } else {
            val lineColor: Int
            when (vehicle) {

                VehicleType.BUS, VehicleType.TRANSIT -> lineColor = R.color.itinerary_step_color_transit

                VehicleType.TRAM -> lineColor = R.color.itinerary_step_color_tram

                VehicleType.SUBWAY -> lineColor = R.color.itinerary_step_color_subway

                else -> lineColor = R.color.itinerary_step_color_transit
            }

            return ContextCompat.getColor(context, lineColor)
        }
    }

    override val travelMode: TravelMode = TravelMode.TRANSIT
}

class TransitLine(val name: String,
                  val shortName: String,
                  val lineIcon: MediaPart? = null,
                  @ColorInt val color: Int?)