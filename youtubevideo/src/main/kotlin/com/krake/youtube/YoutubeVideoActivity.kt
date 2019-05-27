package com.krake.youtube

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.android.youtube.player.YouTubeApiServiceUtil
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubeIntents
import com.krake.core.app.ContentItemListMapActivity
import com.krake.core.component.module.ListMapComponentModule
import com.krake.core.model.ContentItem
import com.krake.youtube.model.YoutubeVideo
import com.krake.youtube.widget.YoutubeVideoHolder

open class YoutubeVideoActivity : ContentItemListMapActivity() {

    override fun onCreate(savedInstanceState: Bundle?, layout: Int) {
        super.onCreate(savedInstanceState, layout)

        if (YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(this) != YouTubeInitializationResult.SUCCESS) {
            AlertDialog.Builder(this)
                .setTitle(R.string.youtube_initialize_error_title)
                .setMessage(R.string.youtube_initialize_error_message)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok) { dialogInterface, i -> finish() }
                .show()
        }
    }

    override fun changeContentVisibility(visible: Boolean) {
        super.changeContentVisibility(visible && YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(this) == YouTubeInitializationResult.SUCCESS)
    }

    override fun onShowContentItemDetails(sender: Any, contentItem: ContentItem) {
        if (contentItem is YoutubeVideo) {
            val identifier = YoutubeVideoUtils.extractVideoIdentifier(contentItem)
            startActivity(
                YouTubeIntents.createPlayVideoIntentWithOptions(
                    this.applicationContext,
                    identifier,
                    true,
                    true
                )
            )
        } else

            super.onShowContentItemDetails(sender, contentItem)
    }

    companion object {
        fun defaultListMapComponentModule(context: Context): ListMapComponentModule {
            return ListMapComponentModule(context)
                .listAdapterClass(YoutubeVideoAdapter::class.java)
                .listFragmentClass(YoutubeVideosFragment::class.java)
                .listViewHolderClass(YoutubeVideoHolder::class.java)
                .listCellLayout(R.layout.video_thumbnail_cell)
                .listRootLayout(R.layout.fragment_content_items_selection_list)
        }
    }
}
