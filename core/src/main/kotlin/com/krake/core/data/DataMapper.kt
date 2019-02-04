package com.krake.core.data

import com.google.gson.JsonObject
import com.krake.core.Configurations
import com.krake.core.OrchardError

interface DataMapper
{
    val configurations: Configurations

    @Throws(OrchardError::class)
    fun parseContentFromResult(result: JsonObject,
                               e: Exception?,
                               requestedPrivacy: Boolean,
                               parameters: Map<String, String>?): String
}