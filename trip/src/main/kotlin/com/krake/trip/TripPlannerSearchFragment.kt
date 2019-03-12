package com.krake.trip

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.material.tabs.TabLayout
import com.krake.core.address.*
import com.krake.core.api.GoogleApiClientFactory
import com.krake.core.component.annotation.BundleResolvable
import com.krake.core.extension.isInSameDay
import com.krake.core.extension.putModules
import com.krake.core.location.GeocoderTask
import com.krake.core.location.LocationRequirementsHelper
import com.krake.core.permission.PermissionListener
import com.krake.core.permission.PermissionManager
import com.krake.core.view.TabLayoutHelper
import com.krake.trip.component.module.TripPlannerModule
import java.text.DateFormat
import java.util.*

/**
 * Fragment per gestire la UI di ricerca del trip planner
 * Created by joel on 18/04/17.
 */
class TripPlannerSearchFragment : Fragment(),
    AdapterView.OnItemClickListener,
    AddressFilterableArrayAdapter.FilterChangedListener,
    SearchBoundsProvider,
    TabLayout.OnTabSelectedListener,
    LocationListener,
    View.OnClickListener,
    TripDatePickerFragment.OnTripDateTimePickerListener,
    OtpBoundingBoxTask.Listener,
    PlaceIdTask.Listener,
    PlacesResultTask.Listener,
    GeocoderTask.Listener,
    PermissionListener,
    GoogleApiClientFactory.ConnectionListener, Observer<Boolean>
{
    companion object {
        private const val ARRIVAL_REQUEST_ID = 546
        private const val DEPARTURE_REQUEST_ID = 547
    }

    @BundleResolvable
    var tripModule: TripPlannerModule = TripPlannerModule()

    lateinit var locationClientFactory: GoogleApiClientFactory
        private set

    private lateinit var locationRequirementsHelper: LocationRequirementsHelper

    private lateinit var boundingBoxTask: OtpBoundingBoxTask
    private lateinit var placesResultTask: PlacesResultTask
    private lateinit var placeIdTask: PlaceIdTask
    private lateinit var geocoderTask: GeocoderTask

    private val fromAdapter: AddressFilterableArrayAdapter  by lazy {
        AddressFilterableArrayAdapter(activity ?: throw IllegalArgumentException("The activity mustn't be null."),
            R.id.departureEditText,
            this
        )
    }
    private val toAdapter: AddressFilterableArrayAdapter by lazy {
        AddressFilterableArrayAdapter(activity ?: throw IllegalArgumentException("The activity mustn't be null."),
            R.id.arrivalEditText,
            this
        )
    }

    var internalSearchBounds: LatLngBounds? = null
    override val searchBounds: LatLngBounds?
        get() = (activity?.application as? SearchBoundsProvider)?.searchBounds ?: internalSearchBounds

    private lateinit var planDateTextView: TextView
    private lateinit var arrivalEditText: AutoCompleteTextView
    private lateinit var progess: ProgressBar

    private lateinit var tripPlanTask: TripPlanViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activity = activity!!
        tripPlanTask = ViewModelProviders.of(activity).get(OpenTripPlanTask::class.java)
        tripPlanTask.loading.observe(this, this)
        locationClientFactory = GoogleApiClientFactory(activity, this, LocationServices.API)
        locationRequirementsHelper = LocationRequirementsHelper(this, this)
        locationRequirementsHelper.permissionManager.rationalMsg(getString(R.string.error_location_permission_required_to_select_your_position))
        locationRequirementsHelper.create()

        boundingBoxTask = OtpBoundingBoxTask(activity, this)
        val client = PlacesClient.createClient(activity)
        placesResultTask = PlacesResultTask(activity, client, this)
        placeIdTask = PlaceIdTask(client, this)
        geocoderTask = GeocoderTask(activity, this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_trip_planner_search, container, false)

        val tab = insertTravelTabType(view as ViewGroup, inflater)

        progess = view.findViewById(R.id.searchProgress)
        progess.visibility = View.INVISIBLE

        arrivalEditText = view.findViewById(R.id.arrivalEditText)

        planDateTextView = view.findViewById(R.id.dateSelectedTextView)
        planDateTextView.setOnClickListener(this)

        val departureEditText = view.findViewById<AutoCompleteTextView>(R.id.departureEditText)
        departureEditText.onItemClickListener = this
        departureEditText.setAdapter(fromAdapter)

        arrivalEditText.onItemClickListener = this
        arrivalEditText.setAdapter(toAdapter)

        if (savedInstanceState != null)
            tripModule.readContent(activity!!, savedInstanceState)
        else if (arguments != null)
            tripModule.readContent(activity!!, activity!!.intent.extras)

        restoreUI(departureEditText, arrivalEditText, tab)

        if (searchBounds == null) {
            boundingBoxTask.load()
        } else {
            onBoundsLoaded(searchBounds!!)
        }

        if (tripModule.request.to?.geocodeAddress == true) {
            val location = tripModule.request.to!!.location!!
            geocoderTask.load(location)
        }

        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putModules(activity!!, tripModule)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        planDateTextView.setOnClickListener(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        boundingBoxTask.release()
        placesResultTask.release()
        placeIdTask.release()
        geocoderTask.release()
        removeLocationUpdates(locationClientFactory)
        locationClientFactory.disconnect()
    }

    override fun onBoundsLoaded(bounds: LatLngBounds) {
        internalSearchBounds = bounds
        locationClientFactory.connect()
    }

    override fun onApiClientConnected() {
        locationRequirementsHelper.request()
    }

    override fun onPermissionsHandled(acceptedPermissions: Array<out String>) {
        if (PermissionManager.containLocationPermissions(acceptedPermissions)) {
            insertUserLocationInOptions()
        }
    }

    private fun insertTravelTabType(view: ViewGroup, inflater: LayoutInflater): TabLayout {

        val activity = activity ?: throw IllegalArgumentException("The activity mustn't be null.")
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val helper = TabLayoutHelper.InflaterBuilder(inflater.context)
            .layout(R.layout.partial_tripmode_tab)
            .tabShowImage(true)
            .tabShowTitle(false)
            .layoutParams(params)
            .build()

        helper.addTab(null,
            ResourcesCompat.getDrawable(resources, R.drawable.ic_directions_car_36dp, null),
            TravelMode.CAR
        )

        helper.addTab(null,
            ResourcesCompat.getDrawable(resources, R.drawable.ic_directions_bus_36dp, null),
            TravelMode.TRANSIT
        )

        helper.addTab(null,
            ResourcesCompat.getDrawable(resources, R.drawable.ic_directions_walk_36dp, null),
            TravelMode.WALK
        )

        helper.addTab(null,
            ResourcesCompat.getDrawable(resources, R.drawable.ic_directions_bike_36dp, null),
            TravelMode.BICYCLE
        )

        val tab = helper.layout()

        tab.addOnTabSelectedListener(this)
        view.findViewById<ViewGroup>(R.id.travelModeTabContainer).addView(tab)
        return tab
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val adapter = (parent.adapter as AddressFilterableArrayAdapter)

        val place = adapter.getItem(position)

        if (adapter.viewIdentifier == R.id.arrivalEditText) {
            tripModule.request.to = place
        } else {
            tripModule.request.from = place
        }

        insertUserLocationInOptions()

        if (place?.placeId != null && place.location == null) {
            val requestId = if (adapter.viewIdentifier == R.id.arrivalEditText) ARRIVAL_REQUEST_ID else DEPARTURE_REQUEST_ID
            placeIdTask.load(place.placeId.toString(), requestId)
        } else {
            startTripPlanningIfRequestIsValid()
        }
    }

    override fun filterChanged(constraint: CharSequence?, adapter: AddressFilterableArrayAdapter) {
        placesResultTask.cancel()

        if (adapter.viewIdentifier == R.id.arrivalEditText) {
            if (tripModule.request.to?.name?.equals(constraint) != true)
                tripModule.request.to = null
        } else {
            if (tripModule.request.from?.name?.equals(constraint) != true)
                tripModule.request.from = null
        }

        val context = context ?: throw IllegalArgumentException("The context mustn't be null.")
        val minimumConstraintLength = context.resources.getInteger(R.integer.geocoder_autocompletion_threshold)
        if (constraint?.length ?: 0 >= minimumConstraintLength
            && !constraint.toString().equals(getString(R.string.user_location), ignoreCase = true)
        ) {

            val requestId = if (adapter.viewIdentifier == R.id.arrivalEditText) ARRIVAL_REQUEST_ID else DEPARTURE_REQUEST_ID
            placesResultTask.load(constraint.toString(), TypeFilter.ADDRESS, this, requestId)
        }
    }

    private fun startTripPlanningIfRequestIsValid() {
        if (tripModule.request.isValid()) {
            val activity = activity ?: throw IllegalArgumentException("The activity mustn't be null.")
            if (activity.currentFocus != null && activity.currentFocus.windowToken != null) {
                //hide the keyboard
                val inputManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(activity.currentFocus.windowToken, 0)
            }

            tripPlanTask.planTrip(activity, tripModule.request)
        }
    }

    private fun insertUserLocationInOptions() {
        if (locationClientFactory.apiClient.isConnected) {
            if (locationRequirementsHelper.permissionManager.areGranted()) {

                if (tripModule.request.to?.isUserLocation == true) {
                    tripModule.request.to?.let { it.location = getLastLocationAvailable(true) }
                }

                if (tripModule.request.from?.isUserLocation == true) {
                    tripModule.request.from?.let { it.location = getLastLocationAvailable(true) }
                }
            } else {
                locationRequirementsHelper.request()
            }
        }
    }

    private fun restoreUI(departureEditText: AutoCompleteTextView, arrivalEditText: AutoCompleteTextView, travelModeTab: TabLayout) {
        val request = tripModule.request

        request.from?.let { departureEditText.setText(it.name) }
        request.to?.let { arrivalEditText.setText(it.name) }

        for (index in 0 until travelModeTab.tabCount) {
            val tab = travelModeTab.getTabAt(index)!!

            val mode = tab.tag as? TravelMode

            if (mode?.equals(request.travelMode) == true) {
                tab.select()
                break
            }
        }

        updateDateTextView()
    }

    override fun onClick(v: View) {
        if (v == planDateTextView) {
            val fragment = TripDatePickerFragment()
            val activity = activity ?: throw IllegalArgumentException("The activity mustn't be null.")
            fragment.arguments = tripModule.writeContent(activity)
            fragment.show(fragmentManager, "Date")
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocationAvailable(automaticallyRequestNewOne: Boolean): Location? {
        if (locationRequirementsHelper.permissionManager.areGranted()) {
            val lastLocation = LocationServices.FusedLocationApi.getLastLocation(locationClientFactory.apiClient)

            if (lastLocation != null && Math.abs((lastLocation.time - Date().time) / 1000) <= resources.getInteger(R.integer.location_fix_validity_seconds)) {
                return lastLocation
            } else if (automaticallyRequestNewOne) {
                val request = LocationRequest()
                LocationServices.FusedLocationApi.requestLocationUpdates(locationClientFactory.apiClient, request, this)
            }
        }
        return null
    }

    override fun onPlacesResultLoaded(requestId: Int, places: MutableList<PlaceResult>) {
        val adapter = if (requestId == ARRIVAL_REQUEST_ID) toAdapter else fromAdapter
        adapter.setResultList(places)
        adapter.notifyDataSetChanged()
    }

    override fun onPlaceLoaded(requestId: Int, place: Place) {
        place.latLng?.let { latLng ->
            val location = Location("")

            location.latitude = latLng.latitude
            location.longitude = latLng.longitude

            var placeResult: PlaceResult? = if (requestId == ARRIVAL_REQUEST_ID)
                tripModule.request.to
            else
                tripModule.request.from

            if (placeResult == null) {
                placeResult = PlaceResult(place.name.toString())
                if (requestId == ARRIVAL_REQUEST_ID)
                    tripModule.request.to = placeResult
                else
                    tripModule.request.from = placeResult

            }
            placeResult.location = location
            startTripPlanningIfRequestIsValid()
        }
    }

    override fun onAddressLoaded(address: Address) {
        val name = address.format()
        tripModule.request.to?.let {
            it.setGeocodedName(name)
            arrivalEditText.setText(name)
        }
    }

    override fun onLocationChanged(location: Location?) {
        removeLocationUpdates(locationClientFactory)
        insertUserLocationInOptions()
        startTripPlanningIfRequestIsValid()
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        if (tab != null) {
            val mode = tab.tag as TravelMode
            tripModule.request.travelMode = mode

            if (mode == TravelMode.TRANSIT)
                planDateTextView.visibility = View.VISIBLE
            else
                planDateTextView.visibility = View.GONE

            startTripPlanningIfRequestIsValid()
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {}

    override fun onTabUnselected(tab: TabLayout.Tab?) {}

    private fun updateDateTextView() {
        val dateString: String = if (tripModule.request.dateSelectedForPlan.isInSameDay(Date()))
            DateFormat.getTimeInstance(DateFormat.SHORT).format(tripModule.request.dateSelectedForPlan)
        else
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(tripModule.request.dateSelectedForPlan)

        val activity = activity ?: throw IllegalArgumentException("The activity mustn't be null.")
        planDateTextView.text = String.format("%s: %s", tripModule.request.datePlanChoice.getVisibleName(activity), dateString)
    }

    override fun onDatePicked(date: Date, dateChoice: DatePlanChoice) {
        tripModule.request.datePlanChoice = dateChoice
        tripModule.request.dateSelectedForPlan = date
        updateDateTextView()
        startTripPlanningIfRequestIsValid()
    }

    private fun removeLocationUpdates(factory: GoogleApiClientFactory) {
        val apiClient = factory.apiClient
        if (apiClient.isConnected) {
            LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this)
        }
    }

    override fun onChanged(t: Boolean?)
    {
        if (t == true)
        {
            progess.visibility = View.VISIBLE
        }
        else
        {
            progess.visibility = View.INVISIBLE
        }
    }
}