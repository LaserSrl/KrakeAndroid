package com.krake.core.widget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.ColorInt

/**
 * View che gestisce i pin da mostrare su mappa.
 * <br>
 * Vengono utilizzati 3 Paint:
 * <ul>
 * <li>pin di base con possibilità di cambiare colore</li>
 * <li>cerchio bianco interno</li>
 * <li>immagine all'interno del cerchio (opzionale) colorata secondo il colore del pin</li>
 * </ul>
 */
class PinView : View {
    companion object {
        private val TAG = PinView::class.java.simpleName
    }

    private lateinit var pinBasePaint: Paint
    private lateinit var circlePaint: Paint
    private lateinit var bitmapPaint: Paint

    private lateinit var pinBasePath: Path
    private lateinit var circlePath: Path

    private lateinit var bitmapRect: Rect

    private var innerBitmap: Bitmap? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    /**
     * Inizializza tutte le configurazioni di default della View per evitare un lavoro eccessivo nell'onDraw()
     */
    private fun init() {
        pinBasePaint = Paint()
        pinBasePaint.apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        circlePaint = Paint()
        circlePaint.apply {
            style = Paint.Style.FILL
            color = Color.WHITE
            isAntiAlias = true
        }

        bitmapPaint = Paint()
        bitmapPaint.isAntiAlias = true

        pinBasePath = Path()
        circlePath = Path()

        bitmapRect = Rect()

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val height = measuredHeight
        // uguale al diametro
        val width = measuredWidth

        if (height / 3 != width / 2) {
            Log.e(TAG, "onDraw: to have a correct drawing process, the height must be equal to the width / 2 * 3")
        }

        // uguale al raggio
        val centerX = (width / 2).toFloat()

        val semiCircleValueInset = (width * 0.05).toFloat()
        var semiCircleValueOffset = (width * 2 / 3).toFloat()
        val topOffset = semiCircleValueOffset - centerX
        semiCircleValueOffset -= topOffset

        pinBasePath.apply {
            moveTo(0f, semiCircleValueOffset)
            // disegna un semicerchio con una curva di Bezier cubica
            cubicTo(semiCircleValueInset, -topOffset, width - semiCircleValueInset, -topOffset, width.toFloat(), semiCircleValueOffset)
            // la Y del punto di controllo delle due curve quadratiche del pin, viene messo molto vicino ai due punti del diametro del semicerchio superiore
            val quadYControlPoint = (height / 5 * 3.2).toFloat()
            quadTo(width.toFloat(), quadYControlPoint, centerX, height.toFloat())
            quadTo(0f, quadYControlPoint, 0f, semiCircleValueOffset)
            close()
        }

        // disegna il path per il semicerchio con una curva di Bezier cubica
        canvas.drawPath(pinBasePath, pinBasePaint)

        val innerCircleRadius = semiCircleValueOffset / 8 * 7
        circlePath.apply {
            addCircle(centerX, semiCircleValueOffset, innerCircleRadius, Path.Direction.CW)
            close()
        }

        // disegna il path per le righe
        canvas.drawPath(circlePath, circlePaint)

        innerBitmap?.let {
            val sizeRatio = it.height.toDouble() / it.width.toDouble()

            val heightRatio = if (sizeRatio < 1) sizeRatio else 1.0
            val widthRatio = if (sizeRatio > 1) sizeRatio else 1.0
            val innerCircleDiameter = innerCircleRadius * 2
            val halfRectHeight = innerCircleDiameter.toDouble() / Math.sqrt(2.0) / 2.0 * heightRatio
            val halfRectWidth = innerCircleDiameter.toDouble() / Math.sqrt(2.0) / 2.0 / widthRatio

            bitmapRect.apply {
                left = (centerX - halfRectWidth).toInt()
                top = (semiCircleValueOffset - halfRectHeight).toInt()
                right = (centerX + halfRectWidth).toInt()
                bottom = (semiCircleValueOffset + halfRectHeight).toInt()
            }

            canvas.drawBitmap(it, null, bitmapRect, bitmapPaint)
        }
    }

    /**
     * Cambia il colore di base del pin, quindi il cerchio bianco è escluso

     * @param color colore da settare al pin
     */
    fun setPinColor(@ColorInt color: Int) {
        pinBasePaint.color = color
        bitmapPaint.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }

    /**
     * Inserisce un'immagine all'interno del cerchio bianco del pin

     * @param bitmap Bitmap da inserire
     */
    fun setInnerBitmap(bitmap: Bitmap?) {
        innerBitmap = bitmap
    }

    /**
     * Inserisce un'immagine all'interno del cerchio bianco del pin

     * @param drawable Drawable da inserire
     */
    fun setInnerDrawable(drawable: Drawable?) {
        val bitmap = if (drawable != null) generateInnerBitmap(drawable) else null
        setInnerBitmap(bitmap)
    }

    /**
     * Genera la [Bitmap] all'interno del pin supportando i vari tipi di drawable (Bitmap, Shape, Vector, ecc..)

     * @param drawable [Drawable] da convertire
     * *
     * @return [Bitmap] generata
     */
    private fun generateInnerBitmap(drawable: Drawable): Bitmap {
        var bitmap: Bitmap?

        // se è una BitmapDrawable, non bisogna fare nessuna conversione
        if (drawable is BitmapDrawable) {
            bitmap = drawable.bitmap
            bitmap?.let {
                return it
            }
        }

        var intrinsicWidth = drawable.intrinsicWidth
        var intrinsicHeight = drawable.intrinsicHeight

        // tutti i colori piani o le forme ritorneranno -1 come larghezza e altezza
        if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
            intrinsicWidth = 1
            intrinsicHeight = 1
        }

        bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap!!)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}