package com.krake.core.extension

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat

/**
 * Set the tint for all API levels on this [Drawable].
 *
 * @param color color used to tint the [Drawable].
 */
fun Drawable.setTintCompat(@ColorInt color: Int) {
    // The Drawable is wrapped for backward compatibility.
    val wrapDrawable = DrawableCompat.wrap(this)
    DrawableCompat.setTint(wrapDrawable.mutate(), color)
}