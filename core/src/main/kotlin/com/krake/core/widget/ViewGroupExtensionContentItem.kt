package com.krake.core.widget

import android.view.ViewGroup
import com.krake.core.model.ContentItem

/**
 * Created by joel on 29/09/16.
 */

/**
 * Assegna un content item a tutte le view figlie di un ViewGroup.
 * La ricerca della view è effettuata in modo ricorsivo, anche in tutti i sotto gruppi presenti
 *
 * @param contentItem elemento di contenuto da visualizzare nella view
 * @param cacheValid indicazione se il contenuto ha una cache valida
 */
fun ViewGroup.setContentItemToChild(contentItem: ContentItem, cacheValid: Boolean) {
    for (index in 0..this.childCount) {
        val subView = this.getChildAt(index)

        when (subView) {
            is ContentItemView -> {
                subView.show(contentItem, cacheValid)
                if (subView is ViewGroup && subView.exploreChildToo)
                    subView.setContentItemToChild(contentItem, cacheValid)
            }

            is ViewGroup -> subView.setContentItemToChild(contentItem, cacheValid)

        }
    }
}

fun ViewGroup.setVisibilityListenerToChild(listener: ContentItemViewContainer) {
    for (index in 0..this.childCount) {
        val subView = this.getChildAt(index)

        when (subView) {
            is ContentItemView -> {
                subView.container = listener
                if (subView is ViewGroup && subView.exploreChildToo)
                    subView.setVisibilityListenerToChild(listener)
            }
            is ViewGroup -> subView.setVisibilityListenerToChild(listener)

        }
    }
}