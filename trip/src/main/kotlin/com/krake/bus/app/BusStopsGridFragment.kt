package com.krake.bus.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.Observer
import com.krake.bus.component.module.BusComponentModule
import com.krake.bus.model.BusPassage
import com.krake.bus.model.BusPassagesReceiver
import com.krake.bus.viewmodel.BusPatternDataModel
import com.krake.core.OrchardError
import com.krake.core.app.ContentItemGridModelFragment
import com.krake.core.component.annotation.BundleResolvable
import com.krake.core.data.DataModel
import java.util.concurrent.TimeUnit

/**
 * Created by antoniolig on 27/04/2017.
 */
class BusStopsGridFragment : ContentItemGridModelFragment(), BusPassagesReceiver
{
    @BundleResolvable
    lateinit var busComponentModule: BusComponentModule
    private val handler = Handler(Looper.getMainLooper())
    private var isJobScheduled = false
    private var calledAtLeastOnce = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycleView.itemAnimator?.changeDuration = 0
    }

    override fun onConfigureModel() {
        internalOrchardComponentModule.startConnectionAfterActivityCreated(false)
        super.onConfigureModel()
    }

    override fun observeDataModel() {
        val model = (dataConnectionModel as? BusPatternDataModel)?.busPatternModel
        model?.observe(this, Observer { dataModel -> onDataModelChanged(dataModel) })
    }

    private fun scheduleRefresh() {
        if (busComponentModule.busStopsAutoRefreshPeriod <= 0 || isJobScheduled || !calledAtLeastOnce)
            return

        val refreshAction = {
            isJobScheduled = false
            dataConnectionModel.restartDataLoading()
        }
        val refreshTime = TimeUnit.SECONDS.toMillis(busComponentModule.busStopsAutoRefreshPeriod.toLong())
        handler.postDelayed(refreshAction, refreshTime)
        isJobScheduled = true
    }

    private fun stopRefresh() {
        handler.removeCallbacksAndMessages(null)
        isJobScheduled = false
    }

    override fun onResume() {
        super.onResume()
        scheduleRefresh()
        dataConnectionModel.restartDataLoading()
    }

    override fun onPause() {
        super.onPause()
        stopRefresh()
    }

    override fun onDataModelChanged(dataModel: DataModel?) {
        super.onDataModelChanged(dataModel)
        calledAtLeastOnce = true

        if (dataModel?.cacheValid == true)
            scheduleRefresh()
    }

    override fun onDataLoadingError(orchardError: OrchardError) {
        super.onDataLoadingError(orchardError)
        calledAtLeastOnce = true
        scheduleRefresh()
    }

    override fun onPassageChosen(passage: BusPassage) {
        // Non utilizzato in questo fragment.
    }

    override fun changeProgressVisibility(isLoading: Boolean) {
        super.changeProgressVisibility(if (calledAtLeastOnce) false else isLoading)
    }
}