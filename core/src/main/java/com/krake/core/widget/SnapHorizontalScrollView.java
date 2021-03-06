package com.krake.core.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

/**
 * Widget che centra automaticamente il contenuto in una HorizontalScrollView
 */
public class SnapHorizontalScrollView extends HorizontalScrollView {
    public SnapHorizontalScrollView(Context context) {
        super(context);

        postInit();
    }

    public SnapHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

        postInit();
    }

    public SnapHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        postInit();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SnapHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        postInit();
    }

    /**
     * Chiamato nel costruttore per le configurazioni iniziali
     */
    private void postInit() {
        SnapManager.addTo(this);
    }
}