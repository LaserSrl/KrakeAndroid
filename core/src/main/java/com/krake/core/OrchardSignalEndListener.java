package com.krake.core;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.JsonObject;

import java.util.Map;

/**
 * Classe invocata alla fine di una chiamata di signal per orchard.
 * Richiede
 * Created by joel on 17/02/15.
 */
public interface OrchardSignalEndListener {
    /**
     * @param correctResult risulato dell'esecuzione se andata a buon fine
     * @param e             eccezione
     * @param extras        bundle passato in fase di invio della richiesta
     */
    void onSignalSent(@NonNull Context context,
                      @Nullable JsonObject correctResult,
                      @Nullable OrchardError e,
                      @NonNull Map<String, String> extras);
}
