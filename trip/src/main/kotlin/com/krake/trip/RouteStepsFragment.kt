package com.krake.trip

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.krake.core.component.annotation.BundleResolvable
import com.krake.core.component.base.ComponentManager
import com.krake.core.widget.ObjectsRecyclerViewAdapter
import com.krake.trip.component.module.RouteModule

/**
 * Fragment che mostra un singolo itinerario.
 * I datono sono  mostrati in modo diversi se l'itinerario è pianificato con #TRANSIT oppure No
 * Created by joel on 20/04/17.
 */
class RouteStepsFragment : Fragment(), ObjectsRecyclerViewAdapter.ClickReceiver<Route> {

    @BundleResolvable
    lateinit var routeModule: RouteModule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ComponentManager.resolveArguments(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_content_items_selection_list, container, false)

        view.findViewById<View>(android.R.id.progress).visibility = View.GONE

        val recycler: RecyclerView = view.findViewById(android.R.id.list)

        if (routeModule.route.steps.size == 1 &&
                routeModule.route.steps.first() is StepGroup) {
            val group = routeModule.route.steps.first() as StepGroup
            val singleStepAdapter = InstructionStepAdapter(activity!!,
                    group.steps.asList(),
                    routeModule.route)
            singleStepAdapter.headerClickReceiver = this
            singleStepAdapter.defaultClickReceiver = object : ObjectsRecyclerViewAdapter.ClickReceiver<SingleStep> {
                @Suppress("NAME_SHADOWING")
                override fun onViewClicked(recyclerView: RecyclerView, view: View, position: Int, item: SingleStep) {
                    (activity as? TripPlannerSearchActivity)?.zoomOnStep(item)
                }
            }

            recycler.adapter = singleStepAdapter

        } else {
            val singleStepAdapter = ComplexStepAdapter(activity!!,
                    routeModule.route)
            singleStepAdapter.headerClickReceiver = this

            singleStepAdapter.defaultClickReceiver = object : ObjectsRecyclerViewAdapter.ClickReceiver<ComplexStep> {
                @Suppress("NAME_SHADOWING")
                override fun onViewClicked(recyclerView: RecyclerView, view: View, position: Int, item: ComplexStep) {
                    (activity as? TripPlannerSearchActivity)?.zoomOnComplexStep(item)
                }
            }
            recycler.adapter = singleStepAdapter
        }

        return view
    }

    override fun onViewClicked(recyclerView: RecyclerView, view: View, position: Int, item: Route) {
        (activity as? TripPlannerSearchActivity)?.expandSteps()
    }

}