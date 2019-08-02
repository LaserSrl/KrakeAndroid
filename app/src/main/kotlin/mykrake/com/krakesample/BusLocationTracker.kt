package mykrake.com.krakesample

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.krake.bus.model.BusPassage
import com.krake.bus.provider.BusMovementMarkerConfiguration
import com.krake.bus.provider.BusMovementProvider
import com.krake.core.image.BitmapGenerator
import com.krake.core.map.image.DefaultMarkerBitmapGenerator
import com.krake.core.model.identifierOrStringIdentifier
import com.krake.core.network.RemoteClient
import com.krake.core.network.RemoteRequest
import com.krake.core.util.ColorUtil

class BusLocationTracker : BusMovementProvider {
    override fun getRefreshPeriod(context: Context): Int = 10

    override fun provideBusMarkerConfiguration(
        context: Context,
        busPassage: BusPassage
    ): BusMovementMarkerConfiguration {
        return object : BusMovementMarkerConfiguration {
            override fun markerPosition(): LatLng = LatLng(0.0, 0.0)

            override fun markerTitle(): String? = null

            override fun markerSubtitle(): String? = null

            override fun provideMarkerKey(context: Context): String =
                busPassage.tripId ?: busPassage.identifierOrStringIdentifier

            override fun markerBitmapGenerator(context: Context): BitmapGenerator {
                return DefaultMarkerBitmapGenerator(
                    context,
                    color = busPassage.pattern?.busRoute?.color ?: ColorUtil.primaryColor(context),
                    innerDrawable = context.getDrawable(R.drawable.bus),
                    label = ""
                )
            }
        }
    }

    override suspend fun provideCurrentBusPosition(context: Context, busPassage: BusPassage): LatLng {
        val tripId = busPassage.tripId!!.split(":")[1]

        val agencyId: String = "TUA"

        val request = RemoteRequest("http://93.57.87.214/tua_ws/")
            .setMethod(RemoteRequest.Method.GET)
            .setPath("gtfs.asmx/GetLocationByTrip")
            .setQuery("Agency", agencyId)
            .setQuery("TripId", tripId)

        val response = RemoteClient.client(false)
            .execute(request)
            .jsonObject() ?: throw RuntimeException("error during retrieve current bus location")

        val vehicleId = response.getAsJsonPrimitive("VehicleID").asString
        if (vehicleId.isNullOrEmpty())
            throw RuntimeException("vehicleId is empty, cannot retrieve location.")

        val lat = response.getAsJsonPrimitive("Lat").asDouble
        val lng = response.getAsJsonPrimitive("Lng").asDouble

        return LatLng(lat, lng)
    }
}