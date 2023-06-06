package com.krake.youtube.widget

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.View
import com.google.android.libraries.places.api.Places
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubeThumbnailLoader
import com.google.android.youtube.player.YouTubeThumbnailView
import com.krake.core.widget.ImageTextCellHolder
import com.krake.core.widget.ViewHolderWithClickGesture
import com.krake.youtube.R

/**
 * Created by joel on 08/10/14.
 */
open class YoutubeVideoHolder(itemView: View) : ImageTextCellHolder(itemView),
    YouTubeThumbnailView.OnInitializedListener,
    ViewHolderWithClickGesture {
    val thumbnailView: YouTubeThumbnailView = itemView.findViewById(R.id.thumbnail_view)
    var thumbnailLoader: YouTubeThumbnailLoader? = null
        private set
    private var index: Int = 0
    private var listener: OnThumbnailLoaderAvailable? = null

    init {
        val ai: ApplicationInfo = itemView.context.packageManager.getApplicationInfo(
            itemView.context.packageName,
            PackageManager.GET_META_DATA
        )
        val value = ai.metaData["com.google.android.geo.API_KEY"]
//        val value = ai.metaData["key_value_google"]

        var keyValue = ""
        if(value != null) keyValue =  value.toString()

        thumbnailView.initialize(keyValue, this)
//        thumbnailView.initialize(itemView.context.getString(R.string.google_api_key), this)
    }

    fun setIndex(index: Int) {
        this.index = index
    }

    override fun onInitializationSuccess(youTubeThumbnailView: YouTubeThumbnailView, youTubeThumbnailLoader: YouTubeThumbnailLoader) {
        if (thumbnailLoader !== youTubeThumbnailLoader && thumbnailLoader != null)
            thumbnailLoader?.release()

        thumbnailLoader = youTubeThumbnailLoader
        listener?.onThumbnailAvailable(index)
    }

    override fun onInitializationFailure(youTubeThumbnailView: YouTubeThumbnailView, youTubeInitializationResult: YouTubeInitializationResult) {

    }

    fun setListener(listener: OnThumbnailLoaderAvailable) {
        this.listener = listener
    }

    fun releaseLoader() {
        if (thumbnailLoader != null) {
            thumbnailLoader!!.release()
            thumbnailLoader = null
        }
    }


    interface OnThumbnailLoaderAvailable {
        fun onThumbnailAvailable(index: Int)
    }
}
