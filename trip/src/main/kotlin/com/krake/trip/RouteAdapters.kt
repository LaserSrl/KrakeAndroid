package com.krake.trip

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.krake.core.media.widget.LoadableImageView
import com.krake.core.text.DistanceNumberFormat
import com.krake.core.widget.ObjectsHeaderRecyclerViewAdapter
import com.krake.core.widget.ObjectsRecyclerViewAdapter
import com.krake.core.widget.ViewHolderWithClickGesture
import java.lang.reflect.Constructor
import java.text.DateFormat

/**
 * Created by joel on 20/04/17.
 */

class InstructionStepAdapter(context: Context,
                             objects: List<SingleStep>?,
                             headerContent: Route) :
        ObjectsHeaderRecyclerViewAdapter<SingleStep, SingleStepHolder, Route, SingleModeHeaderHolder>
        (context, R.layout.route_single_mode_step,
                objects, SingleStepHolder::class.java,
                headerContent, R.layout.route_single_mode_header, SingleModeHeaderHolder::class.java) {
    private val distanceFormat: DistanceNumberFormat
    private val durationFormat: com.krake.core.text.DurationFormat

    init {
        durationFormat = com.krake.core.text.DurationFormat(context)
        distanceFormat = DistanceNumberFormat()
    }

    override fun onBindObjectViewHolder(holder: SingleStepHolder, item: SingleStep?, position: Int) {
        item?.let {
            holder.nameTextView.text = it.instruction
            holder.distanceTextView.text = distanceFormat.formatDistance(it.distance.toFloat())
            holder.instructionImageView?.setImageResource(it.maneuver.drawableResource())
        }
    }

    override fun onBindHeaderHolder(holder: SingleModeHeaderHolder, header: Route) {
        holder.nameTextView.text = durationFormat.format(header.duration)
        holder.distanceTextView.text = "(" + distanceFormat.formatDistance(header.distance.toFloat()) + ")"
    }
}

class ComplexStepAdapter(context: Context,
                         route: Route) :
        ObjectsHeaderRecyclerViewAdapter<ComplexStep, SingleStepHolder, Route, TransitHeaderHolder>
        (context, R.layout.route_complex_group_row,
                route.steps.asList(), SingleStepHolder::class.java,
                route, R.layout.route_transit_header, TransitHeaderHolder::class.java) {
    private val distanceFormat: DistanceNumberFormat
    private val durationFormat: com.krake.core.text.DurationFormat
    private val timeFormat: DateFormat
    private val transitLineConstructor: Constructor<TransitHolder>

    init {
        durationFormat = com.krake.core.text.DurationFormat(context)
        distanceFormat = DistanceNumberFormat().prefix("(").suffix(")")
        timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT)

        transitLineConstructor = TransitHolder::class.java.getConstructor(View::class.java)
    }

    override fun onBindObjectViewHolder(holder: SingleStepHolder, item: ComplexStep?, position: Int) {

        if (item is StepGroup) {
            onBindStepGroupHolder(holder = holder, item = item)
        } else if (item is TransitStep && holder is TransitHolder) {
            onBindTransitHolder(holder, item)
        }
    }

    private fun onBindStepGroupHolder(holder: SingleStepHolder, item: StepGroup) {
        holder.nameTextView.text = String.format("%s per %s", item.travelMode.name(context!!), durationFormat.format(item.duration))
        holder.distanceTextView.text = distanceFormat.formatDistance(item.distance.toFloat())
        holder.instructionImageView?.setImageResource(item.travelMode.drawableResource())
    }

    private fun onBindTransitHolder(holder: TransitHolder, item: TransitStep) {
        holder.startTimeTextView.text = timeFormat.format(item.startTime)
        holder.endTimeTextView.text = timeFormat.format(item.endTime)

        item.setLineNameAndColorToTextView(holder.nameTextView)

        holder.distanceTextView.text = item.headsign

        holder.startOfLineTextView.text = item.from.name
        holder.endOfLineTextView.text = item.to.name

        item.setVehicleOrLineImage(context!!, holder.instructionImageView as LoadableImageView)
    }

    override fun onBindHeaderHolder(holder: TransitHeaderHolder, header: Route) {
        holder.startTimeTextView.text = timeFormat.format(header.startTime)
        holder.endTimeTextView.text = timeFormat.format(header.endTime)
        holder.nameTextView.text = durationFormat.format(header.duration)
        holder.distanceTextView.text = distanceFormat.formatDistance(header.distance.toFloat())

        header.setLineTransitSteps(context!!, holder, timeFormat)
    }

    override fun getItemViewType(position: Int): Int {
        if (position != 0) {
            val step = getItem(position)

            if (step is TransitStep) {
                return R.layout.route_transit_row
            }
        }
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewLayout: Int): SingleStepHolder {
        if (R.layout.route_transit_row == viewLayout) {
            val view = inflater.inflate(viewLayout, viewGroup, false)
            try {
                val t = transitLineConstructor.newInstance(view)
                addDefaultGestureListenerTo(t)
                return t
            } catch (e: Exception) {
                throw e
            }
        }
        return super.onCreateViewHolder(viewGroup, viewLayout)
    }
}

class RoutesAdapter(context: Context, routes: List<Route>) :
        ObjectsRecyclerViewAdapter<Route, TransitHeaderHolder>(context, R.layout.route_transit_header, routes, TransitHeaderHolder::class.java) {
    private val distanceFormat: DistanceNumberFormat
    private val durationFormat: com.krake.core.text.DurationFormat
    private val timeFormat: DateFormat

    init {
        durationFormat = com.krake.core.text.DurationFormat(context)
        distanceFormat = DistanceNumberFormat().prefix("(").suffix(")")
        timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT)
    }

    override fun onBindViewHolder(holder: TransitHeaderHolder, position: Int)
    {

        val header = getItem(position)
        if (header != null)
        {
            holder.startTimeTextView.text = timeFormat.format(header.startTime)
            holder.endTimeTextView.text = timeFormat.format(header.endTime)
            holder.nameTextView.text = durationFormat.format(header.duration)
            holder.distanceTextView.text = distanceFormat.formatDistance(header.distance.toFloat())

            header.setLineTransitSteps(context!!, holder, timeFormat)

        }
    }
}

open class SingleStepHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ViewHolderWithClickGesture {
    override val itemView: View
        get() = super.itemView
    val nameTextView: TextView = itemView.findViewById(R.id.tripNameTextView)
    val distanceTextView: TextView = itemView.findViewById(R.id.tripDistanceTextView)
    val instructionImageView: ImageView? = itemView.findViewById(R.id.tripDirectionImageView)
}

class SingleModeHeaderHolder(itemView: View) : SingleStepHolder(itemView)

class TransitHolder(itemView: View) : SingleStepHolder(itemView), ViewHolderWithClickGesture {
    val startTimeTextView = itemView.findViewById(R.id.tripStartTimeTextView) as TextView
    val endTimeTextView = itemView.findViewById(R.id.tripEndTimeTextView) as TextView
    val startOfLineTextView: TextView = itemView.findViewById(R.id.tripStartTransitNameTextView) as TextView
    val endOfLineTextView = itemView.findViewById(R.id.tripArrivalTransitNameTextView) as TextView
}

class TransitHeaderHolder(itemView: View) : SingleStepHolder(itemView) {
    val startTimeTextView = itemView.findViewById(R.id.tripStartTimeTextView) as TextView
    val endTimeTextView = itemView.findViewById(R.id.tripEndTimeTextView) as TextView
    val stepContainer = itemView.findViewById(R.id.complexStepContainer) as LinearLayout
}