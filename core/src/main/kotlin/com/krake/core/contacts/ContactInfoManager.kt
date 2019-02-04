package com.krake.core.contacts

import android.content.Context
import com.google.gson.Gson

/**
 * Created by joel on 30/11/16.
 */


data class ContactInfo constructor(var name: String?, var mail: String = "", var telephone: String?) {

    override fun toString(): String {
        if (name != null)
            return String.format("%s <%s>", name, mail)
        return mail
    }
}

/**
 * Classe per salvare le informazioni principali dell'utente e presentarle in giro per l'App.
 * In modo da illuderlo che sappiamo chi sia.
 */
class ContactInfoManager private constructor() {
    companion object {
        private val PREF_NAME_KEY = "UserContactPref"

        /**
         * Carica le informazioni salvate dell'utente
         * @param context context dell'App o Activity
         *
         * @return i dati dell'utente oppure null se non ancora salvati
         */
        fun readUserInfo(context: Context): ContactInfo? {
            val prefs = context.getSharedPreferences(PREF_NAME_KEY, Context.MODE_PRIVATE)

            val serializedUser = prefs.getString(PREF_NAME_KEY, "")

            if (serializedUser.length > 0)
                return Gson().fromJson(serializedUser, ContactInfo::class.java)

            return null
        }

        /**
         * Aggiorna le informazioni salvate dell'utente
         */
        fun updateUserInfo(context: Context, userInfo: ContactInfo) {
            val oldUser = readUserInfo(context)

            if (oldUser != null) {
                if (userInfo.name == null) userInfo.name = oldUser.name

                if (userInfo.telephone == null) userInfo.telephone = oldUser.telephone

                if (userInfo.mail.length == 0) userInfo.mail = oldUser.mail
            }

            val editor = context.getSharedPreferences(PREF_NAME_KEY, Context.MODE_PRIVATE).edit()

            editor.putString(PREF_NAME_KEY, Gson().toJson(userInfo))
            editor.apply()
        }
    }
}
