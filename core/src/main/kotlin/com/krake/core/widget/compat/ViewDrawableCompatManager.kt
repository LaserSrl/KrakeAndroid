package com.krake.core.widget.compat

import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.v7.content.res.AppCompatResources
import android.util.AttributeSet
import android.widget.TextView
import com.krake.core.R
import com.krake.core.extension.getColor
import com.krake.core.extension.getResourceId
import com.krake.core.extension.setTintCompat

/**
 * Implementation of [DrawableCompatManager] that can resolve the compat [Drawable]s.
 * The available compat attributes for compound drawables are:
 * - [R.attr.drawableStartCompat]
 * - [R.attr.drawableTopCompat]
 * - [R.attr.drawableEndCompat]
 * - [R.attr.drawableBottomCompat]
 * - [R.attr.drawableTintCompat]
 *
 * @param view the [TextView] that will use the compat resources.
 * @param attrs the additional [AttributeSet] used to get the attributes specified in XML.
 */
class ViewDrawableCompatManager(val view: TextView, attrs: AttributeSet? = null) : DrawableCompatManager {

    @ColorInt
    private var drawableTintCompat: Int?

    init {
        if (attrs != null) {
            val typedArr = view.context.obtainStyledAttributes(attrs, R.styleable.DrawableCompatView)

            // Read the attributes.
            @DrawableRes val startId: Int? = typedArr.getResourceId(R.styleable.DrawableCompatView_drawableStartCompat)
            @DrawableRes val topId: Int? = typedArr.getResourceId(R.styleable.DrawableCompatView_drawableTopCompat)
            @DrawableRes val endId: Int? = typedArr.getResourceId(R.styleable.DrawableCompatView_drawableEndCompat)
            @DrawableRes val bottomId: Int? = typedArr.getResourceId(R.styleable.DrawableCompatView_drawableBottomCompat)
            drawableTintCompat = typedArr.getColor(R.styleable.DrawableCompatView_drawableTintCompat)

            // Recycle the array.
            typedArr.recycle()

            setCompoundDrawablesCompat(startId, topId, endId, bottomId)
            // Invalidate the compat tinting at the first access.
            invalidateDrawablesTintCompat()
        } else {
            drawableTintCompat = null
        }
    }

    @ColorInt
    override fun getCompoundDrawablesTintCompat(): Int? = drawableTintCompat

    override fun setCompoundDrawablesTintCompat(@ColorInt color: Int) {
        drawableTintCompat = color
        // Invalidate the tinting when a new color is set.
        invalidateDrawablesTintCompat()
    }

    override fun invalidateDrawablesTintCompat() {
        drawableTintCompat?.let { tintCompat ->
            // Exclude the null drawables.
            view.compoundDrawables.filter { it != null }
                    .forEach { it.setTintCompat(tintCompat) }
        }
    }

    override fun setCompoundDrawablesCompat(@DrawableRes start: Int?, @DrawableRes top: Int?, @DrawableRes end: Int?, @DrawableRes bottom: Int?) {
        val context = view.context
        // Convert the ids to the drawables if possible.
        val compatStart = start?.let { AppCompatResources.getDrawable(context, it) }
        val compatTop = top?.let { AppCompatResources.getDrawable(context, it) }
        val compatEnd = end?.let { AppCompatResources.getDrawable(context, it) }
        val compatBottom = bottom?.let { AppCompatResources.getDrawable(context, it) }

        // Set the compound drawables retrieved before.
        view.setCompoundDrawablesWithIntrinsicBounds(compatStart, compatTop, compatEnd, compatBottom)
    }
}