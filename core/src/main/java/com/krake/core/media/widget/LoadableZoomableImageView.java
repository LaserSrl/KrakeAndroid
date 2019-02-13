package com.krake.core.media.widget;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.github.chrisbanes.photoview.PhotoView;
import com.krake.core.media.ImageOptions;
import com.krake.core.media.MediaLoadable;
import com.krake.core.media.WidgetLoadableHelper;
import com.krake.core.media.loader.MediaLoader;

/**
 * ImageView zoomabile che permette il caricamento dei dati tramite il {@link MediaLoader}
 * <br/>
 * Per gli attributi custom guardare {@link WidgetLoadableHelper}
 */
public class LoadableZoomableImageView extends PhotoView implements MediaLoadable {
    private WidgetLoadableHelper mHelper;

    public LoadableZoomableImageView(Context context) {
        super(context);
        init(context, null);
    }

    public LoadableZoomableImageView(Context context, AttributeSet attr) {
        super(context, attr);
        init(context, attr);
    }

    public LoadableZoomableImageView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        init(context, attr);
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