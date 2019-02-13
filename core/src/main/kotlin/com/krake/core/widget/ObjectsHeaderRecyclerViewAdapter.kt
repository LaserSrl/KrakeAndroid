package com.krake.core.widget

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import java.lang.reflect.Constructor

/**
 * Created by joel on 20/04/17.
 */

abstract class ObjectsHeaderRecyclerViewAdapter<ObjectClass, ViewHolderClass : RecyclerView.ViewHolder,
        HeaderObject, HeaderHolder : ViewHolderClass>(context: Context,
                                                      @LayoutRes layoutResource: Int,
                                                      objects: List<ObjectClass>? = null,
                                                      holderClass: Class<ViewHolderClass>,
                                                      private val headerContent: HeaderObject,
                                                      @LayoutRes private val headerLayoutResource: Int,
                                                      headerHolderClass: Class<HeaderHolder>) :
        ObjectsRecyclerViewAdapter<ObjectClass, ViewHolderClass>(context, layoutResource, objects, holderClass) {

    private val headerHolderClassConstructor: Constructor<HeaderHolder>

    override var defaultClickReceiver: ClickReceiver<ObjectClass>? = null
        get() = field
        set(value) {
            if (value != null)
                field = object : ClickReceiver<ObjectClass> {
                    override fun onViewClicked(recyclerView: RecyclerView, view: View, position: Int, item: ObjectClass) {
                        value.onViewClicked(recyclerView, view, position - 1, item)
                    }
                }
            else
                field = null
        }

    override var defaultLongClickReceiver: LongClickReceiver<ObjectClass>? = null
        get() = field
        set(value) {
            if (value != null)
                field = object : LongClickReceiver<ObjectClass> {
                    override fun onViewLongClicked(recyclerView: RecyclerView, view: View, position: Int, item: ObjectClass) {
                        value.onViewLongClicked(recyclerView, view, position - 1, item)
                    }

                }
            else
                field = null
        }

    var headerClickReceiver: ClickReceiver<HeaderObject>? = null

    init {
        try {
            headerHolderClassConstructor = headerHolderClass.getConstructor(View::class.java)
        } catch (e: NoSuchMethodException) {
            throw e
        }
    }

    final override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0)
            return headerLayoutResource

        return super.getItemViewType(position - 1)
    }

    final override fun getItem(position: Int): ObjectClass? {
        if (position > 0)
            return super.getItem(position - 1)
        return null
    }

    @Suppress("UNCHECKED_CAST")
    final override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        if (position == 0)
            onBindHeaderHolder(holder as HeaderHolder, headerContent)
        else
            onBindObjectViewHolder(holder, getItem(position), position - 1)
    }

    abstract fun onBindObjectViewHolder(holder: ViewHolderClass, item: ObjectClass?, position: Int)

    abstract fun onBindHeaderHolder(holder: HeaderHolder, header: HeaderObject)

    @SuppressLint("MissingSuperCall")
    override fun onCreateViewHolder(viewGroup: ViewGroup, @LayoutRes viewLayout: Int): ViewHolderClass {
        if (viewLayout != headerLayoutResource) {
            val viewHolderClass = instantiateViewHolder(viewGroup, viewLayout)

            addDefaultGestureListenerTo(viewHolderClass)

            return viewHolderClass
        }

        val view = inflater.inflate(viewLayout, viewGroup, false)
        try {
            val holder = headerHolderClassConstructor.newInstance(view)

            if (headerClickReceiver != null) {
                view.setOnClickListener { v ->
                    headerClickReceiver?.onViewClicked(
                            holder.itemView.parent as RecyclerView,
                            v,
                            0,
                            headerContent)
                }
            }
            return holder

        } catch (e: Exception) {
            throw e
        }
    }
}


