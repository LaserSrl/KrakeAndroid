package com.krake.itineraries

import com.krake.core.app.ContentItemDetailModelFragment
import com.krake.itineraries.model.Itinerary

/**
 * Valori di default da utilizzare per il modulo degli itinerari.
 */
object ItineraryComponentModule {

    /**
     * Valore di default per il content layout di un [ContentItemDetailModelFragment],
     * quando deve essere visualizzato un [Itinerary]
     */
    val DEFAULT_DETAIL_CONTENT_LAYOUT = R.layout.fragment_itinerary_detail_content

    /**
     * Valore di default per il root layout di un [ContentItemDetailModelFragment],
     * quando deve essere visualizzato un [Itinerary]
     */
    val DEFAULT_DETAIL_ROOT_LAYOUT = R.layout.fragment_detail_coordinator_noheadermap
}