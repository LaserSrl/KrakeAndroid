package com.krake.trip

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import com.krake.core.component.annotation.BundleResolvable
import com.krake.core.component.base.ComponentManager
import com.krake.core.widget.ObjectsRecyclerViewAdapter
import com.krake.trip.component.module.RoutesModule

/**
 * Classe per mostrare le alternative dei percorsi.
 * Viene usata solo quando ci sono più percorsi possibili e sono pianificati con #travelMode #TRANSIT
 * Created by joel on 27/04/17.
 */

class TransitRoutesAlternativeFragment : Fragment(), ObjectsRecyclerViewAdapter.ClickReceiver<Route> {

    @BundleResolvable
    lateinit var routesModule: RoutesModule

    lateinit var routesAdapter: RoutesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ComponentManager.resolveArguments(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_trip_selection_list, container, false)
        val recyclerView: RecyclerView = view.findViewById(android.R.id.list)
        view.findViewById<View>(android.R.id.progress)?.visibility = GONE

        routesAdapter = RoutesAdapter(activity!!, routesModule.routes)
        routesAdapter.defaultClickReceiver = this
        recyclerView.adapter = routesAdapter

        return view
    }

    override fun onViewClicked(recyclerView: RecyclerView, view: View, position: Int, item: Route) {
        (activity as? TripPlannerSearchActivity)?.showRoute(item)
    }
}