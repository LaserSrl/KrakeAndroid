package com.krake.core.app

import android.app.Activity
import android.os.Bundle

/**
 * [Activity] that must be set in the manifest as launch activity when the application implements [CustomLaunchIntentProvider]
 * so when the user add a custom launch intent he must add in the manifest:
   <activity android:name="com.krake.core.app.LaunchActivity">
        <intent-filter>
            <action android:name="android.intent.action.MAIN"/>
            <category android:name="android.intent.category.LAUNCHER"/>
        </intent-filter>
   </activity>
 */
class LaunchActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(android.R.style.Theme_NoDisplay)
        super.onCreate(null)
        if (application is CustomLaunchIntentProvider) {
            startActivity((application as CustomLaunchIntentProvider).getLaunchIntent())
            finish()
        } else {
            throw RuntimeException("LaunchActivity must be used only when the Application implements CustomLaunchIntentProvider")
        }
    }
}