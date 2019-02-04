package com.krake.core;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.krake.core.app.KrakeApplication;
import com.krake.core.media.KeyMediaListMap;
import com.krake.core.media.UploadableMediaInfo;

/**
 * Interfaccia che gestisce la fine dell'upload richiamando il listener corretto tramite {@link KrakeApplication#getApiEndListenerClass(String)}.
 * <br/>
 * Per aggiungere un nuovo listener usare il metodo {@link KrakeApplication#registerApiEndListener(String, Class, boolean)}
 * e inserire la classe del nuovo listener che estende {@link UploadCompleteEndListener}.
 */
public interface UploadCompleteEndListener {
    /**
     * Metodo richiamato dopo che tutti gli upload sono stati completati
     *
     * @param context         context dell'application
     * @param keyMediaListMap ArrayMap contenente i media con i valori aggiornati
     * @param e               errore sollevato dal processo di upload, se presente
     * @param endApiCallBundle          Bundle passato nell'intent inizializzato in {@link OrchardService#startServiceToUploadFiles(Context, KeyMediaListMap, Integer, Bundle, Bundle, boolean)}
     */
    void onUploadCompleted(@NonNull Context context, @NonNull KeyListMap<UploadableMediaInfo> keyMediaListMap, @Nullable OrchardError e, @Nullable Bundle endApiCallBundle);
}