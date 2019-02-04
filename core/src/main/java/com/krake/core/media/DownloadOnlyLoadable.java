package com.krake.core.media;

import android.widget.ImageView;

import com.krake.core.R;

/**
 * Adapter di {@link MediaLoadable} che implementa tutti i metodi di MediaLoadable con il default perché Java 8 non è ancora disponibile per Android.
 * <br/>
 * Questo permette al Context di creare un'istanza di {@link DownloadOnlyLoadable} e implementare solo i metodi necessari.
 */
public abstract class DownloadOnlyLoadable implements MediaLoadable {
    @Override
    public ImageOptions getOptions() {
        return ImageOptions.getDefault();
    }

    @Override
    public int getPhotoPlaceholder() {
        return R.drawable.photo_placeholder;
    }

    @Override
    public int getVideoPlaceholder() {
        return R.drawable.video_placeholder;
    }

    @Override
    public int getAudioPlaceholder() {
        return R.drawable.audio_placeholder;
    }

    @Override
    public ImageView.ScaleType getPlaceholderScaleType() {
        return ImageView.ScaleType.FIT_CENTER;
    }

    @Override
    public ImageView.ScaleType getMediaScaleType() {
        return ImageView.ScaleType.CENTER_CROP;
    }

    @Override
    public boolean isAnimated() {
        return true;
    }

    @Override
    public boolean showPlaceholder() {
        return true;
    }
}