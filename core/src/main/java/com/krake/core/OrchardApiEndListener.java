package com.krake.core;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.krake.core.network.RemoteRequest;
import com.krake.core.network.RemoteResponse;

/**
 * Interfaccia dei listener per la gestione della fine delle chiamate API di orchard.
 * <p/>
 * Created by joel on 13/10/14.
 */
public interface OrchardApiEndListener {

    void onApiInvoked(@NonNull Context context,
                      @NonNull RemoteRequest remoteRequest,
                      @Nullable RemoteResponse remoteResponse,
                      @Nullable OrchardError e,
                      @Nullable Object endListenerParameters);
}
