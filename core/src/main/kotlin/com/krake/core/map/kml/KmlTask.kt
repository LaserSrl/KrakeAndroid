package com.krake.core.map.kml

import android.content.Context
import android.os.Build
import android.os.Environment
import com.krake.core.R
import com.krake.core.io.FileUtils
import com.krake.core.network.RemoteClient
import com.krake.core.network.RemoteRequest
import com.krake.core.thread.AsyncTask
import com.krake.core.thread.async
import java.io.File
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

/**
 * Created by antoniolig on 22/09/2017.
 */
class KmlTask(private var listener: Listener?) {

    private var task: AsyncTask<File>? = null

    fun load(context: Context, kmlUrl: String) {
        cancel()
        val realUrl = com.krake.core.media.loader.MediaLoader.getAbsoluteMediaURL(context, kmlUrl)

        if (realUrl != null) {
            task = async {
                val cacheFile = cacheFile(context, realUrl)

                if (isCacheFileValid(context, cacheFile)) {
                    return@async cacheFile
                } else {
                    val request = RemoteRequest(realUrl).setMethod(RemoteRequest.Method.GET)

                    val kmlStream = RemoteClient.client(RemoteClient.Mode.DEFAULT)
                            .execute(request)
                            .inputStream()
                    if (kmlStream != null)
                        FileUtils.copyToFile(kmlStream, cacheFile)
                    return@async cacheFile
                }
            }.completed { kmlFile ->
                listener?.onKmlLoadCompleted(kmlFile)
            }.build()

            task?.load()
        }
    }

    fun cancel() {
        task?.cancel()
    }

    fun release() {
        cancel()
        listener = null
    }

    private fun cacheFile(context: Context, kmlURL: String): File {
        var cacheDir = context.externalCacheDir

        if (cacheDir == null || !isMediaMounted(cacheDir)) {
            cacheDir = context.cacheDir
        }

        return File(cacheDir, digestOfURL(kmlURL))
    }

    private fun isCacheFileValid(context: Context, cacheFile: File): Boolean {

        if (cacheFile.exists()) {
            val lastModified = cacheFile.lastModified()

            val lastValidDate = Date(Date().time - 1000 * context.resources.getInteger(R.integer.kml_cache_validity))
            val dateModified = Date(lastModified)

            return lastValidDate < dateModified
        }


        return false
    }

    private fun isMediaMounted(file: File): Boolean {
        val state: String

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            state = Environment.getExternalStorageState(file)
        } else {
            state = Environment.getExternalStorageState()
        }

        return state != Environment.MEDIA_MOUNTED
    }

    private fun digestOfURL(text: String): String {
        val md: MessageDigest
        try {
            md = MessageDigest.getInstance("SHA-1")
            md.update(text.toByteArray(charset("iso-8859-1")), 0, text.length)
            val sha1hash = md.digest()

            val buf = StringBuilder()
            for (b in sha1hash) {
                var halfByte = b.toInt().ushr(4) and 0x0F
                var twoHalf = 0
                do {
                    buf.append(if (halfByte in 0..9) ('0' + halfByte) else ('a' + (halfByte - 10)))
                    halfByte = b.toInt() and 0x0F
                } while (twoHalf++ < 1)
            }

            return buf.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()

        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        return text.replace("/".toRegex(), "")
    }

    interface Listener {

        fun onKmlLoadCompleted(kml: File)
    }
}