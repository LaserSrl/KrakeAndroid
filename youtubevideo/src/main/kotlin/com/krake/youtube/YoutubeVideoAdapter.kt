package com.krake.youtube

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.krake.core.widget.ContentItemAdapter
import com.krake.core.widget.ImageTextCellHolder
import com.krake.youtube.model.YoutubeVideo
import com.krake.youtube.widget.YoutubeVideoHolder
import java.util.*

/**
 * Se l'adaoter è utilizzato direttamente è necessario che nel metodo onDestroyView() del fragment o activity chiamante
 * sia impostato a null l'adapter della lista che si appoggia all'adapter.
 * Altrimenti non sarà possibile rilasciare correttament i loader per youtube causando dei leak.
 * Created by joel on 14/11/14.
 */
open class YoutubeVideoAdapter(context: Context, layout: Int, holderClass: Class<*>?) :
    ContentItemAdapter(context, layout, holderClass), YoutubeVideoHolder.OnThumbnailLoaderAvailable {

    private val mHolders = ArrayList<YoutubeVideoHolder>()

    override fun instantiateViewHolder(viewGroup: ViewGroup, viewLayout: Int): ImageTextCellHolder {
        val h = super.instantiateViewHolder(viewGroup, viewLayout)
        if (h is YoutubeVideoHolder) {
            mHolders.add(h)
        }
        return h
    }

    override fun onBindViewHolder(holder: ImageTextCellHolder, i: Int) {
        super.onBindViewHolder(holder, i)
        if (holder is YoutubeVideoHolder) {
            holder.setListener(this)
            holder.setIndex(i)
            val item = getItem(i)
            if (holder.thumbnailLoader != null && item is YoutubeVideo) {
                holder.thumbnailLoader!!.setVideo(YoutubeVideoUtils.extractVideoIdentifier(item))
                holder.titleTextView.setText(getItem(i)?.titlePartTitle)
            }
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)

        for (holder in mHolders) {
            holder.releaseLoader()
        }
        mHolders.clear()
    }

    override fun onThumbnailAvailable(index: Int) {
        notifyItemChanged(index)
    }
}