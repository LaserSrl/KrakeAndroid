package com.krake.contentcreation

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.gson.JsonParser
import com.krake.core.Constants
import com.krake.core.OrchardError
import com.krake.core.UploadInterceptor
import com.krake.core.media.MediaProvider
import com.krake.core.media.MediaType
import com.krake.core.media.UploadableMediaInfo
import com.krake.core.network.CancelableRequest
import com.krake.core.network.RemoteClient
import com.krake.core.network.RemoteRequest
import com.krake.core.network.RemoteResponse
import java.util.concurrent.CancellationException

/**
 * [UploadInterceptor] usato per caricare video su Orchard.
 * <br></br>
 * Questo interceptor viene usato come l'interceptor di default all'apertura della [ContentCreationActivity].
 */
class ContentCreationUploadInterceptor(priority: Int, @MediaType availableMedias: Int) : UploadInterceptor(priority, availableMedias), (RemoteResponse?, OrchardError?) -> Unit
{

    private val mParser: JsonParser = JsonParser()

    /**
     * Tiene in memoria l'ultima richiesta avvenuta
     */
    private var mUploadFuture: CancelableRequest? = null

    override fun uploadFile(remoteClient: RemoteClient,
                            context: Context,
                            media: UploadableMediaInfo,
                            uploadParams: Bundle?)
    {

        val request = RemoteRequest(context)
                .setPath(context.getString(R.string.orchard_upload_media_path))
                .setMethod(RemoteRequest.Method.POST)
                .setHeader("Content-Length", "")

        val errorMsgGeneric = context.getString(R.string.error_uploading_file)
        val errorMsgManuallyCancelled = context.getString(R.string.error_upload_cancelled_manually)

        var error: OrchardError? = null
        var mediaId: Long? = null
        isCancelled = false

        val map = mutableMapOf<String, Any>()

        val contentTypeKey = Constants.REQUEST_CONTENT_TYPE
        if (uploadParams != null && uploadParams.containsKey(contentTypeKey))
        {
            map[contentTypeKey] = uploadParams.getString(contentTypeKey)!!
        }

        val tempFile = MediaProvider.createTempFile(context, media.uri)
        map.put("file", tempFile)
        map.put("FileName", "IMG")

        request.setBodyParameters(map)

        try
        {
            val result = remoteClient.execute(request).jsonObject()
            if (result != null)
            {
                error = OrchardError.createErrorFromResult(result)
                if (error == null)
                {
                    mediaId = result.get("Id").getAsLong()
                }
            }
        }
        catch (e: Exception)
        {
            if (e.cause is CancellationException && isCancelled)
            {
                Log.d(TAG, "uploadFile: the upload was cancelled")
                error = OrchardError(errorMsgManuallyCancelled)
            }
            else
            {
                error = OrchardError(errorMsgGeneric)
            }
        }

        if (error != null)
        {
            throw error
        }
        else
        {
            media.setId(mediaId)
            media.isUploaded = true
        }
    }

    override fun invoke(p1: RemoteResponse?, p2: OrchardError?)
    {
    }

    override fun cancelUpload(): Boolean
    {
        if (mUploadFuture != null)
        {
            mUploadFuture?.cancel()
            mUploadFuture = null
            // si cambia lo stato a "cancelled" solo quando l'operazione è stata effettivamente cancellata
            isCancelled = true
            return true
        }
        return false
    }

    companion object
    {
        private val TAG = ContentCreationUploadInterceptor::class.java.simpleName
    }
}