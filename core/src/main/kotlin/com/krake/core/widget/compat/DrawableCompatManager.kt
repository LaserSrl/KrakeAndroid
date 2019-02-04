package com.krake.core.widget.compat

import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes

/**
 * Used to allow the compound drawables features on all API levels.
 */
interface DrawableCompatManager {

    /**
     * Get the tint of the compound [Drawable]s for all API levels.
     *
     * @return the color used to tint the compound [Drawable]s or null if not specified.
     */
    @ColorInt
    fun getCompoundDrawablesTintCompat(): Int?

    /**
     * Set the tint of the compound [Drawable]s for all API levels.
     *
     * @param color the color used to tint the compound [Drawable]s.
     */
    fun setCompoundDrawablesTintCompat(@ColorInt color: Int)

    /**
     * Refresh the current [Drawable]s tint color using the compat tint.
     */
    fun invalidateDrawablesTintCompat()

    /**
     * Set the compound [Drawable] resources that will be resolved correctly for all API levels.
     *
     * @param start the resource used as the start compound [Drawable].
     * @param top the resource used as the top compound [Drawable].
     * @param end the resource used as the end compound [Drawable].
     * @param bottom the resource used as the bottom compound [Drawable].
     */
    fun setCompoundDrawablesCompat(@DrawableRes start: Int?, @DrawableRes top: Int?, @DrawableRes end: Int?, @DrawableRes bottom: Int?)
}