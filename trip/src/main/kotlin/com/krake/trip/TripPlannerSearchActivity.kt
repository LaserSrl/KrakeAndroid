package com.krake.trip

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.krake.core.address.PlaceResult
import com.krake.core.app.LoginAndPrivacyActivity
import com.krake.core.widget.SafeBottomSheetBehavior
import com.krake.trip.component.module.RouteModule
import com.krake.trip.component.module.RoutesModule
import java.util.*

class TripPlannerSearchActivity : LoginAndPrivacyActivity(),
        TripDatePickerFragment.OnTripDateTimePickerListener
{
    companion object {
        private const val PLANNED_TRIP_FRAGMENT_TAG = "PlannedTrip"
        private const val PLANNED_ROUTES_FRAGMENT_TAG = "PlannedRoutes"
        private const val PLANNED_MAP_FRAGMENT_TAG = "PlannedMap"
    }

    var listElementsBehavior: SafeBottomSheetBehavior<View>? = null

    private var searchFragmentIdentifier: Int = R.id.app_bar_layout
    private val progressView by lazy { findViewById(R.id.searchProgress) as ProgressBar }

    private lateinit var tripPlanTask: TripPlanViewModel

    override fun onCreate(savedInstanceState: Bundle?, layout: Int) {
        super.onCreate(savedInstanceState, layout)
        tripPlanTask = ViewModelProviders.of(this).get(OpenTripPlanTask::class.java)

        tripPlanTask.loading.observe(this, Observer {
            progressView.visibility = if (it == true) View.VISIBLE else View.GONE
        })

        tripPlanTask.tripResult.observe(this, Observer<TripResult> { t ->
            if (t != null) {
                if (t.result != null) {
                    val plan = t.result
                    showPlannedTrip(plan)
                } else if (t.error != null) {
                    AlertDialog.Builder(this@TripPlannerSearchActivity)
                            .setTitle(R.string.trip_planning_failed)
                            .setMessage(t.error.originalMessage)
                            .setNeutralButton(android.R.string.ok, null)
                            .show()
                }
            }
        })

        searchFragmentIdentifier = if (!resources.getBoolean(R.bool.is_tablet)) R.id.app_bar_layout else R.id.searchContainer

        layoutInflater.inflate(R.layout.activity_trip_planner, mCoordinator, true)

        findViewById<SwipeRefreshLayout?>(R.id.swipe_refresh)?.isEnabled = false

        if (supportFragmentManager.findFragmentById(searchFragmentIdentifier) == null) {
            val tripPlannerSearchFragment = TripPlannerSearchFragment()
            tripPlannerSearchFragment.arguments = intent.extras

            supportFragmentManager
                    .beginTransaction()
                    .add(searchFragmentIdentifier, tripPlannerSearchFragment)
                    .commit()
        }

        val tripResultStepsView = findViewById<View>(R.id.tripResultSteps)

        val searchFragmentView = findViewById<View>(searchFragmentIdentifier)

        //set search fragment elevation higher than the app bar elevation,
        //in this way the search fragment can slide above the app bar
        searchFragmentView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                searchFragmentView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                ViewCompat.setElevation(tripResultStepsView, ViewCompat.getElevation(searchFragmentView))

                //if i don't set false to drag callback in appbar behavior, the user can't scroll the list when is above the appbar
                ((searchFragmentView.layoutParams as? CoordinatorLayout.LayoutParams)?.behavior as? AppBarLayout.Behavior)?.setDragCallback(object : AppBarLayout.Behavior.DragCallback()
                                                                                                                                            {
                    override fun canDrag(p0: AppBarLayout): Boolean = false
                })
            }
        })

        val behavior = (tripResultStepsView.layoutParams as? CoordinatorLayout.LayoutParams)?.behavior
        listElementsBehavior = behavior as? SafeBottomSheetBehavior<View>

        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.tripResultMap, SupportMapFragment())
                    .commit()
        }

        if (!resources.getBoolean(R.bool.is_tablet))
            supportActionBar?.hide()
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)

        //if the fragment attached is TransitRoutesAlternativeFragment or RouteStepsFragment
        //i have to attach the map fragment only when the behavior of this fragment become expanded.
        if (fragment is TransitRoutesAlternativeFragment || fragment is RouteStepsFragment) {

            val map = if (fragment is TransitRoutesAlternativeFragment) {
                SupportMapFragment()
            } else {
                RouteMapFragment().apply { this.arguments = fragment.arguments }
            }

            val operation: () -> Unit = {
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.tripResultMap, map)
                        .addToBackStack(PLANNED_MAP_FRAGMENT_TAG)
                        .commitAllowingStateLoss()
            }

            //if the list has the behavior, execute the operation only when the behavior is expanded,
            //else execute the operation immediately (in tablet case)
            if (listElementsBehavior != null && listElementsBehavior!!.state != STATE_EXPANDED) {
                listElementsBehavior!!.addBottomSheetCallback(object : SafeBottomSheetBehavior.BottomSheetStateCallback() {
                    override fun onStateWillChange(bottomSheet: View, newState: Int) { }
                    override fun onSlide(p0: View, p1: Float) { }
                    override fun onStateChanged(p0: View, p1: Int) {
                        if (p1 == STATE_EXPANDED) {
                            listElementsBehavior?.removeBottomSheetCallback(this)
                            operation()
                        }
                    }
                })
                listElementsBehavior!!.peekHeight = resources.getDimensionPixelSize(R.dimen.tripPlanStepsPeekHeight)
                listElementsBehavior!!.setStateAndNotify(STATE_EXPANDED)
            } else {
                operation()
            }
        }
    }

    private fun tripSearchFragment(): TripPlannerSearchFragment? = supportFragmentManager.findFragmentById(searchFragmentIdentifier) as? TripPlannerSearchFragment

    override fun changeContentVisibility(visible: Boolean) {}

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 2) {

            //if the last backstack is the transaction with the map fragment, i have to call popBackStack twice, because the first restore the
            //map fragment, and the second restore the list fragment, i can't put all the operations in one transaction because is necessary to show the map fragment
            //only when the list behavior become expanded, so i have to perform two transactions
            val backStackName = supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 1).name
            if (backStackName == PLANNED_MAP_FRAGMENT_TAG)
                supportFragmentManager.popBackStack()

            super.onBackPressed()

            listElementsBehavior?.setStateAndNotify(STATE_EXPANDED)
        } else {
            if (listElementsBehavior?.state == STATE_EXPANDED)
                listElementsBehavior?.setStateAndNotify(STATE_COLLAPSED)
            else
                finish()
        }
    }

    override fun onDatePicked(date: Date, dateChoice: DatePlanChoice) {
        tripSearchFragment()?.onDatePicked(date, dateChoice)
    }

    private fun showPlannedTrip(trip: TripPlanResult) {
        val elementsCount = supportFragmentManager.backStackEntryCount

        for (index in 0 until elementsCount)
            supportFragmentManager.popBackStackImmediate()

        if (trip.request.travelMode == TravelMode.TRANSIT && trip.routes.size > 1)
            showRoutes(trip.routes)
        else
            showRoute(trip.routes.first())
    }

    private fun showRoutes(routes: Array<Route>) {
        val routeBundle = RoutesModule()
                .routes(routes.asList())
                .writeContent(this)

        val routeFragment = TransitRoutesAlternativeFragment().apply { this.arguments = routeBundle }

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.tripResultSteps, routeFragment)
                .addToBackStack(PLANNED_ROUTES_FRAGMENT_TAG)
                .commit()
    }

    fun showRoute(route: Route) {
        val routeBundle = RouteModule()
                .route(route)
                .writeContent(this)

        val steps = RouteStepsFragment().apply { this.arguments = routeBundle }

        supportFragmentManager.popBackStackImmediate(PLANNED_TRIP_FRAGMENT_TAG, POP_BACK_STACK_INCLUSIVE)

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.tripResultSteps, steps)
                .addToBackStack(PLANNED_TRIP_FRAGMENT_TAG)
                .commit()
    }

    fun expandSteps() {
        listElementsBehavior?.setStateAndNotify(STATE_EXPANDED)
    }

    fun zoomOnStep(step: SingleStep) {
        zoomMapOnStep(step.from, step.to)
    }

    fun zoomOnComplexStep(step: ComplexStep) {
        zoomMapOnStep(step.from, step.to)
    }

    private fun zoomMapOnStep(from: PlaceResult, to: PlaceResult?) {
        listElementsBehavior?.setStateAndNotify(STATE_COLLAPSED)

        val builder = LatLngBounds.builder().include(from.latLng)

        if (to != null)
            builder.include(to.latLng)

        val map = supportFragmentManager.findFragmentById(R.id.tripResultMap) as? RouteMapFragment

        map?.let {
            map.zoom(builder.build())
        }
    }
}
