package com.krake.core.map

import android.content.Context
import android.graphics.Bitmap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.krake.core.image.BitmapGenerator
import com.krake.core.map.image.DefaultMarkerBitmapGenerator
import com.krake.core.util.ColorUtil

/**
 * Specifica le proprietà di un [Marker] da inserire sulla mappa.
 * Questa configurazione si può utilizzare anche con un [ClusterManager].
 */
interface MarkerConfiguration : ClusterItem {

    /**
     * @return posizione del [Marker].
     */
    fun markerPosition(): LatLng

    /**
     * @return titolo dell'annotation del [Marker].
     */
    fun markerTitle(): String?

    /**
     * @return sottotitolo dell'annotation del [Marker].
     */
    fun markerSubtitle(): String?

    /**
     * Specifica il generatore per la [Bitmap] che rappresenta l'icona del [Marker].
     * Non è obbligatorio questo metodo che funziona solo da utilità per generare più velocemente una [Bitmap].
     * L'implementazione di default utilizza un [DefaultMarkerBitmapGenerator] e il colore primario come colore del [Marker].
     *
     * @param context [Context] utilizzato per generare l'icona.
     * @return generatore dell'icona del [Marker].
     */
    fun markerBitmapGenerator(context: Context): BitmapGenerator {
        return DefaultMarkerBitmapGenerator(context,
                color = ColorUtil.primaryColor(context))
    }

    /**
     * Specifica l'icona del [Marker] come [BitmapDescriptor].
     * L'implementazione di default utilizza un [BitmapGenerator] per generare una [Bitmap] che
     * rappresenta il [Marker].
     *
     * @param context [Context] utilizzato per generare l'icona.
     * @return icona del [Marker].
     */
    fun markerIcon(context: Context): BitmapDescriptor {
        val bitmap = markerBitmapGenerator(context).generateBitmap()
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    /**
     * @return posizione del [Marker] quando viene usato in un [ClusterManager].
     */
    override fun getPosition(): LatLng {
        return markerPosition()
    }

    /**
     * @return titolo dell'annotation del [Marker] quando viene usato in un [ClusterManager].
     */
    override fun getTitle(): String? {
        return markerTitle()
    }

    /**
     * @return sottotitolo dell'annotation del [Marker] quando viene usato in un [ClusterManager].
     */
    override fun getSnippet(): String? {
        return markerSubtitle()
    }
}