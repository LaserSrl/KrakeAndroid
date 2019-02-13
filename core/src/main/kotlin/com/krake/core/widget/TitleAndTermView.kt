package com.krake.core.widget

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.graphics.LightingColorFilter
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.krake.core.R
import com.krake.core.media.loader.TermIconLoader
import com.krake.core.model.ContentItem
import com.krake.core.model.ContentItemWithTermPart

/**
 * Created by joel on 03/10/16.
 */
open class TitleAndTermView : RelativeLayout, ContentItemView, TermIconLoader.OnTermIconLoadListener {
    override val exploreChildToo: Boolean = true

    override lateinit var container: ContentItemViewContainer

    private var titleTextView: TextView? = null
    private var subTitleTextView: TextView? = null
    private var mTermPartImageView: ImageView? = null

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onFinishInflate() {
        super.onFinishInflate()
        titleTextView = findViewById(R.id.content_item_details_non_header_title)
        subTitleTextView = findViewById(R.id.content_item_details_non_header_subtitle)
        mTermPartImageView = findViewById(R.id.termPartIconImageView)

        mTermPartImageView?.colorFilter = LightingColorFilter(Color.BLACK, ContextCompat.getColor(context, R.color.termPartFilterColor))
    }

    override fun show(contentItem: ContentItem, cacheValid: Boolean) {
        titleTextView?.text = contentItem.titlePartTitle

        val subtitle = subtitleFor(contentItem)
        subTitleTextView?.text = subtitle

        if (subtitle?.length ?: 0 > 0)
            subTitleTextView?.visibility = View.VISIBLE
        else
            subTitleTextView?.visibility = View.GONE

        mTermPartImageView?.visibility = View.GONE
        if (contentItem is ContentItemWithTermPart && contentItem.termPart != null) {
            TermIconLoader.loadTerms(context, contentItem.termPart!!, this)
        }
    }

    /**
     * Notifica che il caricamento è stato completato con successo.
     *
     * @param icon icona scaricata da WS o caricata dalle risorse
     */
    override fun onIconLoadCompleted(icon: Drawable, fromWs: Boolean) {
        mTermPartImageView?.setImageDrawable(icon)
        mTermPartImageView?.visibility = View.VISIBLE
    }

    /**
     * Notifica che il caricamento è fallito.
     * Se la [TermPart] non ha un'icona, questo metodo viene richiamato automaticamente.
     */
    override fun onIconLoadFailed(fromWs: Boolean) {
        mTermPartImageView?.setImageDrawable(null)
        mTermPartImageView?.visibility = View.GONE

    }

    open fun subtitleFor(contentItem: ContentItem): String? {
        if (contentItem is ContentItemWithTermPart) {
            return contentItem.termPart?.name
        }
        return null
    }
}