package com.krake.trip

import android.location.Address
import android.text.TextUtils

/**
 * Formato condiviso di un Address
 * Created by joel on 22/11/16.
 */

fun Address.format(): String {

    val sb = StringBuilder()

    if (!TextUtils.isEmpty(featureName)) {
        sb.append(featureName)
    }

    if (maxAddressLineIndex > 0) {
        if (sb.length > 0)
            sb.append(", ")

        for (i in 0..maxAddressLineIndex - 1) {
            if (i != 0)
                sb.append(" ")
            sb.append(getAddressLine(i))
        }
    }

    if (!TextUtils.isEmpty(subAdminArea)) {
        if (sb.length > 0)
            sb.append(", ")
        sb.append(subAdminArea)
    }
    return sb.toString()
}
