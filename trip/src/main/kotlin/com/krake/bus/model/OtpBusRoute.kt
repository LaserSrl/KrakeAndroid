package com.krake.bus.model

import android.graphics.Color
import com.google.gson.annotations.SerializedName
import com.krake.core.model.ContentItem
import io.realm.RealmObject

class OtpBusRoute(val id: String,
                  val shortName: String,
                  val longName: String,
                  val agencyName: String,
                  @SerializedName("color") val routeColor: String,
                  @SerializedName("mode") val routeMode: String): BusRoute, ContentItem {

    override val stringIdentifier: String
        get() = id

    override val titlePartTitle: String
        get() = longName

    override val color: Int
        get() = Color.parseColor(if (routeColor.startsWith("#")) routeColor else "#$routeColor")

    override val mode: RouteMode
        get() = Bus
}