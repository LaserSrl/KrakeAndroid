package mykrake.com.krakesample

import android.annotation.SuppressLint
import android.location.Address
import android.location.Location
import android.os.Bundle
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.gson.Gson
import com.krake.core.api.GoogleApiClientFactory
import com.krake.core.app.LoginAndPrivacyActivity
import com.krake.core.app.OnContentItemSelectedListener
import com.krake.core.location.GeocoderTask
import com.krake.core.location.LocationRequirementsHelper
import com.krake.core.model.ContentItem
import com.krake.core.permission.PermissionListener
import java.util.*

/**
 * Created by antoniolig on 13/03/2017.
 */
class MainActivity : LoginAndPrivacyActivity(), OnContentItemSelectedListener, Observer,
    GoogleApiClientFactory.ConnectionListener, PermissionListener, GeocoderTask.Listener, LocationListener {
    override fun onLocationChanged(p0: Location?) {
        if (p0 != null) {
            task = GeocoderTask(this, this)
            task.load(p0)
        }
    }

    override fun onAddressLoaded(address: Address) {
        address.locale
    }

    lateinit var locationClientFactory: GoogleApiClientFactory
        private set

    private lateinit var locationRequirementsHelper: LocationRequirementsHelper

    private lateinit var task: GeocoderTask

    @SuppressLint("MissingPermission")
    override fun onPermissionsHandled(acceptedPermissions: Array<out String>) {

        val location = LocationServices.FusedLocationApi.getLastLocation(locationClientFactory.apiClient)

        if (location != null) {
            task = GeocoderTask(this, this)
            task.load(location)
        } else {

        }
    }

    @SuppressLint("MissingPermission")
    override fun onApiClientConnected() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
            locationClientFactory.apiClient,
            LocationRequest(),
            this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?, layout: Int) {
        super.onCreate(savedInstanceState, layout)

        //        getLayoutInflater().inflate(R.layout.activity_main, (ViewGroup) findViewById(R.id.activity_content_container));
        inflateMainView(R.layout.activity_main, true)

        val bounds = LatLngBounds(LatLng(10.0, 15.0), LatLng(20.0, 25.0))

        val s = Gson().toJson(bounds)
        s.toCharArray()

        locationClientFactory = GoogleApiClientFactory(this, this, LocationServices.API)
        locationRequirementsHelper = LocationRequirementsHelper(this, this)
        locationRequirementsHelper.permissionManager.rationalMsg(getString(com.krake.trip.R.string.error_location_permission_required_to_select_your_position))
        locationRequirementsHelper.create()

        locationRequirementsHelper.request()

        locationClientFactory.connect()
    }

    override fun onStart() {
        super.onStart()
        //((KrakeApp) getApplication()).near.addObserver(this);
    }

    override fun onStop() {
        super.onStop()

        //((KrakeApp) getApplication()).near.deleteObserver(this);
    }

    override fun changeContentVisibility(visible: Boolean) {

    }

    override fun onShowContentItemDetails(senderFragment: Any, contentItem: ContentItem) {

    }

    override fun onContentItemInEvidence(senderFragment: Any, contentItem: ContentItem) {

    }

    override fun update(observable: Observable, data: Any) {

    }
}