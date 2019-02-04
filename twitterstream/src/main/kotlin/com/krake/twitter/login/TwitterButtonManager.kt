package com.krake.twitter.login

import android.app.Activity
import android.content.Intent
import android.widget.Button
import com.krake.core.login.LoginManager
import com.krake.core.login.LoginViewManager
import com.krake.core.network.RemoteRequest
import com.krake.twitter.R
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.core.identity.TwitterLoginButton

/**
 * Login tramite Twitter. Effettuato tramite le API ufficiali di Fabric.
 * Le API vengono gestite tramite se sono configurate le stringhe:
 * R.string.twitter_consume_key e R.string.twitter_consume_secret.
 * Per le altre configurazioni fare riferimento alle API ufficiali.
 */
class TwitterButtonManager(activity: Activity, loginFragment: LoginViewManager.LoginListener) : LoginViewManager(activity, loginFragment) {

    private var twitterSession: TwitterSession? = null
    private val twitterLoginButton: TwitterLoginButton = TwitterLoginButton(getActivity())

    init {
        twitterLoginButton.callback = object : Callback<TwitterSession>() {
            override fun success(twitterSessionResult: Result<TwitterSession>) {
                twitterSession = twitterSessionResult.data
                getLoginFragment().onManagerTokenAvailable(this@TwitterButtonManager)

            }

            override fun failure(e: TwitterException)
            {
                e.toString()
            }
        }
    }

    override fun getLoginView(): Button = twitterLoginButton

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        twitterLoginButton.onActivityResult(requestCode, resultCode, data)
        return false
    }

    override fun setButtonEnabled(enabled: Boolean) {
        twitterLoginButton.isEnabled = enabled && twitterSession == null
    }

    override fun sendTokenToOrchard(): Boolean = twitterSession?.also {

        val request = RemoteRequest(activity)
                .setMethod(RemoteRequest.Method.GET)
                .setPath(activity.getString(R.string.orchard_login_url_path))
                .setQuery(getString(R.string.orchard_login_secret_parameter), it.authToken.secret)
                .setQuery(getString(R.string.orchard_login_token_key), it.authToken.token)
                .setQuery(getString(R.string.orchard_login_provider_key), getString(R.string.orchard_login_provider_twitter))
                .setQuery("Culture", getString(R.string.orchard_language))

        LoginManager.shared.login(activity, request, true)
    } != null

    override fun logout() {
        twitterSession = null
    }
}