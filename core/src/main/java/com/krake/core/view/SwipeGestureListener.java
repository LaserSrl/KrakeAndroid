package com.krake.core.view;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.krake.core.R;


public class SwipeGestureListener extends SimpleOnGestureListener {

    // measurements in dips for density independence
    private static final String TAG = "SimpleGestureListener";
    private int scaledDistance;
    private int minScaledVelocity;
    private SwipeGestureEventHandler eventHandler;

    public SwipeGestureListener(Context context, SwipeGestureEventHandler compareTranslationActivity) {
        super();
        this.eventHandler = compareTranslationActivity;
        scaledDistance = context.getResources().getDimensionPixelSize(R.dimen.swipe_size);
        minScaledVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
        // make it easier to swipe
        minScaledVelocity = (int) (minScaledVelocity * 0.66);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1 != null && e2 != null) {
            // get distance between points of the fling
            double vertical = Math.abs(e1.getY() - e2.getY());
            double horizontal = Math.abs(e1.getX() - e2.getX());

            Log.d(TAG, "onFling vertical:" + vertical + " horizontal:" + horizontal + " VelocityX" + velocityX);

            // test vertical distance, make sure it's a swipe
            if (vertical > scaledDistance) {
                return false;
            }
            // test horizontal distance and velocity
            else if (horizontal > scaledDistance && Math.abs(velocityX) > minScaledVelocity) {
                // right to left swipe
                if (velocityX < 0) {
                    eventHandler.onSwipeRightToLeft();
                }
                // left to right swipe
                else {
                    eventHandler.onSwipeLeftToRight();
                }
                return true;
            }
        }
        return false;
    }

    public interface SwipeGestureEventHandler {

        void onSwipeLeftToRight();

        void onSwipeRightToLeft();

    }
}