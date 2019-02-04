package com.krake.core.data

import com.krake.core.OrchardError
import io.realm.RealmModel

/**
 * Created by antoniolig on 10/03/2017.
 */
@Deprecated("Removed")
interface DataListener {
    fun onDefaultDataLoaded(list: List<RealmModel>, cacheValid: Boolean)

    fun onDefaultDataLoadFailed(error: OrchardError, cachePresent: Boolean)

    fun needToAccessDataInMultiThread(): Boolean = false
}