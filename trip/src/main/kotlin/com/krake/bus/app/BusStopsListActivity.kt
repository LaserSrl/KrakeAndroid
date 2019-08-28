package com.krake.bus.app

import android.content.DialogInterface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
import com.krake.core.model.ContentItemWithLocation
import com.krake.core.model.identifierOrStringIdentifier
import com.krake.trip.R

/**
 * Created by antoniolig on 27/04/2017.
 */
class BusStopsListActivity : ContentItemListMapActivity(),
    BusPassagesSender, BusStopGeofenceManager.Listener {

    @BundleResolvable
    lateinit var busComponentModule: BusComponentModule

    private lateinit var linesConnection: DataConnectionModel

    private var stopIdentifier: String? = null
    private var conteItemInEvidence: ContentItemWithLocation? = null
    private lateinit var geofenceManager: BusStopGeofenceManager

    override fun onCreate(savedInstanceState: Bundle?, layout: Int) {
        super.onCreate(savedInstanceState, layout)
        stopIdentifier = orchardComponentModule.recordStringIdentifier

        geofenceManager = BusStopGeofenceManager(this, this)
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

    override fun onContentItemInEvidence(senderFragment: Any, contentItem: ContentItem) {
        super.onContentItemInEvidence(senderFragment, contentItem)
        conteItemInEvidence = geofenceManager.canGeofence(contentItem)
        invalidateOptionsMenu()
    }

    override fun onContentItemNoMoreInEvidence(senderFragment: Any, contentItem: ContentItem) {
        super.onContentItemNoMoreInEvidence(senderFragment, contentItem)
        conteItemInEvidence = null
        invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)

        menuInflater.inflate(R.menu.menu_bus_stop_geofence, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {

        val mainStop = (conteItemInEvidence as? BusStop)?.isMainStop ?: false
        menu?.findItem(R.id.bus_stop_add_geofence_menu_item)?.isVisible = conteItemInEvidence != null &&
                !geofenceManager.isGeofenceMonitored(conteItemInEvidence!!) &&
                !mainStop
        menu?.findItem(R.id.bus_stop_remove_geofence_menu_item)?.isVisible = conteItemInEvidence != null &&
                geofenceManager.isGeofenceMonitored(conteItemInEvidence!!) &&
                !mainStop

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item?.itemId == R.id.bus_stop_add_geofence_menu_item) {
            conteItemInEvidence?.let {

                AlertDialog.Builder(this)
                    .setMessage(getString(R.string.bus_geofence_want_notification))
                    .setNegativeButton(R.string.no, DialogInterface.OnClickListener { dialogInterface, i ->
                        ;
                    })
                    .setPositiveButton(R.string.yes, DialogInterface.OnClickListener { dialogInterface, i ->
                        geofenceManager.addGeofence(it)
                    })
                    .show()

            }

            return true
        } else if (item?.itemId == R.id.bus_stop_remove_geofence_menu_item) {
            conteItemInEvidence?.let {
                AlertDialog.Builder(this)
                    .setMessage(getString(R.string.bus_geofence_disable_notification))
                    .setNegativeButton(R.string.no, DialogInterface.OnClickListener { dialogInterface, i ->
                        ;
                    })
                    .setPositiveButton(R.string.yes, DialogInterface.OnClickListener { dialogInterface, i ->

                        geofenceManager.removeGeofence(it)
                    })
                    .show()
            }

            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getFragmentCreationExtras(mode: FragmentMode): Bundle {
        if (mode == FragmentMode.GRID) {
            val bundle = intent.extras ?: Bundle()

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

    override fun geofenceChanged(identifier: String, enabled: Boolean, success: Boolean) {
        if (conteItemInEvidence?.identifierOrStringIdentifier == identifier) {
            if (success) {
                invalidateOptionsMenu()
            }
        }
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