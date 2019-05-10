package com.krake.core.network

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.MimeTypeMap
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.krake.core.OrchardError
import com.krake.core.R
import com.krake.core.app.AnalyticsApplication
import com.krake.core.login.LoginManager
import com.krake.core.network.interceptor.AuthenticatedUserHeaderInterceptor
import com.krake.core.network.interceptor.CookiePropagateInterceptor
import com.krake.core.network.interceptor.OrchardErrorInterceptor
import com.krake.core.network.interceptor.SecurityHeaderInterceptor
import okhttp3.*
import okhttp3.internal.http.HttpMethod
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference

internal class OkHttpRemoteClient(context: Context, private val mode: RemoteClient.Mode) : RemoteClient
{
    val client: OkHttpClient
    val mainHandler = Handler(Looper.getMainLooper())
    private val enableLogs: Boolean
    private val baseOrchardUrl: String

    private val analyticsWeakRef: WeakReference<AnalyticsApplication>
    val analytics: AnalyticsApplication?
        get() {
            return analyticsWeakRef.get()
        }

    init
    {
        analyticsWeakRef = WeakReference(context.applicationContext as AnalyticsApplication)
        enableLogs = context.resources.getBoolean(R.bool.enable_network_logs)
        baseOrchardUrl = context.getString(R.string.orchard_base_service_url)
        client = OkHttpClient.Builder()
                .cookieJar(
                        PersistentCookieJar(
                                SetCookieCache(),
                                SharedPrefsCookiePersistor(context.getSharedPreferences("OkHttpStore-" + mode.name, Context.MODE_PRIVATE)))
                )
                .addInterceptor(SecurityHeaderInterceptor(context))
                .addNetworkInterceptor(AuthenticatedUserHeaderInterceptor(context))
                .addInterceptor(OrchardErrorInterceptor())
                .apply {
                    if (mode == RemoteClient.Mode.LOGGED)
                        this.addNetworkInterceptor(CookiePropagateInterceptor())
                }
                .build()
    }

    override fun removeAllCookies()
    {
        (client.cookieJar() as? ClearableCookieJar)?.clear()
    }

    override fun execute(remoteRequest: RemoteRequest): RemoteResponse
    {
        val requestCookies = allCookies()
        try
        {
            val response = client.newCall(remoteRequest.asOkHttpRequest()).execute()
            val okResponse = OkHttpResponse(response)
            logRespondeAndCookies(remoteRequest, requestCookies, okResponse)

            return okResponse

        }
        catch (e: IOException)
        {
            handleError(e as? OrchardError)

            throw (e as? OrchardError ?: OrchardError(e)).apply {
                logErrorAndCookies(remoteRequest, requestCookies, this)
            }
        }
    }

    override fun enqueue(remoteRequest: RemoteRequest, callback: (RemoteResponse?, OrchardError?) -> Unit): CancelableRequest
    {
        val requestCookies = allCookies()
        val call = client.newCall(remoteRequest.asOkHttpRequest())
                .apply {
                    this.enqueue(object : Callback
                                 {
                                     override fun onResponse(call: Call?, response: Response?)
                                     {
                                         if (response != null)
                                         {
                                             mainHandler.post {
                                                 callback(OkHttpResponse(response)
                                                     .apply {
                                                         logRespondeAndCookies(remoteRequest, requestCookies, this)
                                                     }
                                                     , null)
                                             }
                                         }
                                     }

                                     override fun onFailure(call: Call?, e: IOException?)
                                     {
                                         if (e?.message != "Canceled")
                                         {
                                             mainHandler.post {
                                                 handleError(e as? OrchardError)

                                                 callback(null, (e as? OrchardError
                                                     ?: OrchardError(e)).apply {
                                                     logErrorAndCookies(remoteRequest, requestCookies, this)
                                                 })
                                             }
                                         }
                                     }
                                 })
                }

        return OkHttpCancelable(call)
    }

    internal fun handleError(e: OrchardError?)
    {
        if (e != null)
        {
            if (e.reactionCode == OrchardError.REACTION_LOGIN &&
                    this.mode == RemoteClient.Mode.LOGGED)
            {
                LoginManager.shared.logout()
            }
        }
    }

    private fun allCookies(): List<Cookie> {
        return client
            .cookieJar()
            .loadForRequest(HttpUrl.parse(baseOrchardUrl)!!)
    }

    override fun cookieValue(context: Context, name: String): String?
    {
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        val cookies = client
                .cookieJar()
                .loadForRequest(HttpUrl.parse(context.getString(R.string.orchard_base_service_url)))

        return cookies.find { cookie -> cookie.name() == name }?.value()
    }

    private fun logErrorAndCookies(
        remoteRequest: RemoteRequest,
        requestCookies: List<Cookie>,
        error: OrchardError
    ) {
        if (enableLogs) {
            val logBundle = Bundle()

            logBundle.putString("Request", remoteRequest.toString())

            logBundle.putString("RequestCookies", requestCookies.toJson().toString())

            logBundle.putString("UpdatedCookies", allCookies().toJson().toString())

            logBundle.putString("Error", error.toString())

            analytics?.logEvent("NetworkError", logBundle)
        }
    }

    private fun logRespondeAndCookies(
        remoteRequest: RemoteRequest,
        requestCookies: List<Cookie>,
        remoteResponse: RemoteResponse
    ) {
        if (enableLogs) {
            val logBundle = Bundle()

            logBundle.putString("Request", remoteRequest.toString())

            logBundle.putString("RequestCookies", requestCookies.toJson().toString())

            logBundle.putString("UpdatedCookies", allCookies().toJson().toString())

            // logBundle.putString("Response", remoteResponse.string())

            analytics?.logEvent("NetworkResponse", logBundle)
        }
    }
}

private fun RemoteRequest.asOkHttpRequest(): Request
{
    val httpUrlBuilder = HttpUrl.parse(this.baseUrl)!!.newBuilder()

    path?.let { httpUrlBuilder.addPathSegments(it) }

    queryParameters.forEach { (key, value) -> httpUrlBuilder.addQueryParameter(key, value) }

    val requestBuilder = Request.Builder()
            .url(httpUrlBuilder.build())

    if (!HttpMethod.requiresRequestBody(method.value) || this.body != null)
    {
        requestBuilder.method(method.value, this.body?.asRequestBody())
    }
    else
    {
        requestBuilder.method(method.value, RequestBody.create(null, ByteArray(0)))
    }
    headers.forEach { (key, value) -> requestBuilder.addHeader(key, value) }

    return requestBuilder.build()
}

private fun Any.asRequestBody(): RequestBody
{
    if (this is JsonElement)
    {
        return RequestBody.create(MediaType.parse("application/json; charset=utf-8"), Gson().toJson(this))
    }
    else if (this is Map<*, *>)
    {
        val containsFile = !this.filterValues { it is File }.isEmpty()
        if (!containsFile)
        {
            val body = FormBody.Builder()

            for (item in this)
            {
                body.add(item.key.toString(), item.value.toString())
            }

            return body.build()
        }
        else
        {
            val multiBuilder = MultipartBody.Builder()
            multiBuilder.setType(MediaType.parse("multipart/form-data")!!)

            for (item in this)
            {
                val key = item.key
                val value = item.value

                if (value is File)
                {
                    multiBuilder
                            .addFormDataPart(key.toString(),
                                             value.name,
                                             RequestBody.create(
                                                     MediaType.parse(MimeTypeMap.getSingleton().getMimeTypeFromExtension(value.extension)),
                                                     value))
                }
                else
                {
                    multiBuilder.addFormDataPart(key.toString(), value.toString())
                }
            }

            return multiBuilder.build()
        }
    }

    throw IllegalArgumentException("Tipo di body non supportato")
}

internal class OkHttpCancelable(private val call: Call) : CancelableRequest
{
    override fun cancel()
    {
        call.cancel()
    }
}