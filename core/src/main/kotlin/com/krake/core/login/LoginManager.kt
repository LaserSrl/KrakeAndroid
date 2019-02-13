package com.krake.core.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.krake.core.Constants
import com.krake.core.OrchardError
import com.krake.core.R
import com.krake.core.gcm.TokenIDService
import com.krake.core.model.RequestCache
import com.krake.core.network.RemoteClient
import com.krake.core.network.RemoteRequest
import com.krake.core.network.readRemoteRequest
import com.krake.core.network.saveAsJson
import io.realm.Realm
import io.realm.kotlin.deleteFromRealm

class LoginManager internal constructor(context: Context)
{
    private val mutableLoggedUser: MutableLiveData<LoginUserOutput> = MutableLiveData()
    val loggedUser: LiveData<LoginUserOutput> = mutableLoggedUser
    private val mutableLoginUserError: MutableLiveData<OrchardError> = MutableLiveData()
    val loginUserError: LiveData<OrchardError> = mutableLoginUserError

    val isLogged: LiveData<Boolean> = Transformations.map(loggedUser) { source ->
        source != null
    }

    private val mutableLogginIn: MutableLiveData<Boolean> = MutableLiveData()
    val isLoginIn: LiveData<Boolean> = mutableLogginIn

    private val TOKEN_INFOS = "TokenRefreshInfos"

    init
    {
        mutableLogginIn.value = false

        val missingAuthCookie = RemoteClient.client(RemoteClient.Mode.LOGGED)
                .cookieValue(context, ".ASPXAUTH").isNullOrEmpty()

        if (!missingAuthCookie)
        {
            val savedLoginInfos = userPrefs(context).getString(USER_LOGIN_PREF, null)

            if (!TextUtils.isEmpty(savedLoginInfos))
            {
                mutableLoggedUser.value = Gson().fromJson(savedLoginInfos, LoginUserOutput::class.java)
            }
        }
        else
        {
            val savedTokenInfos = openSavePrefs(context).getString(TOKEN_INFOS, null)
            if (savedTokenInfos != null)
            {
                val request = Gson().fromJson(savedTokenInfos, JsonObject::class.java).readRemoteRequest()


                login(context, request, true)
            }
        }
    }

    @JvmOverloads
    fun login(context: Context, request: RemoteRequest, saveLoginParametersToRefreshCookie: Boolean = false)
    {

        if (saveLoginParametersToRefreshCookie)
        {
            openSavePrefs(context).edit().putString(TOKEN_INFOS, Gson().toJson(request.saveAsJson())).apply()
        }
        else
        {
            openSavePrefs(context).edit().remove(TOKEN_INFOS).apply()
        }


        request.setQuery(Constants.REQUEST_LANGUAGE_KEY, context.getString(R.string.orchard_language))
        request.setQuery("UUID", TokenIDService.getUUID(context))

        request.setHeader("Cache-Control", "no-cache, no-store, must-revalidate")
                .setHeader("Pragma", "no-cache")
                .setHeader("Expires", "0")

        mutableLogginIn.value = true


        RemoteClient.client(RemoteClient.Mode.LOGGED)
                .enqueue(request) { response, error ->

                    if (response != null)
                    {
                        val data = response.jsonObject()?.getAsJsonObject("Data")
                        if (data != null)
                        {
                            mutableLoginUserError.value = null
                            mutableLoggedUser.value = LoginUserOutput(data)
                        }
                    }
                    else
                    {
                        mutableLoginUserError.value = error
                        mutableLoggedUser.value = null
                        openSavePrefs(context).edit().remove(TOKEN_INFOS).apply()
                    }

                    mutableLogginIn.value = false
                }
    }

    fun logout()
    {
        mutableLoggedUser.value = null

        RemoteClient.clients.forEach { it.value.removeAllCookies() }
    }

    internal fun cleanSavedToken(context: Context)
    {
        openSavePrefs(context).edit().remove(TOKEN_INFOS).apply()
    }

    private fun openSavePrefs(context: Context): SharedPreferences
    {
        return context.applicationContext.getSharedPreferences("KLoginPrefs", Context.MODE_PRIVATE)!!
    }

    companion object
    {
        @JvmStatic
        lateinit var shared: LoginManager
            internal set
        val USER_LOGIN_PREF = "LoginUserOutput"

        private fun userPrefs(context: Context): SharedPreferences
        {
            return context.applicationContext.getSharedPreferences(USER_LOGIN_PREF, Context.MODE_PRIVATE)
        }

        @SuppressLint("ApplySharedPref")
        internal fun updateSavedLoginOutput(context: Context, loggedUser: LoginUserOutput?)
        {
            val editor = userPrefs(context).edit()

            if (loggedUser != null)
            {
                editor.putString(USER_LOGIN_PREF, Gson().toJson(loggedUser))
            }
            else
            {
                editor.remove(USER_LOGIN_PREF)
            }

            editor.commit()

            val cache = RequestCache.findCacheWith(context.getString(R.string.orchard_user_info_display_path))

            if (cache != null)
            {
                val realm = Realm.getDefaultInstance()
                realm.beginTransaction()
                cache.deleteFromRealm()
                realm.commitTransaction()
            }
        }
    }

}