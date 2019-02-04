package com.krake.core.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.krake.core.R
import com.krake.core.model.ContentItem
import com.krake.core.widget.content.AddressTextView

/**
 * Created by joel on 30/09/16.
 */
/**
 * AddressView mostra l'indirizzo di un LocationContentItem su una view.
 * La view deve contenere le subView direttamente nell'xml.
 * è già presente in libreria R.layout.ct_detail_address
 * In particolare serve una textView con id R.id.locationAddressTextView
 * La view gestice già autonomamente il click per l'avvio della navigazione
 * @see NavigationIntentManager
 */
@Deprecated("This view is now deprecated.", ReplaceWith("com.krake.core.widget.content.AddressTextView"))
class AddressView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), ContentItemView {

    private val locationAddressTextView: AddressTextView
    override lateinit var container: ContentItemViewContainer

    init {
        View.inflate(context, R.layout.ci_detail_address, this)
        locationAddressTextView = findViewById(R.id.locationAddressTextView)
    }

    override fun generateDefaultLayoutParams(): LayoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

    override fun show(contentItem: ContentItem, cacheValid: Boolean) {
        locationAddressTextView.show(contentItem, cacheValid)
    }
}

