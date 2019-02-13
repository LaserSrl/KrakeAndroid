package com.krake.core.api;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;
import com.google.maps.android.PolyUtil;
import com.krake.core.OrchardError;
import com.krake.core.network.CancelableRequest;
import com.krake.core.network.RemoteClient;
import com.krake.core.network.RemoteRequest;
import com.krake.core.network.RemoteResponse;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

import java.util.List;
import java.util.Locale;

/*
Classe per fare una richiesta delle indicazioni su come arrivare ad una destinazione
 */
public class Request implements Function2<RemoteResponse, OrchardError, Unit> {
    private static final String GoogleMapsDirectionServiceEndPoint = "http://maps.googleapis.com/maps/api/directions/json";
    private final android.location.Location origin;
    private final android.location.Location destination;
    private final android.location.Location[] waypoints;
    private Listener listener;

    private CancelableRequest mRequest;

    /**
     * Richiesta delle directions. Per ora permette solamente di impostare origin e destination
     *  @param origin      coordinate dell'origine
     * @param destination coordinate destinazione
     * @param waypoints
     */
    public Request(@NonNull Context context, @NonNull android.location.Location origin,
                   @NonNull android.location.Location destination,
                   @Nullable android.location.Location[] waypoints) {
        this.waypoints = waypoints;

        this.origin = origin;
        this.destination = destination;
    }

    /**
     * Avvia il caricamento della richiesta
     *
     * @param listener listenet da notificare a fine del caricamento delle info
     * @return
     */
    public
    @NonNull
    Request load(@NonNull Listener listener) {
        this.listener = listener;

        RemoteRequest request = new RemoteRequest(GoogleMapsDirectionServiceEndPoint)
                .setQuery("sensor", "true")
                .setQuery("origin", String.format(Locale.ENGLISH, "%f,%f", origin.getLatitude(), origin.getLongitude()))
                .setQuery("destination", String.format(Locale.ENGLISH, "%f,%f", destination.getLatitude(), destination.getLongitude()))
                .setQuery("region", Locale.getDefault().getCountry());

        if (waypoints != null && waypoints.length > 0) {
            StringBuffer sb = new StringBuffer();

            for (int index = 0; index < waypoints.length; ++index) {
                if (sb.length() > 0)
                    sb.append("|");

                sb.append(String.format(Locale.ENGLISH, "%f,%f", waypoints[index].getLatitude(), waypoints[index].getLongitude()));
            }

            request.setQuery("waypoints", sb.toString());
        }

        mRequest = RemoteClient.Companion.client(RemoteClient.Mode.DEFAULT)
                .enqueue(request, this);

        return this;
    }


    @Override
    public Unit invoke(RemoteResponse remoteResponse, OrchardError orchardError) {

        mRequest = null;
        List<LatLng> route = null;
        Exception e = orchardError;
        if (remoteResponse != null) {
            try {
                route = parse(remoteResponse.jsonObject());
            } catch (DirectionException e1) {
                e = e1;
            }
        }
        listener.onCompleted(e, route);

        return null;
    }

    public void cancel() {
        if (mRequest != null)
            mRequest.cancel();
    }


    private List<LatLng> parse(JsonObject json) throws DirectionException {
        String status = json.get("status").getAsString();
        if (status.equals("OK")) {
            final JsonObject jsonRoute = json.getAsJsonArray("routes").get(0).getAsJsonObject();

            return PolyUtil.decode(jsonRoute.get("overview_polyline").getAsJsonObject().get("points").getAsString());
        } else {
            throw new DirectionException(status);
        }
    }


    public interface Listener {
        void onCompleted(Exception e, List<LatLng> r);
    }
}
