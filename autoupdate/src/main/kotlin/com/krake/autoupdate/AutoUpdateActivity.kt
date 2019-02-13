package com.krake.autoupdate

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.autoupdate.R
import com.krake.autoupdate.core.ApkVersionUpdater

/**
 * Default Activity Wrapper to use for the auto check.
 * Create an activity that extend this class and override currentBuildVersion with BuildConfig.VERSION_CODE and
 * activityToLaunch with the activity to launch after the check finished.
 * Add the activity created as launcher activity and set apk_activity_to_launch with the activity class to
 * launch when the apk check is finished, for example: com.mykrake.testapp.MainActivity
 */
@Suppress("UNCHECKED_CAST")
abstract class AutoUpdateActivity : AppCompatActivity()
{
    private val apkVersionUpdater by lazy {
        ApkVersionUpdater.Builder(this, this, buildPresenter(), currentBuildVersion())
                .build()
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_update)

        apkVersionUpdater.request()
    }

    override fun onNewIntent(intent: Intent)
    {
        super.onNewIntent(intent)
        apkVersionUpdater.manageActionFromIntent(intent)
    }

    abstract fun currentBuildVersion(): Long

    abstract fun activityToLaunch(): Class<out AppCompatActivity>

    open fun buildPresenter(): AutoUpdatePresenter {
        return AutoUpdatePresenter(activityf = this,
                activityToLaunch = activityToLaunch(),
                listener = null,
                blockWhenCheckFailed = true)
    }
}