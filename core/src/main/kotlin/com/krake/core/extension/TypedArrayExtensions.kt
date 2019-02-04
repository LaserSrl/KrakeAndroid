package com.krake.core.extension

import android.content.res.TypedArray
import android.support.annotation.AnyRes
import android.support.annotation.ColorInt
import android.support.annotation.StyleableRes

/**
 * Used to get a null default value instead of an invalid resource id.
 *
 * @param index index of attribute to retrieve.
 * @return the resource id or null if the value wasn't found.
 */
@AnyRes
fun TypedArray.getResourceId(@StyleableRes index: Int): Int? {
    @AnyRes val resId: Int = getResourceId(index, -1)
    return if (resId == -1) null else resId
}

/**
 * Used to get a null default value instead of an invalid color.
 *
 * @param index index of attribute to retrieve.
 * @return the color or null if the value wasn't found.
 */
@ColorInt
fun TypedArray.getColor(@StyleableRes index: Int): Int? {
    @ColorInt val color: Int = getColor(index, -1)
    return if (color == -1) null else color
}