package com.krake.core.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.krake.core.R
import com.krake.core.model.ContentItemWithLocation

/**
 * Lamba per avviare la navigazione nelle mappe.
 * La gestione di defulat avvia il navigatore di Google Map.
 * Created by joel on 30/09/16.
 */

object NavigationIntentManager {
    var startNavigationIntent: (Context, ContentItemWithLocation) -> Unit = { context, item ->
        if (item.mapPart?.isMapValid ?: false == true) {
            val map = item.mapPart!!
            val uri = Uri.parse(String.format(context.getString(R.string.open_google_map_navigation_uri_format), map.latitude.toString(), map.longitude.toString()))

            val navIntent = Intent(Intent.ACTION_VIEW, uri)
            if (context.getResources().getBoolean(R.bool.open_google_map_navigation_force_google_map_only))
                navIntent.setPackage("com.google.android.apps.maps")

            context.startActivity(navIntent)
        }
    }
}
