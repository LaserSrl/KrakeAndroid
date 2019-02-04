package com.krake.core.widget;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by joel on 24/06/16.
 */
public class AvoidBottomSheetAndActionBarBehavior extends CoordinatorLayout.Behavior<View> {

    public AvoidBottomSheetAndActionBarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) dependency.getLayoutParams();

        return dependency instanceof AppBarLayout || params.getBehavior() instanceof BottomSheetBehavior || params.getBehavior() instanceof BottomSheetNotUnderActionBehavior;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {

        CoordinatorLayout.LayoutParams dependencyLayoutParams = (CoordinatorLayout.LayoutParams) dependency.getLayoutParams();

        CoordinatorLayout.LayoutParams viewParams = (CoordinatorLayout.LayoutParams) child.getLayoutParams();

        if (dependency instanceof AppBarLayout) {
            AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) dependencyLayoutParams.getBehavior();

            viewParams.topMargin = dependency.getMeasuredHeight() + (behavior).getTopAndBottomOffset();
        } else if (dependencyLayoutParams.getBehavior() instanceof BottomSheetBehavior) {
            viewParams.bottomMargin = ((BottomSheetBehavior) dependencyLayoutParams.getBehavior()).getPeekHeight();
        } else if (dependencyLayoutParams.getBehavior() instanceof BottomSheetNotUnderActionBehavior) {
            viewParams.bottomMargin = ((BottomSheetNotUnderActionBehavior) dependencyLayoutParams.getBehavior()).getPeekHeight();
        }
        child.setLayoutParams(viewParams);

        return true;
    }
}
