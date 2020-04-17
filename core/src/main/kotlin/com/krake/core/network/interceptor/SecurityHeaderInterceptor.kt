package com.krake.core.network.interceptor

import android.content.Context
import android.os.SystemClock
import android.text.TextUtils
import android.util.Base64
import com.krake.core.R
import com.krake.core.messaging.MessagingService
import com.krake.core.net.SntpClient
import okhttp3.Interceptor
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class SecurityHeaderInterceptor(context: Context) : Interceptor
{
    private val deviceUUID: String = MessagingService.getUUID(context)
    private val encryptionKey: String = context.getString(R.string.header_key)
    private val orchardApiKey: String = context.getString(R.string.orchard_api_key)
    private val apiChannel: String = context.getString(R.string.orchard_api_channel)
    private val ntpServers: Array<String> = context.resources.getStringArray(R.array.ntp_servers)

    private var ntpTime: Long = 0
    private var ntpSystemReference: Long = 0
    private var ntpClientTimeLoaded = false

    override fun intercept(chain: Interceptor.Chain): Response
    {
        val request = chain.request()

        val builder = request.newBuilder()
                .addHeader("OutputFormat", "lmnv")
                .addHeader("x-UUID", deviceUUID)

        if (!TextUtils.isEmpty(apiChannel))
            builder.addHeader("ApiChannel", apiChannel)

        try
        {
            if (!TextUtils.isEmpty(orchardApiKey) && !TextUtils.isEmpty(encryptionKey))
            {
                if (!ntpClientTimeLoaded)
                    loadNtpTimeStamp()

                val now = (if (ntpClientTimeLoaded) ntpTime + SystemClock.elapsedRealtime() - ntpSystemReference else Date().time) / 1000

                val sb = StringBuilder(orchardApiKey)
                sb.append(':')
                sb.append(now)
                sb.append(':')
                sb.append(Random().nextLong())
                val bos = ByteArrayOutputStream()
                var index = 0
                while (index < encryptionKey.length)
                {
                    val subPart = encryptionKey.substring(index, index + 2)
                    val integer = Integer.valueOf(subPart, 16)

                    bos.write(integer.toByte().toInt())
                    index += 2
                }

                val key = bos.toByteArray()

                val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
                cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"))
                val iv = cipher.iv
                val cipherResult = cipher.doFinal(sb.toString().toByteArray())
                var result = Base64.encodeToString(cipherResult, Base64.NO_WRAP)
                builder.addHeader("ApiKey", result)
                result = Base64.encodeToString(iv, Base64.NO_WRAP)
                builder.addHeader("AKIV", result)
            }
        }
        catch (ignored: Exception)
        {
            ignored.toString()
        }


        return chain.proceed(builder.build())
    }

    private fun loadNtpTimeStamp()
    {
        val client = SntpClient()

        for (server in ntpServers)
        {
            if (client.requestTime(server, 10000))
            {
                ntpTime = client.ntpTime
                ntpSystemReference = client.ntpTimeReference
                ntpClientTimeLoaded = true
                break
            }
        }
    }
}