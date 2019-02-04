package com.krake.core.location.settings.presenter

import android.app.Activity
import android.support.design.widget.Snackbar
import android.view.View
import com.krake.core.widget.SnackbarUtils

/**
 * Implementazione di [GpsSettingsPresenter] che mostra i messaggi in una [Snackbar].
 */
open class SnackbarGpsSettingsPresenter : GpsSettingsPresenter {
    override fun showSettingsChangeUnavailableMessage(activity: Activity, message: String) {
        SnackbarUtils.createSnackbar(snackBarView(activity), message, Snackbar.LENGTH_LONG).show()
    }

    /**
     * Specifica la [View] in cui verrà mostrata la [Snackbar].
     *
     * @param activity [Activity] utilizzata per ricavare la [View] in cui verrà mostrata la [Snackbar].
     * @return [View] che deve mostrare la [Snackbar].
     */
    protected open fun snackBarView(activity: Activity): View =
            activity.findViewById(android.R.id.content)
}