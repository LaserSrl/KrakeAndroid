package com.krake.route.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.krake.bus.component.module.BusComponentModule
import com.krake.core.app.ContentItemListMapActivity
import com.krake.core.component.annotation.BundleResolvable
import com.krake.core.component.base.ComponentManager
import com.krake.core.component.module.ListMapComponentModule
import com.krake.core.component.module.OrchardComponentModule
import com.krake.core.component.module.ThemableComponentModule
import com.krake.core.model.ContentItem
import com.krake.core.model.identifierOrStringIdentifier
import com.krake.route.viewmodel.BusRoutesViewModel
import com.krake.route.viewmodel.Error
import com.krake.route.viewmodel.Loading
import com.krake.trip.R

class RouteListActivity : ContentItemListMapActivity() {
    @BundleResolvable
    lateinit var busComponentModule: BusComponentModule

    private lateinit var viewModel : BusRoutesViewModel

    override fun onCreate(savedInstanceState: Bundle?, layout: Int) {
        super.onCreate(savedInstanceState, layout)
        viewModel = ViewModelProviders.of(this).get(BusRoutesViewModel::class.java)

        viewModel.status.observe(this, Observer {
            updateRefreshStatus(it == Loading)

            if (it == Error) {
                showError()
            }
        })

        viewModel.loadBusRoutes(this)
    }

    override fun onRefresh() {
        viewModel.loadBusRoutes(this)
    }

    override fun getDetailIntent(contentItem: ContentItem): Intent {
        return ComponentManager.createIntent()
            .from(this)
            .to(RouteStopListActivity::class.java)
            .with(
                ThemableComponentModule()
                    .title(contentItem.titlePartTitle)
                    .upIntent(intent),
                OrchardComponentModule()
                    .startConnectionAfterActivityCreated(false)
                    .recordStringIdentifier(contentItem.identifierOrStringIdentifier),
                ListMapComponentModule(this)
                    .activityLayout(R.layout.activity_bus_stops)
                    .listCellLayout(R.layout.cell_bus_stop_passage)
                    .listFragmentClass(RouteStopListFragment::class.java)
                    .mapFragmentClass(RouteStopMapFragment::class.java),
                busComponentModule)
            .build()
    }

    companion object {
        val defaultListMapModule: (Context) -> ListMapComponentModule = { context: Context ->
            ListMapComponentModule(context)
                .showMap(false)
                .listCellLayout(BusComponentModule.DEFAULT_LIST_CELL_LAYOUT)
                .listRootLayout(BusComponentModule.DEFAULT_LIST_ROOT_LAYOUT)
                .listAdapterClass(RouteListAdapter::class.java)
                .listFragmentClass(RouteListFragment::class.java)
        }
    }
}