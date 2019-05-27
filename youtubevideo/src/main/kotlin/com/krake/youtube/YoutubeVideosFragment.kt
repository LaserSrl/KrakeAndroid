package com.krake.youtube

import com.krake.core.app.ContentItemGridModelFragment
import com.krake.youtube.widget.YoutubeVideoHolder


class YoutubeVideosFragment : ContentItemGridModelFragment(), YoutubeVideoHolder.OnThumbnailLoaderAvailable
{
    override fun onDestroyView()
    {
        super.onDestroyView()
        recycleView.adapter = null
    }

    override fun onThumbnailAvailable(index: Int)
    {
        recycleView.adapter?.notifyItemChanged(index)
    }

}