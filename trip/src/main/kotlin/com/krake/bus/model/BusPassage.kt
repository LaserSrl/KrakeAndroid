package com.krake.bus.model

import com.krake.core.model.ContentItem
import java.util.*

/**
 * Created by antoniolig on 27/04/2017.
 */
class BusPassage : ContentItem {
    var lineNumber: String? = null
    var destination: String? = null
    var passage: Date? = null
    var pattern: BusPattern? = null
    override val titlePartTitle: String? = null
}