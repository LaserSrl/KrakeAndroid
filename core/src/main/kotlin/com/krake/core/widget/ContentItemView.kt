package com.krake.core.widget

import com.krake.core.model.ContentItem

/**
 * Created by joel on 30/09/16.
 */

/**
 * Interfaccia che devono implementare le view che mostrano un content item.
 * Alla classe sarà anche assegnata un ContentItemViewVisibilityListener,
 * dopo aver aggiornato la visibility, la view deve indicare al suo listener quando è visibile e
 * quando no in base al suo contenuto.
 */
interface ContentItemView {

    /**
     * Mostra nella view il ContentItem indicato
     * @param contentItem contenuto da mostrare sulla view
     * @param cacheValid indicazione se la cache è valida
     */
    fun show(contentItem: ContentItem, cacheValid: Boolean)

    /**
     * listener da notificare quando la visibility della view cambia
     */
    var container: ContentItemViewContainer

    /**
     * Indicazione se dopo aver assegnato i valori di listener e content item è necessario anche applicarlo ai figli.
     * Questo valore ha senso solo per le view che derivano da ViewGroup
     */
    val exploreChildToo: Boolean
        get() {
            return false
        }
}

interface ContentItemViewContainer {

    fun setAppbarLock(locked: Boolean, forceExpansionCollapse: Boolean = false)
}
