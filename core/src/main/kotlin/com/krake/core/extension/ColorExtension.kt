package com.krake.core.extension

import android.graphics.Color
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils

/**
 * Created by joel on 26/04/17.
 */

fun Int.contrastTextColor(): Int {
    val luminance = ColorUtils.calculateLuminance(this)
    return if (luminance > 0.179) Color.BLACK else Color.WHITE
}