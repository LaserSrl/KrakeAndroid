package com.krake.facebook

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.widget.Button
import androidx.core.content.ContextCompat
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.krake.core.login.LoginFragment
import com.krake.core.login.LoginViewManager
import com.krake.core.network.RemoteRequest

/**
 * Facebook: tramite le API ufficiali FacebookSdk. Il bottone per accedere con FB viene inserito
 * se è stata configurata la stringa R.string.facebook_app_id.
 * Per le altre configurazioni fare riferimento alle API ufficiali.
 * Per supportare anche il logout vedere la classe [FacebookLogoutListener]
 */
class FacebookLoginButtonManager @SuppressLint("InflateParams")
constructor(activity: Activity, loginFragment: LoginViewManager.LoginListener) : LoginViewManager(activity, loginFragment), FacebookCallback<LoginResult> {

    private val facebookCallBackManager: CallbackManager
    private var facebookToken: AccessToken? = null
    private val facebookAuthButton: LoginButton

    init {
        val res = activity.resources

        facebookAuthButton = LoginButton(activity)
        facebookAuthButton.setReadPermissions(*res.getStringArray(R.array.facebook_permissions))
        facebookAuthButton.fragment = loginFragment as LoginFragment
        facebookCallBackManager = CallbackManager.Factory.create()
        facebookAuthButton.registerCallback(facebookCallBackManager, this)

        val paddingLeft = res.getDimensionPixelSize(R.dimen.facebook_btn_padding_left)
        val paddingRight = res.getDimensionPixelSize(R.dimen.facebook_btn_padding_right)
        val paddingTopBottom = res.getDimensionPixelSize(R.dimen.facebook_btn_padding_top_bottom)
        facebookAuthButton.setPadding(paddingLeft, paddingTopBottom, paddingRight, paddingTopBottom)

        val fbIconScale = 1.5f
        val icon = ContextCompat.getDrawable(activity, R.drawable.com_facebook_button_icon)
        icon!!.setBounds(0, 0, (icon.intrinsicWidth * fbIconScale).toInt(), (icon.intrinsicHeight * fbIconScale).toInt())
        facebookAuthButton.setCompoundDrawables(icon, null, null, null)

        val textSize = res.getDimensionPixelSize(R.dimen.facebook_btn_text_size).toFloat()
        facebookAuthButton.setTextSize(0, textSize)
        facebookAuthButton.typeface = Typeface.DEFAULT_BOLD

        facebookToken = AccessToken.getCurrentAccessToken()
    }

    override fun getLoginView(): Button = facebookAuthButton

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean =
            facebookCallBackManager.onActivityResult(requestCode, resultCode, data)

    override fun setButtonEnabled(enabled: Boolean) {
        facebookAuthButton.isEnabled = enabled && facebookToken == null
    }

    override fun sendTokenToOrchard(): Boolean = facebookToken?.let {

        val request = RemoteRequest(activity)
                .setMethod(RemoteRequest.Method.GET)
                .setQuery(getString(R.string.orchard_login_token_key), it.token)
                .setQuery(getString(R.string.orchard_login_provider_key), getString(R.string.orchard_login_provider_facebook))
                .setQuery("Culture", getString(R.string.orchard_language))
                .setPath(activity.getString(R.string.orchard_login_url_path))

        com.krake.core.login.LoginManager.shared.login(activity, request, true)
    } != null

    override fun logout() {
        LoginManager.getInstance().logOut()
        facebookToken = null
    }

    override fun onSuccess(loginResult: LoginResult) {
        facebookToken = loginResult.accessToken
        loginFragment.onManagerTokenAvailable(this@FacebookLoginButtonManager)
    }

    override fun onCancel() {
        /* empty */
    }

    override fun onError(error: FacebookException) {
        /* empty */
    }
}