package com.krake.core.util

fun String.realmCleanClassName(): String
{
    var className = this

    val index = className.lastIndexOf("_")

    if (index > 0)
    {
        className = className.substring(index + 1, className.length)
    }

    return className.replace("RealmProxy", "", true)
}