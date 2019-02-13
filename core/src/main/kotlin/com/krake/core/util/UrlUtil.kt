package com.krake.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.krake.core.R
import com.krake.core.widget.SnackbarUtils

/**
 * Oggetto di utilità varie sugli URL.
 */
object UrlUtil {

    /**
     * Apre un URL nel Play Store se disponibile, altrimenti visualizza una [Snackbar] di errore.
     *
     * @param context [Context] context per mandare l'[Intent].
     * @param view [View] nella quale verrà mostrata la [Snackbar].
     * @param url URL del Play Store relativo all'app.
     */
    @JvmStatic
    fun openInPlayStore(context: Context, view: View, url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)

        // verifica se ci sono app disponibili in grado di gestire questo Intent
        val infos = context.packageManager.queryIntentActivities(intent, 0)
        if (infos.size > 0) {
            // il Play Store è installato
            context.startActivity(intent)
        } else {
            // il Play Store non è presente sul dispositivo
            SnackbarUtils.createSnackbar(view, R.string.error_play_store_not_available, Snackbar.LENGTH_LONG).show()
        }
    }
}