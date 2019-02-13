package com.krake.core.widget.compat

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.AppCompatTextView
import com.krake.core.R

/**
 * Implementation of [AppCompatTextView] that can resolve the compat [Drawable]s.
 * The available compat XML attributes for compound drawables are:
 * - [R.attr.drawableStartCompat]
 * - [R.attr.drawableTopCompat]
 * - [R.attr.drawableEndCompat]
 * - [R.attr.drawableBottomCompat]
 * - [R.attr.drawableTintCompat]
 */
open class DrawableCompatTextView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    /**
     * The manager must be nullable because some methods of this view are called in the
     * constructor of the superclass.
     */
    @Suppress("LeakingThis")
    @VisibleForTesting
    internal open val drawableCompatManager: DrawableCompatManager? = createDrawableManager(attrs)

    /**
     * Get the tint of the compound [Drawable]s for all API levels.
     *
     * @return the color used to tint the compound [Drawable]s or null if not specified.
     */
    @ColorInt
    fun getCompoundDrawablesTintCompat(): Int? = drawableCompatManager?.getCompoundDrawablesTintCompat()

    /**
     * Set the tint of the compound [Drawable]s for all API levels.
     *
     * @param color the color used to tint the compound [Drawable]s.
     */
    fun setCompoundDrawablesTintCompat(@ColorInt color: Int) {
        drawableCompatManager?.setCompoundDrawablesTintCompat(color)
    }

    /**
     * Set the compound [Drawable] resources that will be resolved correctly for all API levels.
     *
     * @param start the resource used as the start compound [Drawable].
     * @param top the resource used as the top compound [Drawable].
     * @param end the resource used as the end compound [Drawable].
     * @param bottom the resource used as the bottom compound [Drawable].
     */
    fun setCompoundDrawablesCompat(@DrawableRes start: Int?, @DrawableRes top: Int?, @DrawableRes end: Int?, @DrawableRes bottom: Int?) {
        drawableCompatManager?.setCompoundDrawablesCompat(start, top, end, bottom)
    }

    override fun setCompoundDrawables(left: Drawable?, top: Drawable?, right: Drawable?, bottom: Drawable?) {
        super.setCompoundDrawables(left, top, right, bottom)
        drawableCompatManager?.invalidateDrawablesTintCompat()
    }

    override fun setCompoundDrawablesRelative(start: Drawable?, top: Drawable?, end: Drawable?, bottom: Drawable?) {
        super.setCompoundDrawablesRelative(start, top, end, bottom)
        drawableCompatManager?.invalidateDrawablesTintCompat()
    }

    protected open fun createDrawableManager(attrs: AttributeSet?): DrawableCompatManager =
            ViewDrawableCompatManager(this, attrs)
}