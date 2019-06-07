package com.krake.core.data

import android.content.Context
import com.krake.core.Constants
import com.krake.core.OrchardError
import com.krake.core.R
import com.krake.core.component.module.LoginComponentModule
import com.krake.core.component.module.OrchardComponentModule
import com.krake.core.model.PolicyText
import com.krake.core.model.RequestCache
import com.krake.core.network.CancelableRequest
import com.krake.core.network.RemoteClient
import com.krake.core.network.RemoteRequest
import com.krake.core.thread.AsyncTask
import io.realm.Realm

class OrchardRemoteDataRepository(context: Context,
                                  override val dataMapper: DataMapper) :
        RemoteDataRepository
{
    protected val baseServiceUrl: String
    protected val dataLoadingPath: String

    init
    {
        baseServiceUrl = context.getString(R.string.orchard_base_service_url)
        dataLoadingPath = context.getString(R.string.orchard_data_service_path)
    }

    override fun loadData(loginModule: LoginComponentModule,
                          orchardModule: OrchardComponentModule,
                          page: Int,
                          callback: (Int, RequestCache?, OrchardError?) -> Unit
    ): CancelableRequest
    {

        val request = this.toRemoteRequest(orchardModule, page)

        val client = RemoteClient.client(if (loginModule.loginRequired)
                                             RemoteClient.Mode.LOGGED
                                         else
                                             RemoteClient.Mode.DEFAULT)

        return loadData(request,
                        client,
                        orchardModule.dataClass != null && PolicyText::class.java.isAssignableFrom(orchardModule.dataClass!!),
                        callback)
    }

    override fun loadData(request: RemoteRequest,
                          client: RemoteClient,
                          requestedPrivacy: Boolean,
                          callback: (Int, RequestCache?, OrchardError?) -> Unit
    ): CancelableRequest
    {

        request.setQuery(Constants.REQUEST_REAL_FORMAT, "true")
        request.setQuery(Constants.REQUEST_COMPLEX_BEHAVIOUR, "returnnulls")
        request.setQuery(Constants.REQUEST_LANGUAGE_KEY, dataMapper.configurations.getLanguageIdentifier())

        val cancellableDataRequest = CancelableDataRequest()


        cancellableDataRequest.networkRequest = client.enqueue(request) { remoteResponse, orchardError ->
            val result = remoteResponse?.jsonObject()
            if (result != null)
            {
                val task =
                    AsyncTask.Builder<String>()
                        .background {
                            dataMapper.parseContentFromResult(
                                result,
                                orchardError,
                                requestedPrivacy,
                                request.queryParameters
                            )
                        }
                        .completed {
                            cancellableDataRequest.clean()
                            cancellableDataRequest.asyncParsing = null
                            Realm.getDefaultInstance().refresh()
                            callback(cancellableDataRequest.code, RequestCache.findCacheWith(it), null)
                        }
                        .error {
                            cancellableDataRequest.clean()
                            callback(cancellableDataRequest.code, null, it as? OrchardError)
                        }
                        .build()
                        .apply {
                            load()
                        }

                cancellableDataRequest.asyncParsing = task
            }
            else
            {
                callback(cancellableDataRequest.code, null, orchardError)
                cancellableDataRequest.clean()
            }
        }

        return cancellableDataRequest
    }

    internal fun toRemoteRequest(orchardModule: OrchardComponentModule,
                                 page: Int): RemoteRequest
    {
        val request: RemoteRequest
        if (orchardModule.webServiceUrl == null)
        {
            request = RemoteRequest(this.baseServiceUrl)
                    .setPath(this.dataLoadingPath)
        }
        else
        {
            request = RemoteRequest(orchardModule.webServiceUrl!!)
        }

        if (orchardModule.pageSize > 0)
            request.setQuery(Constants.REQUEST_PAGE_SIZE_KEY, orchardModule.pageSize.toString())
        else
            request.setQuery(Constants.REQUEST_PAGE_SIZE_KEY, "999999")

        request.setQuery(Constants.REQUEST_PAGE_KEY, page.toString())

        orchardModule.dataPartFilters?.let { request.setQuery(Constants.REQUEST_ITEM_PART_FILTER, it) }

        orchardModule.extraParameters.forEach { (key, value) -> request.setQuery(key, value) }

        orchardModule.displayPath?.let { request.setQuery(Constants.REQUEST_DISPLAY_PATH_KEY, it) }

        for (key in orchardModule.headers.keySet())
        {
            val value = orchardModule.headers.getString(key)
            if (value != null)
                request.setHeader(key, value)
        }

        return request
    }
}

class CancelableDataRequest : CancelableRequest {
    override var code: Int = 0

    internal var networkRequest: CancelableRequest? = null
        set(value) {
            field = value
            if (value != null) {
                code = value.code
            }
        }

    internal var asyncParsing: AsyncTask<String>? = null
    override fun cancel() {
        networkRequest?.cancel()
        asyncParsing?.cancel()
        clean()
    }

    internal fun clean() {
        networkRequest = null
        asyncParsing = null
    }
}

