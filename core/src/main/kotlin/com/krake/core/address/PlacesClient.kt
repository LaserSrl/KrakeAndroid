package com.krake.core.address

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient

/**
 * [PlacesClient] provider that check if [Places] api are initialized.
 * application should call this when need a new [PlacesClient], in order to avoid initialization issues.
 */
object PlacesClient
{
    fun createClient(context: Context): PlacesClient
    {
        if (!Places.isInitialized())
        {
            val ai: ApplicationInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            val value = ai.metaData["com.google.android.geo.API_KEY"]
//            val value = ai.metaData["key_value_google"]
            print("API_KEY google: ${value.toString()}")

            var keyValue = ""
            if(value != null) keyValue = value.toString()

            Places.initialize(context.applicationContext, keyValue)
//            Places.initialize(context.applicationContext, context.getString(R.string.google_api_key))
        }

        return Places.createClient(context)
    }
}