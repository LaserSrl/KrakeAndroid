package com.krake.core.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.krake.core.R


open class ShadowCollapsingToolbarLayout @JvmOverloads constructor(context: Context,
                                                                   attrs: AttributeSet? = null,
                                                                   defStyleAttr: Int = 0) : CollapsingToolbarLayout(context, attrs, defStyleAttr) {

    private val toolbar by lazy { this.findViewById<Toolbar>(R.id.toolbar_actionbar) }
    private val showShadow: Boolean

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ShadowCollapsingToolbarLayout, 0, 0)
        showShadow = a.getBoolean(R.styleable.ShadowCollapsingToolbarLayout_show_shadow, false)
        a.recycle()
    }

    override fun setScrimsShown(shown: Boolean, animate: Boolean) {
        super.setScrimsShown(shown, animate)

        if(showShadow) {
            if (shown)  //remove shadow
                toolbar?.background = null
            else        //add shadow
                toolbar?.background = ContextCompat.getDrawable(context, R.drawable.toolbar_detail_shadow)
        }
    }


}