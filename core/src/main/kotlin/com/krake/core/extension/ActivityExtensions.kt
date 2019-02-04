package com.krake.core.extension

import android.app.Activity
import android.app.Fragment
import android.content.Context
import com.krake.core.app.KrakeApplication

/**
 * Created by joel on 10/04/17.
 */
val Activity.krakeApplication: KrakeApplication get() = application as KrakeApplication

val Fragment.krakeApplication: KrakeApplication get() = activity.krakeApplication

val android.support.v4.app.Fragment.krakeApplication: KrakeApplication get() =
    activity?.krakeApplication ?: throw IllegalArgumentException("The activity mustn't be null.")

val Context.krakeAppContext: KrakeApplication get() = applicationContext as KrakeApplication