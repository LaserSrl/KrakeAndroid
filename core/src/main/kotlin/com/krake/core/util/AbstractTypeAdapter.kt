package com.krake.core.util

import com.google.gson.*
import java.lang.reflect.Type

/**
 * Custom [Gson] serializer and deserializer used to manage abstract classes or interfaces.
 * The serialization/deserialization process is done through a wrapper that will create a root json with two properties:
 * - The full class name of the serialized instance
 * - The original json object
 *
 * @param [T] type of the object that will be serialized/deserialized
 */
abstract class AbstractTypeAdapter<T : Any> : JsonSerializer<T>, JsonDeserializer<T> {

    companion object {
        private val KEY_TYPE = "t"
        private val KEY_ORIGINAL_JSON = "j"
    }

    override fun serialize(obj: T, interfaceType: Type, context: JsonSerializationContext): JsonElement {
        val jsonObj = JsonObject()
        // add type property to deserialize it after
        jsonObj.addProperty(KEY_TYPE, obj::class.java.name)
        // serialize json content
        jsonObj.add(KEY_ORIGINAL_JSON, serializeObject(obj, interfaceType, context))
        return jsonObj
    }

    override fun deserialize(elem: JsonElement, interfaceType: Type, context: JsonDeserializationContext): T {
        val obj = elem as JsonObject
        // get class name
        val className = obj.get(KEY_TYPE).asString
        // get json content
        val originalJson = obj.get(KEY_ORIGINAL_JSON)
        val type: Type
        try {
            // get type with reflection
            type = Class.forName(className)
        } catch (e: ClassNotFoundException) {
            throw JsonParseException(e)
        }

        return deserializeObject(originalJson, type, context)
    }

    /**
     * Defines how the object must be serialized to json.
     *
     * @param obj the object that needs to be converted to Json.
     * @param type the actual type of the source object.
     * @return a JsonElement corresponding to the specified object.
     */
    protected abstract fun serializeObject(obj: T, type: Type, context: JsonSerializationContext): JsonElement

    /**
     * Defines how the object must be deserialized from json.
     *
     * @param elem The Json data being deserialized.
     * @param type The type of the Object to deserialize to.
     * @return a deserialized object of the specified type [type] which is a subclass of [T].
     */
    protected abstract fun deserializeObject(elem: JsonElement, type: Type, context: JsonDeserializationContext): T
}