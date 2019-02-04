package com.krake.core

/**
 * Created by joel on 28/02/17.
 */

fun Any.getProperty(name: String): Any? {
    return ClassUtils.getValueForKeyPath(name, this)
}