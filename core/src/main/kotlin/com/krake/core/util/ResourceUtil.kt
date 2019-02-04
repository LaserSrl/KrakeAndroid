package com.krake.core.util

import android.content.Context

/**
 * Usata per accedere alle risorse tramite reflection.
 */
object ResourceUtil {
    const val DRAWABLE = "drawable"
    const val LAYOUT = "layout"
    const val COLOR = "color"
    const val STRING = "string"

    /**
     * Accede ad una risorsa tramite reflection.
     *
     * @param context [Context] corrente.
     * @param defType tipo di risorsa.
     * @param name nome della risorsa.
     * @return id della risorsa.
     */
    fun resourceForName(context: Context, defType: String, name: String): Int {
        return context.resources.getIdentifier(name, defType, context.applicationContext.packageName)
    }
}