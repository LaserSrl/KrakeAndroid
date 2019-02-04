package com.krake.core.extension

import android.content.res.TypedArray
import android.support.annotation.AnyRes
import android.support.annotation.ColorInt
import android.support.test.filters.SmallTest
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Instrumented test class for [TypedArray] extensions.
 */
@SmallTest
class TypedArrayExtensionsTest {

    private val typedArray = mock<TypedArray>()

    @Test
    fun verifyCorrectResourceIdMethodCalled() {
        typedArray.getResourceId(0)
        verify(typedArray).getResourceId(0, -1)
    }

    @Test
    fun nullResourceIdWithInvalidResult() {
        // Return the default value when the resource id is requested.
        whenever(typedArray.getResourceId(0, -1)).thenReturn(-1)

        @AnyRes val resId = typedArray.getResourceId(0)
        assertNull(resId)
    }

    @Test
    fun validResourceIdWithValidResult() {
        @AnyRes val expectedResult = 5

        // Return the expected value when the resource id is requested.
        whenever(typedArray.getResourceId(0, -1)).thenReturn(expectedResult)

        @AnyRes val resId = typedArray.getResourceId(0)
        assertEquals(expectedResult, resId)
    }

    @Test
    fun verifyCorrectColorMethodCalled() {
        typedArray.getColor(0)
        verify(typedArray).getColor(0, -1)
    }

    @Test
    fun nullColorWithInvalidResult() {
        // Return the default value when the color is requested.
        whenever(typedArray.getColor(0, -1)).thenReturn(-1)

        @ColorInt val color = typedArray.getColor(0)
        assertNull(color)
    }

    @Test
    fun validColorWithValidResult() {
        @ColorInt val expectedResult = 5

        // Return the expected value when the color is requested.
        whenever(typedArray.getColor(0, -1)).thenReturn(expectedResult)

        @ColorInt val color = typedArray.getColor(0)
        assertEquals(expectedResult, color)
    }
}