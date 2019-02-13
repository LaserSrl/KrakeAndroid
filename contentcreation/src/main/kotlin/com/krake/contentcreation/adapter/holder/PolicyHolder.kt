package com.krake.contentcreation.adapter.holder

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.krake.contentcreation.R
import com.krake.core.widget.GestureViewHolder
import com.krake.core.widget.ViewHolderWithClickGesture

/**
 * Type of [GestureViewHolder] attached to a [PolicyAdapter].
 * It enables the click gesture on the root view.
 */
class PolicyHolder(itemView: View) : GestureViewHolder(itemView), ViewHolderWithClickGesture {

    val titleTextView: TextView = itemView.findViewById(R.id.title_text_view)
    val acceptPolicySwitch: SwitchCompat = itemView.findViewById(R.id.privacy_accepted_switch)
}