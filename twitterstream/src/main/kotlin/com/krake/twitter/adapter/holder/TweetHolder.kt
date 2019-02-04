package com.krake.twitter.adapter.holder

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.krake.core.media.widget.LoadableImageView
import com.krake.core.widget.GestureViewHolder
import com.krake.core.widget.ViewHolderWithClickGesture
import com.krake.twitter.R

/**
 * Created by joel on 08/03/17.
 */
class TweetHolder(itemView: View) : GestureViewHolder(itemView), ViewHolderWithClickGesture {
    val tweetContainer: ViewGroup = itemView.findViewById(R.id.tweet_container)
    val tweetAuthorAvatar: LoadableImageView = itemView.findViewById(R.id.tweet_author_avatar_image_view)
    val tweetAuthorFullName: TextView = itemView.findViewById(R.id.tweet_author_full_name_text_view)
    val tweetAuthorScreenName: TextView = itemView.findViewById(R.id.tweet_author_screen_name_text_view)
    val tweetText: TextView = itemView.findViewById(R.id.tweet_text_text_view)

    override fun viewWithClick(): View = tweetContainer
}