package com.krake.trip

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.krake.core.address.PlaceResult
import com.krake.core.component.module.ThemableComponentModule
import com.krake.core.model.ContentItemWithLocation
import com.krake.trip.component.module.TripPlannerModule

/**
 * Created by joel on 14/10/16.
 */
const val ACTION_PLAN_TRIP = "com.laser.trip.plan_trip"

var startTripPlannerIntent: (Context, ContentItemWithLocation) -> Unit = { context, item ->
    if (item.mapPart?.isMapValid == true) {

        val request = TripPlanRequest()
        request.from = PlaceResult.userLocationPlace(context)
        request.to = PlaceResult(item)

        val intent = Intent(ACTION_PLAN_TRIP)
        intent.setPackage(context.getString(R.string.app_package))

        intent.putExtras(
                TripPlannerModule()
                        .request(request)
                        .writeContent(context))

        if (context is Activity) {
            val themableModule = ThemableComponentModule()
            themableModule.upIntent(context.intent)
            intent.putExtras(themableModule.writeContent(context))
        }

        context.startActivity(intent)
    }
}