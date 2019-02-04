package com.krake.contentcreation

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import java.util.*

/**
 * Created by joel on 21/07/17.
 */

fun JsonElement.unwrappedValue(): Any? {
    if (this is JsonPrimitive) {
        if (isNumber)
            return this.asLong

        return this.asString
    }
    return null
}

fun List<Any>.toJsonArray(): JsonArray {
    val jsonArray = JsonArray()

    forEach {
        if (it is Number)
            jsonArray.add(it)
        else if (it is String)
            jsonArray.add(it)
    }

    return jsonArray
}

internal fun JsonArray.unwrapToSelectableValue(): List<SelectableValue> {
    val unwrappedValue = ArrayList<SelectableValue>(size())

    this.unWrapElements(unwrappedValue, 0)

    return unwrappedValue
}

private fun JsonArray.unWrapElements(unwrappedValue: MutableList<SelectableValue>, childLevel: Int) {

    for (index in 0 until size()) {
        val `object` = get(index).asJsonObject
        unwrappedValue.add(EnumTermWrapper(`object`, childLevel))

        val children = `object`.getAsJsonArray(EnumTermWrapper.SELECTABLE_CHILDREN_KEY)

        if (children != null && children.size() > 0) {
            children.unWrapElements(unwrappedValue, childLevel + 1)
        }
    }
}
