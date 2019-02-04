package com.krake.core.data

import com.krake.core.OrchardError
import com.krake.core.component.module.LoginComponentModule
import com.krake.core.component.module.OrchardComponentModule
import com.krake.core.model.RequestCache
import com.krake.core.network.CancelableRequest
import com.krake.core.network.RemoteClient
import com.krake.core.network.RemoteRequest

interface RemoteDataRepository
{
    companion object
    {
        lateinit var shared: RemoteDataRepository
    }

    val dataMapper: DataMapper

    fun loadData(loginModule: LoginComponentModule,
                 orchardModule: OrchardComponentModule,
                 page: Int,
                 callback: (RequestCache?, OrchardError?) -> Unit): CancelableRequest


    fun loadData(request: RemoteRequest,
                 client: RemoteClient,
                 requestedPrivacy: Boolean,
                 callback: (RequestCache?, OrchardError?) -> Unit): CancelableRequest
}
