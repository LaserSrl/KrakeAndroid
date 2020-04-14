package com.krake.surveys.app

import androidx.fragment.app.Fragment
import com.krake.core.app.AnalyticsApplication
import com.krake.core.app.DateTimePickerFragment
import com.krake.core.app.LoginAndPrivacyActivity
import com.krake.core.component.annotation.BundleResolvable
import com.krake.surveys.R
import com.krake.surveys.component.module.SurveyComponentModule
import java.util.*

/**
 * Classe per mostrare i contenuti di tipo [Questionnaire] di Orchard.
 *
 * La classe gestisce autonomamente un fragment [SurveyFragment] e all'occorrenza un ulteriore fragment per la login.
 */
open class SurveyActivity : LoginAndPrivacyActivity(),
    SurveyListener,
    DateTimePickerFragment.OnDateTimePickerListener
{
    @BundleResolvable
    lateinit var surveyComponentModule: SurveyComponentModule

    override fun onStart() {
        super.onStart()
        (application as AnalyticsApplication).logItemList(surveyComponentModule.analyticsName ?: title.toString())
    }

    override fun changeContentVisibility(visible: Boolean) {
        if (visible) {
            var surveyFragment: Fragment? = supportFragmentManager.findFragmentById(R.id.activity_layout_coordinator)
            if (surveyFragment == null) {
                surveyFragment = SurveyFragment()
                surveyFragment.arguments = intent.extras
                supportFragmentManager.beginTransaction().replace(R.id.activity_layout_coordinator, surveyFragment).commitAllowingStateLoss()
            }
        }
    }

    override fun onSurveySent(fragment: SurveyFragment)
    {
        finish()
    }

    override fun noSurveyAvailable(fragment: SurveyFragment)
    {
        finish()
    }

    override fun onDatePicked(startDate: Date, endDate: Date?)
    {
        (supportFragmentManager.findFragmentById(R.id.activity_layout_coordinator) as DateTimePickerFragment.OnDateTimePickerListener).onDatePicked(startDate, endDate)
    }
}

interface SurveyListener
{
    fun onSurveySent(fragment: SurveyFragment)
    fun noSurveyAvailable(fragment: SurveyFragment)
}