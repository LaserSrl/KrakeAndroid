package com.krake.core.network

import android.content.Context
import com.krake.core.OrchardError

interface RemoteClient
{
    companion object
    {
        internal val clients = HashMap<Mode, RemoteClient>()

        fun client(mode: Mode): RemoteClient
        {
            return clients[mode]!!
        }

        fun client(loginRequired: Boolean): RemoteClient
        {
            val mode: RemoteClient.Mode

            if (loginRequired)
                mode = RemoteClient.Mode.LOGGED
            else
                mode = RemoteClient.Mode.DEFAULT

            return clients[mode]!!
        }
    }

    enum class Mode
    {
        DEFAULT,
        LOGGED
    }

    fun removeAllCookies()

    fun cookieValue(context: Context, name: String): String?

    @Throws(OrchardError::class)
    fun execute(remoteRequest: RemoteRequest): RemoteResponse

    fun enqueue(remoteRequest: RemoteRequest, callback: (RemoteResponse?, OrchardError?) -> Unit): CancelableRequest
}

interface CancelableRequest
{
    fun cancel()
}