package com.krake.bus.widget

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import com.krake.bus.model.BusPassage
import com.krake.core.extension.contrastTextColor
import com.krake.core.extension.setTintCompat
import com.krake.core.widget.ContentItemAdapter
import com.krake.core.widget.ImageTextCellHolder
import com.krake.trip.R
import java.text.DateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by antoniolig on 28/04/2017.
 */
class BusPassageAdapter(context: Context, layout: Int, holderClass: Class<*>?) : ContentItemAdapter(context, layout, holderClass)
{

    override fun onBindViewHolder(holder: ImageTextCellHolder, i: Int)
    {
        super.onBindViewHolder(holder, i)
        val passage = getItem(i) as BusPassage
        val passageHolder = holder as BusPassageHolder

        val lastStop: String = if (!passage.lastStop) "" else context!!.getString(R.string.last_stop)

        holder.titleTextView.text = String.format(
            context!!.getString(R.string.bus_line_destination_format),
            passage.lineNumber,
            passage.destination,
            lastStop
        )

        val millisDiff  = Math.abs(passage.passage!!.time - Date().time)
        val timeGap: String
        if (millisDiff < TimeUnit.HOURS.toMillis(1)) {
            timeGap = String.format(context!!.getString(R.string.bus_time_minutes_format), millisDiff / TimeUnit.MINUTES.toMillis(1))
        } else {
            timeGap = DateFormat.getTimeInstance().format(passage.passage)
        }
        passageHolder.timeTextView.text = timeGap
        passageHolder.timeTextView.visibility = View.VISIBLE

        val colorRef: Int
        if (!passage.realTime) {
            colorRef = R.color.time_scheduled_text_color
            passageHolder.realTimeImage.visibility = View.GONE
        } else {
            colorRef = R.color.time_real_text_color
            passageHolder.realTimeImage.visibility = View.VISIBLE
        }
        passageHolder.timeTextView.setTextColor(colorRef)

        passage.pattern?.busRoute?.let { route ->
            holder.imageView?.background = ColorDrawable(route.color)
            holder.imageView?.drawable?.setTintCompat(route.color.contrastTextColor())
        }
    }
}