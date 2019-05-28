package com.krake.bus.app

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.krake.core.app.ContentItemMapModelFragment
import com.krake.core.data.DataModel
import com.krake.bus.viewmodel.BusStopsViewModel

class BusRouteStopMapFragment : ContentItemMapModelFragment() {

    override fun observeDataModel() {
        val viewModel = ViewModelProviders.of(activity!!).get(BusStopsViewModel::class.java)

        viewModel.busStops.observe(this, Observer {
            val dataModel = DataModel(it, true)
            this.onDataModelChanged(dataModel)
        })
    }
}