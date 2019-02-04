package com.krake.core.media;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.krake.core.KeyListMap;

import java.util.List;

/**
 * KeyListMap di {@link UploadableMediaInfo}
 * <br/>
 * In questa classe è gestita la serializzazione e la deserializzazione dell'oggetto con le relative trasformazioni
 */
public class KeyMediaListMap extends KeyListMap<UploadableMediaInfo> {
    private static final String KEY_ID = "i";
    private static final String KEY_URI = "u";
    private static final String KEY_PATH = "p";
    private static final String KEY_TYPE = "t";
    private static final String KEY_UPLOADED = "z";

    @SuppressWarnings("WrongConstant")
    @Override
    protected UploadableMediaInfo jsonElementToType(@NonNull JsonElement element) {
        JsonObject obj = (JsonObject) element;

        UploadableMediaInfo media = new UploadableMediaInfo(obj.get(KEY_TYPE).getAsInt());

        JsonElement e = obj.get(KEY_ID);
        if (e != null && !e.isJsonNull()) {
            media.setId(e.getAsLong());
        }

        e = obj.get(KEY_URI);
        if (e != null && !e.isJsonNull()) {
            media.setUri(Uri.parse(e.getAsString()));
        }

        e = obj.get(KEY_PATH);
        if (e != null && !e.isJsonNull()) {
            media.setPath(e.getAsString());
        }

        media.setUploaded(obj.get(KEY_UPLOADED).getAsBoolean());
        return media;
    }

    @Override
    protected JsonElement typeToJsonElement(@NonNull UploadableMediaInfo typedElement) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(KEY_ID, typedElement.getId());
        Uri uri = typedElement.getUri();
        String uriString = uri != null ? uri.toString() : null;
        jsonObject.addProperty(KEY_URI, uriString);
        jsonObject.addProperty(KEY_PATH, typedElement.getPath());
        jsonObject.addProperty(KEY_TYPE, typedElement.getType());
        jsonObject.addProperty(KEY_UPLOADED, typedElement.isUploaded());
        return jsonObject;
    }

    /**
     * Il calcolo dei media caricabili viene fatto tramite il check su {@link UploadableMediaInfo#isUploaded()}
     *
     * @return numero media caricabili
     */
    public int numberOfUploadableMedias() {
        int uploadableMedias = 0;
        for (int i = 0; i < size(); i++) {
            List<UploadableMediaInfo> mediaList = get(keyAt(i));
            for (int j = 0; j < mediaList.size(); j++) {
                if (!mediaList.get(j).isUploaded()) {
                    uploadableMedias++;
                }
            }
        }
        return uploadableMedias;
    }

    /**
     * Elimina tutti i media dal disco presenti nell'ArrayMap
     *
     * @param context Context corrente
     * @return true se tutti i media sono stati eliminati, false se almeno un media non è stato eliminato
     */
    public boolean deleteAll(@NonNull Context context) {
        boolean deleted = true;
        for (int i = 0; i < size(); i++) {
            List<UploadableMediaInfo> mediaList = get(keyAt(i));
            for (int j = 0; j < mediaList.size(); j++) {
                UploadableMediaInfo media = mediaList.get(j);
                if (!media.delete(context)) {
                    deleted = false;
                }
            }
        }
        return deleted;
    }
}