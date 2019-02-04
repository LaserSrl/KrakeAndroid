package com.krake.core.media.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.krake.core.media.ImageOptions;
import com.krake.core.media.MediaLoadable;
import com.krake.core.media.loader.MediaLoader;
import com.krake.core.media.WidgetLoadableHelper;

/**
 * ImageView che permette il caricamento dei dati tramite il {@link MediaLoader}
 * <br/>
 * Per gli attributi custom guardare {@link WidgetLoadableHelper}
 */
public class LoadableImageView extends AppCompatImageView implements MediaLoadable {
    private WidgetLoadableHelper mHelper;

    public LoadableImageView(Context context) {
        super(context);
        init(context, null);
    }

    public LoadableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LoadableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
    public ScaleType getPlaceholderScaleType() {
        return mHelper.getPlaceholderScaleType();
    }

    @Override
    public ScaleType getMediaScaleType() {
        return mHelper.getMediaScaleType();
    }

    @Override
    public boolean isAnimated() {
        return mHelper.isAnimated();
    }

    @Override
    public boolean showPlaceholder() {
        return mHelper.showPlaceholder();
    }
}