package com.krake.core.widget.compat

import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import com.krake.core.test.R
import com.krake.test.extension.viewTestRule
import com.nhaarman.mockito_kotlin.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented test class for [DrawableCompatButton].
 */
class DrawableCompatButtonTest {

    @JvmField
    @Rule
    val viewTestRule = viewTestRule<DrawableCompatButton>()

    private lateinit var spiedView: DrawableCompatButton

    @Before
    fun setUp() {
        // Reset the spy before each test.
        spiedView = spy(viewTestRule.view)
        // Returned a mock of DrawableCompatManager to verify the interactions with it.
        doReturn(mock<DrawableCompatManager>()).whenever(spiedView).drawableCompatManager
    }

    @Test
    fun getCompoundDrawablesTintCompat() {
        spiedView.getCompoundDrawablesTintCompat()
        verify(spiedView.drawableCompatManager)!!.getCompoundDrawablesTintCompat()
    }

    @Test
    fun setCompoundDrawablesTintCompat() {
        @ColorInt val color = Color.BLACK
        spiedView.setCompoundDrawablesTintCompat(color)
        verify(spiedView.drawableCompatManager)!!.setCompoundDrawablesTintCompat(color)
    }

    @Test
    fun setNullCompoundDrawablesCompat() {
        spiedView.setCompoundDrawablesCompat(null, null, null, null)
        verify(spiedView.drawableCompatManager)!!.setCompoundDrawablesCompat(null, null, null, null)
    }

    @Test
    fun setAllCompoundDrawablesCompat() {
        @DrawableRes val res = R.drawable.ic_vector
        spiedView.setCompoundDrawablesCompat(res, res, res, res)
        verify(spiedView.drawableCompatManager)!!.setCompoundDrawablesCompat(res, res, res, res)
    }

    @Test
    fun setStartCompoundDrawableCompat() {
        @DrawableRes val res = R.drawable.ic_vector
        spiedView.setCompoundDrawablesCompat(res, null, null, null)
        verify(spiedView.drawableCompatManager)!!.setCompoundDrawablesCompat(res, null, null, null)
    }

    @Test
    fun setTopCompoundDrawableCompat() {
        @DrawableRes val res = R.drawable.ic_vector
        spiedView.setCompoundDrawablesCompat(null, res, null, null)
        verify(spiedView.drawableCompatManager)!!.setCompoundDrawablesCompat(null, res, null, null)
    }

    @Test
    fun setEndCompoundDrawableCompat() {
        @DrawableRes val res = R.drawable.ic_vector
        spiedView.setCompoundDrawablesCompat(null, null, res, null)
        verify(spiedView.drawableCompatManager)!!.setCompoundDrawablesCompat(null, null, res, null)
    }

    @Test
    fun setBottomCompoundDrawableCompat() {
        @DrawableRes val res = R.drawable.ic_vector
        spiedView.setCompoundDrawablesCompat(null, null, null, res)
        verify(spiedView.drawableCompatManager)!!.setCompoundDrawablesCompat(null, null, null, res)
    }

    @Test
    fun invalidateOnSetCompoundDrawables() {
        evaluateOnSpy { it.setCompoundDrawables(null, null, null, null) }
        verify(spiedView.drawableCompatManager)!!.invalidateDrawablesTintCompat()
    }

    @Test
    fun invalidateOnSetCompoundDrawablesRelative() {
        evaluateOnSpy { it.setCompoundDrawablesRelative(null, null, null, null) }
        verify(spiedView.drawableCompatManager)!!.invalidateDrawablesTintCompat()
    }

    private inline fun evaluateOnSpy(crossinline block: (DrawableCompatButton) -> Unit) {
        viewTestRule.evaluate {
            block(spiedView)
        }
    }
}