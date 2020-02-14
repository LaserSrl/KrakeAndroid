package com.krake.core.network.interceptor

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.krake.core.OrchardError
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class OrchardErrorInterceptor : Interceptor
{
    override fun intercept(chain: Interceptor.Chain): Response
    {
        val response = chain.proceed(chain.request())

        if (response.header("content-type")?.contains("application/json", true) == true)
        {
            val body = response.body
            if (body != null)
            {
                val contentType = body.contentType()
                val content = body.string()
                body.close()

                val json = try {
                    JsonParser.parseString(content) as? JsonObject
                } catch (e: JsonSyntaxException) {
                    null
                }

                if (json != null)
                {
                    val error = OrchardError.createErrorFromResult(json)

                    if (error != null)
                    {
                        throw error
                    }
                }

                val wrappedBody = content.toResponseBody(contentType)
                return response.newBuilder().body(wrappedBody).build()
            }
        }

        if (!response.isSuccessful)
        {
            throw OrchardError(String.format("%d %s", response.code, response.message))
        }

        return response
    }
}