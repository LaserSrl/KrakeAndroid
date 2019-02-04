package com.krake.cards

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.AppCompatAutoCompleteTextView
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import com.krake.core.contacts.ContactInfo

/**
 * Created by joel on 29/05/14.
 */
class CardsAutoCompleteTextView : AppCompatAutoCompleteTextView, AdapterView.OnItemClickListener {
    private var listener: CardsTextViewPickListener? = null
    private var clearDrawable: Drawable? = null

    var infos: ContactInfo? = null
        set(value) {
            field = value
            text.clear()
            if (value != null) {
                setText(value.toString())
            }
        }


    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    override fun onItemClick(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
        infos = adapter.getItem(i) as ContactInfo
    }

    override fun setOnItemClickListener(l: AdapterView.OnItemClickListener) {
        super.setOnItemClickListener(this)
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (focused) {
            performFiltering(text, 0)
        }
    }

    private fun init(context: Context) {
        onItemClickListener = this
        clearDrawable = ContextCompat.getDrawable(context, R.drawable.ic_action_add_person)
        DrawableCompat.setTint(clearDrawable!!, ContextCompat.getColor(getContext(), R.color.colorPrimary))
        clearDrawable!!.setBounds(0, 0, clearDrawable!!.intrinsicWidth, clearDrawable!!.intrinsicHeight)
        this.setCompoundDrawables(this.compoundDrawables[0], this.compoundDrawables[1], clearDrawable, this.compoundDrawables[3])

        this.setOnTouchListener(View.OnTouchListener { _, event ->
            val et = this@CardsAutoCompleteTextView

            if (et.compoundDrawables[2] == null)
                return@OnTouchListener false

            if (event.action != MotionEvent.ACTION_UP)
                return@OnTouchListener false

            if (event.x > et.width - et.paddingRight - clearDrawable!!.intrinsicWidth) {
                if (listener != null)
                    listener!!.pickContact(et)
            }
            false
        })
    }

    fun setListener(listener: CardsTextViewPickListener) {
        this.listener = listener
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (infos == null)
            return super.onKeyDown(keyCode, event)
        else {
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                infos = null
                text.clear()
            }
            return true
        }
    }

    interface CardsTextViewPickListener {
        fun pickContact(textView: CardsAutoCompleteTextView)
    }
}