package com.krake.bus.app

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.krake.bus.component.module.BusComponentModule
import com.krake.bus.model.*
import com.krake.bus.viewmodel.BusPatternDataModel
import com.krake.core.PrivacyViewModel
import com.krake.core.app.ContentItemListMapActivity
import com.krake.core.cache.CacheManager
import com.krake.core.cache.LocationCacheModifier
import com.krake.core.component.annotation.BundleResolvable
import com.krake.core.component.module.OrchardComponentModule
import com.krake.core.data.DataConnectionModel
import com.krake.core.data.DataModel
import com.krake.core.extension.putModules
import com.krake.core.model.ContentItem
import com.krake.trip.R

/**
 * Created by antoniolig on 27/04/2017.
 */
class BusStopsListActivity : ContentItemListMapActivity(),
        BusPassagesSender {

    @BundleResolvable
    lateinit var busComponentModule: BusComponentModule

    private lateinit var linesConnection: DataConnectionModel

    private var stopIdentifier: String? = null

    override fun onCreate(savedInstanceState: Bundle?, layout: Int) {
        super.onCreate(savedInstanceState, layout)
        stopIdentifier = orchardComponentModule.recordStringIdentifier

        val stopTimesPath = getString(R.string.orchard_path_otp_stoptimes)
        // Aggiunge il path al provider dei paths di around me
        (CacheManager.shared as? LocationCacheModifier)?.addLocationPath(stopTimesPath)

        val lineModule = OrchardComponentModule()
            .startConnectionAfterActivityCreated(false)
            .dataClass(orchardComponentModule.dataClass)
            .displayPath(getString(R.string.orchard_bus_line_display_alias))
            .avoidPagination()

        linesConnection = ViewModelProviders.of(this)
            .get(CacheManager.shared.getModelKey(lineModule), DataConnectionModel::class.java)

        linesConnection.configure(
            lineModule,
            loginComponentModule,
            ViewModelProviders.of(this).get(PrivacyViewModel::class.java)
        )

        linesConnection.multiThreadModel.observe(this, Observer<DataModel> { t ->
            if (t != null) {
                sendBusStops(t.listData as List<BusStop>)
                updateRefreshStatus(false)
            }
        })
    }

    override fun requestPassages() {
    }

    override fun onShowContentItemDetails(sender: Any, contentItem: ContentItem) {
        // Reset the state of the RecyclerView when a OtpBusStop is selected.
        gridFragment?.recycleView?.scrollToPosition(0)
        // Get the BottomSheetBehavior if possible.
        val bottomBehavior =
            (gridContainer.layoutParams as? CoordinatorLayout.LayoutParams)?.behavior as? BottomSheetBehavior
        bottomBehavior?.let {
            // Reset the state of the BottomSheetBehavior when a OtpBusStop is selected.
            it.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        (contentItem as? BusPassage)?.let {
            passageChosen(it)
            it.pattern?.let {
                linesConnection.orchardModule.putExtraParameter("patternid", it.stringIdentifier)
                linesConnection.restartDataLoading()
            }
        }
    }

    override fun getFragmentCreationExtras(mode: FragmentMode): Bundle {
        if (mode == FragmentMode.GRID) {
            val bundle = intent.extras

            val gridOrchard = OrchardComponentModule()
                .displayPath(getString(R.string.orchard_path_otp_stoptimes))
                .dataClass(busComponentModule.patternClass)
                .noCache()
                .avoidPagination()
                .putExtraParameter("Id", orchardComponentModule.recordStringIdentifier)
                .dataConnectionModelClass(BusPatternDataModel::class.java)

            bundle.putAll(gridOrchard.writeContent(this))

            return Bundle().putModules(this, gridOrchard, listMapComponentModule, busComponentModule)
        }

        return super.getFragmentCreationExtras(mode)
    }

    private fun dataLoadFailedOrEmpty() {
        AlertDialog.Builder(this@BusStopsListActivity)
            .setMessage(getString(R.string.error_loading_stop_times))
            .setCancelable(false)
            .setNeutralButton(android.R.string.ok) { _, _ -> finish() }
            .show()
    }

    private fun passageChosen(passage: BusPassage) {
        (gridFragment as? BusPassagesReceiver)?.onPassageChosen(passage)
        (mapFragment as? BusPassagesReceiver)?.onPassageChosen(passage)
    }

    private fun sendBusStops(stops: List<BusStop>) {
        (gridFragment as? BusStopsReceiver)?.onBusStopsReceived(stops)
        (mapFragment as? BusStopsReceiver)?.onBusStopsReceived(stops)
    }

    override fun onRefresh() {
        super.onRefresh()
        sendBusStops(listOf())
    }

    override fun setSwipeRefreshEnabled(enabled: Boolean) {
        //enable progress only when the auto refresh is not set
        super.setSwipeRefreshEnabled(if (busComponentModule.busStopsAutoRefreshPeriod > 0) false else enabled)
    }

    override fun updateRefreshStatus(refreshing: Boolean) {
        //enable progress only when the auto refresh is not set
        super.updateRefreshStatus(if (busComponentModule.busStopsAutoRefreshPeriod > 0) false else refreshing)
    }
}