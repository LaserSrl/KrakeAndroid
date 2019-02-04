package com.krake.core.widget.compat

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.v7.content.res.AppCompatResources
import android.widget.TextView
import com.krake.core.test.R
import com.krake.test.extension.viewTestRule
import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.isNull
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test


/**
 * Instrumented test class for [ViewDrawableCompatManager].
 */
class ViewDrawableCompatManagerTest {

    @JvmField
    @Rule
    val viewTestRule = viewTestRule<TextView>()

    private lateinit var spiedView: TextView

    @Before
    fun setUp() {
        // Reset the spy before each test.
        spiedView = spy(viewTestRule.view)
    }

//    private fun setNotFinal(methodClass: KClass<*>, methodName: String, vararg params: KClass<*>) {
//        val javaParams = params.map { it.java }.toTypedArray()
//
//        var method: Method?
//        var jClass = methodClass.java
//        val findSuperclass = {
//            val superClass = jClass.superclass
//            val containsSuper = superClass != null
//            if (containsSuper) {
//                jClass = superClass
//            }
//            containsSuper
//        }
//        do {
//            method = try {
//                jClass.getDeclaredMethod(methodName, *javaParams)
//            } catch (e: Exception) {
//                null
//            }
//        } while (findSuperclass() && method == null)
//
//        if (method == null)
//            throw ReflectiveOperationException("The method with name $methodName cannot be found in class ${methodClass.java.name}")
//
//        val field = method::class.java.superclass.getDeclaredField("accessFlags")
//        field.isAccessible = true
//        val modifiers = field.get(method) as Int
//        field.set(method, modifiers and Modifier.FINAL.inv())
//    }

    // TODO: add test on xml attrs.
    @Test
    fun xmlAttributesSetCorrectly() {
//        setNotFinal(TextView::class, "getContext")
//        setNotFinal(Context::class, "obtainStyledAttributes", AttributeSet::class, IntArray::class)
//
//        val tintColor = Color.BLACK
//        val attrs = mock<AttributeSet>()
//        val typedArray = mock<TypedArray> {
//            on(it.getColor(R.styleable.DrawableCompatView_drawableTintCompat, -1)).thenReturn(tintColor)
//        }
//
//        val context = mock<Context>()
//        doReturn(context).whenever(spiedView).context
//        doReturn(typedArray).whenever(context).obtainStyledAttributes(attrs, R.styleable.DrawableCompatView)
//
//        val manager = ViewDrawableCompatManager(spiedView, attrs)
//        assertEquals(manager.getCompoundDrawablesTintCompat(), tintColor)
    }

    @Test
    fun getSetDrawableTintCompat() {
        val manager = ViewDrawableCompatManager(spiedView)
        @ColorInt val color = Color.BLACK
        // Set the compat color.
        manager.setCompoundDrawablesTintCompat(color)
        // Get the current color.
        val actual = manager.getCompoundDrawablesTintCompat()
        assertEquals(color, actual)
    }

    @Test
    fun setAllDrawablesCompatTogether() {
        val manager = ViewDrawableCompatManager(spiedView)
        @DrawableRes val res = R.drawable.ic_vector
        // Resolve the vector drawable.
        val vector = AppCompatResources.getDrawable(spiedView.context, res)

        val predicate = { argument: Drawable? ->
            // If the constant state is equal, the Drawables represent the same resource.
            vector?.constantState == argument?.constantState
        }

        viewTestRule.evaluate {
            // Set all resources together.
            manager.setCompoundDrawablesCompat(res, res, res, res)
        }

        // Verify that the view will set the compat resources with the correct resolved vectors.
        verify(spiedView).setCompoundDrawablesWithIntrinsicBounds(argThat(predicate),
                argThat(predicate),
                argThat(predicate),
                argThat(predicate))
    }

    @Test
    fun setStartDrawableCompat() {
        val manager = ViewDrawableCompatManager(spiedView)
        @DrawableRes val res = R.drawable.ic_vector
        // Resolve the vector drawable.
        val vector = AppCompatResources.getDrawable(spiedView.context, res)

        val predicate = { argument: Drawable? ->
            // If the constant state is equal, the Drawables represent the same resource.
            vector?.constantState == argument?.constantState
        }

        viewTestRule.evaluate {
            // Set only the start resource.
            manager.setCompoundDrawablesCompat(res, null, null, null)
        }

        // Verify that the view will set the start compat resource with the correct resolved vector.
        verify(spiedView).setCompoundDrawablesWithIntrinsicBounds(argThat(predicate),
                isNull(),
                isNull(),
                isNull())
    }

    @Test
    fun setTopDrawableCompat() {
        val manager = ViewDrawableCompatManager(spiedView)
        @DrawableRes val res = R.drawable.ic_vector
        // Resolve the vector drawable.
        val vector = AppCompatResources.getDrawable(spiedView.context, res)

        val predicate = { argument: Drawable? ->
            // If the constant state is equal, the Drawables represent the same resource.
            vector?.constantState == argument?.constantState
        }

        viewTestRule.evaluate {
            // Set only the top resource.
            manager.setCompoundDrawablesCompat(null, res, null, null)
        }

        // Verify that the view will set the top compat resource with the correct resolved vector.
        verify(spiedView).setCompoundDrawablesWithIntrinsicBounds(isNull(),
                argThat(predicate),
                isNull(),
                isNull())
    }

    @Test
    fun setEndDrawableCompat() {
        val manager = ViewDrawableCompatManager(spiedView)
        @DrawableRes val res = R.drawable.ic_vector
        // Resolve the vector drawable.
        val vector = AppCompatResources.getDrawable(spiedView.context, res)

        val predicate = { argument: Drawable? ->
            // If the constant state is equal, the Drawables represent the same resource.
            vector?.constantState == argument?.constantState
        }

        viewTestRule.evaluate {
            // Set only the end resource.
            manager.setCompoundDrawablesCompat(null, null, res, null)
        }

        // Verify that the view will set the end compat resource with the correct resolved vector.
        verify(spiedView).setCompoundDrawablesWithIntrinsicBounds(isNull(),
                isNull(),
                argThat(predicate),
                isNull())
    }

    @Test
    fun setBottomDrawableCompat() {
        val manager = ViewDrawableCompatManager(spiedView)
        @DrawableRes val res = R.drawable.ic_vector
        // Resolve the vector drawable.
        val vector = AppCompatResources.getDrawable(spiedView.context, res)

        val predicate = { argument: Drawable? ->
            // If the constant state is equal, the Drawables represent the same resource.
            vector?.constantState == argument?.constantState
        }

        viewTestRule.evaluate {
            // Set only the bottom resource.
            manager.setCompoundDrawablesCompat(null, null, null, res)
        }

        // Verify that the view will set the bottom compat resource with the correct resolved vector.
        verify(spiedView).setCompoundDrawablesWithIntrinsicBounds(isNull(),
                isNull(),
                isNull(),
                argThat(predicate))
    }
}