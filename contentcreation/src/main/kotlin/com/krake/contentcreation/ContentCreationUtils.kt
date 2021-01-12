package com.krake.contentcreation

import android.content.Context
import android.net.Uri
import android.util.SparseArray
import com.google.gson.*
import com.krake.core.media.MediaType
import com.krake.core.model.Policy
import com.krake.core.util.PlainAbstractTypeAdapter
import com.krake.core.util.RealmHierarchyAbstractTypeAdapter
import com.krake.core.util.UriGsonSerializer
import java.util.*

/**
 * Classe di utilità per il modulo ContentCreation
 */
object ContentCreationUtils {

    const val CONTENT_CREATION_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss"

    /**
     * Metodo di utilità che permette di creare una ContentDefinition già valorizzata con i seguenti valori:
     *
     *  * Tab dei media con la possibilità di caricare foto, video, audio
     *  * Tab delle info con i campi: titolo, sottotitolo, descrizione e categoria
     *  * Tab della mappa
     *
     *
     * @param context Context corrente
     * @return ContentDefinition con le configurazioni di default
     */
    @JvmStatic
    fun getDefaultUserContentCreation(context: Context): ContentDefinition {
        val creationInfos = ArrayList<ContentCreationTabInfo>(3)

        // creazione del tab dei media
        creationInfos.add(ContentCreationTabInfo.createMediaInfo(R.string.medias,
                "Gallery",
                "medias",
                true,
                MediaType.IMAGE or MediaType.VIDEO or MediaType.AUDIO,
                1))

        // creazione del tab delle info
        val fields = ArrayList<ContentCreationTabInfo.FieldInfo>(4)
        fields.add(ContentCreationTabInfo.FieldInfo(R.string.title, "TitlePart.Title", "titlePartTitle", ContentCreationTabInfo.FIELD_TYPE_TEXT, true))
        fields.add(ContentCreationTabInfo.FieldInfo(R.string.subtitle, "Sottotitolo", "sottotitoloValue", ContentCreationTabInfo.FIELD_TYPE_TEXT, true))
        val bodyArray = SparseArray<Any>(1)
        bodyArray.put(FieldExtras.Text.KEY_MAX_LINES, FieldExtras.Text.MAX_LINES_NO_LIMIT)
        fields.add(ContentCreationTabInfo.FieldInfo(R.string.description, "BodyPart.Text", "bodyPartText", ContentCreationTabInfo.FIELD_TYPE_TEXT, true, bodyArray, null))
        fields.add(ContentCreationTabInfo.FieldInfo(R.string.category, "UserReport.Categoria", "categoriaTerms", ContentCreationTabInfo.FIELD_TYPE_ENUM_OR_TERM_SELECTION, true))

        creationInfos.add(ContentCreationTabInfo.createFieldsInfo(R.string.infos, fields))

        // creazione del tab della mappa
        creationInfos.add(ContentCreationTabInfo.createMapInfo(R.string.map, "MapPart", "mapPart", true))

        // parametri impliciti
        val jsonParameters = JsonObject()
        jsonParameters.addProperty("Language", context.getString(R.string.orchard_language))
        val status = JsonArray()
        status.add(JsonPrimitive(context.getString(R.string.orchard_content_creation_status)))
        jsonParameters.add("PublishExtensionStatus", status)

        return ContentDefinition(context.getString(R.string.orchard_citizen_user_reports_content_type), true, creationInfos, jsonParameters)
    }

    @JvmStatic
    fun getGsonInstance(): Gson {
        val builder = GsonBuilder()
                .registerTypeAdapter(ContentCreationTabInfo.ContentCreationInfo::class.java, PlainAbstractTypeAdapter())
                .registerTypeAdapter(ContentCreationTabInfo.FieldInfoValidator::class.java, PlainAbstractTypeAdapter())
                .registerTypeAdapter(Uri::class.java, UriGsonSerializer())
                .setDateFormat(CONTENT_CREATION_DATE_PATTERN)

        builder.registerTypeHierarchyAdapter(Policy::class.java, RealmHierarchyAbstractTypeAdapter(builder.create()))
        return builder.create()
    }
}