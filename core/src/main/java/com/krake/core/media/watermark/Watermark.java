package com.krake.core.media.watermark;

import android.net.Uri;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Classe che rappresenta un watermark da inserire su una foto
 * Created by joel on 22/12/15.
 */
public class Watermark {
    static public final int TOP = 1;
    static public final int CENTERV = 1 << 1;
    static public final int BOTTOM = 1 << 2;
    static public final int LEFT = 1 << 4;
    static public final int CENTERH = 1 << 5;
    static public final int RIGHT = 1 << 6;
    public static final int POSITION_TOP_LEFT = TOP + LEFT;
    public static final int POSITION_TOP_CENTER = TOP + CENTERH;
    public static final int POSITION_TOP_RIGHT = TOP + RIGHT;
    public static final int POSITION_CENTER_LEFT = CENTERV + LEFT;
    public static final int POSITION_CENTER = CENTERV + CENTERH;
    public static final int POSITION_CENTER_RIGHT = CENTERV + RIGHT;
    public static final int POSITION_BOTTOM_LEFT = BOTTOM + LEFT;
    public static final int POSITION_BOTTOM_CENTER = BOTTOM + CENTERH;
    public static final int POSITION_BOTTOM_RIGHT = BOTTOM + RIGHT;

    public static final int FILL_NONE = 0;
    public static final int FILL_VERTICAL = 1;
    public static final int FILL_HORIZONTAL = 2;

    private final String watermarkURL;
    private final
    @Position
    int position;
    private final
    @Fill
    int fill;
    private Uri mLocalWaterMarkUri;

    /**
     * INdicazione di un watermark
     *
     * @param watermarkURL url del watermark puo' anche essere relativo al media path di orchard
     */
    public Watermark(@NonNull String watermarkURL) {
        this(watermarkURL, POSITION_TOP_LEFT, FILL_NONE);
    }

    /**
     * INdicazione di un watermark
     *
     * @param watermarkURL url del watermark puo' anche essere relativo al media path di orchard
     * @param position     posizione nell'immagine del watermark
     */
    public Watermark(@NonNull String watermarkURL, @Position int position) {
        this(watermarkURL, position, FILL_NONE);
    }

    /**
     * INdicazione di un watermark
     *
     * @param watermarkURL url del watermark puo' anche essere relativo al media path di orchard
     * @param position     posizione nell'immagine del watermark
     * @param fill         modo per scalare l'immagine
     */
    public Watermark(@NonNull String watermarkURL, @Position int position, @Fill int fill) {
        this.watermarkURL = watermarkURL;
        this.position = position;
        this.fill = fill;
    }

    public boolean isAvailable() {
        return mLocalWaterMarkUri != null;
    }

    public Uri getLocalWaterMarkUri() {
        return mLocalWaterMarkUri;
    }

    public void setLocalWaterMarkUri(Uri mLocaleWaterMarkUri) {
        this.mLocalWaterMarkUri = mLocaleWaterMarkUri;
    }

    public String getWatermarkURL() {
        return watermarkURL;
    }

    public int getPosition() {
        return position;
    }

    public int getFill() {
        return fill;
    }

    @IntDef({POSITION_TOP_LEFT, POSITION_TOP_CENTER, POSITION_TOP_RIGHT, POSITION_CENTER_LEFT, POSITION_CENTER, POSITION_CENTER_RIGHT, POSITION_BOTTOM_LEFT, POSITION_BOTTOM_CENTER, POSITION_BOTTOM_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Position {
    }

    @IntDef({FILL_NONE, FILL_VERTICAL, FILL_HORIZONTAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Fill {
    }

}