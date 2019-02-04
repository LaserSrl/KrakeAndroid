package com.krake.contentcreation.component.module

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.google.gson.Gson
import com.krake.contentcreation.ContentCreationActivity
import com.krake.contentcreation.ContentCreationUtils
import com.krake.contentcreation.ContentDefinition
import com.krake.contentcreation.R
import com.krake.core.Constants
import com.krake.core.UploadInterceptor
import com.krake.core.component.base.ComponentModule
import com.krake.core.component.module.LoginComponentModule
import com.krake.core.component.module.OrchardComponentModule
import com.krake.core.extension.putModules

/**
 * Modulo utilizzato per specificare gli attributi di un'[Activity] usata per creare un nuovo contenuto
 * o modificarne uno esistente.
 * Utilizzato principalmente da [ContentCreationActivity] e dalle [Activity] che la estendono.
 */
class ContentCreationComponentModule(context: Context) : ComponentModule {
    companion object {
        private const val ARG_AVOID_ACTIVITY_CLOSING_AFTER_CONTENT_SENT = "argAvoidClosingAfterSent"
        private const val ARG_CONTENT_DEFINITION = "argContentCreationInfos"
        private const val ARG_ORIGINAL_OBJECT_CONNECTION = "argOriginalObjectConnection"
        private const val ARG_UPLOAD_PARAMS = "argUploadParams"
    }

    var avoidActivityClosingAfterContentSent: Boolean
        private set

    var contentDefinition: ContentDefinition
        private set

    var originalObjectConnection: OrchardComponentModule?
        private set

    var uploadParams: Bundle
        private set

    private val gson: Gson = ContentCreationUtils.getGsonInstance()

    init {
        avoidActivityClosingAfterContentSent = false
        contentDefinition = ContentCreationUtils.getDefaultUserContentCreation(context)
        originalObjectConnection = null
        uploadParams = Bundle()
    }

    /**
     * Specifica se chiudere la [ContentCreationActivity] o meno dopo che è stato effettuato un upload.
     * DEFAULT: false
     *
     * @param avoidActivityClosingAfterContentSent true nel caso in cui l'[Activity] non si debba chiudere.
     */
    fun avoidActivityClosingAfterContentSent(avoidActivityClosingAfterContentSent: Boolean) = apply {
        this.avoidActivityClosingAfterContentSent = avoidActivityClosingAfterContentSent
    }

    /**
     * Specifica le definizioni di contenuto da passare alla [ContentCreationActivity] per la visualizzazione e
     * l'upload del contenuto.
     * DEFAULT: [ContentCreationUtils.getDefaultUserContentCreation]
     *
     * @param contentDefinition definizione di contenuto passata alla [ContentCreationActivity].
     */
    fun contentDefinition(contentDefinition: ContentDefinition) = apply {
        this.contentDefinition = contentDefinition
        if (!this.uploadParams.containsKey(Constants.REQUEST_CONTENT_TYPE))
        {
            this.uploadParams.putString(Constants.REQUEST_CONTENT_TYPE, contentDefinition.contentType)
        }
    }

    /**
     * Specifica il modulo di tipo [OrchardComponentModule] da usare per scaricare un contenuto già esistente.
     * In questo modo è possibile modificare un contenuto già esistente su Orchard.
     * La [ContentDefinition] del contenuto da modificare deve essere coerente con quella presente su Orchard.
     * DEFAULT: null
     *
     * @param originalObjectConnection modulo che specifica gli attributi della connection per scaricare un contenuto già esistente su Orchard.
     */
    fun originalObjectConnection(originalObjectConnection: OrchardComponentModule?) = apply { this.originalObjectConnection = originalObjectConnection }

    /**
     * Specifica il [Bundle] di extra da passare agli [UploadInterceptor].
     * DEFAULT: [Bundle] vuoto
     *
     * @param uploadParams [Bundle] di extra da passare agli [UploadInterceptor].
     */
    fun uploadParams(uploadParams: Bundle) = apply { this.uploadParams = uploadParams }

    /**
     * Legge il contenuto di un [Bundle] e modifica le sue proprietà.
     *
     * @param context il [Context] utilizzato per leggere il [Bundle].
     * @param bundle container dal quale vengono letti i vari arguments.
     */
    override fun readContent(context: Context, bundle: Bundle) {
        avoidActivityClosingAfterContentSent = bundle.getBoolean(ARG_AVOID_ACTIVITY_CLOSING_AFTER_CONTENT_SENT, avoidActivityClosingAfterContentSent)
        val contentDefinitionJson = bundle.getString(ARG_CONTENT_DEFINITION)
        contentDefinition = gson.fromJson(contentDefinitionJson, ContentDefinition::class.java)
        val originalObjectConnectionBundle = bundle.getBundle(ARG_ORIGINAL_OBJECT_CONNECTION)
        originalObjectConnectionBundle?.let {
            originalObjectConnection = OrchardComponentModule()
            originalObjectConnection!!.readContent(context, originalObjectConnectionBundle)
        }
        uploadParams = bundle.getBundle(ARG_UPLOAD_PARAMS) ?: Bundle()
        bundle.putString(context.getString(R.string.orchard_new_content_type_parameter), contentDefinition.contentType)
    }

    /**
     * Scrive le proprietà di un modulo su un [Bundle].
     *
     * @param context il [Context] utilizzato per creare il [Bundle] e scrivere su di esso.
     * @return container che contiene le proprietà del modulo corrente.
     */
    override fun writeContent(context: Context): Bundle {
        val bundle = Bundle()
        bundle.putBoolean(ARG_AVOID_ACTIVITY_CLOSING_AFTER_CONTENT_SENT, avoidActivityClosingAfterContentSent)
        val contentDefinitionString = gson.toJson(contentDefinition)
        bundle.putString(ARG_CONTENT_DEFINITION, contentDefinitionString)
        originalObjectConnection?.let {
            bundle.putModules(context, ARG_ORIGINAL_OBJECT_CONNECTION, originalObjectConnection!!)
        }
        bundle.putBundle(ARG_UPLOAD_PARAMS, uploadParams)
        bundle.putString(context.getString(R.string.orchard_new_content_type_parameter), contentDefinition.contentType)
        return bundle
    }

    /**
     * Definisce la lista delle classi da cui questo modulo deve dipendere.
     *
     * @return array di dipendenze.
     */
    override fun moduleDependencies(): Array<Class<out ComponentModule>> {
        return arrayOf(LoginComponentModule::class.java)
    }
}