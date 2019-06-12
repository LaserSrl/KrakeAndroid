package com.krake.trip

import android.annotation.TargetApi
import android.content.Context
import android.location.Location
import android.os.Build
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import com.google.android.gms.maps.model.LatLngBounds
import com.google.gson.JsonObject
import com.krake.colorValue
import com.krake.core.OrchardError
import com.krake.core.address.PlaceResult
import com.krake.core.network.CancelableRequest
import com.krake.core.network.RemoteClient
import com.krake.core.network.RemoteRequest
import com.krake.core.network.RemoteResponse
import java.text.SimpleDateFormat
import java.util.*

/**
 * Implementazione di #TripPlanTask che utilizza il planner di OTP per pianificare i dati
 *
 * Created by joel on 23/03/17.
 */

class OpenTripPlanTask : TripPlanViewModel()
{
    lateinit var uselessStreetNames: Array<String>
    private var cancelableRequest: CancelableRequest? = null

    override fun planTrip(context: Context, request: TripPlanRequest)
    {
        cancelableRequest?.cancel()

        uselessStreetNames = context.getResources().getStringArray(R.array.useless_street_names)
        val client = RemoteClient.client(RemoteClient.Mode.DEFAULT)

        val clientRequest = RemoteRequest(context.getString(R.string.open_trip_planner_base_url) + context.getString(R.string.plan_trip_command))
                .setQuery("fromPlace", request.from!!.location!!.openTripEncode())
                .setQuery("toPlace", request.to!!.location!!.openTripEncode())
                .setQuery("mode", request.travelMode.openTripEncode())
                .setQuery("time", SimpleDateFormat("hh:mma", Locale.US).format(request.dateSelectedForPlan))
                .setQuery("date", SimpleDateFormat("MM-dd-yyyy", Locale.US).format(request.dateSelectedForPlan))
                .setQuery("arriveBy", (request.datePlanChoice == DatePlanChoice.ARRIVAL).toString())

        if (request.maxWalkDistance == 0) {
            clientRequest.setQuery("maxWalkDistance", request.maxWalkDistance.toString())
        }

        mutableLoading.value = true
        cancelableRequest = client.enqueue(clientRequest, object : (RemoteResponse?, OrchardError?) -> Unit
        {
            override fun invoke(p1: RemoteResponse?, p2: OrchardError?)
            {
                mutableLoading.value = false
                cancelableRequest = null
                parse(context, request, p1, p2)
            }
        })

    }

    private fun parse(context: Context,
                      tripPlanRequest: TripPlanRequest,
                      p1: RemoteResponse?,
                      p2: OrchardError?)
    {
        val finalError: OrchardError?
        val tripResult: TripPlanResult?

        if (p1 != null)
        {
            val resultObject = p1.jsonObject()

            if (resultObject != null && resultObject.get("error") == null)
            {

                val planObject = resultObject.getAsJsonObject("plan")
                val itineraries = planObject.getAsJsonArray("itineraries")

                val routes = mutableListOf<Route>()
                for (i in 0 until itineraries.size())
                {
                    routes.add(parseItinerary(context, itineraries.get(i).asJsonObject))
                }

                tripResult = TripPlanResult(tripPlanRequest, routes.toTypedArray())
                finalError = null

            }
            else
            {

                var errorMessage = resultObject!!.getAsJsonObject("error").get("msg").asString

                if (errorMessage.contains("data boundary"))
                    errorMessage = context.getString(R.string.error_trip_plan_outside_dataBounds)
                else if (errorMessage.contains("No transit times available"))
                    errorMessage = context.getString(R.string.error_trip_plan_no_times)

                finalError = OrchardError(errorMessage)
                tripResult = null
            }
        }
        else
        {
            finalError = p2
            tripResult = null
        }

        mutableTripResult.value = TripResult(tripResult, finalError)
    }

    private fun distance(steps: List<ComplexStep>): Int {
        var distance = 0

        for (step in steps) {
            if (step is StepGroup)
                distance += step.distance
            else {
                distance = 0
                break
            }
        }

        return distance
    }

    private fun bounds(steps: List<ComplexStep>): LatLngBounds {
        val bounds = LatLngBounds.Builder()

        for (step in steps) {
            if (step.from.latLng != null)
                bounds.include(step.from.latLng)

            if (step.to.latLng != null)
                bounds.include(step.to.latLng)
        }

        return bounds.build()
    }

    private fun parseItinerary(context: Context, itineraryInfos: JsonObject): Route
    {

        val legs = itineraryInfos.getAsJsonArray("legs")

        val steps = mutableListOf<ComplexStep>()

        for (i in 0 until legs.size()) {
            steps.add(parseLeg(context, legs.get(i).asJsonObject))
        }

        return Route(startTime = Date(itineraryInfos.get("startTime").asLong),
                endTime = Date(itineraryInfos.get("endTime").asLong),
                duration = itineraryInfos.get("duration").asLong,
                distance = distance(steps),
                walkDistance = itineraryInfos.get("walkDistance").asInt,
                bounds = bounds(steps),
                steps = steps.toTypedArray()
        )
    }

    private fun parseLeg(context: Context, infos: JsonObject): ComplexStep
    {

        val from = parseLocation(infos.getAsJsonObject("from"))
        val to = parseLocation(infos.getAsJsonObject("to"))
        val polyline = infos.getAsJsonObject("legGeometry").get("points").asString

        if (infos.get("transitLeg").asBoolean) {
            val line = TransitLine(name = (infos.get("routeLongName").asString),
                    shortName = (infos.get("routeShortName").asString),
                    color = infos.get("routeColor")?.asString.let { ("#" + it).colorValue() })

            return TransitStep(vehicle = VehicleType.valueOfOrDefault(infos.get("mode").asString, VehicleType.OTHER),
                    headsign = infos.get("headsign")?.asString,
                    from = from,
                    to = to,
                    polyline = polyline,
                    startTime = Date(infos.get("startTime").asLong),
                    endTime = Date(infos.get("endTime").asLong),
                    line = line,
                    duration = (infos.get("endTime").asLong - infos.get("startTime").asLong) / 1000L)
        } else {
            val jSteps = infos.getAsJsonArray("steps")
            val steps = mutableListOf<SingleStep>()

            val travelMode = TravelMode.valueOfOrDefault(infos.get("mode").asString, TravelMode.TRANSIT)
            for (i in 0 until jSteps.size()) {
                steps.add(parseStep(context, jSteps.get(i).asJsonObject, travelMode))
            }

            return StepGroup(from = from,
                    to = to,
                    polyline = polyline,
                    duration = infos.get("duration").asLong,
                    distance = infos.get("distance").asInt,
                    travelMode = travelMode,
                    steps = steps.toTypedArray(),
                    instruction = SpannableString(travelMode.instructionDescription(context) + " " + to.name))
        }
    }

    private fun parseLocation(jsonObject: JsonObject): PlaceResult {

        val result = PlaceResult(jsonObject.get("name")?.asString ?: "")

        val location = Location("")
        location.latitude = jsonObject.get("lat").asDouble
        location.longitude = jsonObject.get("lon").asDouble
        result.location = location

        return result
    }

    private fun parseStep(context: Context, infos: JsonObject, travelMode: TravelMode): SingleStep
    {

        val maneuver = Maneuver.valueOfOrDefault(infos.get("relativeDirection").asString)

        return SingleStep(travelMode = travelMode,
                          from = parseLocation(infos),
                          distance = infos.get("distance").asInt,
                          maneuver = maneuver,
                          instruction = instruction(context, maneuver, infos.get("streetName").asString, infos.get("exit")?.asString))
    }

    private fun instruction(context: Context, direction: Maneuver, streetName: String, exit: String?): Spanned
    {
        var directionDescription = ""

        when (direction) {
            Maneuver.CIRCLE_COUNTERCLOCKWISE, Maneuver.CIRCLE_CLOCKWISE -> {
                directionDescription = String.format(context.getString(R.string.direction_description_circle), exit)
            }

            Maneuver.RIGHT, Maneuver.HARD_RIGHT -> {
                directionDescription = context.getString(R.string.direction_description_right)
            }

            Maneuver.LEFT, Maneuver.HARD_LEFT -> {
                directionDescription = context.getString(R.string.direction_description_left)
            }

            Maneuver.CONTINUE -> {
                directionDescription = context.getString(R.string.direction_description_continue)
            }

            Maneuver.DEPART -> {
                directionDescription = context.getString(R.string.direction_description_depart)
            }

            Maneuver.SLIGHTLY_LEFT -> {
                directionDescription = context.getString(R.string.direction_description_slightly_left)
            }

            Maneuver.SLIGHTLY_RIGHT -> {
                directionDescription = context.getString(R.string.direction_description_slightly_right)
            }

            Maneuver.UTURN_LEFT -> {
                directionDescription = context.getString(R.string.direction_description_uturn)
            }

            Maneuver.UTURN_RIGHT -> {
                directionDescription = context.getString(R.string.direction_description_uturn)
            }
        }

        if (!uselessStreetNames.contains(streetName.toLowerCase())) {
            @TargetApi(24)
            if (BuildConfig.VERSION_CODE >= Build.VERSION_CODES.N) {
                return SpannableString.valueOf(Html.fromHtml(String.format("<b>%s</b> %s <b>%s</b>", directionDescription, context.getString(R.string.step_direction_word), streetName),
                        Html.FROM_HTML_MODE_COMPACT))
            }
            @Suppress("DEPRECATION")
            return SpannableString.valueOf(Html.fromHtml(String.format("<b>%s</b> %s <b>%s</b>", directionDescription, context.getString(R.string.step_direction_word), streetName)))
        } else {
            return SpannableString(directionDescription)
        }
    }

}

fun Location.openTripEncode(): String {
    return String.format(Locale.ENGLISH, "%f,%f", this.latitude, this.longitude)
}

fun TravelMode.openTripEncode(): String {
    when (this) {
        TravelMode.TRANSIT -> return "TRANSIT,WALK"
        else -> return this.name
    }
}
