package com.krake.core.app;

import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.krake.core.Constants;
import com.krake.core.R;
import com.krake.core.api.GoogleApiClientFactory;
import com.krake.core.cache.CacheManager;
import com.krake.core.cache.LocationCacheModifier;
import com.krake.core.data.DataConnectionModel;
import com.krake.core.location.LocationRequirementsHelper;
import com.krake.core.permission.PermissionListener;
import com.krake.core.permission.PermissionManager;
import com.krake.core.text.DistanceNumberFormat;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Eestensione della classe {@link ContentItemListMapActivity} che gestisce la posizione dell'utente.
 * Ogni volta che viene aggiornata la posizione dell'utente o il raggio i nuovi parametri sono passati ai
 * fragment per il caricamento dei dati.
 */
public class AroundMeActivity extends ContentItemListMapActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        SeekBar.OnSeekBarChangeListener,
        GoogleApiClientFactory.ConnectionListener,
        PermissionListener {

    private static final String STATE_RADIUS = "radius";
    private static final String STATE_POSITION = "position";

    private Location mCurrentLocation;
    private int minRadiusInMeters;
    private int maxRadiusInMeters;
    private int currentRadiusInMeters;
    private TextView mDistanceTextView;
    private LocationRequirementsHelper mLocationRequirementsHelper;
    private GoogleApiClientFactory mGoogleApiClientFactory;

    @Override
    public void onCreate(Bundle savedInstanceState, int layout) {
        super.onCreate(savedInstanceState, layout);

        CacheManager cacheManager = CacheManager.Companion.getShared();
        if (cacheManager instanceof LocationCacheModifier) {
            String path = orchardComponentModule.getDisplayPath();
            if (path != null) {
                ((LocationCacheModifier) cacheManager).addLocationPath(path);
            }
        }

        //noinspection unchecked
        mGoogleApiClientFactory = new GoogleApiClientFactory(this, this, LocationServices.API);
        mLocationRequirementsHelper = new LocationRequirementsHelper(this, this, null);
        mLocationRequirementsHelper.getPermissionManager().rationalMsg(getString(R.string.error_enable_location_to_search_around_me));
        mLocationRequirementsHelper.create();

        minRadiusInMeters = getResources().getInteger(R.integer.around_me_minimum_radius);
        maxRadiusInMeters = getResources().getInteger(R.integer.around_me_max_radius);

        currentRadiusInMeters = getResources().getInteger(R.integer.around_me_default_radius);

        if (savedInstanceState != null) {
            currentRadiusInMeters = savedInstanceState.getInt(STATE_RADIUS, currentRadiusInMeters);

            if (savedInstanceState.containsKey(STATE_POSITION)) {
                mCurrentLocation = savedInstanceState.getParcelable(STATE_POSITION);
            }
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mCurrentLocation != null) {
            outState.putParcelable(STATE_POSITION, mCurrentLocation);
        }
        outState.putInt(STATE_RADIUS, currentRadiusInMeters);
    }

    @Override
    public void changeContentVisibility(boolean visible) {
        super.changeContentVisibility(visible);

        if (visible) {
            updateFragmentConnections();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClientFactory.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        removeLocationUpdates(mGoogleApiClientFactory);
        mGoogleApiClientFactory.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClientFactory.destroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.around_me_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        View distanceRadiusView = menu.findItem(R.id.action_radius).getActionView();

        SeekBar radiusSeekBar = distanceRadiusView.findViewById(R.id.radiusSeekBar);
        radiusSeekBar.setMax(maxRadiusInMeters - minRadiusInMeters);
        radiusSeekBar.setProgress(currentRadiusInMeters - minRadiusInMeters);
        radiusSeekBar.setOnSeekBarChangeListener(this);
        mDistanceTextView = distanceRadiusView.findViewById(R.id.radiusDistanceTextView);

        mDistanceTextView.setText(DistanceNumberFormat.getSharedInstance().formatDistance(currentRadiusInMeters));
        return true;
    }

    @Override
    public void onApiClientConnected() {
        mLocationRequirementsHelper.request(false, true);
    }

    @Override
    public void onApiClientConnectionSuspended(int code) {
        GoogleApiClientFactory.defaultConnectionSuspendedResolution(code);
    }

    @Override
    public void onApiClientConnectionFailed(@NotNull ConnectionResult result) {
        GoogleApiClientFactory.defaultConnectionFailedResolution(result);
    }

    @Deprecated
    @Override
    public void onConnected(Bundle bundle) {
        onApiClientConnected();
    }

    @Deprecated
    @Override
    public void onConnectionSuspended(int i) {
        onApiClientConnectionSuspended(i);
    }

    @Deprecated
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        onApiClientConnectionFailed(connectionResult);
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onPermissionsHandled(@NotNull String[] acceptedPermissions) {
        if (PermissionManager.containLocationPermissions(acceptedPermissions)) {
            LocationRequest request = new LocationRequest();
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            GoogleApiClient apiClient = mGoogleApiClientFactory.getApiClient();
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(apiClient);
            request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            request.setInterval(getResources().getInteger(R.integer.around_me_location_update_interval_seconds) * 1000L);

            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, request, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        updateFragmentConnections();

    }

    private void updateLocationParameters(DataConnectionModel connection, Location location) {
        connection.getOrchardModule().putExtraParameter(Constants.REQUEST_LATITUDE, String.format(Locale.US, "%f", location.getLatitude()));
        connection.getOrchardModule().putExtraParameter(Constants.REQUEST_RADIUS, String.valueOf(currentRadiusInMeters));
        connection.getOrchardModule().putExtraParameter(Constants.REQUEST_LONGITUDE, String.format(Locale.US, "%f", location.getLongitude()));
        connection.restartDataLoading();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        currentRadiusInMeters = progress + minRadiusInMeters;
        mDistanceTextView.setText(DistanceNumberFormat.getSharedInstance().formatDistance(currentRadiusInMeters));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) { /* empty */ }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        updateFragmentConnections();
    }

    private void updateFragmentConnections() {
        if (mCurrentLocation != null) {
            updateLocationParameters(getGridFragment().getDataConnectionModel(), mCurrentLocation);
            updateLocationParameters(getMapFragment().getDataConnectionModel(), mCurrentLocation);
        }
    }

    private void removeLocationUpdates(@NonNull GoogleApiClientFactory factory) {
        final GoogleApiClient apiClient = factory.getApiClient();
        if (apiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this);
        }
    }
}