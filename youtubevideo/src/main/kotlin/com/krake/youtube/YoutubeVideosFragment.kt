package com.krake.youtube

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.google.android.youtube.player.YouTubeIntents
import com.krake.core.OrchardError
import com.krake.core.app.OrchardDataModelFragment
import com.krake.core.data.DataModel
import com.krake.core.util.LayoutUtils
import com.krake.core.widget.ObjectsRecyclerViewAdapter
import com.krake.youtube.model.YoutubeVideo
import com.krake.youtube.widget.YoutubeVideoHolder


class YoutubeVideosFragment : OrchardDataModelFragment(), YoutubeVideoHolder.OnThumbnailLoaderAvailable, ObjectsRecyclerViewAdapter.ClickReceiver<YoutubeVideo>
{
    private lateinit var mList: RecyclerView

    private lateinit var mAdapter: YoutubeVideoAdapter

    private var progress: ProgressBar? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val view = inflater.inflate(R.layout.fragment_content_items_selection_list, container, false)

        LayoutUtils.attachScrollingBehavior(view)

        progress = view.findViewById(android.R.id.progress)
        progress?.visibility = View.VISIBLE

        mList = view.findViewById(android.R.id.list)

        mAdapter = YoutubeVideoAdapter(activity, R.layout.video_thumbnail_cell, null, YoutubeVideoHolder::class.java)
        mAdapter.defaultClickReceiver = this
        mList.adapter = mAdapter

        return view
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        mList.adapter = null
    }

    override fun onDataModelChanged(dataModel: DataModel?)
    {
        if (dataModel != null)
        {
            mAdapter.swapList(dataModel.listData as List<YoutubeVideo>, true)
            if (progress != null)
                progress!!.visibility = View.GONE
        }
    }

    override fun onDataLoadingError(orchardError: OrchardError)
    {

    }

    override fun onThumbnailAvailable(index: Int)
    {
        mAdapter.notifyItemChanged(index)
    }

    override fun onViewClicked(recyclerView: RecyclerView, view: View, position: Int, item: YoutubeVideo)
    {
        val identifier = YoutubeVideoUtils.extractVideoIdentifier(item)
        activity?.startActivity(YouTubeIntents.createPlayVideoIntentWithOptions(activity!!.applicationContext, identifier, true, true))
    }
}