package com.krake.core.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.appbar.AppBarLayout;

public class NotUnderActionBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

    /**
     * Default constructor for instantiating BottomSheetBehaviors.
     */
    public NotUnderActionBehavior() {
    }

    /**
     * Default constructor for inflating BottomSheetBehaviors from layout.
     *
     * @param context The {@link Context}.
     * @param attrs   The {@link AttributeSet}.
     */
    public NotUnderActionBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, V child, View dependency) {

        return dependency instanceof AppBarLayout;
    }


    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, V child, View dependency) {

        CoordinatorLayout.LayoutParams dependencyLayoutParams = (CoordinatorLayout.LayoutParams) dependency.getLayoutParams();

        if (dependency instanceof AppBarLayout) {
            AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) dependencyLayoutParams.getBehavior();

            int topPadding = dependency.getMeasuredHeight() + (behavior).getTopAndBottomOffset();

            child.setPadding(child.getPaddingLeft(), topPadding, child.getPaddingRight(), child.getPaddingBottom());

            return true;

        }

        return false;
    }
}
