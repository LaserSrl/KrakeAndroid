package com.krake.core.network

import okhttp3.Cookie
import okhttp3.Request
import okhttp3.Response

fun Request.cookie(name: String): Cookie?
{
    val serialCookie = this.header("Cookie")

    var cookie: Cookie? = null

    if (serialCookie != null)
    {
        if (serialCookie.contains("; "))
        {
            val splitSerialCookies = serialCookie.split("; ")

            splitSerialCookies.forEach {
                val splitCookie = Cookie.parse(this.url, it)
                if (splitCookie?.name == name)
                {
                    cookie = splitCookie
                }
            }

        }
        else
        {
            cookie = Cookie.parse(this.url, serialCookie)
            if (cookie?.name != name)
                cookie = null
        }
    }
    else
    {
        val cookies = Cookie.parseAll(this.url, this.headers)
        cookie = cookies.find { it.name == name }
    }

    return cookie
}

fun Response.cookie(name: String): Cookie?
{
    val serialCookie = this.header("Cookie")

    var cookie: Cookie?

    if (serialCookie != null)
    {
        cookie = Cookie.parse(this.request.url, serialCookie)
        if (cookie?.name != name)
            cookie = null
    }
    else
    {
        val cookies = Cookie.parseAll(this.request.url, this.headers)
        cookie = cookies.find { it.name == name }
    }

    return cookie
}