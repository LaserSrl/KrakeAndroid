package com.krake.autoupdate

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Intent
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.example.autoupdate.R
import com.krake.autoupdate.core.ApkVersionUpdater
import com.krake.core.gcm.OrchardContentNotifier

/**
 * Wrapper that handles the presentation for the callbacks of the [ApkVersionUpdater]
 * the activity can have a [progressView] and a [progressTextView].
 * the application must specify an activity to launch when the check is finished with the string apk_activity_to_launch
 * for example: com.mykrake.testapp.MainActivity
 */
open class AutoUpdatePresenter(var activityf: FragmentActivity?,
                               private val activityToLaunch: Class<out AppCompatActivity>?,
                               private var listener: Listener? = null,
                               private val blockWhenCheckFailed: Boolean = true) : ApkVersionUpdater.Listener,
        LifecycleObserver
{
    private var activity = activityf!!
    private val progressView by lazy { activity.findViewById<ProgressBar>(android.R.id.progress) }
    private val progressTextView by lazy { activity.findViewById<TextView>(R.id.progressMessageTextView) }

    init
    {
        activity.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    protected fun onDestroy()
    {
        activity.lifecycle.removeObserver(this)
        listener = null
        activityf = null
    }

    override fun onInitCheckApkVersion()
    {
        showProgress(true, activity.getString(R.string.apk_new_version_check))
    }

    override fun onApkInDownload()
    {
        showProgress(true, activity.getString(R.string.apk_download_new_version))
    }

    override fun onUpdateUnavailable()
    {
        showProgress(false)
        if (activityToLaunch != null) {
            activity.startActivity(Intent(activity, activityToLaunch))
            activity.finish()
        } else {
            listener?.onCheckFinished()
        }
    }

    override fun onPermissionDenied()
    {
        AlertDialog.Builder(activity)
                .setTitle(R.string.apk_dialog_title_advice)
                .setMessage(activity.getString(R.string.apk_dowloader_permanentlymsg))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    activity.finish()
                }
                .show()
    }

    override fun onApkDownloadStarted()
    {
        showProgress(true, activity.getString(R.string.apk_download_new_version))
    }

    override fun onNewApkStarted()
    {
        activity.finish()
    }

    override fun onUnableToStartNewApk()
    {
        AlertDialog.Builder(activity)
                .setTitle(R.string.apk_dialog_title_advice)
                .setMessage(activity.getString(R.string.apk_install_new_version))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    activity.finish()
                }
                .show()
    }

    override fun onMessageAvailable(message: String)
    {
        OrchardContentNotifier.showNotification(activity,
                                                message,
                                                null,
                                                null,
                                                null,
                                                null)
    }

    override fun onApkDownloadFailed() = handleApkRequestError(activity.getString(R.string.apk_update_download_generic_error), block = true)

    override fun onUpdateRequestError() = handleApkRequestError(activity.getString(R.string.apk_update_request_generic_error), block = blockWhenCheckFailed)

    override fun onApkDownloadFailedForDownloadManagerIssue() = handleApkRequestError(activity.getString(R.string.apk_update_download_generic_error), true, activity.getString(R.string.apk_error_download_manager_not_enabled))

    /**
     * @param titleError title that will be displayed in the alert
     * @param block block the execution of the app if an error occurred
     * @param messageError optional message for the alert
     */
    private fun handleApkRequestError(titleError: String, block: Boolean, messageError: String? = null)
    {
        showProgress(false)
        val alert = android.app.AlertDialog.Builder(activity)
                .setCancelable(false)
                .setTitle(titleError)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    if (block)
                        activity.finish()
                    else
                        listener?.onErrorHandled()
                }

        messageError?.let { alert.setMessage(messageError) }
        alert.show()
    }

    private fun showProgress(visible: Boolean, message: String = activity.getString(R.string.apk_waiting))
    {
        progressView?.visibility = if (visible) View.VISIBLE else View.GONE
        progressTextView?.visibility = if (visible) View.VISIBLE else View.GONE
        progressTextView?.text = message
    }

    interface Listener {
        fun onCheckFinished()

        fun onErrorHandled()
    }
}