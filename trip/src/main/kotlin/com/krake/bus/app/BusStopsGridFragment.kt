package com.krake.bus.app

import android.content.Context
import androidx.lifecycle.Observer
import com.krake.bus.model.BusPassage
import com.krake.bus.model.BusPassagesReceiver
import com.krake.bus.model.BusPassagesSender
import com.krake.core.app.ContentItemGridModelFragment
import com.krake.core.data.DataModel

/**
 * Created by antoniolig on 27/04/2017.
 */
class BusStopsGridFragment : ContentItemGridModelFragment(), BusPassagesReceiver
{
    private var passagesSender: BusPassagesSender? = null

    override fun onAttach(activity: Context?) {
        super.onAttach(activity)
        passagesSender = activity as? BusPassagesSender
    }

    override fun onDetach() {
        super.onDetach()
        passagesSender = null
    }

    override fun observeDataModel()
    {
        val model = (dataConnectionModel as? BusPatternDataModel)?.busPatternModel
        model?.observe(this, Observer { dataModel -> onDataModelChanged(dataModel) })
    }

    override fun onDataModelChanged(dataModel: DataModel?)
    {
        super.onDataModelChanged(dataModel)
    }

    override fun onPassageChosen(passage: BusPassage) {
        // Non utilizzato in questo fragment.
    }

    override fun onRefresh() {
        // Vuoto per evitare il refresh tramite OrchardService.
    }
}