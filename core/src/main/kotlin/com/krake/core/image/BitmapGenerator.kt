package com.krake.core.image

import android.graphics.Bitmap

/**
 * Interfaccia che gestisce la generazione di una [Bitmap].
 */
interface BitmapGenerator {

    /**
     * Genera una [Bitmap] che può essere utilizzata sulla UI o per salvare le informazioni
     * di un'immagine.
     *
     * @return bitmap generata.
     */
    fun generateBitmap(): Bitmap
}