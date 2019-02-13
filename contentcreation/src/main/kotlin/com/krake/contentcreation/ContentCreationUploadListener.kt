package com.krake.contentcreation

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.krake.core.OrchardApiEndListener
import com.krake.core.OrchardError
import com.krake.core.Signaler
import com.krake.core.network.RemoteRequest
import com.krake.core.network.RemoteResponse
import java.lang.ref.WeakReference

class ContentCreationUploadListener(context: Context, listener: ContentApiServiceListener) :
        LifecycleObserver,
        OrchardApiEndListener
{
    private val weakContext = WeakReference<ContentApiServiceListener>(listener)
    private val apiPath: String

    init
    {
        apiPath = context.getString(R.string.orchard_api_path_content_modify)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun connectListener()
    {
        val context = weakContext.get()
        if (context != null)
            Signaler.shared.registerApiEndListener(apiPath, this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun disconnectListener()
    {
        Signaler.shared.removeApiEndListener(apiPath, this)
    }

    override fun onApiInvoked(context: Context,
                              remoteRequest: RemoteRequest,
                              remoteResponse: RemoteResponse?,
                              e: OrchardError?,
                              endListenerParameters: Any?)
    {
        if (remoteRequest.method == RemoteRequest.Method.POST)
        {
            if (remoteResponse != null)
            {
                weakContext.get()?.onContentCreated()
            }
            else if (e != null)
            {
                weakContext.get()?.onContentCreationFailed(e)
            }
        }
    }

}

interface ContentApiServiceListener
{
    fun onContentCreated()

    fun onContentCreationFailed(orchardError: OrchardError)
}