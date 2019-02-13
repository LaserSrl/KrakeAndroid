package com.krake.test.view

import android.content.Intent
import android.view.View

/**
 * An [ActivityTestRule] used to test a custom [View].
 * The [View] will be created through reflection in a [ViewActivity].
 *
 * @param viewClass the class of the [View] that must be created.
 */
// TODO: add support for AttributeSet
class ViewTestRule<out V : View>(private val viewClass: Class<out View>) : ActivityTestRule<ViewActivity>(ViewActivity::class.java) {

    /**
     * Used to get the created [View] by the [ViewActivity].
     */
    val view: V
        @Suppress("UNCHECKED_CAST")
        get() = activity.view as V

    override fun getActivityIntent(): Intent = ViewActivity.createTestIntent(viewClass)

    /**
     * Permits to operates on the UI thread when calling [View]'s method.
     *
     * @param block the closure that must run on the UI thread.
     */
    fun evaluate(block: (V) -> Unit) {
        runOnUiThread {
            block(view)
        }
    }
}