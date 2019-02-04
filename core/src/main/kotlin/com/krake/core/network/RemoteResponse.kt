package com.krake.core.network

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.InputStream

interface RemoteResponse
{
    fun header(name: String): String?

    fun string(): String?

    fun jsonObject(): JsonObject?

    fun jsonArray(): JsonArray?

    fun inputStream(): InputStream?
}