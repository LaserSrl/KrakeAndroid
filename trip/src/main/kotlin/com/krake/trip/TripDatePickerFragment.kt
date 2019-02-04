package com.krake.trip

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.DialogFragment
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import com.krake.core.component.annotation.BundleResolvable
import com.krake.core.extension.putModules
import com.krake.core.view.TabLayoutHelper
import com.krake.trip.component.module.TripPlannerModule
import java.util.*

/**
 * classe per scegliere il tipo di data per la ricerca.
 * Created by joel on 27/04/17.
 */
class TripDatePickerFragment : DialogFragment(), DialogInterface.OnClickListener, TabLayout.OnTabSelectedListener {

    @BundleResolvable
    var tripModule: TripPlannerModule = TripPlannerModule()

    lateinit private var mTimePicker: TimePicker
    lateinit private var mDatePicker: DatePicker
    lateinit var mTabLayout: TabLayout

    private var mListener: OnTripDateTimePickerListener? = null

    override fun onAttach(activity: Context?) {
        super.onAttach(activity)

        if (activity is OnTripDateTimePickerListener)
            mListener = activity
        else
            throw ClassCastException(context.toString() + " must implement " + OnTripDateTimePickerListener::class.java.simpleName)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null)
            tripModule.readContent(activity!!, savedInstanceState)
        else if (arguments != null)
            tripModule.readContent(activity!!, arguments!!)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val alert = AlertDialog.Builder(activity!!)

        alert.setView(createView(savedInstanceState))

        alert.setNegativeButton(android.R.string.cancel, null)
        alert.setPositiveButton(android.R.string.ok, this)

        return alert.create()
    }

    private fun createView(savedInstanceState: Bundle?): View {
        val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // Inflate the layout for this fragment
        @SuppressLint("InflateParams") val view = inflater.inflate(R.layout.fragment_trip_date_picker, null, false)

        mTabLayout = insertTravelTabType(view.findViewById(R.id.tripDateContainer))

        mDatePicker = view.findViewById(R.id.datePicker)

        mTimePicker = view.findViewById(R.id.timePicker)
        mTimePicker.setIs24HourView(true)

        updateUIWithDate()
        for (index in 0 until mTabLayout.tabCount) {
            val tab = mTabLayout.getTabAt(index)!!

            val mode = tab.tag as? DatePlanChoice

            if (mode?.equals(tripModule.request.datePlanChoice) == true) {
                tab.select()
                break
            }
        }

        mTabLayout.addOnTabSelectedListener(this)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mTabLayout.removeOnTabSelectedListener(this)
    }

    private fun insertTravelTabType(view: ViewGroup): TabLayout {
        val activity = activity ?: throw IllegalArgumentException("The activity mustn't be null.")
        val helper = TabLayoutHelper.CreationBuilder(activity)
                .contentSelectedColor(ResourcesCompat.getColor(resources, R.color.colorAccent, null))
                .contentUnselectedColor(ResourcesCompat.getColor(resources, R.color.colorPrimary, null))
                .bgColor(Color.TRANSPARENT)
                .tabShowImage(false)
                .tabShowTitle(true)
                .build()

        helper.addTab(DatePlanChoice.DEPARTURE.getVisibleName(activity),
                null,
                DatePlanChoice.DEPARTURE)

        helper.addTab(DatePlanChoice.ARRIVAL.getVisibleName(activity),
                null,
                DatePlanChoice.ARRIVAL)

        val tab = helper.layout()

        tab.addOnTabSelectedListener(this)

        view.addView(tab, 0)

        return tab
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putModules(activity!!, tripModule)
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }


    @Suppress("DEPRECATION")
    private fun updateUIWithDate() {
        val calendar = Calendar.getInstance()

        calendar.timeInMillis = tripModule.request.dateSelectedForPlan.time

        mDatePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mTimePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
            mTimePicker.minute = calendar.get(Calendar.MINUTE)
        } else {
            mTimePicker.currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            mTimePicker.currentMinute = calendar.get(Calendar.MINUTE)
        }
    }

    override fun onClick(dialogInterface: DialogInterface, i: Int) {
        mListener?.onDatePicked(selectedDate, tripModule.request.datePlanChoice)
    }

    @Suppress("DEPRECATION")
    private val selectedDate: Date
        get() {
            val calendar = Calendar.getInstance()
            calendar.timeZone = TimeZone.getDefault()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                calendar.set(mDatePicker.year,
                        mDatePicker.month,
                        mDatePicker.dayOfMonth,
                        mTimePicker.hour,
                        mTimePicker.minute,
                        0)
            } else {
                calendar.set(mDatePicker.year,
                        mDatePicker.month,
                        mDatePicker.dayOfMonth,
                        mTimePicker.currentHour,
                        mTimePicker.currentMinute,
                        0)
            }

            calendar.add(Calendar.SECOND, 1)
            return calendar.time
        }

    override fun onTabSelected(tab: TabLayout.Tab) {

        tripModule.request.datePlanChoice = tab.tag as DatePlanChoice
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {

    }

    override fun onTabReselected(tab: TabLayout.Tab) {

    }

    /**
     * Interfaccia che devono implementare le activity per poter comunicare col Fragment
     */
    interface OnTripDateTimePickerListener {

        /**
         * Informazione che l'utente ha selezionato una nuova data.
         */
        fun onDatePicked(date: Date, dateChoice: DatePlanChoice)
    }
}