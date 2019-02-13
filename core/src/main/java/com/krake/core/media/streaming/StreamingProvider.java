package com.krake.core.media.streaming;

import android.content.Context;
import androidx.annotation.NonNull;

/**
 * Interfaccia che definisce le proprietà di un provider utilizzato per lo streaming.
 */
public interface StreamingProvider {
    /**
     * Usato per fare il confronto tra nome e url
     *
     * @return nome del provider (case insensitive)
     */
    String providerName();

    /**
     * Gestisce la decifratura della stringa che arriva da WS e la trasforma in un url effettivo di streaming
     *
     * @param context      Context corrente
     * @param sourceString stringa di partenza
     * @return url valido per lo streaming
     */
    String retrieveVideoUrl(@NonNull Context context, @NonNull String sourceString);
}