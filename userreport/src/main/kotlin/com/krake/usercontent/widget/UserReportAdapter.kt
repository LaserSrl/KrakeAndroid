package com.krake.usercontent.widget

import android.content.Context
import com.krake.core.widget.ContentItemAdapter
import com.krake.core.widget.ImageTextCellHolder
import com.krake.usercontent.StatusLabelUtils
import com.krake.usercontent.model.UserCreatedContent

/**
 * Created by antoniolig on 27/02/2017.
 */
open class UserReportAdapter(context: Context, layout: Int, holderClass: Class<*>) :
        ContentItemAdapter(context, layout, holderClass)
{
    var loginRequired = false
    override fun onBindViewHolder(holder: ImageTextCellHolder, i: Int) {
        super.onBindViewHolder(holder, i)

        val userReportHolder = holder as? UserReportHolder
        val userContent = getItem(i) as? UserCreatedContent?

        if (userReportHolder != null && userContent != null) {
            userReportHolder.subtitleTextView.text = userContent.sottotitoloValue

            StatusLabelUtils.setStatusLabel(userReportHolder.statusTextView,
                                            loginRequired,
                                            userContent.publishExtensionPartPublishExtensionStatus)
        }
    }
}