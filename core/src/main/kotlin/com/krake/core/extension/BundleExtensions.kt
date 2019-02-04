package com.krake.core.extension

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.krake.core.component.base.ComponentModule

/**
 * Extension di [Bundle] che permette di ottenere una classe da esso.
 * La classe viene letta in formato [String] e viene creata successivamente con la reflection.
 *
 * @param key chiave abbinata alla classe.
 * @param classIfNotFound classe da ritornare nel caso in cui non sia stata trovata nessuna classe abbinata alla chiave passata per parametro oppure venga sollevata eccezione.
 * @return classe abbinata alla chiave specificata come parametro. Se non trovata verrà ritornata la classe di default.
 */
@JvmOverloads
fun <T> Bundle.getClass(key: String?, classIfNotFound: Class<out T>? = null): Class<out T>? {
    val className = getString(key)
    if (className != null) {
        try {
            @Suppress("UNCHECKED_CAST")
            return Class.forName(className) as Class<out T>
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
    }
    return classIfNotFound
}

/**
 * Extension di [Bundle] che permette di inserire una classe in esso.
 * La classe viene scritta in formato [String] usando il suo canonical name.
 *
 * @param key chiave abbinata alla classe.
 * @param cls classe da inserire nel [Bundle].
 */
fun Bundle.putClass(key: String?, cls: Class<*>?) {
    putString(key, cls?.canonicalName)
}

/**
 * Extension di [Bundle] che permette di inserire dei moduli.
 * Se viene specificata una chiave, i valori all'interno dei [Bundle] dei [ComponentModule] verranno scritti direttamente nel [Bundle],
 * altrimenti, per ogni modulo verrà creato un [Bundle] che verrà poi inserito nel [Bundle] principale.
 *
 * @param context [Context] con il quale effettuare la scrittura.
 * @param key chiave abbinata ai moduli da inserire.
 * @param modules vararg di [ComponentModule] che verranno serializzati.
 */
fun Bundle.putModules(context: Context, key: String?, vararg modules: ComponentModule): Bundle {
    val bundle = Bundle()

    modules.map { it.writeContent(context) }
            .forEach { bundle.putAll(it) }

    if (key == null) {
        putAll(bundle)
    } else {
        putBundle(key, bundle)
    }
    return this
}

fun Bundle.putModules(context: Context, vararg modules: ComponentModule): Bundle {
    return putModules(context, key = null, modules = *modules)
}

/**
 * Extension di [Bundle] che permette di ottenere un [Bundle] da esso.
 *
 * @param key chiave abbinata al [Bundle].
 * @param bundleIfNotFound valore ritornato se non viene trovato nessun [Bundle] oppure ne viene trovato uno nullo.
 * @return [Bundle] abbinato alla chiave passata per parametro. Se non viene trovato oppure è nullo, verrà ritornato il [Bundle] di default.
 */
fun Bundle.getBundle(key: String?, bundleIfNotFound: Bundle): Bundle {
    return getBundle(key) ?: bundleIfNotFound
}

/**
 * Verifica dell'uguaglianza di due bundle.
 * Due bundle sono ugualie se hanno le stesse chiavi e se per ogni chiave
 * hanno valori che ritornano true se confrontati con equals().
 * I Bundle interni sono esplorati in modo ricorsivo.
 */
fun Bundle.equalToBundle(bundle: Bundle): Boolean {
    if (this.size() != bundle.size())
        return false

    val setOne = this.keySet()
    var valueOne: Any?

    var valueTwo: Any?

    for (key in setOne) {
        valueOne = this.get(key)
        valueTwo = bundle.get(key)
        if (valueOne is Bundle && valueTwo is Bundle) {
            if (!valueOne.equalToBundle(valueTwo)) {
                return false
            }
        } else if (valueOne is Intent && valueTwo is Intent) {
            if (!valueOne.equalsToIntent(valueTwo)) {
                return false

            }
        } else if (valueOne is Array<*> && valueTwo is Array<*>) {

            if (valueOne.size == valueTwo.size) {
                for (index in valueOne.indices) {
                    if (valueOne[index] != valueTwo[index])
                        return false
                }
            }
        } else if (valueOne == null) {
            if (valueTwo != null || !bundle.containsKey(key))
                return false
        } else if (valueOne != valueTwo)
            return false
    }

    return true
}