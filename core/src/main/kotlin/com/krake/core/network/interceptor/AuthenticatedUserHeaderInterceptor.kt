package com.krake.core.network.interceptor

import android.content.Context
import android.util.Base64
import com.krake.core.R
import com.krake.core.network.cookie
import okhttp3.Cookie
import okhttp3.Interceptor
import okhttp3.Response
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class AuthenticatedUserHeaderInterceptor(context: Context) : Interceptor
{
    private val encryptionKey: String

    init
    {
        encryptionKey = context.getString(R.string.header_key)
    }

    override fun intercept(chain: Interceptor.Chain?): Response
    {
        val request = chain!!.request()

        val builder = request.newBuilder()

        val cookie: Cookie? = request.cookie(".ASPXAUTH")

        if (cookie != null && request.header("X-XSRF-TOKEN").isNullOrBlank())
        {
            builder.addHeader("X-XSRF-TOKEN", generateHMAC(cookie.value(), encryptionKey))
        }

        return chain.proceed(builder.build())
    }


    private fun generateHMAC(data: String, key: String): String
    {
        val mac: Mac
        var result = ""
        try
        {
            val bytesKey = key.toByteArray()
            val bytesData = data.toByteArray()
            val secretKey = SecretKeySpec(bytesKey, "HmacSHA512")
            mac = Mac.getInstance("HmacSHA512")
            mac.init(secretKey)
            val macData = mac.doFinal(bytesData)
            result = Base64.encodeToString(macData, Base64.NO_WRAP)
        }
        catch (e: Exception)
        {
            e.toString()
        }

        return result
    }

}