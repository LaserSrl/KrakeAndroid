package com.krake.core.extension

import java.util.*

/**
 * Created by joel on 27/04/17.
 */

fun Date.isInSameDay(other: Date): Boolean {
    val calendar = Calendar.getInstance()
    val otherCalendar = Calendar.getInstance()

    calendar.time = this
    otherCalendar.time = other

    return calendar.get(Calendar.DAY_OF_YEAR) == otherCalendar.get(Calendar.DAY_OF_YEAR) &&
            calendar.get(Calendar.YEAR) == otherCalendar.get(Calendar.YEAR)
}