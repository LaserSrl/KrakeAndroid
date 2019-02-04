package com.krake.core.widget.content

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.krake.core.R
import com.krake.core.model.ContentItem
import com.krake.core.model.ContentItemWithLocation
import com.krake.core.widget.ContentItemView
import com.krake.core.widget.ContentItemViewContainer
import com.krake.core.widget.NavigationIntentManager
import com.krake.core.widget.compat.DrawableCompatTextView

/**
 * Implementation of [ContentItemView] that shows the address of a [ContentItemWithLocation]
 * and allows the navigation to that address through the [NavigationIntentManager].
 */
class AddressTextView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : DrawableCompatTextView(context, attrs, defStyleAttr), ContentItemView {

    override lateinit var container: ContentItemViewContainer

    override fun show(contentItem: ContentItem, cacheValid: Boolean) {
        val locationItem = contentItem as? ContentItemWithLocation
        val mapPart = locationItem?.mapPart

        if (mapPart?.isMapValid == true) {
            val address = mapPart.locationAddress?.trim()
            if (!address.isNullOrEmpty() && address != "\"\"") {
                text = address
            } else {
                setText(R.string.navigate_to_point)
            }

            setOnClickListener {
                NavigationIntentManager.startNavigationIntent(context, locationItem)
            }

            visibility = View.VISIBLE

        } else {
            setOnClickListener(null)
            visibility = View.GONE
        }
    }
}