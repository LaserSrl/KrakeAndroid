package com.krake.core.widget

import android.os.Handler
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.view.View
import com.krake.core.R

/**
 * Classe per creare snackbar con un particolare colore del bottone di action.
 */
object SnackbarUtils
{

    /**
     * Crea e mostra una snackbar informativa. Quando la snackbar si chiude viene inviato un messaggio all'
     * handler
     *
     * @param view               destinazione della snackbar
     * @param snackbarText       testo
     * @param handler            cui spedire il messagio da specire
     * @param handlerMessageCode codice del messaggio da inviare quando si chiude lo snackbar
     */
    @JvmStatic
    fun showCloseSnackbar(view: View, @StringRes snackbarText: Int, handler: Handler, handlerMessageCode: Int)
    {
        val snackbar = createSnackbar(view, snackbarText, view.context.resources.getInteger(R.integer.close_snackbar_duration))
        val duration = snackbar.duration

        snackbar.setAction(android.R.string.ok) {
            handler.removeMessages(handlerMessageCode)
            handler.sendEmptyMessage(handlerMessageCode)
        }


        snackbar.show()

        handler.sendEmptyMessageDelayed(handlerMessageCode, duration.toLong())
    }

    @JvmStatic
    fun showCloseSnackbar(view: View, @StringRes snackbarText: Int, handler: Handler, runnable: Runnable)
    {
        val snackbar = createSnackbar(view, snackbarText, view.context.resources.getInteger(R.integer.close_snackbar_duration))
        val duration = snackbar.duration

        handler.postDelayed(runnable, duration.toLong())

        snackbar.show()
    }

    /**
     * Crea una snackbar in cui viene impostato il colore del testo delle action come R.color.snackbar_text_color
     *
     * @param view         destinazione della snackbar
     * @param snackbarText testo
     * @param length       durata delo snack
     * @return la snackart
     */
    @JvmStatic
    fun createSnackbar(view: View, @StringRes snackbarText: Int, length: Int): Snackbar
    {
        return createSnackbar(view, view.context.getString(snackbarText), length)
    }

    /**
     * Crea una snackbar in cui viene impostato il colore del testo delle action come R.color.snackbar_text_color
     *
     * @param view   destinazione della snackbar
     * @param text   testo
     * @param length durata delo snack
     * @return la snackart
     */
    @JvmStatic
    fun createSnackbar(view: View, text: String, length: Int): Snackbar
    {
        val snackbar = Snackbar.make(view, text, length)
        snackbar.setActionTextColor(ContextCompat.getColor(view.context, R.color.snackbar_text_color))

        return snackbar
    }
}
