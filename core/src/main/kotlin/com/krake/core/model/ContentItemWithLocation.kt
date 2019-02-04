package com.krake.core.model

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.krake.core.image.BitmapGenerator
import com.krake.core.map.MarkerConfiguration
import com.krake.core.map.MarkerInvalidator
import com.krake.core.map.MarkerKeyProvider
import com.krake.core.map.image.DefaultMarkerBitmapGenerator
import com.krake.core.media.loader.TermIconLoader
import com.krake.core.util.ColorUtil

/**
 * Interfaccia usata per definire un [ContentItem] che ha una posizione e che può essere
 * rappresentato su mappa.
 * Definisce anche come deve essere configurato il [Marker] abbinato al [ContentItem].
 */
interface ContentItemWithLocation : ContentItem, MarkerKeyProvider, MarkerConfiguration {
    val mapPart: MapPart?

    override fun markerPosition(): LatLng {
        return mapPart?.latLng ?: throw IllegalArgumentException("A valid position is required to show the marker.")
    }

    override fun markerTitle(): String? {
        return titlePartTitle
    }

    override fun markerSubtitle(): String? {
        return mapPart?.locationAddress
    }

    override fun markerBitmapGenerator(context: Context): BitmapGenerator {
        return DefaultMarkerBitmapGenerator(context,
                color = markerColor(context),
                innerDrawable = markerInnerDrawable(context),
                label = markerLabel(context))
    }

    override fun provideMarkerKey(context: Context): String {
        val iconId = (this as? ContentItemWithTermPart)?.termPart?.icon?.identifierOrStringIdentifier
        val color = markerColor(context)
        val label = markerLabel(context)

        val builder = StringBuilder()
        builder.append(ContentItemWithLocation::class.java.name)
        builder.append('|')
        builder.append(color)
        builder.append('|')
        iconId?.let {
            builder.append(it)
        }
        builder.append('|')
        label?.let {
            builder.append(it)
        }
        return builder.toString()
    }

    /**
     * Definisce il colore del [Marker] utilizzato da [DefaultMarkerBitmapGenerator].
     * DEFAULT: colore primario dell'app
     *
     * @param context [Context] utilizzato per ricavare il colore.
     * @return colore del [Marker].
     */
    @ColorInt fun markerColor(context: Context): Int {
        return ColorUtil.primaryColor(context)
    }

    /**
     * Definisce l'immagine interna del [Marker] che verrà utilizzata da [DefaultMarkerBitmapGenerator].
     * DEFAULT: icona della [TermPart] se il [ContentItem] implementa [ContentItemWithTermPart].
     *
     * @param context [Context] utilizzato per ricavare l'immagine interna.
     * @return immagine interna oppure null se l'immagine non deve essere inserita.
     */
    fun markerInnerDrawable(context: Context): Drawable? {
        var innerDrawable: Drawable? = null
        (this as? ContentItemWithTermPart)?.termPart?.let {
            // Carica l'icona della categoria.
            TermIconLoader.loadTerms(context, it, object : TermIconLoader.OnTermIconLoadListener {
                override fun onIconLoadCompleted(icon: Drawable, fromWs: Boolean) {
                    innerDrawable = icon
                    if (fromWs) {
                        // Invalida il Marker aggiornando l'icona.
                        (context.applicationContext as? MarkerInvalidator.Provider)?.provideMarkerInvalidator()?.invalidate(context, this@ContentItemWithLocation)
                    }
                }

                override fun onIconLoadFailed(fromWs: Boolean) {
                    /* empty */
                }
            })
        }
        return innerDrawable
    }

    /**
     * Definisce la label aggiuntiva applicata sopra il [Marker] per aggiungere del testo oltre all'annotation
     * che verrà utilizzata da [DefaultMarkerBitmapGenerator].
     * DEFAULT: null
     *
     * @param context [Context] utilizzato per ricavare la label.
     * @return testo scritto dentro la label del [Marker].
     */
    fun markerLabel(context: Context): String? {
        return null
    }
}