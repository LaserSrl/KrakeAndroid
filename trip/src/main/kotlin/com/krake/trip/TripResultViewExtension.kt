package com.krake.trip

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.TextView
import com.krake.core.extension.contrastTextColor
import com.krake.core.extension.isInSameDay
import com.krake.core.media.loader.MediaLoader
import com.krake.core.media.widget.LoadableImageView
import java.text.DateFormat
import java.util.*

/**
 * Created by joel on 26/04/17.
 */

fun TransitStep.setLineNameAndColorToTextView(nameTextView: TextView) {
    nameTextView.text = this.line.shortName
    val color = this.line.color
    if (color != null) {
        nameTextView.setBackgroundColor(color)
        nameTextView.setTextColor(color.contrastTextColor())
    } else {
        nameTextView.setBackgroundColor(Color.TRANSPARENT)
        nameTextView.setTextColor(Color.BLACK)
    }
}

fun Route.setLineTransitSteps(context: Context, holder: TransitHeaderHolder, timeFormat: DateFormat) {
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    val steps = this.steps.filterIsInstance(TransitStep::class.java)

    holder.stepContainer.removeAllViews()

    steps.forEachIndexed { index, complexStep ->
        val transitView = inflater.inflate(R.layout.route_transit_header_transit_resume_logo, holder.stepContainer, false)
        val imageView: LoadableImageView = transitView.findViewById(R.id.tripDirectionImageView)
        val nameTextView: TextView = transitView.findViewById(R.id.tripStartTransitNameTextView)

        nameTextView.text = complexStep.line.shortName

        complexStep.setLineNameAndColorToTextView(nameTextView)

        complexStep.setVehicleOrLineImage(context, imageView)

        if (index != 0)
            inflater.inflate(R.layout.route_transit_header_transit_resume_separator, holder.stepContainer)

        holder.stepContainer.addView(transitView)
    }

    steps.firstOrNull()?.let {
        if (it.startTime.isInSameDay(Date()))
            holder.startTimeTextView.text = timeFormat.format(it.startTime)
        else
            holder.startTimeTextView.text = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(it.startTime)
    }

    steps.lastOrNull()?.let {
        holder.endTimeTextView.text = timeFormat.format(it.endTime)
    }
}

fun TransitStep.setVehicleOrLineImage(context: Context, imageView: LoadableImageView) {
    if (this.line.lineIcon != null) {
        MediaLoader.with(context, imageView)
                .mediaPart(this.line.lineIcon)
                .load()
    } else {
        imageView.setImageResource(this.vehicle.drawableResource())
    }
}