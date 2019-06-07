package com.krake.bus.widget

import android.content.Context
import android.graphics.drawable.ColorDrawable
import com.krake.core.extension.contrastTextColor
import com.krake.core.extension.setTintCompat
import com.krake.core.widget.ContentItemAdapter
import com.krake.core.widget.ImageTextCellHolder
import com.krake.bus.model.OtpBusRoute

class BusRouteListAdapter(context: Context?, layout: Int, holderClass: Class<*>?) : ContentItemAdapter(context, layout, holderClass) {

    override fun onBindViewHolder(holder: ImageTextCellHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)

        val item = getItem(position) as OtpBusRoute
        holder.imageView?.background = ColorDrawable(item.color)
        holder.imageView?.drawable?.setTintCompat(item.color.contrastTextColor())
    }
}