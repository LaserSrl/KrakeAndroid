package com.krake.events

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.krake.core.app.ContentItemListMapActivity
import com.krake.core.app.DateTimePickerFragment
import com.krake.core.component.annotation.BundleResolvable
import com.krake.core.component.module.OrchardComponentModule
import com.krake.core.component.module.TermsModule
import com.krake.core.extension.putModules
import com.krake.core.model.TermPart
import com.krake.core.util.toDate
import com.krake.events.component.module.EventComponentModule
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by zaninom on 23/02/2017.
 */
open class EventActivity : ContentItemListMapActivity(), DateTimePickerFragment.OnDateTimePickerListener {
    companion object {
        private const val OUT_STATE_START_DATE_SELECTED = "otsDateStartSelected"
        private const val OUT_STATE_END_DATE_SELECTED = "otsEndDateSelected"
    }

    protected var startSelectedDate: Date? = null
    protected var endSelectedDate: Date? = null
    private val dateFormat = DateFormat.getDateInstance()
    private lateinit var orchardDateFormat: DateFormat
    private var userCanSelectEndDate: Boolean = false

    @BundleResolvable
    lateinit var eventComponentModule: EventComponentModule

    override fun onCreate(savedInstanceState: Bundle?, layout: Int) {
        super.onCreate(savedInstanceState, layout)

        userCanSelectEndDate = eventComponentModule.endDateSelectableByUser

        orchardDateFormat = SimpleDateFormat(eventComponentModule.remoteDateFormat, Locale.getDefault())

        if (savedInstanceState == null) {
            eventComponentModule.startDateMillis?.let { startSelectedDate = Date(it) }
            eventComponentModule.endDateMillis?.let { endSelectedDate = Date(it) }
        } else {
            savedInstanceState.getLong(OUT_STATE_START_DATE_SELECTED).let { startSelectedDate = Date(it) }

            endSelectedDate = if (userCanSelectEndDate)
                savedInstanceState.getLong(OUT_STATE_END_DATE_SELECTED).let { Date(it) }
            else
                startSelectedDate
        }

        startSelectedDate?.let {
            supportActionBar?.subtitle = getFormattedSubtitle(it, endSelectedDate)
        }
    }

    private fun getFormattedSubtitle(startDate: Date, endDate: Date?): String {
        val formattedStartDate = dateFormat.format(startDate)
        return if (!userCanSelectEndDate || endDate == null)
        {
            formattedStartDate
        } else {
            val formattedEndDate = dateFormat.format(endDate)
            if (formattedEndDate == formattedStartDate) {
                formattedStartDate
            } else {
                String.format("%s - %s", formattedStartDate, formattedEndDate)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        startSelectedDate?.let { outState.putLong(OUT_STATE_START_DATE_SELECTED, it.time) }
        if (userCanSelectEndDate && endSelectedDate != null)
            outState.putLong(OUT_STATE_END_DATE_SELECTED, endSelectedDate!!.time)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_event, menu)
        super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_select_date) {
            val startSelectedDate = this.startSelectedDate
                    ?: eventComponentModule.minDateMillis?.toDate() ?: Date()
            val endSelectedDate = if (this.userCanSelectEndDate && this.endSelectedDate == null) startSelectedDate else this.endSelectedDate

            if (userCanSelectEndDate)
                DateTimePickerFragment.newInstance(startSelectedDate, endSelectedDate!!,
                                                   eventComponentModule.minDateMillis?.toDate(),
                                                   eventComponentModule.maxDateMillis?.toDate()).show(supportFragmentManager, "Date")
            else
                DateTimePickerFragment.newInstance(startSelectedDate,
                                                   eventComponentModule.minDateMillis?.toDate(),
                                                   eventComponentModule.maxDateMillis?.toDate()).show(supportFragmentManager, "Date")

            return true
        }

        return super.onOptionsItemSelected(item)
    }

    protected fun updateDateSelected(startDate: Date, endDate: Date?) {
        startSelectedDate = startDate
        endSelectedDate = endDate ?: startSelectedDate

        supportActionBar?.subtitle = getFormattedSubtitle(startDate, endDate)

        var formattedDate = orchardDateFormat.format(startSelectedDate)

        gridFragment.setExtraParameter(getString(R.string.orchard_event_begin_date_key), formattedDate, false)

        if (mapFragment != null) {
            mapFragment.setExtraParameter(getString(R.string.orchard_event_begin_date_key), formattedDate, false)
        }

        formattedDate = orchardDateFormat.format(endSelectedDate)

        if (mapFragment != null)
            mapFragment.setExtraParameter(getString(R.string.orchard_event_end_date_key), formattedDate, false)

        gridFragment.setExtraParameter(getString(R.string.orchard_event_end_date_key), formattedDate, true)

    }

    override fun getFragmentCreationExtras(mode: FragmentMode): Bundle
    {
        val initialBundle = super.getFragmentCreationExtras(mode)

        if (startSelectedDate == null || endSelectedDate == null)
            return initialBundle

        val orchardModule = OrchardComponentModule()
        orchardModule.readContent(this, initialBundle)

        orchardModule.putExtraParameter(getString(R.string.orchard_event_begin_date_key), orchardDateFormat.format(startSelectedDate))
        orchardModule.putExtraParameter(getString(R.string.orchard_event_end_date_key), orchardDateFormat.format(endSelectedDate))

        val bundle = Bundle()
        bundle.putAll(initialBundle)
        bundle.putModules(this, orchardModule)
        return bundle
    }

    override fun onDatePicked(startDate: Date, endDate: Date?) {
        updateDateSelected(startDate, endDate)
    }

    override fun selectedFilterTermPart(termPart: TermPart?, termsModule: TermsModule) {
        super.selectedFilterTermPart(termPart, termsModule)
        /* TODO: verificare perchè presente  if (startSelectedDate != null)
              updateDateSelected(startSelectedDate!!, endSelectedDate)

  */
    }
}
