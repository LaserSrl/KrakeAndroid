package com.krake.events.model

import com.krake.core.model.ActivityPart
import com.krake.core.model.ContentItem

/**
 * Created by joel on 08/03/17.
 */
interface Event : ContentItem {
    val activityPart: ActivityPart?
}
