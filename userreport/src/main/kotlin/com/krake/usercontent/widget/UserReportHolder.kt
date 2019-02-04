package com.krake.usercontent.widget

import android.view.View
import android.widget.TextView
import com.krake.core.widget.ImageTextCellHolder
import com.krake.usercontent.R

/**
 * Created by antoniolig on 27/02/2017.
 */
open class UserReportHolder(itemView: View) : ImageTextCellHolder(itemView) {
    val subtitleTextView: TextView = itemView.findViewById(R.id.cellSubtitleTextView)
    val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
}