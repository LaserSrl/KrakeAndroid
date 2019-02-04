package com.krake.core.widget

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import android.view.View
import com.krake.core.ClassUtils
import com.krake.core.R
import com.krake.core.StringUtils
import com.krake.core.model.ContentItem

/**
 * Classe per mostrare un testo di un content item
 */
open class ContentItemTextView : AppCompatTextView, ContentItemView {
    override lateinit var container: ContentItemViewContainer

    private var methodName = "getBodyPartText"

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        readAttributes(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        readAttributes(context, attrs)
    }

    private fun readAttributes(context: Context, attrs: AttributeSet) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ContentItemTextView, 0, 0)
        val attr = a.getString(R.styleable.ContentItemTextView_methodName)
        if (!attr.isNullOrEmpty()) {
            methodName = StringUtils.methodName(null, attr, null, StringUtils.MethodType.GETTER)
        }
        a.recycle()
    }

    override fun show(contentItem: ContentItem, cacheValid: Boolean) {

        val textValue: String? = ClassUtils.getValueInDestination(methodName, contentItem)?.toString()

        if (!textValue.isNullOrEmpty()) {
            text = textValue
            visibility = View.VISIBLE
        } else {
            visibility = View.GONE
        }
    }
}