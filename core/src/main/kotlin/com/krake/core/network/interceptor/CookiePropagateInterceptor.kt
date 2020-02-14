package com.krake.core.network.interceptor

import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.krake.core.Constants
import com.krake.core.network.OkHttpRemoteClient
import com.krake.core.network.RemoteClient
import com.krake.core.network.cookie
import okhttp3.Cookie
import okhttp3.Interceptor
import okhttp3.Response

class CookiePropagateInterceptor : Interceptor
{
    override fun intercept(chain: Interceptor.Chain): Response
    {
        val request = chain.request()

        val response = chain.proceed(request)

        val requestCookie: Cookie? = request.cookie(Constants.COOKIE_PRIVACY_ANSWERS)

        val responseCookie = response.cookie(Constants.COOKIE_PRIVACY_ANSWERS)

        if (responseCookie != null &&
                (requestCookie == null || responseCookie.value != requestCookie.value))
        {

            val cookie = Cookie.Builder()
                    .domain(responseCookie.domain)
                    .expiresAt(responseCookie.expiresAt)
                    .domain(responseCookie.domain)
                    .name(responseCookie.name)
                    .value(responseCookie.value)
                    .build()

            val cookieJar = (RemoteClient.client(RemoteClient.Mode.DEFAULT) as? OkHttpRemoteClient)
                    ?.client
                    ?.cookieJar as? PersistentCookieJar

            val oldCookies = cookieJar?.loadForRequest(request.url)


            val newCookies = mutableListOf(cookie)

            if (oldCookies != null)
            {
                for (oldCookie in oldCookies)
                {
                    if ((oldCookie.name == Constants.COOKIE_PRIVACY_ANSWERS).not())
                    {
                        newCookies.add(oldCookie)
                    }
                }
            }

            cookieJar?.clear()

            cookieJar
                    ?.saveFromResponse(request.url, newCookies)
        }

        return response
    }

}