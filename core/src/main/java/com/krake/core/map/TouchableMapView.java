package com.krake.core.map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;

import java.lang.ref.WeakReference;

public class TouchableMapView extends MapView {
    private WeakReference<OnMapTouchListener> mListener;

    public TouchableMapView(Context context) {
        super(context);
    }

    public TouchableMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchableMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TouchableMapView(Context context, GoogleMapOptions options) {
        super(context, options);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        if (mListener != null && mListener.get() != null && (action == MotionEvent.ACTION_MOVE ||
                action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_CANCEL ||
                action == MotionEvent.ACTION_DOWN)) {
            mListener.get().onMapTouch(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    public final void setOnMapTouchListener(final TouchableMapView.OnMapTouchListener listener) {
        mListener = new WeakReference<>(listener);
    }

    public interface OnMapTouchListener {
        void onMapTouch(MotionEvent event);
    }
}