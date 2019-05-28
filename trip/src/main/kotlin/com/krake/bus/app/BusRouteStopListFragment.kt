package com.krake.bus.app

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.krake.core.app.ContentItemGridModelFragment
import com.krake.core.data.DataModel
import com.krake.bus.viewmodel.BusStopsViewModel
import com.krake.bus.viewmodel.Error
import com.krake.bus.viewmodel.Loading

class BusRouteStopListFragment : ContentItemGridModelFragment() {

    override fun observeDataModel() {
        val viewModel = ViewModelProviders.of(activity!!).get(BusStopsViewModel::class.java)

        viewModel.busStops.observe(this, Observer {
            val dataModel = DataModel(it, true)
            this.onDataModelChanged(dataModel)
        })

        viewModel.status.observe(this, Observer {
            changeProgressVisibility(it == Loading)

            if (it == Error && viewModel.busStops.value?.isEmpty() != false)
                showNoElementsLayout()
        })
    }
}