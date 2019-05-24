package com.krake.route.app

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.krake.bus.model.BusStop
import com.krake.core.app.ContentItemListMapActivity
import com.krake.core.model.ContentItem
import com.krake.core.widget.ContentItemAdapter
import com.krake.core.widget.ImageTextCellHolder
import com.krake.route.model.OtpBusStop
import com.krake.route.viewmodel.BusRoutesViewModel
import com.krake.route.viewmodel.BusStopsViewModel
import com.krake.route.viewmodel.Error
import com.krake.route.viewmodel.Loading
import com.krake.trip.R
import java.util.*

class RouteStopListActivity : ContentItemListMapActivity() {
    private lateinit var viewModel: BusStopsViewModel
    private var selectedStop: OtpBusStop? = null
    private var selectedDate: Date? = null

    private val behavior by lazy {
        val stopTimesContainer = findViewById<ViewGroup>(R.id.stopTimesContainer)
        (stopTimesContainer.layoutParams as CoordinatorLayout.LayoutParams).behavior as BottomSheetBehavior<View>
    }

    override fun onCreate(savedInstanceState: Bundle?, layout: Int) {
        super.onCreate(savedInstanceState, layout)

        viewModel = ViewModelProviders.of(this).get(BusStopsViewModel::class.java)

        val stopTimesList = findViewById<RecyclerView>(R.id.stopTimesList)
        val adapter = ContentItemAdapter(this, R.layout.cell_bus_stop_passage, ImageTextCellHolder::class.java)
        stopTimesList.adapter = adapter

        findViewById<Button>(R.id.previousButton).setOnClickListener {
            viewModel.loadBusTimesByDate(this, selectedStop!!, selectedDate!!)
        }

        findViewById<Button>(R.id.nextButton).setOnClickListener {
            viewModel.loadBusTimesByDate(this, selectedStop!!, selectedDate!!)
        }

        viewModel.status.observe(this, Observer {
            updateRefreshStatus(it == Loading)

            if (it == Error) {
                showError()
            }
        })

        viewModel.stopTimes.observe(this, Observer {
            adapter.swapList(it, true)
        })

        val routeId = orchardComponentModule.recordStringIdentifier!!
        viewModel.loadStopsByBusRoute(this, routeId)

        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun onRefresh() {
        val routeId = orchardComponentModule.recordStringIdentifier!!
        viewModel.loadStopsByBusRoute(this, routeId)
    }

    override fun onShowContentItemDetails(sender: Any, contentItem: ContentItem) {
        selectedStop = contentItem as OtpBusStop

        selectedDate = Date()
        viewModel.loadBusTimesByDate(this, selectedStop!!, selectedDate!!)

        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}