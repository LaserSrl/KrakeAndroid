package com.krake.twitter.adapter

import android.content.Context
import android.text.Html
import android.view.View
import com.krake.core.media.loader.MediaLoader
import com.krake.core.model.MediaPart
import com.krake.core.widget.ObjectsRecyclerViewAdapter
import com.krake.twitter.R
import com.krake.twitter.adapter.holder.TweetHolder
import com.twitter.sdk.android.core.internal.UserUtils
import com.twitter.sdk.android.core.models.Tweet

/**
 * Adapter that handle the view of a [Tweet] in a [TweetHolder]
 */
open class TweetAdapter(context: Context, tweets: List<Tweet>? = null) : ObjectsRecyclerViewAdapter<Tweet, TweetHolder>(context, R.layout.tweet_view, tweets, TweetHolder::class.java) {
    override fun onBindViewHolder(holder: TweetHolder, position: Int) {
        val tweet = items[position].retweetedStatus ?: items[position]

        if (!tweet.user.name.isNullOrBlank())
            holder.tweetAuthorFullName.text = tweet.user.name

        if (!tweet.user.screenName.isNullOrBlank()) {
            holder.tweetAuthorScreenName.text = tweet.user.screenName
            holder.tweetAuthorScreenName.visibility = View.VISIBLE
        }
        if (!tweet.text.isNullOrBlank()) {
            @Suppress("DEPRECATION")
            holder.tweetText.text = Html.fromHtml(tweet.text)
            holder.tweetText.visibility = View.VISIBLE
        }

        val url = UserUtils.getProfileImageUrlHttps(tweet.user,
                UserUtils.AvatarSize.REASONABLY_SMALL)

        MediaLoader.with(context!!, holder.tweetAuthorAvatar)
                .mediaPart(getTwitterImageMediaPart(url))
                .load()
    }

    open fun getTwitterImageMediaPart(imageUrl: String, mimeType: String = MediaPart.MIME_TYPE_IMAGE, title: String = "", fileName: String = "", folderPath: String = "", logicalType: String = "") = object : MediaPart {
        override val mimeType: String = mimeType
        override val fileName: String = fileName
        override val title: String = title
        override val folderPath: String = folderPath
        override val logicalType: String = logicalType
        override val mediaUrl: String = imageUrl
    }
}