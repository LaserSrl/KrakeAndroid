package com.krake.core.network

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.Response
import java.io.InputStream

class OkHttpResponse(private val response: Response) : RemoteResponse
{


    private var bodyString: String? = null

    override fun header(name: String): String?
    {
        return response.header(name)
    }

    override fun string(): String
    {
        if (bodyString == null)
        {
            response.body?.byteStream()
            bodyString = response.body?.string()
            response.body?.close()

        }
        return bodyString!!
    }

    override fun jsonObject(): JsonObject?
    {
        return JsonParser.parseString(string())?.asJsonObject
    }

    override fun jsonArray(): JsonArray?
    {
        return JsonParser.parseString(string()).asJsonArray
    }

    override fun inputStream(): InputStream?
    {
        return response.body?.byteStream()
    }

}