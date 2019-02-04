package com.krake.core.widget.content

import android.view.View
import com.krake.core.R
import com.krake.core.model.ContentItem
import com.krake.core.model.ContentItemWithLocation
import com.krake.core.model.MapPart
import com.krake.test.extension.viewTestRule
import com.nhaarman.mockito_kotlin.KStubbing
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import junit.framework.Assert.*
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented test class for [AddressTextView].
 */
class AddressTextViewTest {

    @JvmField
    @Rule
    val viewTestRule = viewTestRule<AddressTextView>()

    private val view get() = viewTestRule.view

    @Test
    fun hiddenWithoutLocationItem() {
        val item = mock<ContentItem>()
        viewTestRule.evaluate { it.show(item, true) }
        // The view must be hidden.
        assertEquals(view.visibility, View.GONE)
        // The view mustn't have listeners on it.
        assertFalse(view.hasOnClickListeners())
    }

    @Test
    fun hiddenWithInvalidMapPart() {
        val locationItem = locationItemMock {
            on(it.isMapValid).thenReturn(false)
        }
        viewTestRule.evaluate { it.show(locationItem, true) }
        // The view must be hidden.
        assertEquals(view.visibility, View.GONE)
        // The view mustn't have listeners on it.
        assertFalse(view.hasOnClickListeners())
    }

    @Test
    fun shownWithValidMapPart() {
        val locationItem = locationItemMock {
            on(it.isMapValid).thenReturn(true)
        }
        viewTestRule.evaluate { it.show(locationItem, true) }
        // The view must be visible.
        assertEquals(view.visibility, View.VISIBLE)
        // The view must have a click listener on it.
        assertTrue(view.hasOnClickListeners())
    }

    @Test
    fun defaultTextWithInvalidAddress() {
        val defaultText = view.context.getString(R.string.navigate_to_point)

        val locationItem = locationItemMock {
            on(it.isMapValid).thenReturn(true)
        }

        val wheneverAddress = { address: String? ->
            whenever(locationItem.mapPart?.locationAddress).thenReturn(address)
        }

        val assertDefaultText = {
            viewTestRule.evaluate { it.show(locationItem, true) }
            // The text must be the default one.
            assertEquals(view.text, defaultText)
        }

        wheneverAddress(null)
        assertDefaultText()

        wheneverAddress("")
        assertDefaultText()

        wheneverAddress(" ")
        assertDefaultText()

        wheneverAddress("\"\"")
        assertDefaultText()

        wheneverAddress("\"\"  ")
        assertDefaultText()
    }

    @Test
    fun addressTextWithValidAddress() {
        val locationItem = locationItemMock {
            on(it.isMapValid).thenReturn(true)
            on(it.locationAddress).thenReturn("Example address")
        }

        viewTestRule.evaluate { it.show(locationItem, true) }
        // The text must be equal to the address.
        assertEquals(view.text, locationItem.mapPart?.locationAddress)
    }

    private inline fun locationItemMock(mapStub: KStubbing<MapPart>.(MapPart) -> Unit): ContentItemWithLocation {
        val mapMock = mock(stubbing = mapStub)
        return mock {
            on(it.mapPart).thenReturn(mapMock)
        }
    }
}