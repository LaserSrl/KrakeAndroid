package com.krake.core.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Widget che centra automaticamente il contenuto in una ScrollView
 */
public class SnapScrollView extends ScrollView {
    public SnapScrollView(Context context) {
        super(context);

        postInit();
    }

    public SnapScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

        postInit();
    }

    public SnapScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        postInit();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SnapScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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