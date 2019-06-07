package com.krake.core.network

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.Cookie
import java.util.*

fun Cookie.toJson(): JsonObject {
    val json = JsonObject()

    json.addProperty("name", name())
    json.addProperty("value", value())
    json.addProperty("expires", Date(expiresAt()).toString())
    json.addProperty("persistent", persistent())

    return json
}

fun List<Cookie>.toJson(): JsonArray {
    return map {
        it.toJson()
    }
        .fold(JsonArray(), { acc, jsonObject ->
            acc.add(jsonObject)
            acc
        })
}