package com.krake.trip

/**
 * Created by joel on 25/11/16.
 */
/*
class TripPlaceStep(tripPlannerPlace: JsonObject) : PlaceResult(tripPlannerPlace.get("name").asString) {
    val isBusStop: Boolean
    val arrival: Date?
    val departure: Date?

    init {
        var location = Location("")
        location.latitude = tripPlannerPlace.get("lat").asDouble
        location.longitude = tripPlannerPlace.get("lon").asDouble

        this.location = location

        isBusStop = tripPlannerPlace.get("stopId") != null

        if (tripPlannerPlace.get("arrival") != null)
            arrival = Date(tripPlannerPlace.get("arrival").asLong)
        else
            arrival = null

        if (tripPlannerPlace.get("departure") != null)
            departure = Date(tripPlannerPlace.get("departure").asLong)
        else
            departure = null
    }
}*/