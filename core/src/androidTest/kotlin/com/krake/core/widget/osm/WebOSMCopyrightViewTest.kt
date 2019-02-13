package com.krake.core.widget.osm

import android.webkit.WebView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.SmallTest
import com.krake.core.R
import com.krake.test.extension.viewTestRule
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented test class for [WebOSMCopyrightView].
 */
@SmallTest
class WebOSMCopyrightViewTest {

    @JvmField
    @Rule
    val viewTestRule = viewTestRule<WebOSMCopyrightView>()

    private val view get() = viewTestRule.view

    @Test
    fun emptyContainerAtFirstAccess() {
        // The container must be empty.
        assertEquals(0, view.childCount)
    }

    @Test
    fun containerPopulatedAfterCopyrightHasBeenShown() {
        viewTestRule.evaluate { it.showOSMCopyright() }
        // The container must contains a view.
        assertEquals(1, view.childCount)
    }

    @Test
    fun ignoreSubsequentShowCalls() {
        viewTestRule.evaluate { it.showOSMCopyright() }
        val expectedCount = 1
        // The container must contains a view.
        assertEquals(expectedCount, view.childCount)
        viewTestRule.evaluate { it.showOSMCopyright() }
        // The number of views mustn't increase.
        assertEquals(expectedCount, view.childCount)
    }

    @Test
    fun inflatedWebViewSuccessfully() {
        // Check that the view doesn't exist before.
        onView(withId(R.id.osm_copyright_view))
                .check(doesNotExist())

        viewTestRule.evaluate { it.showOSMCopyright() }

        // The view must be displayed now.
        onView(withId(R.id.osm_copyright_web_view))
                .check(matches(instanceOf(WebView::class.java)))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }
}