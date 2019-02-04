package com.krake.core.extension

import com.google.android.gms.maps.model.MarkerOptions

/**
 * Permette la copia degli attributi di un'istanza di [MarkerOptions] ad un'altra istanza di [MarkerOptions].
 *
 * @param options istanza nella quale verranno copiati gli attributi.
 */
fun MarkerOptions.copyTo(options: MarkerOptions) {
    options.position(position)
            .zIndex(zIndex)
            .anchor(anchorU, anchorV)
            .infoWindowAnchor(infoWindowAnchorU, infoWindowAnchorV)
            .icon(icon)
            .title(title)
            .snippet(snippet)
            .draggable(isDraggable)
            .visible(isVisible)
            .flat(isFlat)
            .rotation(rotation)
            .alpha(alpha)
}