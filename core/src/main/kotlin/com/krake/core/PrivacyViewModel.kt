package com.krake.core

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.support.v4.util.LongSparseArray
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.krake.core.login.PrivacyException
import com.krake.core.network.CancelableRequest
import com.krake.core.network.RemoteRequest
import com.krake.core.network.RemoteResponse

class PrivacyViewModel : ViewModel()
{
    private val mutablePrivacyStatus = MutableLiveData<PrivacyStatus>().apply { this.value = PrivacyStatus.ACCEPTED }

    val privacyStatus: LiveData<PrivacyStatus> = mutablePrivacyStatus

    @JvmOverloads
    fun acceptPrivacies(context: Context,
                        privacies: LongSparseArray<Boolean>,
                        finalCallback: ((RemoteResponse?, OrchardError?) -> Unit)? = null): CancelableRequest
    {
        val request = RemoteRequest(context)
                .setPath(context.getString(R.string.orchard_api_privacy))
                .setMethod(RemoteRequest.Method.POST)

        val parameters = JsonObject()

        parameters.addProperty(Constants.REQUEST_LANGUAGE_KEY, context.getString(R.string.orchard_language))

        val jsonPrivacies = JsonArray()

        for (i in 0 until privacies.size())
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

        request.setBody(parameters)

        val callback =
                object : (RemoteResponse?, OrchardError?) -> Unit
                {
                    override fun invoke(p1: RemoteResponse?, p2: OrchardError?)
                    {
                        if (p2 != null)
                        {
                            val status = PrivacyStatus.FAILED
                            status.error = p2
                            status.privacyException = privacyStatus.value?.privacyException

                            mutablePrivacyStatus.value = status
                        }
                        else
                        {
                            mutablePrivacyStatus.value = PrivacyStatus.ACCEPTED
                        }

                        finalCallback?.let { it(p1, p2) }
                    }
                }

        return Signaler.shared.invokeAPI(context,
                                         request,
                                         true,
                                         null,
                                         callback
        )
    }

    fun needToAcceptPrivacy(privacyException: PrivacyException)
    {
        if (mutablePrivacyStatus.value != PrivacyStatus.PENDING)
        {
            val status = PrivacyStatus.PENDING
            status.privacyException = privacyException
            mutablePrivacyStatus.value = status
        }
    }

    fun cancelPrivacyAcceptance()
    {
        if (mutablePrivacyStatus.value?.equals(PrivacyStatus.REFUSED)?.not() ?: true)
        {
            mutablePrivacyStatus.value = PrivacyStatus.REFUSED
        }
    }
}

enum class PrivacyStatus constructor(privacyException: PrivacyException?, error: OrchardError?)
{
    ACCEPTED(null, null),
    PENDING(null, null),
    FAILED(null, null),
    REFUSED(null, null);

    var privacyException: PrivacyException?
        internal set
    var error: OrchardError?
        internal set

    init
    {
        this.privacyException = privacyException
        this.error = error
    }
}