package com.krake.contentcreation

import com.google.gson.JsonObject
import com.krake.core.media.MediaPartURLWrapper
import com.krake.core.model.*

/**
 * Created by joel on 21/07/17.
 */

interface SelectableValue {
    val mediaPart: MediaPart?

    val name: String

    val value: Any?

    val level: Int
}


class EnumTermWrapper(private val originalObject: JsonObject, override val level: Int) : SelectableValue {

    override val mediaPart: MediaPart?

    init {

        mediaPart = if (originalObject.has(SELECTABLE_IMAGE_KEY) && !originalObject.get(SELECTABLE_IMAGE_KEY).isJsonNull)
        {
            MediaPartURLWrapper(originalObject.get(SELECTABLE_IMAGE_KEY).toString())
        } else
            null
    }

    override val name: String
        get() = originalObject.get(SELECTABLE_NAME_KEY).asString

    override val value: Any?
        get() {
            val primitive = originalObject.getAsJsonPrimitive(SELECTABLE_VALUE_KEY)

            if (primitive != null) {
                if (primitive.isNumber)
                    return primitive.asLong

                return primitive.asString
            }
            return null
        }

    companion object {
        const val SELECTABLE_NAME_KEY = "Name"
        const val SELECTABLE_VALUE_KEY = "Value"
        const val SELECTABLE_CHILDREN_KEY = "Children"

        const val SELECTABLE_IMAGE_KEY = "ImageId"
    }
}

class SelectableContentItem(private val item: ContentItem) : SelectableValue {
    override val mediaPart: MediaPart?
        get() = (item as? ContentItemWithGallery)?.firstPhoto
    override val name: String
        get() = item.titlePartTitle ?: ""
    override val value: Any?
        get() {
            if (item is RecordWithIdentifier)
                return item.identifier
            else if (item is RecordWithStringIdentifier)
                return item.stringIdentifier
            return null
        }

    override val level: Int
        get() = 0

}
