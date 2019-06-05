package com.krake.bus.widget

import android.content.Context
import android.view.View
import com.krake.bus.model.OtpStopTime
import com.krake.core.widget.ContentItemAdapter
import com.krake.core.widget.ImageTextCellHolder
import com.krake.trip.R
import java.text.SimpleDateFormat
import java.util.*

class BusStopTimesAdapter(context: Context?, layout: Int, holderClass: Class<*>?) : ContentItemAdapter(context, layout, holderClass) {
    private val calendar = Calendar.getInstance().apply {
        time = Date()
    }
    private val dateFormatter = SimpleDateFormat("HH:mm a", Locale.getDefault())

    override fun onBindViewHolder(holder: ImageTextCellHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val item = getItem(position) as OtpStopTime

        val scheduled = item.scheduledDeparture!!.toInt()
        val real = item.realtimeDeparture!!.toInt()

        calendar.add(Calendar.SECOND, real)

        val colorRef: Int
        if (scheduled == real) {
            colorRef = R.color.time_scheduled_text_color
            holder.imageView?.visibility = View.INVISIBLE

        } else {
            colorRef = R.color.time_real_text_color
            holder.imageView?.visibility = View.VISIBLE
        }

        holder.titleTextView.setTextColor(context!!.resources.getColor(colorRef))


        holder.titleTextView.text = dateFormatter.format(calendar.time)
    }
}