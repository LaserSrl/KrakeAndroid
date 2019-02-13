package com.krake.core.extension

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.krake.core.R

/**
 * Extension di [RecyclerView.ViewHolder] che restituisce la view di default cliccabile
 */
fun RecyclerView.ViewHolder.viewWithClick(): View {
    return (itemView.findViewWithTag(itemView.context.getString(R.string.cell_selection_view_tag)) ?: itemView)
}

