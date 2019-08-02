package com.krake.core.address

import android.content.Context

interface RemoteAddressSeacher {
    fun searchAddress(
        context: Context,
        addressName: String,
        searchBoundsProvider: SearchBoundsProvider? = null
    ): List<PlaceResult>?

    fun release()
}