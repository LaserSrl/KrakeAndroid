package com.krake.core.widget

import android.view.View
import android.widget.ImageView
import com.krake.core.R

/**
 * Created by joel on 15/03/17.
 */
class ImageViewHolder(itemView: View) : GestureViewHolder(itemView), ViewHolderWithClickGesture {
    val imageView: ImageView = itemView.findViewById(R.id.cellImageView)
}