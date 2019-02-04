package com.krake.core.widget

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.krake.core.R

/**
 * Created by joel on 16/03/17.
 */
open class ImageTextCellHolder(itemView: View) : GestureViewHolder(itemView), ViewHolderWithClickGesture {
    val titleTextView: TextView = itemView.findViewById(R.id.cellTextView)
    val imageView: ImageView? = itemView.findViewById(R.id.cellImageView)
}