package com.krake.core.extension

import android.os.Bundle
import com.krake.core.ClassUtils
import io.realm.RealmModel

/**
 * Created by joel on 10/03/17.
 */

/**
 * Extension di [Bundle] che permette di ottenere una classe da esso.
 * La classe viene letta in formato [String] e viene creata successivamente con la reflection.
 *
 * @param key chiave abbinata alla classe.
 * @param classIfNotFound classe da ritornare nel caso in cui non sia stata trovata nessuna classe abbinata alla chiave passata per parametro oppure venga sollevata eccezione.
 * @return classe abbinata alla chiave specificata come parametro. Se non trovata verrà ritornata la classe di default.
 */
fun Bundle.getDataClass(key: String): Class<out RealmModel>? {
    val className = getString(key)
    if (!className.isNullOrEmpty()) {
        @Suppress("UNCHECKED_CAST")
        return ClassUtils.dataClassForName(className)
    }
    return null
}

/**
 * Extension di [Bundle] che permette di inserire una classe in esso.
 * La classe viene scritta in formato [String] usando il suo canonical name.
 *
 * @param key chiave abbinata alla classe.
 * @param cls classe da inserire nel [Bundle].
 */
fun Bundle.putDataClass(key: String, cls: Class<*>?) {
    putString(key, cls?.simpleName)
}