package com.krake.core.widget

import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout

/**
 * Created by joel on 23/02/17.
 */

fun CoordinatorLayout.isOneOfChildBottomSheetExpanded(): Boolean {
    for (index in 0..this.getChildCount() - 1) {
        val view = this.getChildAt(index)

        val behavior = (view.getLayoutParams() as CoordinatorLayout.LayoutParams).behavior

        if (behavior != null && behavior is BottomSheetBehavior<*>) {
            if (behavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                return true
            }
        }
    }
    return false
}