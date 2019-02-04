package com.krake.core.media.loader

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.annotation.UiThread
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.util.ArrayMap
import com.krake.core.R
import com.krake.core.extension.asDrawable
import com.krake.core.media.DownloadOnlyLoadable
import com.krake.core.model.RecordWithIdentifier
import com.krake.core.model.TermPart
import com.krake.core.util.ResourceUtil

/**
 * Classe che espone i metodi per ottenere l'icona da una {@link TermPart}.
 * L'icona verrà scaricata dal WS solo se non è già stata scaricata precedentemente dal plugin di Gradle.
 */
object TermIconLoader {

    /**
     * Scarica un'icona, come [Bitmap], legata ad una [TermPart]
     *
     * @param context  context corrente
     * @param termPart istanza di [TermPart] relativa ad un ContentItem
     * @param listener listener per gestire lo stato di download dell'icona
     */
    fun loadTerms(context: Context, termPart: TermPart, listener: OnTermIconLoadListener) = load(context, termPart, listener)

    /**
     * Scarica un'icona, come [Bitmap], legata ad una [TermPart]
     *
     * @param termPart istanza di [TermPart] relativa ad un ContentItem
     * @param listener listener per gestire lo stato di download dell'icona
     */
    fun loadTerms(activity: Activity, termPart: TermPart, listener: OnTermIconLoadListener) = load(activity, termPart, listener)

    /**
     * Scarica un'icona, come [Bitmap], legata ad una [TermPart]
     *
     * @param termPart istanza di [TermPart] relativa ad un ContentItem
     * @param listener listener per gestire lo stato di download dell'icona
     */
    fun loadTerms(fragment: Fragment, termPart: TermPart, listener: OnTermIconLoadListener) = load(fragment.activity ?:
            throw IllegalArgumentException("The activity mustn't be null"), termPart, listener)

    private val mTermImages = ArrayMap<String, Drawable>()

    /**
     * Scarica un'icona, come [Bitmap], legata ad una [TermPart]
     *
     * @param context  context corrente
     * @param termPart istanza di [TermPart] relativa ad un ContentItem
     * @param listener listener per gestire lo stato di download dell'icona
     */
    private fun load(context: Context, termPart: TermPart, listener: OnTermIconLoadListener) {
        val termIcon = termPart.icon
        if (termIcon == null) {
            assignListenerToDrawable(listener, null, false)
            return
        }

        // Accede ad un'icona legata alle categorie scaricate dal PinMapManager
        var categoryResGen = 0
        if (termIcon is RecordWithIdentifier) {
            categoryResGen = ResourceUtil.resourceForName(context, ResourceUtil.DRAWABLE, context.getString(R.string.partial_term_icon_name) + (termIcon as RecordWithIdentifier).identifier)
        }
        val mediaUrl = termIcon.mediaUrl
        var icon: Drawable? = mTermImages[mediaUrl]

        if (icon == null) {
            if (categoryResGen == 0) {

                MediaLoader.typedWith<Drawable>(context, object : DownloadOnlyLoadable() {})
                        .mediaPart(termIcon)
                        .getRequest()
                        .asDrawable()
                        .addListener(object : ImageLoader.RequestListener<Drawable> {
                            override fun onDataLoadSuccess(resource: Drawable?) {
                                mTermImages.put(mediaUrl, resource)
                                assignListenerToDrawable(listener, resource, true)
                            }

                            override fun onDataLoadFailed() {}
                        })
                        .load()
            } else {
                icon = ContextCompat.getDrawable(context, categoryResGen)
                assignListenerToDrawable(listener, icon, false)
            }
        } else {
            assignListenerToDrawable(listener, icon, false)
        }
    }

    private fun assignListenerToDrawable(listener: OnTermIconLoadListener, icon: Drawable?, fromWs: Boolean) {
        if (icon != null) {
            listener.onIconLoadCompleted(icon, fromWs)
        } else {
            listener.onIconLoadFailed(fromWs)
        }
    }

    /**
     * Listener che gestisce lo stato di download dell'icona.
     */
    interface OnTermIconLoadListener {

        /**
         * Notifica che il caricamento è stato completato con successo.
         *
         * @param icon icona scaricata da WS o caricata dalle risorse
         */
        @UiThread
        fun onIconLoadCompleted(icon: Drawable, fromWs: Boolean)

        /**
         * Notifica che il caricamento è fallito.
         * Se la [TermPart] non ha un'icona, questo metodo viene richiamato automaticamente.
         */
        @UiThread
        fun onIconLoadFailed(fromWs: Boolean)
    }
}