package com.krake.core.model


/**
 * Created by joel on 01/03/17.
 */

interface ActivityPart {
    val allDay: Boolean?
    val dateTimeEnd: java.util.Date?
    val dateTimeStart: java.util.Date?
}