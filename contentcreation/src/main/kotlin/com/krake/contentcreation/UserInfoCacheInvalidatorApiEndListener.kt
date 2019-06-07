package com.krake.contentcreation

import android.content.Context
import com.google.gson.JsonObject
import com.krake.core.Constants
import com.krake.core.OrchardApiEndListener
import com.krake.core.OrchardError
import com.krake.core.model.RequestCache
import com.krake.core.network.RemoteRequest
import com.krake.core.network.RemoteResponse
import io.realm.Realm
import java.util.*

class UserInfoCacheInvalidatorApiEndListener : OrchardApiEndListener {
    override fun onApiInvoked(
        context: Context,
        remoteRequest: RemoteRequest,
        remoteResponse: RemoteResponse?,
        e: OrchardError?,
        endListenerParameters: Any?
    ) {

        if (remoteRequest.method == RemoteRequest.Method.POST) {
            val body = remoteRequest.body
            if (e == null && body is JsonObject) {
                if (body.get(Constants.REQUEST_CONTENT_TYPE).asString == context.getString(R.string.orchard_user_content_type)) {
                    val realm = Realm.getDefaultInstance()
                    realm.beginTransaction()
                    val cache = RequestCache.findCacheWith(context.getString(R.string.orchard_user_info_display_path))

                    cache!!.dateExecuted = Date(0)

                    realm.commitTransaction()
                }
            }
        }
    }
}
