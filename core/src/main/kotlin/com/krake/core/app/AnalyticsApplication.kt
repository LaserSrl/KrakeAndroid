package com.krake.core.app

import android.os.Bundle
import com.krake.core.model.ContentItem

/**
 * Interfaccia che deve implementare l'Application per abilitare l'invio delle reportistiche anayltics
 */
interface AnalyticsApplication {

    /**
     * Invia un contenuto ad Analytics, metodo più rapido per configurare i campi da un [ContentItem].
     *
     * richiama la [logSelectContent]
     * contentType = contentItem::class.java.simpleName
     * itemId = contentItem.titlePartTitle
     */
    fun logSelectContent(contentItem: ContentItem, extraParameters: Bundle? = null)

    fun logSelectContent(contentType: String, itemId: String?, extraParameters: Bundle? = null)

    fun logItemList(itemCategory: String)

    /**
     * Metodo radice: tutti gli altri richiamano questo metodo
     */
    fun logEvent(event: String, parameters: Bundle)

    fun logShare(contentType: String, itemId: String?, socialIntent: String)

    fun setUserProperty(key: String, value: String)

    fun setUserId(id: String)
}
