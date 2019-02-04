package com.krake.core.data

import com.krake.core.component.module.LoginComponentModule
import com.krake.core.component.module.OrchardComponentModule

interface DataConnectionBase
{
    val orchardModule: OrchardComponentModule
    val loginModule: LoginComponentModule
    var page: Int
    val isLoadingData: Boolean

    fun loadDataFromRemote()
}