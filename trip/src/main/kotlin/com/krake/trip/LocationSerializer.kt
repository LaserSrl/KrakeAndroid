package com.krake.trip

import android.location.Location
import com.google.gson.*
import java.lang.reflect.Type

/**
 * Simple serializer for [Gson] for the [Location]
 */
class LocationSerializer : JsonSerializer<Location?>, JsonDeserializer<Location?>
{
    override fun serialize(src: Location?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement?
    {
        src?.let {
            return JsonObject().apply {
                addProperty("provider", src.provider)
                addProperty("latitude", src.latitude)
                addProperty("longitude", src.longitude)
                addProperty("bearing", src.bearing)
                addProperty("altitude", src.altitude)
                addProperty("accuracy", src.accuracy)
                addProperty("elapsedRealtimeNanos", src.elapsedRealtimeNanos)
                addProperty("speed", src.speed)
                addProperty("time", src.time)
            }
        }
        return null
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Location?
    {
        (json as? JsonObject)?.let {
            return Location(Location(it.get("provider").asString).apply {
                latitude = it.get("latitude").asDouble
                longitude = it.get("longitude").asDouble
                bearing = it.get("bearing").asFloat
                altitude = it.get("altitude").asDouble
                accuracy = it.get("accuracy").asFloat
                elapsedRealtimeNanos = it.get("elapsedRealtimeNanos").asLong
                speed = it.get("speed").asFloat
                time = it.get("time").asLong
            })
        }
        return null

    }
}