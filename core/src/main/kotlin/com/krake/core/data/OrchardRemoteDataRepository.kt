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
                          callback: (RequestCache?, OrchardError?) -> Unit): CancelableRequest
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
                          callback: (RequestCache?, OrchardError?) -> Unit): CancelableRequest
    {

        request.setQuery(Constants.REQUEST_REAL_FORMAT, "true")
        request.setQuery(Constants.REQUEST_COMPLEX_BEHAVIOUR, "returnnulls")
        request.setQuery(Constants.REQUEST_LANGUAGE_KEY, dataMapper.configurations.getLanguageIdentifier())

        return client.enqueue(request) { remoteResponse, orchardError ->
            val result = remoteResponse?.jsonObject()
            if (result != null)
            {
                AsyncTask.Builder<String>()
                        .background {
                            dataMapper.parseContentFromResult(result,
                                                              orchardError,
                                                              requestedPrivacy,
                                                              request.queryParameters)
                        }
                        .completed {
                            Realm.getDefaultInstance().refresh()
                            callback(RequestCache.findCacheWith(it), null)
                        }
                        .error { callback(null, it as? OrchardError) }
                        .build()
                        .load()
            }
            else
            {
                callback(null, orchardError)
            }
        }
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

