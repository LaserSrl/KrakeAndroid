package com.krake.core.address

import android.content.Context
import com.krake.core.R

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
            Places.initialize(context.applicationContext, context.getString(R.string.google_api_key))
        }

        return Places.createClient(context)
    }
}