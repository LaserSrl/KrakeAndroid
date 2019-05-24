package com.krake.route.app

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.krake.core.app.ContentItemGridModelFragment
import com.krake.core.data.DataModel
import com.krake.route.viewmodel.BusRoutesViewModel
import com.krake.route.viewmodel.BusStopsViewModel
import com.krake.route.viewmodel.Error
import com.krake.route.viewmodel.Loading

class RouteStopListFragment : ContentItemGridModelFragment() {

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