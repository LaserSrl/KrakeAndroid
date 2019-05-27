package com.krake.route.app

import android.content.Context
import com.krake.core.widget.ContentItemAdapter
import com.krake.core.widget.ImageTextCellHolder
import com.krake.route.model.OtpStopTime
import java.text.SimpleDateFormat
import java.util.*

class StopTimesAdapter(context: Context?, layout: Int, holderClass: Class<*>?) : ContentItemAdapter(context, layout, holderClass) {
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
        calendar.add(Calendar.SECOND, item.scheduledDeparture!!.toInt())

        holder.titleTextView.text = dateFormatter.format(calendar.time)
    }
}