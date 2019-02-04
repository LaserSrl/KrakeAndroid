/**
 * Created by joel on 10/02/17.
 */

package com.krake.core

import android.content.Context
import com.google.gson.JsonObject
import com.krake.core.model.RecordWithIdentifier
import com.krake.core.network.RemoteRequest
import com.krake.core.network.RemoteResponse

class UserTagging()
{

    companion object {
        private var _shared: UserTagging? = null
        var shared: UserTagging
            get() = _shared!!
            set(value) {
                _shared = value
            }

        fun destroy() {
            _shared = null
        }
    }

    fun tagUserBehavior(context: Context, record: RecordWithIdentifier)
    {
        tagUserBehavior(context, record.identifier)
    }

    fun tagUserBehavior(context: Context, recordId: Long)
    {

        val params = JsonObject()
        params.addProperty("ID", recordId)

        val request = RemoteRequest(context)
                .setMethod(RemoteRequest.Method.POST)
                .setPath(context.getString(R.string.user_tag_api_path))
                .setBody(params)


        Signaler.shared.invokeAPI(context,
                                  request,
                                  true,
                                  null,
                                  object : (RemoteResponse?, OrchardError?) -> Unit
                                  {
                                      override fun invoke(p1: RemoteResponse?, p2: OrchardError?)
                                      {
                                      }
                                  })
    }

}