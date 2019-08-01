package com.krake.bus.widget

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.krake.core.widget.ImageTextCellHolder
import com.krake.core.widget.ViewHolderWithClickGesture
import com.krake.trip.R

class BusPassageHolder(itemView: View) : ImageTextCellHolder(itemView), ViewHolderWithClickGesture {
    val timeTextView: TextView = itemView.findViewById(R.id.time_text_view)
    val realTimeImage: ImageView = itemView.findViewById(R.id.realTimeImageView)
}