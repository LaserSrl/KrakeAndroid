package com.krake.core.media;

import com.krake.core.model.MediaPart;

import java.util.List;

/**
 * Created by joel on 30/09/14.
 */
public interface MediaSelectionListener {
    void onMediaPartSelected(List<MediaPart> media, MediaPart selectedMediaPart);
}
