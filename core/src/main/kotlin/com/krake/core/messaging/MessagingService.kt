package com.krake.core.messaging

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.JsonObject
import com.krake.core.Constants
import com.krake.core.OrchardError
import com.krake.core.R
import com.krake.core.Signaler
import com.krake.core.component.module.LoginComponentModule
import com.krake.core.component.module.OrchardComponentModule
import com.krake.core.data.RemoteDataRepository
import com.krake.core.gcm.OrchardContentNotifier
import com.krake.core.login.LoginManager
import com.krake.core.model.RequestCache
import com.krake.core.network.RemoteRequest

/**
 * [Service] richiamato da Firebase quando arriva una push al dispositivo.
 */
class MessagingService : FirebaseMessagingService() {
    companion object {
        @SuppressLint("HardwareIds")
        @JvmStatic
        fun getUUID(context: Context): String {
            return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }
    }

    override fun onNewToken(p0: String?) {
        super.onNewToken(p0)
        val refreshedToken = p0
        Log.d("FCM id", "onTokenRefresh: " + (refreshedToken ?: "null"))

        val parameters = JsonObject()
        parameters.addProperty("Token", refreshedToken)
        parameters.addProperty("Device", getString(R.string.orchard_mobile_platform))
        parameters.addProperty("UUID", getUUID(this))
        parameters.addProperty("Language", getString(R.string.orchard_language))
        parameters.addProperty("Produzione", getString(R.string.orchard_gcm_production))

        val enabled = true
        val extras = Bundle()
        extras.putBoolean(getString(R.string.orchard_api_token_enabled), enabled)

        val method = if (enabled) "PUT" else "DELETE"

        val request = RemoteRequest(this)
            .setMethod(if (enabled) RemoteRequest.Method.PUT else RemoteRequest.Method.DELETE)
            .setBody(parameters)
            .setPath(getString(R.string.orchard_push_api))


        Signaler.shared
            .invokeAPI(
                this,
                request,
                false,
                extras
            ) { remoteResponse, orchardError -> null }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val remoteExtras = remoteMessage.data

        val contentDisplayPath = remoteExtras[Constants.RESPONSE_PUSH_REFERENCE_DISPLAY_PATH]
        val text = remoteExtras[Constants.RESPONSE_PUSH_TEXT]
        val externalUrl = remoteExtras[Constants.RESPONSE_PUSH_EXTERNAL_URL]
        val notification = remoteMessage.notification

        if (!TextUtils.isEmpty(contentDisplayPath))
        {
            val module = OrchardComponentModule()
                    .displayPath(contentDisplayPath)

            loadDataFromOrchard(module,
                                LoginComponentModule(),
                                remoteExtras)
        }
        else if (text != null && !TextUtils.isEmpty(text))
            OrchardContentNotifier.showNotification(this,
                                                    text,
                                                    null,
                                                    externalUrl,
                                                    null,
                                                    remoteExtras)
        else
        {
            OrchardContentNotifier.showNotification(this,
                                                    notification?.getBody() ?: "",
                                                    null,
                                                    null,
                                                    null,
                                                    remoteExtras)
        }
    }

    private fun loadDataFromOrchard(orchardComponentModule: OrchardComponentModule,
                                    loginComponentModule: LoginComponentModule,
                                    remoteExtras: Map<String, String>)
    {

        RemoteDataRepository.shared
                .loadData(loginComponentModule,
                          orchardComponentModule,
                          1,
                    object : (Int, RequestCache?, OrchardError?) -> Unit
                          {
                              override fun invoke(code: Int, p1: RequestCache?, p2: OrchardError?)
                              {
                                  //Se tutto null è stato inviato un contenuto non mappato in App
                                  if (p1 != null || p2 != null) {
                                      if (p1 != null || !needToRedoDataLoadingWithUserCookie(
                                              p2!!,
                                              loginComponentModule
                                          )
                                      ) {
                                          val parsedResult = p1?.elements(null)

                                          val resultForNotification: Any?

                                          if (parsedResult?.size ?: 0 == 1) {
                                              resultForNotification = parsedResult?.first()
                                          } else {
                                              resultForNotification = parsedResult
                                          }

                                          OrchardContentNotifier.showNotification(
                                              this@MessagingService,
                                              remoteExtras[Constants.RESPONSE_PUSH_TEXT],
                                              resultForNotification,
                                              orchardComponentModule.displayPath,
                                              null,
                                              remoteExtras
                                          )
                                      } else {
                                          loginComponentModule.loginRequired(true)
                                          loadDataFromOrchard(
                                              orchardComponentModule,
                                              loginComponentModule,
                                              remoteExtras
                                          )
                                      }
                                  }
                                  else {
                                      OrchardContentNotifier.showNotification(
                                          this@MessagingService,
                                          remoteExtras[Constants.RESPONSE_PUSH_TEXT],
                                          null,
                                          null,
                                          null,
                                          remoteExtras
                                      )
                                  }
                              }

                          }
                          )
    }

    private fun needToRedoDataLoadingWithUserCookie(e: OrchardError, loginModule: LoginComponentModule): Boolean
    {
        return e.reactionCode == OrchardError.REACTION_LOGIN &&
                loginModule.loginRequired.not() &&
                LoginManager.shared.loggedUser.value != null
    }
}