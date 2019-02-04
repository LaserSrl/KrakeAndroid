package com.krake.core

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.support.v4.util.LongSparseArray
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.krake.core.login.PrivacyException
import com.krake.core.network.CancelableRequest
import com.krake.core.network.RemoteRequest
import com.krake.core.network.RemoteResponse

class PolicyManager
{
    val privacyException = MutableLiveData<PrivacyException>()

    val privacyError = MutableLiveData<OrchardError>()

    fun acceptPrivacies(context: Context,
                        privacies: LongSparseArray<Boolean>): CancelableRequest
    {
        val request = RemoteRequest(context)
                .setPath(context.getString(R.string.orchard_api_privacy))
                .setMethod(RemoteRequest.Method.POST)

        val parameters = JsonObject()

        parameters.addProperty(Constants.REQUEST_LANGUAGE_KEY, context.getString(R.string.orchard_language))

        val jsonPrivacies = JsonArray()

        for (i in 0..privacies.size())
        {
            val key = privacies.keyAt(i)

            val acceptedJson = JsonObject()

            acceptedJson.addProperty("AnswerId", 0)
            acceptedJson.addProperty("PolicyTextId", key)
            acceptedJson.addProperty("OldAccepted", false)
            acceptedJson.addProperty("Accepted", privacies[key])
            acceptedJson.addProperty("AnswerDate", "0001-01-01T00:00:00")

            jsonPrivacies.add(acceptedJson)
        }


        parameters.add("PoliciesForUser",
                       JsonObject().apply {
                           this.add("Policies", jsonPrivacies)
                       })

        val callback =
                object : (RemoteResponse?, OrchardError?) -> Unit
                {
                    override fun invoke(p1: RemoteResponse?, p2: OrchardError?)
                    {
                        privacyError.value = p2

                        if (p1 != null)
                        {

                        }
                    }
                }

        return Signaler.shared.invokeAPI(context,
                                         request,
                                         true,
                                         null,
                                         callback
        )
    }

    companion object
    {
        lateinit var shared: PolicyManager
            internal set
    }
}