package com.krake.core.util

import com.google.gson.*
import io.realm.Realm
import io.realm.RealmModel
import io.realm.kotlin.isManaged
import io.realm.kotlin.isValid
import java.lang.reflect.Type

/**
 * Implementation of [AbstractTypeAdapter] that uses another instance of [Gson] to serialize interfaces.
 * It can serialize an interface searching in the hierarchy tree.
 * It can be registered using [GsonBuilder.registerTypeHierarchyAdapter].
 * It supports [Realm] models.
 *
 * @param gson instance of [Gson] used to serialize and deserialize the interface.
 */
class RealmHierarchyAbstractTypeAdapter(private val gson: Gson = Gson()) : AbstractTypeAdapter<Any>() {

    private val realm by lazy { Realm.getDefaultInstance() }

    override fun serializeObject(obj: Any, type: Type, context: JsonSerializationContext): JsonElement {
        val model = if (obj is RealmModel && obj.isManaged() && obj.isValid()) {
            realm.copyFromRealm(obj)
        } else obj
        return gson.toJsonTree(model)
    }

    override fun deserializeObject(elem: JsonElement, type: Type, context: JsonDeserializationContext): Any {
        return gson.fromJson(elem, type)
    }
}