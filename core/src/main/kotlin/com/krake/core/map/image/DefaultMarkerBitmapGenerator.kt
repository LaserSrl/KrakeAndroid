package com.krake.core.map.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import com.google.android.gms.maps.model.Marker
import com.krake.core.R
import com.krake.core.image.BitmapGenerator
import com.krake.core.widget.PinView

/**
 * Implementazione di default di [BitmapGenerator] per generare la [Bitmap] del [Marker].
 * La [Bitmap] verrà generata utilizzando una [PinView] per creare l'icona del [Marker] con i path.
 *
 * @param context [Context] utilizzato per accedere alle risorse durante la generazione della [Bitmap].
 * @param color colore principale del [Marker].
 * @param innerDrawable immagine interna al [Marker] che riceverà il colore principale. Se null, non verrà inserita.
 * @param label [View] aggiuntiva applicata sopra il [Marker] per aggiungere del testo oltre all'annotation. Se null, non verrà inserita.
 */
class DefaultMarkerBitmapGenerator(context: Context, @ColorInt val color: Int, val innerDrawable: Drawable? = null, val label: String? = null) : BitmapGenerator {
    private val pinViewHeight: Int
    private val pinViewWidth: Int
    private val labelMaxWidth: Int
    private val labelInnerPadding: Int
    private val inflater = LayoutInflater.from(context)

    init {
        val res = context.resources
        pinViewHeight = res.getDimensionPixelSize(R.dimen.pin_height)
        pinViewWidth = res.getDimensionPixelSize(R.dimen.pin_width)
        labelMaxWidth = res.getDimensionPixelSize(R.dimen.pin_label_max_size)
        labelInnerPadding = res.getDimensionPixelSize(R.dimen.pin_label_inner_padding)
    }

    override fun generateBitmap(): Bitmap {
        // se la label è valorizzata, allora verrà disegnata
        val drawLabel = label?.isNotEmpty() ?: false

        @LayoutRes val layout = if (drawLabel) R.layout.pin_label_layout else R.layout.pin_base_layout
        val pinView = inflater.inflate(layout, null)

        val pinImage: PinView
        val viewWidth: Int

        if (drawLabel) {
            pinImage = pinView.findViewById(R.id.pin_image)
            val pinLabel: TextView = pinView.findViewById(R.id.pin_label)
            pinLabel.minWidth = labelMaxWidth / 2
            pinLabel.text = label

            val paint = Paint()
            paint.typeface = pinLabel.typeface
            paint.textSize = pinLabel.textSize
            val textWidth = paint.measureText(label)

            val labelWidth = textWidth + labelInnerPadding * 2
            viewWidth = if (labelWidth > pinViewWidth) labelWidth.toInt() else pinViewWidth
        } else {
            pinImage = pinView as PinView
            viewWidth = pinViewWidth
        }

        pinImage.setPinColor(color)

        innerDrawable?.let {
            pinImage.setInnerDrawable(it)
        }

        return drawBitmapFromView(pinView, viewWidth)
    }

    /**
     * Disegna la Bitmap partendo da una View e applica la colorazione

     * @param pinView   ImageView del pin
     * @param viewWidth larghezza della view in px
     * *
     * @return bitmap inserita in cache
     */
    private fun drawBitmapFromView(pinView: View, viewWidth: Int): Bitmap {
        // inizia la parte di creazione della Bitmap
        pinView.isDrawingCacheEnabled = true
        // si ottengono le misure e si trasforma l'ImageView
        pinView.measure(View.MeasureSpec.makeMeasureSpec(viewWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(pinViewHeight, View.MeasureSpec.EXACTLY))
        pinView.layout(0, 0, viewWidth, pinViewHeight)
        // viene generata la cache sul Canvas della view
        pinView.buildDrawingCache(true)
        // si crea la Bitmap dell'ImageView
        val src = Bitmap.createBitmap(pinView.drawingCache)
        // si finalizza l'operazione
        pinView.isDrawingCacheEnabled = false
        return src
    }
}