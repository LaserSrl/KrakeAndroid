package com.krake.puzzlegame

import android.view.View
import android.widget.TextView
import com.krake.core.widget.GestureViewHolder
import com.krake.core.widget.ViewHolderWithClickGesture

class AnswerViewHolder(itemView: View) : GestureViewHolder(itemView), ViewHolderWithClickGesture {
    val textView: TextView = itemView.findViewById(R.id.answerText)
}