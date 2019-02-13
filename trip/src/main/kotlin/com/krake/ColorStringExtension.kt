package com.krake

import android.graphics.Color
import androidx.annotation.ColorInt

/**
 * Exten
 * Created by joel on 24/03/17.
 */
/**
 * Ottiene un colore da una stringa html.
 * Estensione per rendere
 */
@ColorInt
fun String.colorValue(): Int?
{
    try {
        return Color.parseColor(this)
    } catch (ignored: Exception) {
    }
    return null
}