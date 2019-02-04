package com.krake.core.gcm;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.gson.JsonObject;
import com.krake.core.OrchardError;
import com.krake.core.R;
import com.krake.core.Signaler;
import com.krake.core.network.RemoteRequest;
import com.krake.core.network.RemoteResponse;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class TokenIDService extends FirebaseInstanceIdService {
    public TokenIDService() {
    }

    static public String getUUID(Context mContext) {
        return Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("FCM id", "onTokenRefresh: "+ (refreshedToken == null ? "null" : refreshedToken));

        JsonObject parameters = new JsonObject();
        parameters.addProperty("Token", refreshedToken);
        parameters.addProperty("Device", getString(R.string.orchard_mobile_platform));
        parameters.addProperty("UUID", TokenIDService.getUUID(this));
        parameters.addProperty("Language", getString(R.string.orchard_language));
        parameters.addProperty("Produzione", getString(R.string.orchard_gcm_production));

        boolean enabled = true;
        Bundle extras = new Bundle();
        extras.putBoolean(getString(R.string.orchard_api_token_enabled), enabled);

        String method = enabled ? "PUT" : "DELETE";

        RemoteRequest request = new RemoteRequest(this)
                .setMethod(enabled ? RemoteRequest.Method.PUT : RemoteRequest.Method.DELETE)
                .setBody(parameters)
                .setPath(getString(R.string.orchard_push_api));


        Signaler.shared
                .invokeAPI(this,
                        request,
                        false,
                        extras,
                        new Function2<RemoteResponse, OrchardError, Unit>() {
                            @Override
                            public Unit invoke(RemoteResponse remoteResponse, OrchardError orchardError) {
                                return null;
                            }
                        }
                );
    }

}
