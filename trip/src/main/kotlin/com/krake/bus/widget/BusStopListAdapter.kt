package com.krake.bus.widget

import android.content.Context
import com.krake.bus.model.BusStop
import com.krake.core.widget.ContentItemAdapter
import com.krake.core.widget.ImageTextCellHolder

class BusStopListAdapter(context: Context?, layout: Int, holderClass: Class<*>?) : ContentItemAdapter(context, layout, holderClass) {

    override fun onBindViewHolder(holder: ImageTextCellHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)

        val item = getItem(position) as BusStop
        holder.imageView?.setImageDrawable(item.markerInnerDrawable(context!!))
    }
}