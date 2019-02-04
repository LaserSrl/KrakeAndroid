package com.krake.core.messaging

import android.app.Service
import android.text.TextUtils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.krake.core.Constants
import com.krake.core.OrchardError
import com.krake.core.component.module.LoginComponentModule
import com.krake.core.component.module.OrchardComponentModule
import com.krake.core.data.RemoteDataRepository
import com.krake.core.gcm.OrchardContentNotifier
import com.krake.core.login.LoginManager
import com.krake.core.model.RequestCache

/**
 * [Service] richiamato da Firebase quando arriva una push al dispositivo.
 */
class MessagingService : FirebaseMessagingService() {
    companion object {
        private val TAG = MessagingService::class.java.simpleName
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
                          object : (RequestCache?, OrchardError?) -> Unit
                          {
                              override fun invoke(p1: RequestCache?, p2: OrchardError?)
                              {
                                  if (p1 != null || !needToRedoDataLoadingWithUserCookie(p2!!, loginComponentModule))
                                  {
                                      val parsedResult = p1?.elements(null)

                                      val resultForNotification: Any?

                                      if (parsedResult?.size ?: 0 == 1)
                                      {
                                          resultForNotification = parsedResult?.first()
                                      }
                                      else
                                      {
                                          resultForNotification = parsedResult
                                      }

                                      OrchardContentNotifier.showNotification(this@MessagingService,
                                                                              remoteExtras[Constants.RESPONSE_PUSH_TEXT],
                                                                              resultForNotification,
                                                                              orchardComponentModule.displayPath,
                                                                              null,
                                                                              remoteExtras)
                                  }
                                  else
                                  {
                                      loginComponentModule.loginRequired(true)
                                      loadDataFromOrchard(orchardComponentModule, loginComponentModule, remoteExtras)
                                  }
                              }
                          })
    }

    private fun needToRedoDataLoadingWithUserCookie(e: OrchardError, loginModule: LoginComponentModule): Boolean
    {
        return e.reactionCode == OrchardError.REACTION_LOGIN &&
                loginModule.loginRequired.not() &&
                LoginManager.shared.loggedUser.value != null
    }
}