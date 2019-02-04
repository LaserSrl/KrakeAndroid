package com.krake.core.network

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.io.File

@Throws
internal fun RemoteRequest.saveAsJson(): JsonObject
{
    val jsonObject = JsonObject()

    jsonObject.addProperty("url", this.baseUrl)
    jsonObject.addProperty("method", this.method.value)

    this.path?.let {
        jsonObject.addProperty("path", it)
    }

    JsonObject()
            .let {
                headers.forEach { e -> it.addProperty(e.key, e.value) }
                jsonObject.add("headers", it)
            }

    JsonObject()
            .let {
                queryParameters.forEach { e -> it.addProperty(e.key, e.value) }
                jsonObject.add("queryParameters", it)
            }

    body?.let {
        if (it is File)
            throw IllegalArgumentException()

        if (it is JsonElement)
            jsonObject.add("body", it)
    }

    return jsonObject
}

internal fun JsonObject.readRemoteRequest(): RemoteRequest
{
    val remoteRequest = RemoteRequest(this.get("url").asString)

    remoteRequest.setMethod(RemoteRequest.Method.valueOf(get("method").asString))

    get("path").asString?.let { remoteRequest.setPath(it) }

    getAsJsonObject("headers")
            .entrySet()
            .forEach {
                remoteRequest.setHeader(it.key, it.value.asString)
            }

    getAsJsonObject("queryParameters")
            .entrySet()
            .forEach {
                remoteRequest.setQuery(it.key, it.value.asString)
            }

    get("body")?.let {
        if (it is JsonObject)
        {
            remoteRequest.setBody(it.asJsonObject)
        }
        else if (it is JsonArray)
        {
            remoteRequest.setBody(it.asJsonArray)
        }
        else
        {

        }
    }

    return remoteRequest
}