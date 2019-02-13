package com.krake.core.widget

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.core.content.ContextCompat
import com.krake.core.R
import com.krake.core.extension.setTintCompat
import com.krake.core.util.ColorUtil
import com.krake.core.widget.compat.DrawableCompatManager
import com.krake.core.widget.compat.ViewDrawableCompatManager

/**
 * Created by joel on 29/05/14.
 */
class InstantAutoCompleteTextView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.autoCompleteTextViewStyle
) : AppCompatAutoCompleteTextView(context, attrs, defStyleAttr) {

    private val clearDrawable: Drawable
    private val defaultString: String

    private var drawableCompatManager: DrawableCompatManager? = null

    init {
        drawableCompatManager = ViewDrawableCompatManager(this, attrs)
        clearDrawable = ContextCompat.getDrawable(context, R.drawable.ic_close_24dp)!!
        clearDrawable.setTintCompat(ColorUtil.accentColor(context))
        clearDrawable.setBounds(0, 0, clearDrawable.intrinsicWidth, clearDrawable.intrinsicHeight)
        defaultString = context.getString(R.string.autocomplete_default_text)
        updateClearButton()
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {
                updateClearButton()
            }

            override fun afterTextChanged(editable: Editable) {
                val text = editable.toString()
                val index = text.indexOf(defaultString)
                if (index != -1 && !text.equals(defaultString, ignoreCase = true)) {
                    editable.replace(index, index + defaultString.length, "")
                }
            }
        })

        setOnTouchListener(View.OnTouchListener { _, event ->
            val et = this@InstantAutoCompleteTextView

            if (et.compoundDrawables[2] == null)
                return@OnTouchListener false

            if (event.action != MotionEvent.ACTION_UP)
                return@OnTouchListener false

            if (event.x > et.width - et.paddingRight - clearDrawable.intrinsicWidth) {
                et.setText("")
                et.updateClearButton()
            }
            false
        })
    }

    override fun enoughToFilter(): Boolean = true

    @ColorInt
    fun getCompoundDrawablesTintCompat(): Int? = drawableCompatManager?.getCompoundDrawablesTintCompat()

    fun setCompoundDrawablesTintCompat(@ColorInt color: Int) {
        drawableCompatManager?.setCompoundDrawablesTintCompat(color)
    }

    fun setCompoundDrawablesCompat(@DrawableRes start: Int?, @DrawableRes top: Int?, @DrawableRes end: Int?, @DrawableRes bottom: Int?) {
        drawableCompatManager?.setCompoundDrawablesCompat(start, top, end, bottom)
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (filter != null && focused) {
            performFiltering(text, 0)
        }
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && isPopupShowing) {
            val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val focusedView = findFocus()
            if (focusedView != null) {
                if (inputManager.hideSoftInputFromWindow(focusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)) {
                    return true
                }
            }
        }

        return super.onKeyPreIme(keyCode, event)
    }

    override fun setCompoundDrawables(left: Drawable?, top: Drawable?, right: Drawable?, bottom: Drawable?) {
        super.setCompoundDrawables(left, top, right, bottom)
        drawableCompatManager?.invalidateDrawablesTintCompat()
    }

    override fun setCompoundDrawablesRelative(start: Drawable?, top: Drawable?, end: Drawable?, bottom: Drawable?) {
        super.setCompoundDrawablesRelative(start, top, end, bottom)
        drawableCompatManager?.invalidateDrawablesTintCompat()
    }

    internal fun updateClearButton() {
        if (TextUtils.isEmpty(this.text.toString())) {
            // add the clear button
            this.setCompoundDrawables(this.compoundDrawables[0], this.compoundDrawables[1], null, this.compoundDrawables[3])
        } else {
            //remove clear button
            this.setCompoundDrawables(this.compoundDrawables[0], this.compoundDrawables[1], clearDrawable, this.compoundDrawables[3])
        }
    }
}

