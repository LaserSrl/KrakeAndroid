package com.krake.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.krake.core.media.UploadableMediaInfo;

/**
 * Oggetto output di un {@link UploadInterceptor} per avere la lista di ID dei media da relazionare ad un contenuto e i possibili errori sollevati dal processo.
 */
public class UploadOutput {
    private UploadableMediaInfo media;
    private OrchardError orchardError;

    /**
     * Crea un oggetto che viene utilizzato per relazionare gli id dei media al contenuto
     *
     * @param media lista degli id dei media da relazionare
     * @param error possibile errore sollevato dal processo
     */
    public UploadOutput(@NonNull UploadableMediaInfo media, @Nullable OrchardError error) {
        this.media = media;
        this.orchardError = error;
    }

    public UploadableMediaInfo getMedia() {
        return media;
    }

    public void setMedia(@NonNull UploadableMediaInfo media) {
        this.media = media;
    }

    public OrchardError getError() {
        return orchardError;
    }

    public void setError(@Nullable OrchardError error) {
        this.orchardError = error;
    }
}