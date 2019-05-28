package com.krake.bus.app

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.collection.ArrayMap
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import com.krake.bus.component.module.BusComponentModule
import com.krake.bus.map.MapModifier
import com.krake.bus.provider.PinDropListener
import com.krake.bus.widget.BusPassageAdapter
import com.krake.bus.widget.BusPassageHolder
import com.krake.core.Constants
import com.krake.core.address.*
import com.krake.core.api.GoogleApiClientFactory
import com.krake.core.app.AnalyticsApplication
import com.krake.core.app.ContentItemListMapActivity
import com.krake.core.app.OrchardDataModelFragment
import com.krake.core.cache.CacheManager
import com.krake.core.cache.LocationCacheModifier
import com.krake.core.component.annotation.BundleResolvable
import com.krake.core.component.base.ComponentManager
import com.krake.core.component.module.ListMapComponentModule
import com.krake.core.component.module.OrchardComponentModule
import com.krake.core.component.module.ThemableComponentModule
import com.krake.core.location.GeocoderTask
import com.krake.core.location.LocationRequirementsHelper
import com.krake.core.location.settings.GpsSettingsListener
import com.krake.core.model.ContentItem
import com.krake.core.model.identifierOrStringIdentifier
import com.krake.core.permission.PermissionListener
import com.krake.core.permission.PermissionManager
import com.krake.trip.OtpBoundingBoxTask
import com.krake.trip.R
import java.util.*

/**
 * Created by antoniolig on 11/04/2017.
 */
open class BusSearchActivity : ContentItemListMapActivity(),
        PinDropListener,
        SearchBoundsProvider,
        LocationListener,
        GoogleApiClientFactory.ConnectionListener,
        PermissionListener,
        GpsSettingsListener,
        OtpBoundingBoxTask.Listener,
        PlacesResultTask.Listener,
        PlaceIdTask.Listener,
        GeocoderTask.Listener,
        BusSearchFormFragment.Listener {

    companion object {
        private val TAG = BusSearchActivity::class.java.simpleName
        private const val OUT_STATE_SAVED_LOCATION = "otsSavedLocation"
        private const val OUT_STATE_BUS_SEARCH_EXPANDED = "otsBusSearchExpanded"
        private const val BUS_SEARCH_ANIMATION_MILLIS = 250L

        val defaultListMapModule: (Context) -> ListMapComponentModule = { context: Context ->
            ListMapComponentModule(context)
                    .contentPriority(ListMapComponentModule.PRIORITY_MAP)
                    .listCellLayout(BusComponentModule.DEFAULT_LIST_CELL_BUS_STOP_LAYOUT)
                    .listRootLayout(BusComponentModule.DEFAULT_LIST_ROOT_LAYOUT)
                    .mapAvoidLocationPermissions()
                    .mapUseCluster(true)
                    .mapFragmentClass(BusComponentModule.DEFAULT_MAP_FRAGMENT)
        }
    }

    @BundleResolvable
    lateinit var busComponentModule: BusComponentModule

    private lateinit var boundingBoxTask: OtpBoundingBoxTask
    private lateinit var placesResultTask: PlacesResultTask
    private lateinit var placeIdTask: PlaceIdTask
    private lateinit var geocoderTask: GeocoderTask

    private lateinit var searchFormFragment: BusSearchFormFragment
    private lateinit var frameContainer: FrameLayout

    lateinit var locationClientFactory: GoogleApiClientFactory
        private set

    private lateinit var locationRequirementsHelper: LocationRequirementsHelper

    var selectedPlace: PlaceResult? = null
        private set

    var internalSearchBounds: LatLngBounds? = null
    override val searchBounds: LatLngBounds?
        get() = (application as? SearchBoundsProvider)?.searchBounds ?: internalSearchBounds

    private var compressAnimator: ValueAnimator? = null
    private var busSearchExpanded: Boolean = true

    private val updateListener: ValueAnimator.AnimatorUpdateListener by lazy {
        ValueAnimator.AnimatorUpdateListener { valueAnimator ->
            val layoutParams = frameContainer.layoutParams
            layoutParams.height = valueAnimator.animatedValue as Int
            frameContainer.layoutParams = layoutParams
        }
    }

    private lateinit var busStopsPath: String

    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?, layout: Int) {
        super.onCreate(savedInstanceState, layout)

        busStopsPath = getString(R.string.orchard_path_bus_stops)
        // Aggiunge il path al provider dei paths di around me
        (CacheManager.shared as? LocationCacheModifier)?.addLocationPath(busStopsPath)
        // Inflate del layout aggiuntivo della parte di ricerca.
        val appBarLayout: AppBarLayout = findViewById(R.id.app_bar_layout)
        frameContainer = FrameLayout(this)
        frameContainer.id = R.id.bus_search_form_fragment_container
        appBarLayout.addView(frameContainer, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        (savedInstanceState?.getString(OUT_STATE_SAVED_LOCATION))?.let {
            selectedPlace = Gson().fromJson(it, PlaceResult::class.java)
        }

        busSearchExpanded = (savedInstanceState?.getBoolean(OUT_STATE_BUS_SEARCH_EXPANDED)) ?: busSearchExpanded

        locationClientFactory = GoogleApiClientFactory(this, this, LocationServices.API)
        locationRequirementsHelper = LocationRequirementsHelper(this, this, this).apply {
            permissionManager.rationalMsg(getString(R.string.error_location_permission_required_to_select_your_position))
            create()
        }

        boundingBoxTask = OtpBoundingBoxTask(this, this)

        val placeClient = PlacesClient.createClient(this)
        placesResultTask = PlacesResultTask(this, placeClient, this)
        placeIdTask = PlaceIdTask(placeClient, this)
        geocoderTask = GeocoderTask(this, this)

        searchFormFragment = BusSearchFormFragment.newInstance(selectedPlace?.name)
        supportFragmentManager.beginTransaction()
                .replace(frameContainer.id, searchFormFragment)
                .commit()
    }

    override fun onStart() {
        super.onStart()
        (application as? AnalyticsApplication)?.logSelectContent("Trasporto Pubblico", null, null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selectedPlace?.let {
            outState.putString(OUT_STATE_SAVED_LOCATION, Gson().toJson(it))
        }
        outState.putBoolean(OUT_STATE_BUS_SEARCH_EXPANDED, busSearchExpanded)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bus_search, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val busSearchItem = menu.findItem(R.id.action_bus_search)
        busSearchItem.isVisible = compressAnimator != null
        busSearchItem.icon = getSearchItemIcon()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_bus_search) {
            compressAnimator?.let {
                if (!it.isRunning) {
                    if (busSearchExpanded) {
                        it.start()
                    } else {
                        it.reverse()
                    }
                    busSearchExpanded = !busSearchExpanded
                    item.icon = getSearchItemIcon()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseAnimatorListeners()
        boundingBoxTask.release()
        placesResultTask.release()
        placeIdTask.release()
        geocoderTask.release()
        removeLocationUpdates(locationClientFactory)
        locationClientFactory.disconnect()
    }

    override fun onSearchFormLayoutReady() {
        // Rimuove tutti i listener per evitare memory leak
        releaseAnimatorListeners()

        compressAnimator = ValueAnimator.ofInt(frameContainer.measuredHeight, 0).apply {
            addUpdateListener(updateListener)
            duration = BUS_SEARCH_ANIMATION_MILLIS
            interpolator = interpolator
        }
        invalidateOptionsMenu()

        // Se la ricerca era nascosta prima della rotazione, bisogna nasconderla nuovamente
        if (!busSearchExpanded) {
            compressAnimator!!.start()
        }

        if (searchBounds == null) {
            boundingBoxTask.load()
        } else {
            onBoundsLoaded(searchBounds!!)
        }
    }

    override fun onBoundsLoaded(bounds: LatLngBounds) {
        internalSearchBounds = bounds
        locationClientFactory.connect()
    }

    override fun onApiClientConnected() {
        if (selectedPlace?.isUserLocation != false) {
            searchLocation()
        } else {
            onPlaceChanged()
        }
    }

    override fun onPermissionsHandled(acceptedPermissions: Array<out String>)
    {
        mapFragment.onPermissionsHandled(acceptedPermissions)

        if (!PermissionManager.containLocationPermissions(acceptedPermissions))
            geocoderTask.load(busComponentModule.defaultLocation!!)
    }

    override fun onGpsSettingsAcquired() {
        mapFragment.onGpsSettingsAcquired()

        if (selectedPlace == null || selectedPlace?.isUserLocation == true) {
            searchLocation()
        }
    }

    override fun onGpsSettingsUnavailable() {
        mapFragment.onGpsSettingsUnavailable()
        geocoderTask.load(busComponentModule.defaultLocation!!)
    }

    override fun onPlacesResultLoaded(requestId: Int, places: MutableList<PlaceResult>) {
        searchFormFragment.setPlaceResultList(places)
    }

    override fun onPlaceLoaded(requestId: Int, place: Place) {
        place.latLng?.let { latLng ->
            val location = Location("")
            location.latitude = latLng.latitude
            location.longitude = latLng.longitude
            selectedPlace?.location = location
            Log.d(TAG, "notifying new location with latitude: ${location.latitude} and longitude: ${location.longitude}")
            onPlaceChanged()
        }
    }

    override fun onAddressLoaded(address: Address) {
        val addressBuilder = StringBuilder()
        for (i in 0 until address.maxAddressLineIndex) {
            val line = address.getAddressLine(i)
            if (i != 0) {
                addressBuilder.append(", ")
            }
            addressBuilder.append(line)
        }
        val fullAddress = addressBuilder.toString()
        selectedPlace = PlaceResult(fullAddress)
        val location = Location("")
        location.latitude = address.latitude
        location.longitude = address.longitude
        selectedPlace?.location = location
        onPlaceChanged()
    }

    fun searchAddress(constraint: CharSequence?) {
        placesResultTask.cancel()

        if (constraint != null && constraint.length >= resources.getInteger(R.integer.geocoder_autocompletion_threshold)) {
            placesResultTask.load(constraint.toString(), TypeFilter.ADDRESS, this)
        }
    }

    fun searchPlace(place: PlaceResult) {
        selectedPlace = place
        Log.d(TAG, "searching place, is user location: ${place.isUserLocation}")
        if (place.isUserLocation) {
            searchLocation()
            return
        }

        if (place.placeId != null) {
            placeIdTask.load(place.placeId!!)
        }
    }

    override fun onLocationChanged(location: Location) {
        removeLocationUpdates(locationClientFactory)
        notifyLocation(location)
    }

    @SuppressLint("MissingPermission")
    private fun searchLocation() {
        if (!locationClientFactory.apiClient.isConnected) {
            locationClientFactory.connect()
        } else if (locationRequirementsHelper.permissionManager.areGranted() && locationRequirementsHelper.gpsSettingsManager.isActive()) {
            val location = LocationServices.FusedLocationApi.getLastLocation(locationClientFactory.apiClient)
            val locationTimeIsValid = {
                Math.abs((location.time - Date().time) / 1000) <= resources.getInteger(R.integer.location_fix_validity_seconds)
            }
            if (location != null && locationTimeIsValid()) {
                notifyLocation(location)
            } else {
                val request = LocationRequest()
                request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                LocationServices.FusedLocationApi.requestLocationUpdates(locationClientFactory.apiClient, request, this)
            }
        } else {
            locationRequirementsHelper.request(true)
        }
    }

    private fun notifyLocation(location: Location) {
        selectedPlace = PlaceResult.userLocationPlace(this)
        val locationIsInBounds = {
            val userLatLng = LatLng(location.latitude, location.longitude)
            searchBounds?.contains(userLatLng) ?: false
        }
        if (locationIsInBounds()) {
            // location valid
            Log.d(TAG, "notifying current location with latitude: ${location.latitude} and longitude: ${location.longitude}")
            selectedPlace?.location = location
            onPlaceChanged()
        } else if (busComponentModule.defaultLocation != null) {
            geocoderTask.load(busComponentModule.defaultLocation!!)
        }
    }

    fun onPlaceChanged() {
        val placeName = selectedPlace?.name
        Log.d(TAG, "new place found with name: $placeName")
        searchFormFragment.setAddress(placeName)
        selectedPlace?.location?.let {
            refreshData(it)
            val latLng = selectedPlace!!.latLng!!
            val mapModifier = mapFragment as? MapModifier
            mapModifier?.drawCircle(latLng, searchFormFragment.radius.toDouble(), drawMarker = true)
        }
    }

    override fun onPinDropped(latLng: LatLng) {
        val placeLocation = selectedPlace?.location
        if (latLng.latitude == placeLocation?.latitude && latLng.longitude == placeLocation.longitude)
            return

        geocoderTask.load(latLng)
    }

    override fun onRadiusChange(finished: Boolean) {
        selectedPlace?.location?.let {
            if (!finished) {
                val latLng = selectedPlace!!.latLng!!
                val mapModifier = mapFragment as? MapModifier
                mapModifier?.drawCircle(latLng, searchFormFragment.radius.toDouble())
            } else {
                refreshData(it)
            }
        }
    }

    override fun onAddressTextChange(addressName: String) {
        searchAddress(addressName)
    }

    override fun onPlaceResultChosen(placeResult: PlaceResult) {
        searchPlace(placeResult)
    }

    override fun getDetailIntent(contentItem: ContentItem): Intent {
        return ComponentManager.createIntent()
                .from(this)
                .to(BusStopsListActivity::class.java)
                .with(ThemableComponentModule()
                        .title(contentItem.titlePartTitle)
                        .upIntent(intent),
                        OrchardComponentModule()
                                .dataClass(contentItem::class.java)
                                .recordStringIdentifier(contentItem.identifierOrStringIdentifier),
                        ListMapComponentModule(this)
                                .contentPriority(ListMapComponentModule.PRIORITY_MAP)
                                .listCellLayout(BusComponentModule.DEFAULT_LIST_CELL_LAYOUT)
                                .listRootLayout(R.layout.fragment_bus_list)
                                .listAdapterClass(BusPassageAdapter::class.java)
                                .listViewHolderClass(BusPassageHolder::class.java)
                                .listFragmentClass(BusStopsGridFragment::class.java)
                                .mapFragmentClass(BusStopsMapFragment::class.java),
                        busComponentModule)
                .build()
    }

    protected open fun getSearchItemIcon(): Drawable {
        @DrawableRes val iconRes: Int = if (busSearchExpanded) R.drawable.ic_keyboard_arrow_up_24dp else R.drawable.ic_search_24dp
        return ContextCompat.getDrawable(this, iconRes)!!
    }

    private fun refreshData(location: Location) {
        val connectionArgs = ArrayMap<String, String>().apply {
            put(Constants.REQUEST_RADIUS, searchFormFragment.radius.toString())
            put(Constants.REQUEST_LATITUDE, (location.latitude).toString())
            put(Constants.REQUEST_LONGITUDE, (location.longitude).toString())
        }

        val restartConnectionInFragment = { fragment: OrchardDataModelFragment ->
            fragment.orchardComponentModule.dataClass(busComponentModule.stopItemClass)
                    .displayPath(busStopsPath)
                    .extraParameters(connectionArgs)
                    .avoidPagination()

            fragment.dataConnectionModel.restartDataLoading()
        }

        restartConnectionInFragment(gridFragment)
        restartConnectionInFragment(mapFragment)
    }

    private fun removeLocationUpdates(factory: GoogleApiClientFactory) {
        val apiClient = factory.apiClient
        if (apiClient.isConnected) {
            LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this)
        }
    }

    private fun releaseAnimatorListeners() {
        compressAnimator?.apply {
            removeAllListeners()
            removeAllUpdateListeners()
        }
    }
}