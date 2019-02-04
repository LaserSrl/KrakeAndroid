package com.krake.core.extension

import android.graphics.Color
import android.os.Build

/**
 * Created by joel on 26/04/17.
 */

fun Int.contrastTextColor(): Int {
    val luminance: Double
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        luminance = Color.luminance(this).toDouble()
    } else {

        val uicolors: List<Double> = arrayOf(Color.red(this), Color.green(this), Color.blue(this)).map {
            if (it <= 0.03928) it / 12.92
            else Math.pow((it + 0.055) / 1.055, 2.4)
        }

        luminance = 0.2126 * uicolors[0] + 0.7152 * uicolors[1] + 0.0722 * uicolors[2]
    }

    if (luminance > 0.179)
        return Color.BLACK
    else
        return Color.WHITE
}