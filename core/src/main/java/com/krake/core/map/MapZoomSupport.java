package com.krake.core.map;

import android.view.View;
import android.view.ViewTreeObserver;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;

/**
 * Funzione per aggiornare il livello di zoom della mappa.
 * Per evitare problemi se la mappa non ha ancora una dimensione calcolata
 */
public class MapZoomSupport {
    private View mRootView;
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;
    private boolean invalidateObserver;

    public MapZoomSupport(View view) {
        mRootView = view;
    }

    public void updateCamera(final CameraUpdate camera, final GoogleMap mMap) {

        if (mRootView.getHeight() != 0 && mRootView.getWidth() != 0)
            try {
                mMap.animateCamera(camera);
            }
            catch (Exception e) {
                insertLayoutObserver(camera,mMap);
            }
        else {
            insertLayoutObserver(camera, mMap);
        }
    }

    private void insertLayoutObserver(final CameraUpdate camera, final GoogleMap mMap) {
        if (mGlobalLayoutListener == null) {
            mGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (!invalidateObserver && mRootView.getHeight() != 0 && mRootView.getWidth() != 0) {
                        try {
                            mMap.animateCamera(camera);
                            invalidateObserver = true;
                            mRootView.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
                            mGlobalLayoutListener = null;
                        } catch (IllegalStateException ignored) {

                        }
                    }
                }
            };

            mRootView.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
            invalidateObserver = false;
        }
    }


}
