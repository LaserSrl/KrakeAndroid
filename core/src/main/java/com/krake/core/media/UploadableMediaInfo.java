package com.krake.core.media;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Classe utilizzata per salvare le informazioni di un media.
 * <br/>
 * Questa classe verrà serializzata tramite Gson per salvare le informazioni durante la rotazione (nel Bundle savedInstanceState) o alla chiusura dell'Activity (nelle SharedPreferences)
 */
public class UploadableMediaInfo {
    private Long id;
    private Uri uri;
    private String path;
    @MediaType
    private int type;
    private boolean uploaded;

    /**
     * Crea un media di un certo tipo vuoto.
     *
     * @param type tipo del media
     */
    public UploadableMediaInfo(@MediaType int type) {
        this(null, null, null, type);
    }

    /**
     * Crea un media dando la possibilità di inizializzarlo completamente attraverso il costruttore.
     *
     * @param uri         uri del media
     * @param path        path del file creato tramite il FileProvider
     * @param type        tipo del media
     */
    public UploadableMediaInfo(@Nullable Long id, @Nullable Uri uri, @Nullable String path, @MediaType int type) {
        this.id = id;
        this.uri = uri;
        this.path = path;
        this.type = type;
    }
    public Long getId() {
        return id;
    }

    public void setId(@Nullable Long id) {
        this.id = id;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(@Nullable Uri uri) {
        this.uri = uri;
    }

    public String getPath() {
        return path;
    }

    public void setPath(@Nullable String path) {
        this.path = path;
    }

    @MediaType
    public int getType() {
        return type;
    }

    public void setType(@MediaType int type) {
        this.type = type;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    /**
     * Cancella il file dal disco basandosi sul suo Uri, nel caso in cui non sia nullo.
     *
     * @param context Context corrente
     * @return true se il file è stato eliminato
     */
    public boolean delete(@NonNull Context context) {
        int deleted = 0;
        if (uri != null) {
            deleted = context.getContentResolver().delete(uri, null, null);
        }
        return deleted > 0;
    }
}