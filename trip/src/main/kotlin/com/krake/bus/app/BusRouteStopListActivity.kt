package com.krake.bus.app

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.krake.core.app.ContentItemListMapActivity
import com.krake.core.model.ContentItem
import com.krake.core.widget.ImageTextCellHolder
import com.krake.core.widget.SafeBottomSheetBehavior
import com.krake.bus.model.OtpBusStop
import com.krake.bus.viewmodel.BusStopsViewModel
import com.krake.bus.viewmodel.Error
import com.krake.bus.viewmodel.Loading
import com.krake.trip.R
import java.text.SimpleDateFormat
import java.util.*

class BusRouteStopListActivity : ContentItemListMapActivity() {

    private lateinit var viewModel: BusStopsViewModel
    private var selectedStop: OtpBusStop? = null

    private lateinit var behavior : SafeBottomSheetBehavior<View>
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    private val routeId by lazy { orchardComponentModule.recordStringIdentifier!! }

    override fun onCreate(savedInstanceState: Bundle?, layout: Int) {
        super.onCreate(savedInstanceState, layout)

        viewModel = ViewModelProviders.of(this).get(BusStopsViewModel::class.java)

        viewModel.loadStopsByBusRoute(routeId)

        val stopTimesList = findViewById<RecyclerView>(R.id.stopTimesList)
        val adapter =
            BusStopTimesAdapter(this, R.layout.cell_stop_times, ImageTextCellHolder::class.java)
        stopTimesList.adapter = adapter

        findViewById<ImageButton>(R.id.previousButton).setOnClickListener {
            calendar.add(Calendar.DATE, -1)
            viewModel.loadBusTimesByDate(selectedStop!!, routeId, calendar.time)
        }

        findViewById<ImageButton>(R.id.nextButton).setOnClickListener {
            calendar.add(Calendar.DATE, 1)
            viewModel.loadBusTimesByDate(selectedStop!!, routeId, calendar.time)
        }

        val callback = object : SafeBottomSheetBehavior.BottomSheetStateCallback() {
            override fun onStateWillChange(bottomSheet: View, newState: Int) { }

            override fun onSlide(p0: View, p1: Float) { }

            override fun onStateChanged(p0: View, p1: Int) {
                if (p1 == BottomSheetBehavior.STATE_EXPANDED)
                    setSwipeRefreshEnabled(false)
            }
        }

        val stopTimesContainer = findViewById<ViewGroup>(R.id.stopTimesContainer)
        behavior = (stopTimesContainer.layoutParams as CoordinatorLayout.LayoutParams).behavior as SafeBottomSheetBehavior<View>
        behavior.apply {
            isHideable = true
            peekHeight = 0
            state = BottomSheetBehavior.STATE_HIDDEN
            addBottomSheetCallback(callback)
        }

        val stopTimesProgressBar = findViewById<ProgressBar>(R.id.stopTimesProgressBar)
        val stopTimesDateTextView= findViewById<TextView>(R.id.dateTextView)
        val noElementsView= findViewById<View>(R.id.noElementsView)

        viewModel.status.observe(this, Observer {
            stopTimesProgressBar.visibility = if (behavior.state != BottomSheetBehavior.STATE_HIDDEN && it == Loading) View.VISIBLE else View.INVISIBLE
            stopTimesList.visibility = if (behavior.state != BottomSheetBehavior.STATE_HIDDEN && it == Loading) View.INVISIBLE else View.VISIBLE
            updateRefreshStatus(behavior.state != BottomSheetBehavior.STATE_EXPANDED && it == Loading)

            if (it == Error) {
                showError()
            }
        })

        viewModel.stopTimes.observe(this, Observer {
            stopTimesDateTextView.text = dateFormatter.format(calendar.time)
            adapter.swapList(it, true)
            noElementsView.visibility = if (it.isEmpty()) View.VISIBLE else View.INVISIBLE
        })
    }

    override fun onRefresh() {
        val routeId = orchardComponentModule.recordStringIdentifier!!
        viewModel.loadStopsByBusRoute(routeId)
    }

    override fun onBackPressed() {
        if (behavior.state == BottomSheetBehavior.STATE_EXPANDED)
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
        else
            super.onBackPressed()
    }

    override fun onShowContentItemDetails(sender: Any, contentItem: ContentItem) {
        selectedStop = contentItem as OtpBusStop

        calendar.time = Date()
        viewModel.loadBusTimesByDate(selectedStop!!, routeId, calendar.time)

        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}