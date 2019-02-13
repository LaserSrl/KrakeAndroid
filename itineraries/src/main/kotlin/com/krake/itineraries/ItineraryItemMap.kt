package com.krake.itineraries

import android.app.ActivityOptions
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import androidx.collection.ArrayMap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.krake.core.app.ContentItemDetailActivity
import com.krake.core.component.base.ComponentManager
import com.krake.core.component.module.DetailComponentModule
import com.krake.core.component.module.OrchardComponentModule
import com.krake.core.component.module.ThemableComponentModule
import com.krake.core.map.MarkerCreator
import com.krake.core.model.ContentItemWithLocation
import com.krake.core.model.MapPart
import com.krake.core.model.RecordWithAutoroute
import com.krake.core.widget.ContentItemMapView
import com.krake.core.widget.getActivity
import com.krake.itineraries.model.Itinerary
import com.krake.itineraries.model.ItineraryItem
import java.io.File

/**
 * Created by joel on 12/10/16.
 */
class ItineraryItemMapView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet, defStyleAttr: Int = 0
) : ContentItemMapView(context, attrs, defStyleAttr), GoogleMap.OnInfoWindowClickListener {

    private val shouldShowDetailsOnClick: Boolean
    private val markerToPOIs = ArrayMap<Marker, ContentItemWithLocation>()

    init {
        // Read the styled attributes.
        val typedArr = context.obtainStyledAttributes(attrs, R.styleable.ItineraryItemMapView)
        shouldShowDetailsOnClick = typedArr.getBoolean(R.styleable.ItineraryItemMapView_showDetailsOnClick, true)
        typedArr.recycle()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mapManager.getMapAsync { it.setOnInfoWindowClickListener(this) }
    }

    override fun showDataOnMap(map: GoogleMap, contentItem: ContentItemWithLocation, kmlFile: File?, cacheValid: Boolean) {
        super.showDataOnMap(map, contentItem, kmlFile, cacheValid)

        if (contentItem is Itinerary) {
            markerToPOIs.clear()

            if (atLeastOnePoiHasAValidMap(contentItem)) {
                mLocationItemMarker?.remove()

                val builder = LatLngBounds.builder()

                for ((index, item) in contentItem.locationPoints.withIndex()) {
                    val mapPart: MapPart? = item.mapPart

                    if (mapPart != null && mapPart.isMapValid) {
                        val markerCreator = MarkerCreator.shared
                        (item as? ItineraryItem)?.count = index + 1
                        val options = markerCreator.createMarker(context, item)
                        val marker = map.addMarker(options)

                        builder.include(mapPart.latLng)
                        markerToPOIs.put(marker, item)
                    }
                }

                if (kmlFile == null) {
                    mapZoomSupport.updateCamera(
                            CameraUpdateFactory.newLatLngBounds(builder.build(), resources.getDimensionPixelSize(R.dimen.activity_horizontal_margin)), map)
                }
            }
        }
    }

    override fun shouldShowMapWithContentItem(contentItem: ContentItemWithLocation): Boolean =
            super.shouldShowMapWithContentItem(contentItem) || (contentItem is Itinerary && atLeastOnePoiHasAValidMap(contentItem))

    private fun atLeastOnePoiHasAValidMap(contentItem: Itinerary): Boolean {
        return contentItem.locationPoints
                .map { it.mapPart }
                .any { it?.isMapValid ?: false }
    }

    override fun onInfoWindowClick(it: Marker) {
        if (!shouldShowDetailsOnClick)
            return

        val activity = getActivity()
        val poi = markerToPOIs[it]
        if (poi != null && activity != null) {
            val orchardModule = OrchardComponentModule()
                    .dataClass(poi.javaClass)

            if (poi is RecordWithAutoroute) {
                orchardModule.displayPath(poi.autoroutePartDisplayAlias)
            } else {
                orchardModule.record(poi)
            }

            val detailIntent = ComponentManager.createIntent()
                    .from(context)
                    .to(ContentItemDetailActivity::class.java)
                    .with(DetailComponentModule(context),
                            orchardModule,
                            ThemableComponentModule()
                                    .upIntent(activity.intent))
                    .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                activity.startActivity(detailIntent, ActivityOptions.makeSceneTransitionAnimation(activity).toBundle())
            else
                activity.startActivity(detailIntent)
        }
    }
}