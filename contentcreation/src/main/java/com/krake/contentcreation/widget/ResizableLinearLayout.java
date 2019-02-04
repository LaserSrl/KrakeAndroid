package com.krake.contentcreation.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * LinearLayout che manda una callback quando avviene un cambiamento all'interno delle view.
 * <br/>
 * Questo layout è stato creato come alternativa per il ViewTreeObserver.OnGlobalLayoutListener considerando che quest'ultimo non notifica il resize
 * o lo notifica eccessivamente (anche dopo le chiamate in invalidate() e requestLayout().
 */
public class ResizableLinearLayout extends LinearLayout {
    private int mActualWidth;
    private int mActualHeight;
    private ResizeCallback mResizeCallback;

    public ResizableLinearLayout(Context context) {
        super(context);
    }

    public ResizableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ResizableLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ResizableLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int w = r - l;
        int h = b - t;
        // la callback viene mandata solo se le misure sono effettivamente cambiate
        if (mActualWidth != w || mActualHeight != h) {
            mActualWidth = w;
            mActualHeight = h;
            if (mResizeCallback != null) {
                mResizeCallback.onResize(w, h);
            }
        }
    }

    /**
     * Metodo per ottenere la larghezza attuale, anche dopo un resize
     *
     * @return larghezza attuale dopo il resize, se diversa da 0, in caso contrario la measured width
     */
    public int getActualWidth() {
        if (mActualWidth != 0) {
            return mActualWidth;
        }
        return getMeasuredWidth();
    }

    /**
     * Metodo per ottenere l'altezza attuale, anche dopo un resize
     *
     * @return altezza attuale dopo il resize, se diversa da 0, in caso contrario la measured height
     */
    public int getActualHeight() {
        if (mActualHeight != 0) {
            return mActualHeight;
        }
        return getMeasuredHeight();
    }

    /**
     * Aggiunge la callback per il resize
     *
     * @param resizeCallback oggetto che riceve la notifa della callback
     */
    public void addResizeCallback(@Nullable ResizeCallback resizeCallback) {
        mResizeCallback = resizeCallback;
    }

    /**
     * Interfaccia per ricevere le callback di un cambiamento nelle dimensioni del layout
     */
    public interface ResizeCallback {
        /**
         * Viene chiamato se avviene un cambiamento nelle dimensioni del layout
         *
         * @param newWidth  nuova larghezza del layout
         * @param newHeight nuova altezza del layout
         */
        void onResize(int newWidth, int newHeight);
    }
}