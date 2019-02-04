package com.krake.core.app

import android.app.Activity
import android.content.Intent

/**
 * Interfaccia usata per ricevere le notifiche di [Activity.onActivityResult].
 * Utilizzata come listener da [ResultManager].
 */
interface ActivityResultCallback {

    /**
     * Metodo per accedere al risultato di [Activity.onActivityResult] al di fuori di un'[Activity].
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
}