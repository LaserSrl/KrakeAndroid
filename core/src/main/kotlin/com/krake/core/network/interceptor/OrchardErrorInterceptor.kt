package com.krake.core.network.interceptor

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.krake.core.OrchardError
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody

class OrchardErrorInterceptor : Interceptor
{
    override fun intercept(chain: Interceptor.Chain?): Response
    {
        val response = chain!!.proceed(chain.request())

        if (response.header("content-type")?.contains("application/json", true) ?: false)
        {
            val body = response.body()
            if (body != null)
            {
                val contentType = body.contentType()
                val content = body.string()
                body.close()

                var json: JsonObject? = null
                try {
                    json = JsonParser().parse(content) as? JsonObject
                } catch (e: JsonSyntaxException) {
                    json = null
                }

                if (json != null)
                {
                    val error = OrchardError.createErrorFromResult(json)

                    if (error != null)
                    {
                        throw error
                    }
                }

                val wrappedBody = ResponseBody.create(contentType, content)
                return response.newBuilder().body(wrappedBody).build()
            }
        }

        if (!response.isSuccessful)
        {
            throw OrchardError(String.format("%d %s", response.code(), response.message()));
        }

        return response
    }
}