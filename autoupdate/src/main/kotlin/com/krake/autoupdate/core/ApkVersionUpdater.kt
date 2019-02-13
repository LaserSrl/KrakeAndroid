package com.krake.autoupdate.core

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.annotation.RequiresPermission
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.example.autoupdate.R
import com.krake.autoupdate.KrakeVersionChecker
import com.krake.core.permission.PermissionListener
import com.krake.core.permission.PermissionManager
import com.krake.core.thread.async

/**
 * Creates a new instance of [ApkVersionUpdater] to get apk callbacks.

 * @param context  current [Context]
 * *
 * @param listener used to have callback on apk updates
 */
open class ApkVersionUpdater private constructor(private var context: Context,
                                            private val lifecycleOwner: LifecycleOwner,
                                            private val versionChecker: VersionChecker,
                                            private var listener: Listener?,
                                            private val activityStartupClass: Class<out Activity>,
                                            private val currentBuildVersion: Long,
                                            permissionManager: PermissionManager) : PermissionListener,
        LifecycleObserver
{

    private val downloadReceiver = DownloadCompleteReceiver()
    private var updateUrl: String? = null
    private val sharedPreferences: SharedPreferences by lazy { context.getSharedPreferences(PREFS_VERSION_UPDATER, Context.MODE_PRIVATE) }

    private val permissionManager by lazy {
        permissionManager
                .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .addListener(this).apply {
                    create()
                }
    }

    init
    {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    fun request()
    {
        if (isDownloadingUpdate())
        {
            listener?.onApkInDownload()
            registerCallbackForUpdate()
        }
        else
        {
            listener?.onInitCheckApkVersion()
            checkForUpdates()
        }
    }

    /**
     * Check if the DownloadManager is downloading an update.

     * @return true if the update is being downloaded
     */
    private fun isDownloadingUpdate(): Boolean
    {
        val downloadId = sharedPreferences.getLong(PREFS_VERSION_UPDATER_CURRENT_DOWNLOAD_ID_KEY, -1)
        if (downloadId == -1L)
            return false

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val q = DownloadManager.Query()
        q.setFilterById(downloadId)
        val c = manager.query(q)
        if (c != null)
        {
            if (c.moveToFirst())
            {
                val status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))
                return status == DownloadManager.STATUS_RUNNING || status == DownloadManager.STATUS_PENDING
            }
            c.close()
        }
        return false
    }

    /**
     * Register the receiver for an update.
     */
    private fun registerCallbackForUpdate()
    {
        context.registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    /**
     * Check for new updates with a WS request.
     */
    private fun checkForUpdates()
    {
        async {
            versionChecker.loadApkVersion()
        }.completed {
            onLoadCompleted(it)
        }.error {
            it.printStackTrace()
            listener?.onUpdateRequestError()
        }.load()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    protected fun onDestroy()
    {
        destroy()
        lifecycleOwner.lifecycle.removeObserver(this)
    }

    @SuppressLint("MissingPermission")
    override fun onPermissionsHandled(acceptedPermissions: Array<out String>)
    {
        if (acceptedPermissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE))
        {
            downloadUpdate()
        }
        else
        {
            listener?.onPermissionDenied()
        }
    }

    /**
     * Destroy the updater to avoid memory leak.
     * The download receiver will be unregistered.
     */
    private fun destroy()
    {
        try
        {
            context.unregisterReceiver(downloadReceiver)
        }
        catch (ignored: IllegalArgumentException)
        {
            // receiver wasn't registered
        }
        listener = null
    }

    private fun onLoadCompleted(result: ApkVersion)
    {
        result.message?.let {message ->
            listener?.onMessageAvailable(message)
        }

        if (result.versionCode != null && result.versionCode!! > currentBuildVersion)
        {
            updateUrl = result.apkUrl
            permissionManager.request()
        }
        else
        {
            listener?.onUpdateUnavailable()
        }
    }

    @RequiresPermission(value = Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private fun downloadUpdate()
    {
        listener?.onApkDownloadStarted()
        downloadApkFromUrl()
        registerCallbackForUpdate()
    }

    private fun downloadApkFromUrl()
    {
        val fileName = extractedFilenameFromUrl(updateUrl!!, APK_EXTENSION)
        if (fileName == null)
        {
            listener?.onApkDownloadFailed()
            return
        }

        val state = context.packageManager.getApplicationEnabledSetting("com.android.providers.downloads")
        if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER ||
                state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED)
        {

            // Cannot download using download manager because is disabled
            listener?.onApkDownloadFailedForDownloadManagerIssue()
            return
        }

        // use Download manager to start Download
        val req = DownloadManager.Request(Uri.parse(updateUrl))

        // set request parameters
        req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setTitle(fileName)
                .setAllowedOverRoaming(false)
                // show a notification while downloading and after apk is downloaded
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                // destination in download folder
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setMimeType(APK_MIME_TYPE)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        // enqueue the request to start it
        val downloadId = downloadManager.enqueue(req)
        // Insert it in preferences
        sharedPreferences
                .edit()
                .putLong(PREFS_VERSION_UPDATER_CURRENT_DOWNLOAD_ID_KEY, downloadId)
                .apply()
    }

    /**
     * Manage the action added to the received [Intent].

     * @param intent [Intent] received from the receiver.
     */
    fun manageActionFromIntent(intent: Intent)
    {
        if (ACTION_DOWNLOAD_COMPLETED == intent.action)
        {
            val fileUri = intent.getStringExtra(ARG_APK_DOWNLOADED_URI)

            val apkIntent = Intent()
            apkIntent.action = Intent.ACTION_VIEW
            apkIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            apkIntent.setDataAndType(Uri.parse(fileUri), ApkVersionUpdater.APK_MIME_TYPE)

            if (apkIntent.resolveActivity(context.packageManager) != null)
            {
                // Intent can be handled.
                context.startActivity(apkIntent)
                listener?.onNewApkStarted()
            }
            else
            {
                listener?.onUnableToStartNewApk()
            }

        }
        else
        {
            listener?.onApkDownloadFailed()
        }
    }

    inner class DownloadCompleteReceiver : BroadcastReceiver()
    {

        override fun onReceive(context: Context, intent: Intent)
        {
            // get request bundle
            val extras = intent.extras
            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val fileUri = manager.getUriForDownloadedFile(extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID))
            val downloadId = extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID)

            sharedPreferences
                    .edit()
                    .remove(PREFS_VERSION_UPDATER_CURRENT_DOWNLOAD_ID_KEY)
                    .apply()

            val action: String
            if (fileUri != null)
            {
                val q = DownloadManager.Query()
                q.setFilterById(downloadId)
                val c = manager.query(q)
                if (c != null)
                {
                    action = if (c.moveToFirst())
                    {
                        val status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))
                        if (status == DownloadManager.STATUS_SUCCESSFUL)
                        {
                            ACTION_DOWNLOAD_COMPLETED
                        }
                        else
                        {
                            ACTION_DOWNLOAD_CANCELED
                        }
                    }
                    else
                    {
                        ACTION_DOWNLOAD_CANCELED
                    }
                    c.close()
                }
                else
                {
                    action = ACTION_DOWNLOAD_CANCELED
                }
            }
            else
            {
                // Download canceled
                action = ACTION_DOWNLOAD_CANCELED
            }

            val receiveIntent = Intent(context, activityStartupClass)
            receiveIntent.action = action
            receiveIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            if (action == ACTION_DOWNLOAD_COMPLETED)
            {
                receiveIntent.putExtra(ARG_APK_DOWNLOADED_URI, fileUri!!.toString())
            }
            context.startActivity(receiveIntent)

            try
            {
                context.unregisterReceiver(this)
            }
            catch (ignored: IllegalArgumentException)
            {
                // receiver wasn't registered
            }

        }
    }

    interface Listener
    {
        /**
         * called when the check for the apk is started
         */
        fun onInitCheckApkVersion() { }

        /**
         * called when an error occurred while the [versionChecker] is called
         */
        fun onUpdateRequestError() { }

        /**
         * called when the version checked is lower then the current version
         */
        fun onUpdateUnavailable() { }

        /**
         * called when a message is available when the apk version is downloaded
         */
        fun onMessageAvailable(message: String) { }

        /**
         * called when the permissions aren't granted
         */
        fun onPermissionDenied() { }

        /**
         * called when the apk download is started
         */
        fun onApkDownloadStarted() { }

        /**
         * called when the apk is in download
         */
        fun onApkInDownload() { }

        /**
         * called when there is an error in the download
         */
        fun onApkDownloadFailed() { }

        /**
         * called when the downloadManager is disabled
         */
        fun onApkDownloadFailedForDownloadManagerIssue() { }

        /**
         * called when the apk is downloaded and launched
         */
        fun onNewApkStarted() { }

        /**
         * called when the apk is not launched because there aren't application for start the apk
         */
        fun onUnableToStartNewApk() { }
    }

    class Builder(private val context: Context,
                  private val lifecycleOwner: LifecycleOwner,
                  private val listener: Listener,
                  private val currentBuildVersion: Long)
    {

        private var activityStartupClass: Class<out Activity>? = null
        private var versionChecker: VersionChecker? = null
        private var permissionManager: PermissionManager? = null

        /**
         * [Activity] class that will be launch when the apk is downloaded,
         * @default activity passed in the constructor
         */
        fun withActivityToStart(activity: Class<out Activity>) = apply { this.activityStartupClass = activity }

        /**
         * [PermissionManager] used for ask storage permission for store the apk to download
         */
        fun withPermissionManager(permissionManager: PermissionManager) = apply { this.permissionManager = permissionManager }

        /**
         * [VersionChecker] used for check the version
         */
        fun withVersionChecker(versionChecker: VersionChecker) = apply { this.versionChecker = versionChecker }

        fun build(): ApkVersionUpdater
        {
            if (activityStartupClass == null)
                activityStartupClass = (context as Activity)::class.java

            if (versionChecker == null)
                versionChecker = KrakeVersionChecker(context.getString(R.string.apk_version_url))

            if (permissionManager == null)
            {
                permissionManager = PermissionManager(context as FragmentActivity)
                        .permanentlyRefusedMsg(context.getString(R.string.apk_dowloader_permanentlymsg))
            }

            return ApkVersionUpdater(context, lifecycleOwner, versionChecker!!, listener, activityStartupClass!!, currentBuildVersion, permissionManager!!)
        }
    }

    companion object
    {
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
        private const val APK_EXTENSION = ".apk"
        private const val ARG_APK_DOWNLOADED_URI = "argApkDownloadedUri"

        private const val ACTION_DOWNLOAD_COMPLETED = "actionDownloadCompleted"
        private const val ACTION_DOWNLOAD_CANCELED = "actionDownloadCanceled"

        private const val PREFS_VERSION_UPDATER = "PREFS_VERSION_UPDATER"
        private const val PREFS_VERSION_UPDATER_CURRENT_DOWNLOAD_ID_KEY = "PREFS_VERSION_UPDATER_CURRENT_DOWNLOAD_ID_KEY"

        private fun extractedFilenameFromUrl(url: String, fileExtension: String): String?
        {
            val partialUrl = url.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (partialUrl.isNotEmpty())
            {
                val finalUrlSegment = partialUrl[partialUrl.size - 1]
                if (finalUrlSegment.contains(fileExtension))
                {
                    return finalUrlSegment
                }
            }
            return null
        }
    }
}

