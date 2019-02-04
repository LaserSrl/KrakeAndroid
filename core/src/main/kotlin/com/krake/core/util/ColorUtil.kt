package com.krake.core.util

import android.content.Context
import android.support.annotation.ColorInt
import android.support.annotation.StyleableRes
import android.util.TypedValue
import com.krake.core.R

/**
 * Classe di utilità che gestisce i colori.
 */
object ColorUtil {

    /**
     * Permette di ottenere il color accent dal tema.
     *
     * @param context [Context] usato per ottenere il tema.
     * @return color accent risolto.
     */
    @ColorInt fun accentColor(context: Context): Int {
        return colorFromAttr(context, R.attr.colorAccent)
    }

    /**
     * Permette di ottenere il color primary dal tema.
     *
     * @param context [Context] usato per ottenere il tema.
     * @return color primary risolto.
     */
    @ColorInt fun primaryColor(context: Context): Int {
        return colorFromAttr(context, R.attr.colorPrimary)
    }

    /**
     * Permette di ottenere un colore da un attributo del tema.
     *
     * @param context [Context] usato per ottenere il tema.
     * @param colorAttr attributo del tema contenente una reference al colore.
     * @return colore risolto.
     */
    @ColorInt fun colorFromAttr(context: Context, @StyleableRes colorAttr: Int): Int {
        val theme = context.theme
        val value = TypedValue()
        theme.resolveAttribute(colorAttr, value, true)
        return value.data
    }
}