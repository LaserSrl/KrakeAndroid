package com.krake.core.util

import android.net.Uri
import com.google.gson.*
import java.lang.reflect.Type

/**
 * Created by joel on 02/12/15.
 */
class UriGsonSerializer : JsonSerializer<Uri>, JsonDeserializer<Uri> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Uri? =
            if (json.isJsonNull) null else Uri.parse(json.asString)

    override fun serialize(src: Uri?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement =
            if (src != null) JsonPrimitive(src.toString()) else JsonNull.INSTANCE
}
