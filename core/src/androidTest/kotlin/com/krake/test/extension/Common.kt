package com.krake.test.extension

import android.view.View
import com.krake.test.lock.CountDownLatchLock
import com.krake.test.lock.Lock
import com.krake.test.view.ViewTestRule

/**
 * Helper function used to get the default implementation of a [Lock] for unit tests.
 *
 * @return default implementation of [Lock].
 */
fun lock(): Lock = CountDownLatchLock()

/**
 * Helper function used to create a new [ViewTestRule] with a type parameter.
 *
 * @param V the type of the [View] used in the rule.
 * @return the [ViewTestRule] related to the class of [V].
 */
inline fun <reified V : View> viewTestRule() = ViewTestRule<V>(V::class.java)