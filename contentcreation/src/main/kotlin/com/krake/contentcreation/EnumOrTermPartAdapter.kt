package com.krake.contentcreation

import android.content.Context
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.krake.core.media.MediaLoadable
import com.krake.core.media.loader.MediaLoader
import java.util.*


internal class EnumOrTermPartAdapter(context: Context,
                                     private val callingFragment: Fragment,
                                     private val editingEnabled: Boolean,
                                     resource: Int,
                                     private val multipleSelection: Boolean,
                                     allValues: List<SelectableValue>) : ArrayAdapter<SelectableValue>(context, resource, android.R.id.text1, allValues) {

    var allValues: List<SelectableValue> = LinkedList()
        set(value) {
            field = value
            clear()
            addAll(value)
            notifyDataSetChanged()
        }

    init {
        this.allValues = allValues
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view: View
        if (getItemViewType(position) == 0)
            view = super.getView(position, convertView, parent)
        else {
            if (convertView != null)
                view = convertView
            else
                view = LayoutInflater.from(context).inflate(R.layout.single_selection_enum_term_row, parent, false)
        }

        setupView(position, view, parent)

        return view
    }

    override fun getViewTypeCount(): Int {
        if (multipleSelection)
            return 2
        return 1
    }

    override fun getItemViewType(position: Int): Int {
        if (!multipleSelection || isEnabled(position))
            return 0

        return 1
    }

    override fun isEnabled(position: Int): Boolean = getItem(position)?.value != null

    private fun setupView(position: Int, view: View, parentView: ViewGroup) {
        val tw = getItem(position)!!
        val textView = view.findViewById<TextView>(android.R.id.text1)

        textView.text = tw.name

        if (textView is CheckedTextView && parentView is ListView) {
            val flags = parentView.checkedItemPositions
            val selected = flags.get(position)

            textView.isChecked = selected
        }
        textView.isEnabled = editingEnabled
        textView.alpha = if (isEnabled(position)) 1.0f else 0.6f
        val leftPadding = view.paddingRight * (1 + tw.level)
        view.setPadding(leftPadding, view.paddingTop, view.paddingRight, view.paddingBottom)

        val imageView = view.findViewById<ImageView>(android.R.id.icon)

        val mediaPart = tw.mediaPart

        if (mediaPart != null) {
            MediaLoader.Companion.with(callingFragment, imageView as MediaLoadable)
                    .mediaPart(mediaPart)
                    .load()

            imageView.visibility = View.VISIBLE
        } else
            imageView.visibility = View.GONE
    }

    override fun areAllItemsEnabled(): Boolean = false

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)

        setupView(position, view, parent)

        return view
    }
}