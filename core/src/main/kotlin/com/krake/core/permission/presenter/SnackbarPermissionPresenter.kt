package com.krake.core.permission.presenter

import android.annotation.TargetApi
import android.app.Activity
import android.os.Build
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.krake.core.permission.PermissionFragment
import com.krake.core.widget.SnackbarUtils

/**
 * Implementazione di [PermissionPresenter] che mostra i messaggi in una [Snackbar].
 */
open class SnackbarPermissionPresenter : PermissionPresenter {
    @TargetApi(Build.VERSION_CODES.M)
    override fun showRationalMessage(activity: Activity, permissionFragment: PermissionFragment, message: String) {
        // Mostra la Snackbar con l'azione per poter richiedere nuovamente i permessi.
        SnackbarUtils.createSnackbar(snackBarView(activity), message, Snackbar.LENGTH_LONG)
                .setAction(android.R.string.ok, {
                    // Richiede nuovamente i permessi.
                    permissionFragment.requestPermissions()
                })
                .show()
    }

    override fun showPermanentlyDeniedMessage(activity: Activity, permissionFragment: PermissionFragment, message: String) {
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