package com.krake.bus.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.krake.bus.component.module.BusComponentModule
import com.krake.bus.viewmodel.BusPatternDataModel
import com.krake.core.app.ContentItemListMapActivity
import com.krake.core.component.annotation.BundleResolvable
import com.krake.core.component.base.ComponentManager
import com.krake.core.component.module.ListMapComponentModule
import com.krake.core.component.module.OrchardComponentModule
import com.krake.core.component.module.ThemableComponentModule
import com.krake.core.model.ContentItem
import com.krake.core.model.identifierOrStringIdentifier
import com.krake.bus.viewmodel.Error
import com.krake.bus.viewmodel.Loading
import com.krake.bus.widget.BusRouteListAdapter
import com.krake.bus.widget.BusStopListAdapter
import com.krake.trip.R

class BusRouteListActivity : ContentItemListMapActivity() {
    @BundleResolvable
    lateinit var busComponentModule: BusComponentModule

    private lateinit var viewModel : BusPatternDataModel

    override fun onCreate(savedInstanceState: Bundle?, layout: Int) {
        super.onCreate(savedInstanceState, layout)
        viewModel = ViewModelProviders.of(this).get(BusPatternDataModel::class.java)

        viewModel.status.observe(this, Observer {
            updateRefreshStatus(it == Loading)

            if (it == Error) {
                showError()
            }
        })
    }

    override fun onRefresh() {
        viewModel.loadBusRoutes()
    }

    override fun getDetailIntent(contentItem: ContentItem): Intent {
        return ComponentManager.createIntent()
            .from(this)
            .to(BusRouteStopListActivity::class.java)
            .with(
                ThemableComponentModule()
                    .title(contentItem.titlePartTitle)
                    .upIntent(intent),
                OrchardComponentModule()
                    .startConnectionAfterActivityCreated(false)
                    .recordStringIdentifier(contentItem.identifierOrStringIdentifier),
                ListMapComponentModule(this)
                    .activityLayout(R.layout.activity_bus_stops)
                    .listCellLayout(BusComponentModule.DEFAULT_LIST_CELL_BUS_STOP_LAYOUT)
                    .listAdapterClass(BusStopListAdapter::class.java)
                    .listFragmentClass(BusRouteStopListFragment::class.java)
                    .mapFragmentClass(BusRouteStopMapFragment::class.java),
                busComponentModule)
            .build()
    }

    companion object {
        val defaultListMapModule: (Context) -> ListMapComponentModule = { context: Context ->
            ListMapComponentModule(context)
                .showMap(false)
                .listCellLayout(BusComponentModule.DEFAULT_LIST_CELL_LAYOUT)
                .listRootLayout(BusComponentModule.DEFAULT_LIST_ROOT_LAYOUT)
                .listAdapterClass(BusRouteListAdapter::class.java)
                .listFragmentClass(BusRouteListFragment::class.java)
        }
    }
}