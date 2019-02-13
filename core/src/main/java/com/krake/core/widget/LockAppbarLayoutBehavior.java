package com.krake.core.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.krake.core.R;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by joel on 16/11/15.
 */
public class LockAppbarLayoutBehavior extends AppBarLayout.Behavior {

    private final SlideCallback callback;
    private boolean locked = false;
    private boolean permanentlyLocked = false;
    private Set<BottomSheetNotUnderActionBehavior> behaviors;
    private WeakReference<AppBarLayout> appBar;

    public LockAppbarLayoutBehavior(Context context, android.util.AttributeSet attributeSet) {

        TypedArray array = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.LockAppBar, 0, 0);

        if (array.getBoolean(R.styleable.LockAppBar_autoClose, true))
        {
            callback = new SlideCallback(this);
            behaviors = new HashSet<>();
        } else {
            callback = null;
        }
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, AppBarLayout child, View dependency) {

        if (callback != null) {
            CoordinatorLayout.Behavior b = ((CoordinatorLayout.LayoutParams) dependency.getLayoutParams()).getBehavior();

            if (b instanceof BottomSheetNotUnderActionBehavior) {
                ((BottomSheetNotUnderActionBehavior) b).addBottomSheetCallback(callback);
                behaviors.add((BottomSheetNotUnderActionBehavior) b);
            }
        }

        return super.layoutDependsOn(parent, child, dependency);
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, AppBarLayout abl, int layoutDirection) {
        if (appBar == null) {
            appBar = new WeakReference<>(abl);
        }

        return super.onLayoutChild(parent, abl, layoutDirection);
    }

    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, float velocityX, float velocityY) {
        return !locked && super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY);
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout parent, AppBarLayout child, View directTargetChild, View target, int nestedScrollAxes, int type) {
        return !locked && super.onStartNestedScroll(parent, child, directTargetChild, target, nestedScrollAxes, type);
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, AppBarLayout child, MotionEvent ev) {
        return !locked && super.onInterceptTouchEvent(parent, child, ev);
    }

    protected void onBehaviorStateChanged(int newState) {
        boolean willclose;
        if (!(willclose = (newState == BottomSheetBehavior.STATE_EXPANDED))) {
            for (BottomSheetNotUnderActionBehavior b : behaviors) {
                if (b.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    willclose = true;
                    break;
                }
            }
        }

        if (willclose) {
            appBar.get().setExpanded(false, true);
        }

        locked = (willclose || permanentlyLocked);
    }

    public boolean isPermanentlyLocked() {
        return permanentlyLocked;
    }

    public void setPermanentlyLocked(boolean permanentlyLocked) {
        this.permanentlyLocked = permanentlyLocked;

        locked = permanentlyLocked;
    }

    private static class SlideCallback extends SafeBottomSheetBehavior.BottomSheetStateCallback {
        final WeakReference<LockAppbarLayoutBehavior> reference;

        SlideCallback(LockAppbarLayoutBehavior reference) {
            this.reference = new WeakReference<>(reference);
        }

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (reference.get() != null) {
                reference.get().onBehaviorStateChanged(newState);
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

        }

        @Override
        public void onStateWillChange(@NonNull View bottomSheet, int newState) {
            if (reference.get() != null) {
                reference.get().onBehaviorStateChanged(newState);
            }
        }
    }
}
