package com.krake.core.widget

import android.app.Activity
import android.content.ContextWrapper
import android.support.annotation.StringRes
import android.support.design.widget.CoordinatorLayout
import android.view.View

/**
 * Created by joel on 30/09/16.
 */

fun View.getString(@StringRes string: Int): String {
    return context.getString(string)
}

fun View.getActivity(): Activity? {
    var context = getContext()
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }
        context = context.getBaseContext()
    }
    return null
}

fun View.getCoordinatorLayout(): CoordinatorLayout? {
    var parent = parent

    do {
        if (parent is CoordinatorLayout)
            return parent

        parent = parent.parent
    } while (parent != null)

    return null
}