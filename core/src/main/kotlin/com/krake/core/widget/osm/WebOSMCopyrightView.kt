package com.krake.core.widget.osm

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView
import android.widget.FrameLayout
import com.krake.core.R
import com.krake.core.view.DescriptionWebViewClient
import com.krake.core.view.MapUtils
import com.krake.core.widget.getActivity

/**
 * Implementation of [OSMCopyrightView] that will show the Open Street Map copyright
 * in a [WebView] that will be inflated when the method [showOSMCopyright] is called.
 * The layout resource of the [WebView] can't be customized programmatically
 * and is: [R.layout.partial_web_osm_copyright_view].
 */
class WebOSMCopyrightView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), OSMCopyrightView {

    private val osmCopyright by lazy { MapUtils.loadOSMCopyrights(context) }
    private val webViewClient by lazy { DescriptionWebViewClient(getActivity() as FragmentActivity) }

    override fun showOSMCopyright() {
        // Check if the WebView was added before.
        if (findViewById<View?>(R.id.osm_copyright_web_view) != null)
            return

        // Inflate the WebView.
        View.inflate(context, R.layout.partial_web_osm_copyright_view, this)

        val webView: WebView = findViewById(R.id.osm_copyright_web_view)
        // Set the custom client.
        webView.webViewClient = webViewClient
        // Load the copyright.
        webView.loadData(osmCopyright, "text/html", "UTF-8")
    }
}