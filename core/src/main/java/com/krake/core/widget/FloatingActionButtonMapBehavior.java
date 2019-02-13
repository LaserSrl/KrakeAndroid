package com.krake.core.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.krake.core.R;

/**
 * Behavior designed for use with {@link FloatingActionButton} instances. It's main function
 * is to move {@link FloatingActionButton} views so that any displayed {@link Snackbar}s do
 * not cover them.
 */
public class FloatingActionButtonMapBehavior extends FloatingActionButton.Behavior {

    private int mActionBarSize;

    private VisibilityListener mListener;

    public FloatingActionButtonMapBehavior() {
    }

    public FloatingActionButtonMapBehavior(Context context, AttributeSet attrs) {
        super();
        TypedValue typedValue = new TypedValue();
        int[] textSizeAttr = new int[]{R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = context.obtainStyledAttributes(typedValue.data, textSizeAttr);
        mActionBarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, FloatingActionButton child, int layoutDirection) {
        if (super.onLayoutChild(parent, child, layoutDirection)) {
            updateFabVisibility(parent, child);
        }

        return true;
    }

    private void updateFabVisibility(CoordinatorLayout parent, FloatingActionButton child) {
        CoordinatorLayout.LayoutParams fLp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();

        Rect fRect = new Rect();
        int h = parent.getHeight();
        parent.offsetDescendantRectToMyCoords(child, fRect);

        int diff = h - child.getHeight() - fRect.bottom - fLp.bottomMargin;

        View anchorView = parent.findViewById(fLp.getAnchorId());

        boolean visible;
        View loopView = anchorView;
        do {
            visible = loopView.getVisibility() == View.VISIBLE;
            loopView = loopView.getParent() instanceof View ? (View) loopView.getParent() : null;
        } while (visible && loopView != null);

        boolean hidden = diff == 0 || fRect.top <= mActionBarSize || !visible;
        if (hidden) {
            child.hide();

        } else {
            child.show();
        }

        if (mListener != null)
            mListener.onVisibilityChange(!hidden);
    }

    @Override
    public boolean onNestedFling(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, float velocityX, float velocityY, boolean consumed) {
        boolean fling = super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
        updateFabVisibility(coordinatorLayout, child);
        return fling;
    }

    @Override
    public boolean onStartNestedScroll(final CoordinatorLayout coordinatorLayout, final FloatingActionButton child,
                                       final View directTargetChild, final View target, final int nestedScrollAxes) {
        // Ensure we react to vertical scrolling
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
                || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target) {
        super.onStopNestedScroll(coordinatorLayout, child, target);
        updateFabVisibility(coordinatorLayout, child);
    }

    @Override
    public void onNestedScroll(final CoordinatorLayout coordinatorLayout, final FloatingActionButton child,
                               final View target, final int dxConsumed, final int dyConsumed,
                               final int dxUnconsumed, final int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);

        updateFabVisibility(coordinatorLayout, child);
    }

    public VisibilityListener getmListener() {
        return mListener;
    }

    public void setmListener(VisibilityListener mListener) {
        this.mListener = mListener;
    }

    public interface VisibilityListener {
        void onVisibilityChange(boolean visible);
    }
}