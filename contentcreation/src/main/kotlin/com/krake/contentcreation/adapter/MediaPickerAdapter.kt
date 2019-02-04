package com.krake.contentcreation.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import com.krake.contentcreation.adapter.holder.MediaPickerHolder
import com.krake.core.media.UploadableMediaInfo
import com.krake.core.media.task.MediaInfoPreviewTask
import com.krake.core.widget.ObjectsRecyclerViewAdapter

/**
 * adapter used by a [MediaPickerFragment]
 * this delegate the load of the bitmap in the [MediaPickerHolder] to a [MediaInfoPreviewTask]
 */
class MediaPickerAdapter(context: Context,
                         private val editingEnabled: Boolean,
                         private val clickListener: View.OnClickListener,
                         layout: Int, objects: List<UploadableMediaInfo>,
                         holderClass: Class<MediaPickerHolder>) : ObjectsRecyclerViewAdapter<UploadableMediaInfo, MediaPickerHolder>(context, layout, objects, holderClass) {

    override fun onBindViewHolder(holder: MediaPickerHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.task.load(item)

            holder.fab.setOnClickListener(clickListener)
            holder.fab.tag = position

            if (!editingEnabled)
                holder.fab.visibility = View.INVISIBLE
            holder.cellClickView.tag = position
            holder.cellClickView.setOnClickListener(clickListener)
            // The cast is necessary due to overload.
            (holder as RecyclerView.ViewHolder).itemView.setOnClickListener(null)
        }
    }

    override fun onViewRecycled(holder: MediaPickerHolder) {
        super.onViewRecycled(holder)
        holder.task.cancel()
    }

    fun release() {

    }
}