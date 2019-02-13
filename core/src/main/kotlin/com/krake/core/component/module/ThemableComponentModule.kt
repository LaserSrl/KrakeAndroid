package com.krake.core.component.module

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StyleRes
import com.krake.core.R
import com.krake.core.app.ThemableNavigationActivity
import com.krake.core.component.base.ComponentModule

/**
 * Modulo che viene utilizzato per specificare degli attributi della UI.
 * Utilizzato principalmente da [ThemableNavigationActivity] e dalle [Activity] che la estendono.
 */
class ThemableComponentModule : ComponentModule {
    companion object {
        private const val ARG_SHOW_FLOATING = "argShowFloating"
        private const val ARG_THEME = "argTheme"
        private const val ARG_TITLE = "argTitle"
        private const val ARG_UP_INTENT = "argUpIntent"
    }

    var showFloating: Boolean
        private set

    @StyleRes
    var theme: Int
        private set

    var title: String?
        private set

    var upIntent: Intent?
        private set

    init {
        showFloating = false
        theme = 0
        title = null
        upIntent = null
    }

    /**
     * Specifica se l'[Activity] deve essere avviata in modalità floating.
     * DEFAULT: false -> nella [ThemableNavigationActivity] viene presa dall'attr [R.styleable.BaseTheme_isFloatingWindow]
     *
     * @param showFloating true se l'[Activity] deve essere avviata in modalità floating.
     */
    fun showFloating(showFloating: Boolean) = apply { this.showFloating = showFloating }

    /**
     * Specifica il tema dell'[Activity] che verrà settato appena verrà aperta.
     * DEFAULT: 0 -> nella [ThemableNavigationActivity] viene preso dal manifest.
     *
     * @param theme tema che verrà settato nell'[Activity].
     */
    fun theme(@StyleRes theme: Int) = apply { this.theme = theme }

    /**
     * Specifica il titolo della sezione. Se non null, sostituirà il titolo inserito nel manifest.
     * DEFAULT: null
     *
     * @param title titolo della sezione.
     */
    fun title(title: String?) = apply { this.title = title }

    /**
     * Specifica l'[Intent] da lanciare quando viene premuta l'up arrow.
     * Se non presente, l'up arrow non verrà mostrata.
     * DEFAULT: null
     *
     * @param upIntent [Intent] relativo all'azione da lanciare quando l'up arrow viene premuta.
     */
    fun upIntent(upIntent: Intent?) = apply { this.upIntent = upIntent }

    /**
     * Legge il contenuto di un [Bundle] e modifica le sue proprietà.
     *
     * @param context il [Context] utilizzato per leggere il [Bundle].
     * @param bundle container dal quale vengono letti i vari arguments.
     */
    override fun readContent(context: Context, bundle: Bundle) {
        showFloating = bundle.getBoolean(ARG_SHOW_FLOATING, showFloating)
        theme = bundle.getInt(ARG_THEME, theme)
        upIntent = bundle.getParcelable(ARG_UP_INTENT)
        title = bundle.getString(ARG_TITLE)
    }

    /**
     * Scrive le proprietà di un modulo su un [Bundle].
     *
     * @param context il [Context] utilizzato per creare il [Bundle] e scrivere su di esso.
     * @return container che contiene le proprietà del modulo corrente.
     */
    override fun writeContent(context: Context): Bundle {
        val bundle = Bundle()
        bundle.putBoolean(ARG_SHOW_FLOATING, showFloating)
        bundle.putInt(ARG_THEME, theme)
        bundle.putString(ARG_TITLE, title)
        bundle.putParcelable(ARG_UP_INTENT, upIntent)
        return bundle
    }
}