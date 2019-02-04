package com.krake.core.util

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import java.lang.reflect.Type

/**
 * Implementation of [AbstractTypeAdapter] that uses the most optimized version to serialize/deserialize objects.
 * It can serialize an interface only if it's implemented directly in the subclasses.
 * It can be registered using [GsonBuilder.registerTypeAdapter].
 */
class PlainAbstractTypeAdapter: AbstractTypeAdapter<Any>() {

    override fun serializeObject(obj: Any, type: Type, context: JsonSerializationContext): JsonElement =
            context.serialize(obj)

    override fun deserializeObject(elem: JsonElement, type: Type, context: JsonDeserializationContext): Any =
            context.deserialize(elem, type)
}