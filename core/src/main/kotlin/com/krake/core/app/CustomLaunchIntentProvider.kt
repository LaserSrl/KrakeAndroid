package com.krake.core.app

import android.content.Intent

/**
 * Interface for provide a custom launch intent for the initial activity.
 * if the app implements this interface, the user must insert in the manifest:
 * <activity android:name="com.krake.core.app.LaunchActivity">
        <intent-filter>
            <action android:name="android.intent.action.MAIN"/>
            <category android:name="android.intent.category.LAUNCHER"/>
        </intent-filter>
    </activity>
 */
interface CustomLaunchIntentProvider {
    /**
     * function that handles the user to specify a custom intent for the launch of the main activity
     * @return the custom intent
     */
    fun getLaunchIntent(): Intent
}