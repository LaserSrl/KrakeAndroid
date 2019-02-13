package com.krake.core.network

import android.content.Context
import androidx.annotation.StringRes
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.krake.core.R

class RemoteRequest(baseUrl: String)
{
    enum class Method(val value: String)
    {
        GET("GET"),
        POST("POST"),
        PUT("PUT"),
        DELETE("DELETE"),
        PATCH("PATCH")
    }

    enum class StandardPath(@StringRes val path: Int)
    {
        DATA_WEBSERVICE(R.string.orchard_data_service_path),
        SEND_SIGNAL(R.string.orchard_signal_command)
    }

    var baseUrl: String
        private set

    var path: String? = null
        private set

    var queryParameters: MutableMap<String, String> = HashMap()
        private set

    var headers: MutableMap<String, String> = HashMap()
        private set

    /**
     * body della richiesta attualemtene supportati
     * Map<String.String> per invio di POST form
     * JsonObject e JsonArray per invio di dati json
     * File per caricare un file su orchard
     */
    var body: Any? = null
        private set

    var method: Method = Method.GET
        private set

    constructor(context: Context) : this(context.getString(R.string.orchard_base_service_url))

    init
    {
        this.baseUrl = baseUrl
    }

    fun setBaseUrl(url: String): RemoteRequest = apply { this.baseUrl = url }

    fun setPath(path: String?): RemoteRequest = apply { this.path = path }

    fun setPath(context: Context, path: StandardPath): RemoteRequest = apply { this.path = context.getString(path.path) }

    fun setMethod(method: Method): RemoteRequest = apply { this.method = method }

    fun setQuery(name: String, value: String): RemoteRequest = apply { queryParameters[name] = value }

    fun setHeader(name: String, value: String): RemoteRequest = apply { headers[name] = value }

    fun setBody(json: JsonObject): RemoteRequest = apply { this.body = json }

    fun setBody(json: JsonArray): RemoteRequest = apply { this.body = json }

    /**
     * Parametri devono essere di tipo String oppure File.
     * Se contiene uno o file file viene utilizzato un multipartdata
     */
    fun setBodyParameters(parameters: Map<String, Any>): RemoteRequest = apply { this.body = parameters }
}