package com.krake.core.widget

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.krake.core.R
import com.krake.core.getProperty
import com.krake.core.model.ContentItem
import com.krake.core.model.ContentItemWithDescription
import com.krake.core.model.RecordWithAutoroute
import com.krake.core.view.DescriptionWebViewClient

/**
 * Widget to display an html string in a [WebView]
 * By default the webview shows the [ContentItemWithDescription.bodyPartText] of the contentItem
 *
 * You can change the field displayed and the html style using xml attributes.
 * <ul>
 *     <li>backgroundColor: background of the html. Default: R.color.details_background_color</li>
 *     <li>linkColor: color used buy a tags. Default: R.color.details_link_color</li>
 *     <li>htmlTextColor: color of the text. Default: R.color.details_text_color</li>
 *     <li>cssAlignment: text alignment. Must be one of the text-align attributes of HTML. Default: R.string.content_item_detail_text_alignement_css</li>
 *     <li>contentMethod: the name of the var to access the content to display. If null shows the [ContentItemWithDescription.bodyPartText]. Default: null</li>
 * </ul>
 *
 * Created by joel on 30/09/16.
 */

open class ContentItemWebView : WebView, ContentItemView {
    override lateinit var container: ContentItemViewContainer

    private var linkColor: Int = Color.BLACK
    private var textColor: Int = Color.BLACK
    private var htmlBackgroundColor: Int = Color.WHITE
    private lateinit var cssBackground: String
    private var contentMethodName: String? = null

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    {
        readAttributes(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    {
        readAttributes(context, attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
    {
        readAttributes(context, attrs)
    }

    private fun readAttributes(context: Context, attrs: AttributeSet)
    {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ContentItemWebView, 0, 0)

        htmlBackgroundColor = a.getColor(R.styleable.ContentItemWebView_backgroundColor, ContextCompat.getColor(context, R.color.details_background_color))
        linkColor = a.getColor(R.styleable.ContentItemWebView_linkColor, ContextCompat.getColor(context, R.color.details_link_color))
        textColor = a.getColor(R.styleable.ContentItemWebView_htmlTextColor, ContextCompat.getColor(context, R.color.details_text_color))
        cssBackground = a.getString(R.styleable.ContentItemWebView_cssAlignment) ?: getString(R.string.content_item_detail_text_alignement_css)
        contentMethodName = a.getString(R.styleable.ContentItemWebView_contentMethod)

        a.recycle()
    }

    override fun show(contentItem: ContentItem, cacheValid: Boolean) {

        val description = contentToDisplay(contentItem)

        if (!description.isNullOrEmpty()) {
            loadHtmlContent(contentItem, description)
        } else {
            visibility = View.GONE
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setWebViewClient(object : DescriptionWebViewClient(getActivity() as? FragmentActivity) {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        setWebViewClient(null)
    }

    private fun contentToDisplay(contentItem: ContentItem): String?
    {
        if (contentMethodName.isNullOrEmpty())
        {
            return (contentItem as? ContentItemWithDescription)?.bodyPartText
        }
        else
        {
            return contentItem.getProperty(contentMethodName!!) as? String
        }
    }

    protected open fun loadHtmlContent(contentItem: ContentItem, content: String) {
        var url = getString(R.string.orchard_base_service_url)
        if (contentItem is RecordWithAutoroute)
            url += contentItem.autoroutePartDisplayAlias

        val sb = StringBuilder()

        sb.append("<html><head><style type=\"text/css\">")
        sb.append(htmlStyle())
        sb.append("</style></head><body>")
        sb.append(content)
        sb.append("</body></html>")

        loadDataWithBaseURL(url, sb.toString(), "text/html", "UTF-8", null)
    }

    protected fun htmlStyle(): String {

        val linkColor = "#" + Integer.toHexString(linkColor and 0x00FFFFFF)
        val backgroundColor = "#" + Integer.toHexString(htmlBackgroundColor and 0x00FFFFFF)
        val textColor = "#" + Integer.toHexString(textColor and 0x00FFFFFF)

        val sb = StringBuilder("body{background-color:")

        sb.append(backgroundColor)
        sb.append("; text-align:")
        sb.append(cssBackground)
        sb.append("; color:")
        sb.append(textColor)

        sb.append("}\na{color:")
        sb.append(linkColor)
        sb.append("}")

        return sb.toString()
    }
}