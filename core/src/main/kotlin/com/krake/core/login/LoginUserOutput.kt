package com.krake.core.login

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject

/**
 * Created by joel on 17/10/17.
 */

class LoginUserOutput {
    companion object {
        private val gson: Gson by lazy { Gson() }
        @JvmStatic
        fun loadLoginOutput(string: String?): LoginUserOutput? {
            if (string != null) {
                return gson.fromJson(string, LoginUserOutput::class.java)
            }
            return null
        }
    }

    val roles: ArrayList<String> = ArrayList()
    val completeOutput: JsonObject
    val userIdentifier: String?
    val contactIdentifier: String?

    constructor(orchardData: JsonObject) {
        completeOutput = orchardData

        userIdentifier = orchardData.get("UserId")?.asString

        contactIdentifier = orchardData.get("ContactId")?.asString

        val jsonRoles = orchardData.getAsJsonArray("Roles")

        if (jsonRoles != null) {
            for (index in 0 until jsonRoles.size())
                roles.add(jsonRoles.get(index).asString)
        }
    }

    val registeredServices: JsonArray? get() = completeOutput.getAsJsonArray("RegisteredServices")
}