package com.krake.core.component.module

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.krake.core.component.base.ComponentModule
import com.krake.core.extension.getDataClass
import com.krake.core.extension.putDataClass
import com.krake.core.media.MediaPartFullscreenActivity
import com.krake.core.model.MediaPart
import com.krake.core.model.RecordWithIdentifier
import com.krake.core.model.RecordWithStringIdentifier
import io.realm.RealmModel
import java.lang.reflect.Type
import java.util.*

/**
 * Modulo utilizzato per specificare gli attributi di un'[Activity] usata per visualizzare le immagini.
 * Il tipo di oggetto utilizzato è [MediaPart].
 * Utilizzato principalmente da [MediaPartFullscreenActivity] e dalle [Activity] che la estendono.
 */
class MediaComponentModule : ComponentModule {
    companion object {
        private const val ARG_MEDIA_CLASS = "argMediaClass"
        private const val ARG_MEDIA_IDS = "argMediaIds"
        private const val ARG_MEDIA_STRING_IDS = "argMediaStringIds"
        private const val ARG_MEDIA_URLS = "argMediaUrls"
        private const val ARG_MEDIA_INDEX = "argMediaIndex"
    }

    var mediaPartClass: Class<out RealmModel>?
        private set

    private var mediaPartList: List<MediaPart>
    private var selectedMediaPart: MediaPart?

    var mediaIndex: Int
        private set
    var mediaIds: MutableList<Long>?
        private set
    var mediaStringIds: MutableList<String>?
        private set

    var mediaUrls: MutableList<String>?
        private set

    val gson = Gson()

    init {
        mediaPartClass = null
        mediaPartList = mutableListOf()
        selectedMediaPart = null
        mediaIndex = 0
        mediaIds = null
        mediaStringIds = null
        mediaUrls = null
    }

    /**
     * Specifica la classe di dato da caricare di tipo [MediaPart].
     * DEFAULT: null
     *
     * @param mediaPartClass classe di tipo [MediaPart].
     */
    fun mediaPartClass(mediaPartClass: Class<out RealmModel>?) = apply { this.mediaPartClass = mediaPartClass }

    /**
     * Specifica la lista delle [MediaPart] che verrà visualizzata nella galleria.
     * DEFAULT: lista vuota
     *
     * @param mediaPartList lista delle [MediaPart] da visualizzare in galleria.
     */
    fun mediaPartList(mediaPartList: List<MediaPart>) = apply { this.mediaPartList = mediaPartList }

    /**
     * Specifica la [MediaPart] della lista che è stata selezionata.
     * DEFAULT: null
     *
     * @param selectedMediaPart [MediaPart] della lista che è stata selezionata.
     */
    fun selectedMediaPart(selectedMediaPart: MediaPart?) = apply { this.selectedMediaPart = selectedMediaPart }

    /**
     * Legge il contenuto di un [Bundle] e modifica le sue proprietà.
     *
     * @param context il [Context] utilizzato per leggere il [Bundle].
     * @param bundle container dal quale vengono letti i vari arguments.
     */
    override fun readContent(context: Context, bundle: Bundle) {
        mediaPartClass = bundle.getDataClass(ARG_MEDIA_CLASS)
        mediaIndex = bundle.getInt(ARG_MEDIA_INDEX, mediaIndex)
        val valueIds = bundle.getString(ARG_MEDIA_IDS)
        if (valueIds != null) {
            val longListType: Type = object : TypeToken<MutableList<Long>>() {
            }.type
            mediaIds = gson.fromJson(valueIds, longListType)
        }
        val valueStringIds = bundle.getString(ARG_MEDIA_STRING_IDS)
        if (valueStringIds != null) {
            val stringListTYpe: Type = object : TypeToken<MutableList<String>>() {
            }.type
            mediaStringIds = gson.fromJson(valueIds, stringListTYpe)
        }

        val valueUrls = bundle.getString(ARG_MEDIA_URLS)
        if (valueUrls != null) {
            val stringListTYpe: Type = object : TypeToken<MutableList<String>>() {
            }.type
            mediaUrls = gson.fromJson(valueUrls, stringListTYpe)
        }
    }

    /**
     * Scrive le proprietà di un modulo su un [Bundle].
     *
     * @param context il [Context] utilizzato per creare il [Bundle] e scrivere su di esso.
     * @return container che contiene le proprietà del modulo corrente.
     */
    override fun writeContent(context: Context): Bundle {
        val bundle = Bundle()
        bundle.putDataClass(ARG_MEDIA_CLASS, mediaPartClass)
        val index: Int
        if (selectedMediaPart != null) {
            index = mediaPartList.indexOf(selectedMediaPart!!)
        } else {
            index = 0
        }
        bundle.putInt(ARG_MEDIA_INDEX, index)

        if (mediaPartClass != null && RecordWithIdentifier::class.java.isAssignableFrom(mediaPartClass!!)) {
            val ids = mediaPartList.mapTo(LinkedList<Long>()) { (it as RecordWithIdentifier).identifier }
            bundle.putString(ARG_MEDIA_IDS, gson.toJson(ids))
        } else if (mediaPartClass != null && RecordWithStringIdentifier::class.java.isAssignableFrom(mediaPartClass!!)) {
            val ids = mediaPartList.mapTo(LinkedList<String>()) { (it as RecordWithStringIdentifier).stringIdentifier }
            bundle.putString(ARG_MEDIA_STRING_IDS, gson.toJson(ids))
        } else {

            val ids = mediaPartList.mapTo(LinkedList()) { it.mediaUrl ?: "" }
            bundle.putString(ARG_MEDIA_URLS, gson.toJson(ids))
        }

        return bundle
    }
}