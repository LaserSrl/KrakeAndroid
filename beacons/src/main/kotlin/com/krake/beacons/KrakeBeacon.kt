package com.krake.beacons

import com.krake.core.model.ContentItem
import com.krake.core.model.FieldExternal

interface KrakeBeacon : ContentItem {

    val url: FieldExternal?

    val uUIDValue: String

    val majorValue: Long?

    val minorValue: Long?
}
