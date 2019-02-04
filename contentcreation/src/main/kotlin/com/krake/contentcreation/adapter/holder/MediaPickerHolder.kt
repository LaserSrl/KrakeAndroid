package com.krake.contentcreation.adapter.holder

import android.support.design.widget.FloatingActionButton
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.krake.contentcreation.R
import com.krake.core.media.task.MediaInfoPreviewTask
import com.krake.core.media.task.ProgressMediaInfoViewer
import com.krake.core.widget.GestureViewHolder
import com.krake.core.widget.ImageTextCellHolder

/**
 * Type of [GestureViewHolder] attached to a [MediaPickerAdapter].
 */
class MediaPickerHolder(itemView: View) : ImageTextCellHolder(itemView) {
    val fab: FloatingActionButton = itemView.findViewById(R.id.deleteMediaFab)
    val cellClickView: ViewGroup = itemView.findViewById(R.id.cellClickView)
    val progressBar: ProgressBar = itemView.findViewById(R.id.mediaProgress)
    val task: MediaInfoPreviewTask = MediaInfoPreviewTask(itemView.context, createBitmapViewer())

    private fun createBitmapViewer(): MediaInfoPreviewTask.Viewer = ProgressMediaInfoViewer(imageView!!, progressBar, cellClickView)
}