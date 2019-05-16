package com.krake.core.fetcher.content

import android.os.Trace
import android.util.Log
import android.webkit.WebView

/**
 * Implementation of [FetchableContent] that will pre-fetch the [WebView]'s packages.
 */
class WebViewFetchableContent : FetchableContent {

    companion object {
        private val TAG = WebViewFetchableContent::class.java.simpleName
    }

    override fun fetch() {
        Trace.beginSection("preFetchWebView")
        Log.d(TAG, "pre-fetching the WebView packages...")
        try {
            // Workaround used to initialize the static web factory once.
            WebView.setWebContentsDebuggingEnabled(false)
            WebView.enableSlowWholeDocumentDraw()
        } catch (ignored: Exception) {
            // The exception is ignored because it will be thrown every time.
        }
        Log.d(TAG, "pre-fetching finished.")
        Trace.endSection()
    }
}