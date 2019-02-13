package com.krake.contentcreation

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.Marker
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.krake.contentcreation.model.LocationSelectionItem
import com.krake.core.ClassUtils
import com.krake.core.StringUtils
import com.krake.core.address.*
import com.krake.core.api.GoogleApiClientFactory
import com.krake.core.location.LocationRequirementsHelper
import com.krake.core.map.MarkerCreator
import com.krake.core.map.manager.FragmentMapManager
import com.krake.core.map.manager.MapManager
import com.krake.core.model.MapPart
import com.krake.core.permission.PermissionListener
import com.krake.core.permission.PermissionManager
import com.krake.core.view.MapUtils
import com.krake.core.widget.InstantAutoCompleteTextView
import com.krake.core.widget.SnackbarUtils

/**
 * Type of [ContentCreationFragment] that permits to add or change a location.
 * The search bar is used only to add the point on the app, the address won't be sent to the WS.
 */
class LocationSelectionFragment : Fragment(),
        PermissionListener,
        GoogleApiClientFactory.ConnectionListener,
        PlacesResultTask.Listener,
        PlaceIdTask.Listener,
        GoogleMap.OnMarkerDragListener,
        ContentCreationFragment,
        AddressFilterableArrayAdapter.FilterChangedListener,
        AdapterView.OnItemClickListener,
        LocationListener {

    companion object {

        /**
         * Creates a new instance of [LocationSelectionFragment].
         *
         * @return newly created instance of [LocationSelectionFragment].
         */
        fun newInstance(): LocationSelectionFragment = LocationSelectionFragment()
    }

    private var contentCreationActivity: ContentCreationActivity? = null
    private lateinit var apiClientFactory: GoogleApiClientFactory
    private lateinit var locationRequirementsHelper: LocationRequirementsHelper
    private lateinit var placesResultTask: PlacesResultTask
    private lateinit var placeIdTask: PlaceIdTask
    private lateinit var mapManager: MapManager
    private lateinit var locationEditText: AutoCompleteTextView
    private val minimumThreshold: Int by lazy { resources.getInteger(R.integer.geocoder_autocompletion_threshold) }
    private val locationInfo by lazy { LocationInfo() }
    private var currentError: String?
        set(value) {
            locationInfo.error = value
        }
        get() = locationInfo.error

    private var selectedPoint: PlaceResult?
        set(value) {
            locationInfo.placeResult = value
        }
        get() = locationInfo.placeResult

    private var currentMarker: Marker? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contentCreationActivity = context as? ContentCreationActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activity = contentCreationActivity ?: throw NullPointerException("The activity mustn't be null.")
        apiClientFactory = GoogleApiClientFactory(contentCreationActivity!!, this, LocationServices.API)
        locationRequirementsHelper = LocationRequirementsHelper(this, this)
        locationRequirementsHelper.permissionManager.rationalMsg(getString(R.string.error_location_permission_required_to_select_your_position))
        locationRequirementsHelper.create()

        val placesClient = PlacesClient.createClient(activity)
        placesResultTask = PlacesResultTask(activity, placesClient, this)
        placeIdTask = PlaceIdTask(placesClient, this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_location_selection, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = contentCreationActivity ?: throw NullPointerException("The activity mustn't be null.")

        locationEditText = view.findViewById(R.id.locationEditText) as InstantAutoCompleteTextView
        locationEditText.setAdapter(AddressFilterableArrayAdapter(activity, 0, this))
        locationEditText.onItemClickListener = this

        mapManager = FragmentMapManager(this) {
            view.findViewById(R.id.location_map) as MapView
        }.apply {
            onCreate(savedInstanceState)
            getMapAsync {
                // Style the map.
                MapUtils.styleMap(it, activity)
                // Add the listener used to drag the marker.
                it.setOnMarkerDragListener(this@LocationSelectionFragment)
            }
        }

        val result = activity.getFragmentData(this) as? LocationInfo
        if (result != null) {
            selectedPoint = result.placeResult
            currentError = result.error?.also {
                // Show the error with a Snackbar.
                showErrorSnackbar(it)
            }
        } else {
            val mapInfo = activity.getFragmentCreationInfo(this) as ContentCreationTabInfo.MapInfo
            val originalObj = activity.originalObject
            val dataKey = mapInfo.dataKey
            if (originalObj != null && dataKey != null) {
                // Get the MapPart from DB.
                val mapPart = ClassUtils.getValueInDestination(StringUtils.methodName(null,
                        dataKey,
                        null,
                        StringUtils.MethodType.GETTER),
                        originalObj) as MapPart

                if (mapPart.isMapValid) {
                    selectedPoint = PlaceResult(mapPart.locationAddress ?: "")
                    selectedPoint?.location = mapPart.location
                    // Save the data obtained from DB to restore them on rotation.
                    persistData()
                }
            }
        }

        // Invalidate the map with the new location.
        bindLocation()
        selectedPoint?.name?.let {
            // Set the text in the edit text using the current point if any.
            locationEditText.setText(selectedPoint?.name)
        }
    }

    override fun onStart() {
        super.onStart()
        apiClientFactory.connect()
    }

    override fun onResume() {
        super.onResume()
        mapManager.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapManager.onPause()
    }

    override fun onStop() {
        super.onStop()
        apiClientFactory.disconnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapManager.onDestroy()
        apiClientFactory.destroy()
        placeIdTask.release()
        placesResultTask.release()
    }

    override fun onDetach() {
        contentCreationActivity = null
        super.onDetach()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapManager.onLowMemory()
    }

    override fun onApiClientConnected() {
        if (selectedPoint?.isUserLocation == true) {
            locationRequirementsHelper.request(true)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onPermissionsHandled(acceptedPermissions: Array<out String>)
    {
        if (PermissionManager.containLocationPermissions(acceptedPermissions))
        {
            mapManager.getMapAsync {
                // Enable the UI properties used to show the user's location.
                it.isMyLocationEnabled = true
                it.uiSettings.isMyLocationButtonEnabled = true
            }

            val apiClient = apiClientFactory.apiClient

            val lastLocation = LocationServices.FusedLocationApi.getLastLocation(apiClient)

            if (selectedPoint?.isUserLocation == true) {
                if (lastLocation != null) {
                    selectedPoint?.location = lastLocation
                    persistData()
                    bindLocation()
                } else {
                    startLocationUpdate()
                }
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        stopLocationUpdate()
        selectedPoint?.location = location
        persistData()
        bindLocation()
    }

    override fun onPlacesResultLoaded(requestId: Int, places: MutableList<PlaceResult>) {
        (locationEditText.adapter as AddressFilterableArrayAdapter).apply {
            // Change the list.
            setResultList(places)
            // Notify the adapter.
            notifyDataSetChanged()
        }
    }

    override fun onPlaceLoaded(requestId: Int, place: Place) {
        place.latLng?.let { latLng ->
            val location = Location("").apply {
                latitude = latLng.latitude
                longitude = latLng.longitude
            }

            selectedPoint?.location = location
            persistData()
            bindLocation()
        }
    }

    override fun onMarkerDragStart(marker: Marker) {
        /* Empty implementation. */
    }

    override fun onMarkerDrag(marker: Marker) {
        /* Empty implementation. */
    }

    override fun onMarkerDragEnd(marker: Marker) {
        if (selectedPoint == null)
            selectedPoint = PlaceResult("")

        selectedPoint?.location = Location("").apply {
            val position = marker.position
            latitude = position.latitude
            longitude = position.longitude
        }

        persistData()
    }

    override fun filterChanged(constraint: CharSequence?, adapter: AddressFilterableArrayAdapter) {
        if (constraint?.length ?: 0 >= minimumThreshold && constraint != selectedPoint?.name) {
            // Search the places if there are at least a specific number of letters (minimumThreshold)
            // and if the current point is not the same of the one that the user is searching.
            placesResultTask.load(constraint.toString(), TypeFilter.ADDRESS)
        }
    }

    override fun onItemClick(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        val point = parent.adapter.getItem(position) as PlaceResult
        selectedPoint = point
        persistData()
        bindLocation()

        if (point.isUserLocation) {
            // Force the connection to the GoogleApiClient to restart the location workflow.
            apiClientFactory.connect()
        }

        if (point.placeId != null && point.location == null) {
            placeIdTask.load(point.placeId!!)
        }
    }

    override fun validateDataAndSaveError(activity: ContentCreationActivity, creationInfo: ContentCreationTabInfo.ContentCreationInfo, savedInfos: Any?): Boolean {
        val selectedLatLng = selectedPoint?.latLng
        if (selectedLatLng != null)
            return true

        if (selectedPoint?.isUserLocation == true) {
            // Show the error.
            currentError = activity.getString(R.string.error_location_no_yet_found)
            // Save the error to restore it after rotation.
            persistData()
            showErrorSnackbar(currentError!!)
            return false
        }

        val mapInfo = creationInfo as ContentCreationTabInfo.MapInfo
        val requiredPosition = mapInfo.isRequired
        if (requiredPosition) {
            // Show the error.
            currentError = activity.getString(R.string.error_no_location_indicated)
            // Save the error to restore it after rotation.
            persistData()
            showErrorSnackbar(currentError!!)
        }
        return !requiredPosition
    }

    override fun insertDataToUpload(activity: ContentCreationActivity, creationInfo: ContentCreationTabInfo.ContentCreationInfo, savedInfos: Any?, parameters: JsonObject): Boolean {
        selectedPoint?.let {
            val mapInfo = creationInfo as ContentCreationTabInfo.MapInfo
            val orchardKey = mapInfo.orchardKey
            val location = it.location ?: throw IllegalArgumentException("A location is necessary to upload the point.")
            parameters.addProperty(orchardKey + ".Latitude", location.latitude)
            parameters.addProperty(orchardKey + ".Longitude", location.longitude)
        }
        return true
    }

    override fun deserializeSavedInstanceState(activity: ContentCreationActivity, creationInfo: ContentCreationTabInfo.ContentCreationInfo, gson: Gson, serializedInfos: String?): Any =
            gson.fromJson(serializedInfos, LocationInfo::class.java)

    private fun bindLocation() {
        val point = selectedPoint
        val position = point?.latLng
        if (position != null) {
            // Reset the error when the position changes.
            currentError = null
            // Remove the previous marker if any.
            currentMarker?.remove()

            mapManager.getMapAsync {
                val activity = contentCreationActivity ?: throw NullPointerException("The activity mustn't be null.")
                val options = MarkerCreator.shared.createMarker(activity, LocationSelectionItem(position))
                options.draggable(true)
                currentMarker = it.addMarker(options)
                it.animateCamera(CameraUpdateFactory.newLatLngZoom(position, resources.getInteger(R.integer.default_zoom).toFloat()))
            }
        } else if (currentMarker != null) {
            // Remove the previous marker if any.
            currentMarker?.remove()
        }
    }

    private fun persistData() {
        contentCreationActivity?.updateFragmentData(this, locationInfo)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdate() {
        // Create the location request.
        val request = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 2000
        }

        val apiClient = apiClientFactory.apiClient
        if (apiClient.isConnected) {
            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, request, this)
        }
    }

    private fun stopLocationUpdate() {
        val apiClient = apiClientFactory.apiClient
        if (apiClient.isConnected) {
            LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this)
        }
    }

    private fun showErrorSnackbar(error: String) {
        val rootView = contentCreationActivity?.findViewById<CoordinatorLayout>(R.id.activity_layout_coordinator)

        if (rootView != null)
        {
            SnackbarUtils.createSnackbar(rootView, error, Snackbar.LENGTH_LONG).show()
        }
        else
        {
            throw IllegalArgumentException("The CoordinatorLayout with id activity_layout_coordinator is necessary.")
        }
    }

    class LocationInfo(var placeResult: PlaceResult? = null, var error: String? = null)
}