package com.krake.core

import android.content.Context
import androidx.collection.ArrayMap
import com.google.gson.JsonObject
import com.krake.core.network.CancelableRequest
import com.krake.core.network.RemoteClient
import com.krake.core.network.RemoteRequest
import com.krake.core.network.RemoteResponse
import java.util.*

class Signaler internal constructor(context: Context)
{
    private val language: String
    private val signalEndListener = ArrayMap<String, Class<out OrchardSignalEndListener>>()
    private val apiCallEndListener = ArrayMap<String, MutableList<OrchardApiEndListener>>()

    init
    {
        language = context.getString(R.string.orchard_language)
    }

    @Throws(OrchardError::class)
    fun executeAPI(context: Context,
                   apiRequest: RemoteRequest,
                   loginRequired: Boolean,
                   endListenerParameters: Any? = null): RemoteResponse
    {
        val appContext = context.applicationContext

        if (apiRequest.queryParameters.isEmpty())
            apiRequest.setQuery("Language", language)

        var response: RemoteResponse? = null
        var error: OrchardError? = null

        try
        {
            response = RemoteClient.client(loginRequired).execute(apiRequest)
        }
        catch (e: OrchardError)
        {
            error = e
        }

        callApiEndListener(appContext,
                           apiRequest,
                           response,
                           error,
                           endListenerParameters)

        if (response != null)
            return response

        throw error!!
    }

    fun invokeAPI(context: Context,
                  apiRequest: RemoteRequest,
                  loginRequired: Boolean,
                  endListenerParameters: Any? = null,
                  callback: (RemoteResponse?, OrchardError?) -> Unit): CancelableRequest
    {
        if (apiRequest.queryParameters.isEmpty())
            apiRequest.setQuery("Language", language)

        val appContext = context.applicationContext
        return RemoteClient
                .client(loginRequired)
                .enqueue(apiRequest, object : (RemoteResponse?, OrchardError?) -> Unit
                {
                    override fun invoke(p1: RemoteResponse?, p2: OrchardError?)
                    {
                        callback(p1, p2)
                        callApiEndListener(appContext,
                                           apiRequest,
                                           p1,
                                           p2,
                                           endListenerParameters)
                    }
                })

    }

    /**
     * Metodo per inviare un segnale ad Orchard.
     * Il metodo permette di avviare flussi di lavoro semplici lato orchard.
     * Alla fine della chiamata del segnale saranno chiamati {@link OrchardSignalEndListener} prelevati da
     * {@link KrakeApplication#registerSignalEndListener(String, Class)}.
     * Inoltre sarà inviato il messaggio {@link com.krake.core.OrchardService.Output#MESSAGE_SIGNAL_SENT}.
     * Il WS si occuperà di spedire i messaggi per i cambiamenti di stato {@link com.krake.core.OrchardService.Output#MESSAGE_STATUS}
     * all'inizio e termine della chiamata
     *
     * @param context       app context
     * @param signalName    nome del segnale da inviare ad orchard
     * @param body    parametri da passare
     * @param loginRequired se il segnale deve essere inviato loggato
     */
    fun sendSignal(context: Context,
                   signalName: String,
                   body: Map<String, String>,
                   loginRequired: Boolean,
                   callback: (RemoteResponse?, OrchardError?) -> Unit): CancelableRequest
    {
        val mode: RemoteClient.Mode

        if (loginRequired)
            mode = RemoteClient.Mode.LOGGED
        else
            mode = RemoteClient.Mode.DEFAULT

        val bodyParameters = body.toMutableMap()
        bodyParameters[Constants.REQUEST_LANGUAGE_KEY] = language
        bodyParameters[Constants.REQUEST_SIGNAL_NAME] = signalName

        val remoteRequest = RemoteRequest(context)
                .setBodyParameters(bodyParameters)
                .setPath(context, RemoteRequest.StandardPath.SEND_SIGNAL)
                .setMethod(RemoteRequest.Method.POST)

        val appContext = context.applicationContext

        return RemoteClient.client(mode)
                .enqueue(remoteRequest, object : (RemoteResponse?, OrchardError?) -> Unit
                {
                    override fun invoke(p1: RemoteResponse?, p2: OrchardError?)
                    {
                        callback(p1, p2)
                        callSignalEndListener(appContext,
                                              signalName,
                                              p1?.jsonObject(),
                                              p2,
                                              bodyParameters)
                    }
                })
    }

    fun registerSignalEndListener(signalName: String, listenerCLass: Class<out OrchardSignalEndListener>)
    {
        signalEndListener.put(signalName, listenerCLass)
    }

    fun signalListener(signalName: String): Class<out OrchardSignalEndListener>?
    {
        if (signalEndListener.containsKey(signalName))
            return signalEndListener[signalName]

        return null
    }

    private fun callSignalEndListener(context: Context,
                                      signalName: String,
                                      result: JsonObject?,
                                      error: OrchardError?,
                                      bodyParameters: Map<String, String>)
    {
        signalListener(signalName)?.let {
            try
            {
                val endListener = it.getConstructor().newInstance()

                endListener.onSignalSent(context, result, error, bodyParameters)
            }
            catch (ignored: Exception)
            {

            }
        }
    }

    /**
     * Permette di registrare una classe da invocare a fine delle chiamata di un API su orchard
     * La classe deve avere un costruttore pubblico senza parametri e implementare l'interfaccia
     * [OrchardApiEndListener][com.krake.core.OrchardApiEndListener]

     * @param apiPath       path delle API su cui registrare il listener
     * @param listenerClass classe del listener
     */
    fun registerApiEndListener(apiPath: String,
                               listenerClass: OrchardApiEndListener)
    {
            if (apiCallEndListener.containsKey(apiPath))
            {
                val classes = apiCallEndListener[apiPath]!!

                if (classes.none { listenerClass::class.java == it::class.java })
                    classes.add(listenerClass)
            }
            else
            {
                val classes = LinkedList<OrchardApiEndListener>()
                classes.add(listenerClass)
                apiCallEndListener.put(apiPath, classes)
            }
    }

    fun removeApiEndListener(apiPath: String,
                             listenerClass: OrchardApiEndListener)
    {
        apiCallEndListener[apiPath]?.remove(listenerClass)
    }


    /**
     * @param apiPath apiPAth per cui si vuole ottnere la classe listener
     * *
     * @return la classe, oppure null
     */
    fun apiEndListenerClass(apiPath: String): List<OrchardApiEndListener>?
    {
        if (apiCallEndListener.containsKey(apiPath))
            return apiCallEndListener[apiPath]

        return null
    }

    private fun callApiEndListener(context: Context,
                                   remoteRequest: RemoteRequest,
                                   remoteResponse: RemoteResponse?,
                                   error: OrchardError?,
                                   endListenerParameters: Any?)
    {
        remoteRequest.path?.let {
            val endListenerClasses = apiEndListenerClass(it)
            if (endListenerClasses != null)
            {
                for (endListener in endListenerClasses)
                {
                    try
                    {
                        endListener.onApiInvoked(context, remoteRequest, remoteResponse, error, endListenerParameters)
                    }
                    catch (ignored: Exception)
                    {
                        ignored.toString()
                    }
                }
            }
        }

    }

    companion object
    {
        lateinit var shared: Signaler
    }
}