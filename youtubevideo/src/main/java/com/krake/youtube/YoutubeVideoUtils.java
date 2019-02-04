package com.krake.youtube;

import android.net.Uri;

import com.krake.youtube.model.YoutubeVideo;

import java.util.List;

/**
 * Created by joel on 15/01/16.
 */
public class YoutubeVideoUtils {
    static public String extractVideoIdentifier(YoutubeVideo video) {
        Uri youtubeUri = Uri.parse(video.getVideoUrlValue());
        String identifier = youtubeUri.getQueryParameter("v");
        if (identifier == null) {
            List<String> segments = youtubeUri.getPathSegments();
            return segments.get(segments.size() - 1);
        }

        return identifier;
    }
}
