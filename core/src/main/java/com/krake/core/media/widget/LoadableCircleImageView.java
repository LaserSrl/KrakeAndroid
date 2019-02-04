package com.krake.core.media.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.krake.core.media.ImageOptions;
import com.krake.core.media.MediaLoadable;
import com.krake.core.media.loader.MediaLoader;
import com.krake.core.media.WidgetLoadableHelper;
import com.krake.core.widget.CircleImageView;

/**
 * ImageView circolare che permette il caricamento dei dati tramite il {@link MediaLoader}
 * <br/>
 * Per gli attributi custom guardare {@link WidgetLoadableHelper}
 */
public class LoadableCircleImageView extends CircleImageView implements MediaLoadable {
    private WidgetLoadableHelper mHelper;

    public LoadableCircleImageView(Context context) {
        super(context);
        init(context, null);
    }

    public LoadableCircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LoadableCircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    /**
     * Inizializza l'ImageView per ogni costruttore
     *
     * @param context context corrente
     * @param attrs   attributi presi da xml
     */
    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        mHelper = new WidgetLoadableHelper(context, attrs);
    }

    @Override
    public ImageOptions getOptions() {
        return mHelper.getOptions();
    }

    @Override
    public int getPhotoPlaceholder() {
        return mHelper.getPhotoPlaceholder();
    }

    @Override
    public int getVideoPlaceholder() {
        return mHelper.getVideoPlaceholder();
    }

    @Override
    public int getAudioPlaceholder() {
        return mHelper.getAudioPlaceholder();
    }

    @Override
    public ImageView.ScaleType getPlaceholderScaleType() {
        return ImageView.ScaleType.CENTER_CROP;
    }

    @Override
    public ImageView.ScaleType getMediaScaleType() {
        return ImageView.ScaleType.CENTER_CROP;
    }

    @Override
    public boolean isAnimated() {
        // quando è circolare viene ritornato false per evitare problemi di caricamento con Glide
        return false;
    }

    @Override
    public boolean showPlaceholder() {
        return mHelper.showPlaceholder();
    }
}