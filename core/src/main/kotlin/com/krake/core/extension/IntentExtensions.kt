package com.krake.core.extension

import android.content.Intent

/**
 * Created by joel on 10/04/17.
 */
/**
 * Verifica dell'uguaglianza di due Intent.
 * Due Intent sono uguali se hanno lo stesso component, le stesse chiavi e se per ogni chiave
 * hanno valori che ritornano true se confrontati con equals().
 * I Bundle interni sono esplorati in modo ricorsivo.
 * @param intent
 * @return true se bundle contengono dati uguali
 */

fun Intent.equalsToIntent(intent: Intent): Boolean {

    if (this.component?.compareTo(intent.component) == 0) {
        val oneExtras = this.extras

        val twoExtras = intent.extras
        if (oneExtras != null && twoExtras != null)
            return oneExtras.equalToBundle(twoExtras)
        else if ((oneExtras == null || oneExtras.size() == 0) && (twoExtras == null || twoExtras.size() == 0))
            return true
    }
    return false
}