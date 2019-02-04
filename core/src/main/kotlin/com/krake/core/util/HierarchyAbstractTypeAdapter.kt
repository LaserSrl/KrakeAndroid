package com.krake.core.util

import com.google.gson.*
import java.lang.reflect.Type

/**
 * Implementation of [AbstractTypeAdapter] that uses another instance of [Gson] to serialize interfaces.
 * It can serialize an interface searching in the hierarchy tree.
 * It can be registered using [GsonBuilder.registerTypeHierarchyAdapter].
 *
 * @param gson instance of [Gson] used to serialize and deserialize the interface.
 */
class HierarchyAbstractTypeAdapter(private val gson: Gson = Gson()): AbstractTypeAdapter<Any>() {

    override fun serializeObject(obj: Any, type: Type, context: JsonSerializationContext): JsonElement =
            gson.toJsonTree(obj)

    override fun deserializeObject(elem: JsonElement, type: Type, context: JsonDeserializationContext): Any =
            gson.fromJson(elem, type)
}