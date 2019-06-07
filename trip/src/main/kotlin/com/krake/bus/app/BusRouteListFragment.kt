package com.krake.bus.app

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.krake.bus.viewmodel.BusPatternDataModel
import com.krake.core.app.ContentItemGridModelFragment
import com.krake.core.data.DataModel
import com.krake.bus.viewmodel.Error
import com.krake.bus.viewmodel.Loading

class BusRouteListFragment : ContentItemGridModelFragment() {

    override fun observeDataModel() {
        val viewModel = ViewModelProviders.of(activity!!).get(BusPatternDataModel::class.java)

        viewModel.busRoutes.observe(this, Observer {
            val dataModel = DataModel(it, true)
            this.onDataModelChanged(dataModel)
        })

        viewModel.status.observe(this, Observer {
            changeProgressVisibility(it == Loading)

            if (it == Error && viewModel.busRoutes.value?.isEmpty() != false)
                showNoElementsLayout()
        })
    }
}