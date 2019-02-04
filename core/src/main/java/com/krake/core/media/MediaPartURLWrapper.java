package com.krake.core.media;

import android.support.annotation.NonNull;

import com.krake.core.model.MediaPart;

/**
 * Created by joel on 20/01/16.
 */
public class MediaPartURLWrapper implements MediaPart {

    String mediaURL;

    public MediaPartURLWrapper(@NonNull String mediaURL) {
        this.mediaURL = mediaURL;
    }

    @Override
    public String getMimeType() {
        return "image/*";
    }

    @Override
    public String getFileName() {
        return null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getFolderPath() {
        return null;
    }

    @Override
    public String getLogicalType() {
        return null;
    }

    @Override
    public String getMediaUrl() {
        return mediaURL;
    }

    @Override
    public int getMediaType() {
        return MediaType.IMAGE;
    }
}
