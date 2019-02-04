package com.krake.core.widget

import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Created by joel on 15/03/17.
 */

open class GestureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ViewHolderAccess {
    override val itemView: View
        get() = super.itemView
}