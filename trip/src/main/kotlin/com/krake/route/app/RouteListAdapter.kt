package com.krake.route.app

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.VectorDrawable
import com.krake.core.extension.contrastTextColor
import com.krake.core.extension.setTintCompat
import com.krake.core.widget.ContentItemAdapter
import com.krake.core.widget.ImageTextCellHolder
import com.krake.route.model.OtpBusRoute
import com.krake.route.model.OtpStopTime
import java.text.SimpleDateFormat
import java.util.*

class RouteListAdapter(context: Context?, layout: Int, holderClass: Class<*>?) : ContentItemAdapter(context, layout, holderClass) {

    override fun onBindViewHolder(holder: ImageTextCellHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)

        val item = getItem(position) as OtpBusRoute
        val color = Color.parseColor(if (item.color.startsWith("#")) item.color else "#${item.color}")
        holder.imageView?.background = ColorDrawable(color)
        holder.imageView?.drawable?.setTintCompat(color.contrastTextColor())
    }
}