package com.krake.core.component.module

import android.content.Context
import android.os.Bundle
import androidx.collection.ArrayMap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.krake.core.Constants
import com.krake.core.component.base.ComponentModule
import com.krake.core.data.DataConnectionModel
import com.krake.core.extension.getBundle
import com.krake.core.extension.getDataClass
import com.krake.core.extension.putDataClass
import com.krake.core.model.RecordWithAutoroute
import com.krake.core.model.RecordWithIdentifier
import com.krake.core.model.RecordWithStringIdentifier
import io.realm.RealmModel
import java.lang.reflect.Type
import java.util.*

/**
 * Modulo utilizzato per specificare gli attributi relativi alla gestione dei dati.
 * Questi attributi influenzano:
 * <ul>
 * <li>Chiamate ad Orchard</li>
 * <li>Caricamento da DB</li>
 * </ul>
 * Utilizzato principalmente da [BaseOrchardServiceDataConnection] e dalle classi che la estendono.
 * N.B. Gli attributi fanno temporaneamente riferimento a quelli della connection, che non usa ancora i [ComponentModule].
 */
class OrchardComponentModule : ComponentModule {
    companion object {
        const val ARG_DATA_CLASS = "argDataClass"
        const val ARG_DATA_PART_FILTERS = Constants.REQUEST_ITEM_PART_FILTER
        const val ARG_DISPLAY_PATH = "argDisplayPath"
        const val ARG_EXTRA_PARAMETERS = "argExtraParameters"
        const val ARG_HEADERS = "LoadHeaders"
        const val ARG_PAGE_SIZE = Constants.REQUEST_PAGE_SIZE_KEY
        const val ARG_RECORD_IDENTIFIER = "argIdentifier"
        const val ARG_RECORD_STRING_IDENTIFIER = "argStringIdentifier"
        const val ARG_SEARCH_APPLIED_ONLINE = "argSearchOnline"
        const val ARG_SEARCH_COLUMN_NAME = "argSearchColumn"
        const val ARG_DATA_CONNECTION_CLASS = "argConnetionClass"
        const val ARG_START_CONNECTION_AFTER_ACTIVITY_CREATED = "argStartConnectionAfterActivityCreated"
        const val ARG_WEB_SERVICE_URL = "webServiceUrl"

        const val VALUE_PAGE_SIZE_NO_PAGING = -1
        const val VALUE_DEEP_LEVEL = Constants.REQUEST_DEEP_LEVEL
    }

    var webServiceUrl: String?
        private set

    var dataClass: Class<out RealmModel>?
        private set

    var dataPartFilters: String?
        private set

    var displayPath: String?
        private set

    var extraParameters: MutableMap<String, String> = ArrayMap()
        private set

    var headers: Bundle
        private set

    var pageSize: Int
        private set

    var recordIdentifier: Long?
        private set

    var recordStringIdentifier: String?
        private set

    var searchAppliedOnline: Boolean
        private set

    var searchColumnsName: Array<out String>?
        private set

    var startConnectionAfterActivityCreated: Boolean
        private set

    var dataConnectionModelClass: Class<out DataConnectionModel>
        private set

    val valueListeners: MutableList<ValueListener>

    private val gson = Gson()
    private val mapType: Type = object : TypeToken<MutableMap<String, String>>() {
    }.type

    init {
        dataConnectionModelClass = DataConnectionModel::class.java
        dataClass = null
        dataPartFilters = null
        displayPath = null
        headers = Bundle()
        pageSize = 25
        recordIdentifier = null
        recordStringIdentifier = null
        searchAppliedOnline = false
        searchColumnsName = null
        valueListeners = LinkedList()
        startConnectionAfterActivityCreated = true
        webServiceUrl = null
    }

    /**
     * Disattiva la paginazione. Questo influisce sia sul numero dei risultati ottenuti dal WS, sia sul caricamento dei dati nella lista.
     * DEFAULT: [pageSize] null
     */
    fun avoidPagination() = apply { this.pageSize = VALUE_PAGE_SIZE_NO_PAGING }

    /**
     * Classe di dato che da caricare dopo aver ricevuto i dati da Orchard.
     * Per caricare i dati in App è sempre necessario sapere quale tipo di informazione sarà restituito.
     * DEFAULT: [RealmModel]
     *
     * @param dataClass classe dei dati da caricare che estende [RealmModel].
     */
    fun dataClass(dataClass: Class<out RealmModel>?) = apply { this.dataClass = dataClass }

    /**
     * Specifica quali campi dei contenuti di Orchard devono essere restituiti. I campi
     * devono essere separati da virgola ed indicare il nome del campo indicato in Orchard (case sensitive).
     * Questa opzione è utilizzabile solo caricando dati propri di Orchard tramite una projection.
     * In caso non sia indicato saranno restituiti tutti i campi degli oggetti.
     * DEFAULT: null
     *
     * @param dataPartFilters stringa contenente tutti i campi da caricare separati da virgola.
     */
    fun dataPartFilters(dataPartFilters: String?) = apply { this.dataPartFilters = dataPartFilters }

    /**
     * Profondità massima dei nodi del Json scaricato da Orchard.
     * Un valore maggiore ritorna più dati ma richiede maggiore tempo di parsing/download.
     * DEFAULT: null (definito da Orchard)
     *
     * @param deepLevel numero di nodi massimo del Json scaricato da Orchard.
     */
    fun deepLevel(deepLevel: Int) = apply { putExtraParameter(VALUE_DEEP_LEVEL, Integer.valueOf(deepLevel).toString()) }

    /**
     * Url parziale del contenuto da richiedere ad Orchard.
     * Se relativo ad un singolo contenuto usare [RecordWithAutoroute.autoroutePartDisplayAlias].
     * DEFAULT: null
     *
     * @param displayPath url parziale del contenuto che verrà scaricato da Orchard.
     */
    fun displayPath(displayPath: String?) = apply {
        this.displayPath = displayPath
        valueListeners.forEach { it.onOrchardModuleValueChanged() }
    }

    /**
     * [Bundle] di chiavi - valori degli attributi da passare in query string.
     * Gli attributi verranno appesi in questo modo: $key1=$value1&key2=$value2&...
     * DEFAULT: null
     *
     * @param extraParameters [Bundle] con all'interno i parametri da passare in query string.
     */
    fun extraParameters(extraParameters: MutableMap<String, String>) = apply {
        this.extraParameters = extraParameters
        valueListeners.forEach { it.onOrchardModuleValueChanged() }
    }

    /**
     * webService da chiamare al posto di R.string.orchard_base_service_url + R.string.orchard_web_service_path
     * DEFAULT: null
     */
    fun webServiceUrl(url: String) = apply { this.webServiceUrl = url }

    /**
     * [Bundle] di chiavi - valori che verranno passati come header.
     * DEFAULT: null
     *
     * @param headers [Bundle] con all'interno gli headers.
     */
    fun headers(headers: Bundle) = apply { this.headers = headers }

    /**
     * Aggiunge gli headers per indicare al server di non effettuare nessuna cache.
     * Gli headers vengono aggiunti al [Bundle] [headers]
     */
    fun noCache() = apply {
        headers.putString("Cache-Control", "no-cache")
        headers.putString("Pragma", "no-cache")
        headers.putString("Expires", "0")
    }

    fun removeNoCache() = apply {
        headers.remove("Cache-Control")
        headers.remove("Pragma")
        headers.remove("Expires")
    }

    /**
     * Numero di elementi da scaricare in una pagina.
     * Per disattivare la paginazione usare [VALUE_PAGE_SIZE_NO_PAGING] o il metodo [avoidPagination].
     * DEFAULT: 25
     *
     * @param pageSize intero che indica il numero di elementi in una pagina.
     */
    fun pageSize(pageSize: Int) = apply { this.pageSize = pageSize }

    /**
     * Aggiunge un'extra alla lista dei parametri da passare in query string.
     * Se il valore è null, l'extra viene rimosso.
     *
     * @param key chiave da mettere in query string.
     * @param value valore da abbinare alla chiave.
     */
    fun putExtraParameter(key: String, value: String?) = apply {
        if (value == null) {
            extraParameters.remove(key)
        } else {
            extraParameters.put(key, value)
        }
        valueListeners.forEach { it.onOrchardModuleValueChanged() }
    }

    /**
     * Aggiunge un header alla lista degli headers.
     *
     * @param key chiave da mettere nell'header.
     * @param value valore da abbinare alla chiave.
     */
    fun putHeaderParameter(key: String, value: String) = apply { headers.putString(key, value) }

    /**
     * Identificativo del contenuto da mostrare preso da [RecordWithIdentifier.identifier].
     * Il caricamento dei dati da identificativo non esegue nessuna chiamata al WS per caricare i dati.
     * DEFAULT: null
     *
     * @param recordIdentifier identificativo del dato da caricare.
     */
    fun recordIdentifier(recordIdentifier: Long?) = apply { this.recordIdentifier = recordIdentifier }

    /**
     * String identifier del contenuto da mostrare preso da [RecordWithStringIdentifier.stringIdentifier].
     * Il caricamento dei dati da identificativo non esegue nessuna chiamata al WS per caricare i dati.
     * DEFAULT: null
     *
     * @param recordStringIdentifier identificativo del dato da caricare.
     */
    fun recordStringIdentifier(recordStringIdentifier: String?) = apply { this.recordStringIdentifier = recordStringIdentifier }

    /**
     * fa startare la [DataConnection] all'onCreate
     * DEFAULT: true
     *
     * @param startConnectionAfterActivityCreated start della [DataConnection] all'onCreate
     */
    fun startConnectionAfterActivityCreated(startConnectionAfterActivityCreated: Boolean) = apply { this.startConnectionAfterActivityCreated = startConnectionAfterActivityCreated }

    /**
     * Carica il record identifier oppure lo string identifier in base alla classe del record
     * Il caricamento dei dati da identificativo non esegue nessuna chiamata al WS per caricare i dati.
     * DEFAULT: null
     *
     * @param recordStringIdentifier identificativo del dato da caricare.
     */
    fun record(record: RealmModel) = apply {
        if (record is RecordWithIdentifier)
            recordIdentifier(record.identifier)
        else if (record is RecordWithStringIdentifier)
            recordStringIdentifier(record.stringIdentifier)
    }


    /**
     * Specifica se il filtro sugli elementi, quando viene utilizzata la funzionalità di ricerca, è applicato online oppure in locale.
     * Se il filtro viene applicato in locale, la paginazione deve essere disattivata.
     * La ricerca viene effettuata in like con questo formato: <i>%string_to_search%</i>.
     * DEFAULT: false
     *
     * @param searchAppliedOnline true se la ricerca deve essere applicata online.
     */
    fun searchAppliedOnline(searchAppliedOnline: Boolean) = apply {
        this.searchAppliedOnline = searchAppliedOnline
        if (!searchAppliedOnline && searchColumnsName?.isNotEmpty() ?: false) {
            avoidPagination()
        }
    }

    /**
     * Specifica la lista dei nomi delle colonne sulle quali è effettuata la ricerca.
     * La ricerca viene effettuata in like con questo formato: <i>%string_to_search%</i>.
     * DEFAULT: null
     *
     * @param columnName varargs di colonne sulle quali è effettuata la ricerca.
     */
    fun searchColumnsName(vararg columnName: String) = apply {
        searchColumnsName = columnName
        if (!searchAppliedOnline && searchColumnsName?.isNotEmpty() ?: false) {
            avoidPagination()
        }
    }

    fun dataConnectionModelClass(connectionClass: Class<out DataConnectionModel>) = apply {
        dataConnectionModelClass = connectionClass
    }

    /**
     * Legge il contenuto di un [Bundle] e modifica le sue proprietà.
     *
     * @param context il [Context] utilizzato per leggere il [Bundle].
     * @param bundle container dal quale vengono letti i vari arguments.
     */
    override fun readContent(context: Context, bundle: Bundle) {
        dataClass = bundle.getDataClass(ARG_DATA_CLASS)
        if (bundle.containsKey(ARG_RECORD_IDENTIFIER)) {
            recordIdentifier = bundle.getLong(ARG_RECORD_IDENTIFIER)
        }
        dataPartFilters = bundle.getString(ARG_DATA_PART_FILTERS)
        displayPath = bundle.getString(ARG_DISPLAY_PATH)
        recordStringIdentifier = bundle.getString(ARG_RECORD_STRING_IDENTIFIER)
        val params = bundle.getString(ARG_EXTRA_PARAMETERS)
        if (params != null) {
            extraParameters = gson.fromJson(params, mapType)
        }
        headers = bundle.getBundle(ARG_HEADERS, headers)
        pageSize = bundle.getInt(ARG_PAGE_SIZE, pageSize)
        searchAppliedOnline = bundle.getBoolean(ARG_SEARCH_APPLIED_ONLINE, searchAppliedOnline)
        searchColumnsName = bundle.getStringArray(ARG_SEARCH_COLUMN_NAME)
        startConnectionAfterActivityCreated = bundle.getBoolean(ARG_START_CONNECTION_AFTER_ACTIVITY_CREATED, startConnectionAfterActivityCreated)
        webServiceUrl = bundle.getString(ARG_WEB_SERVICE_URL)

        bundle.getString(ARG_DATA_CONNECTION_CLASS)?.let {
            val connectionClass = Class.forName(it) as? Class<out DataConnectionModel>
            if (connectionClass != null)
                dataConnectionModelClass = connectionClass
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
        bundle.putDataClass(ARG_DATA_CLASS, dataClass)
        recordIdentifier?.let {
            bundle.putLong(ARG_RECORD_IDENTIFIER, recordIdentifier!!)
        }
        recordStringIdentifier?.let { bundle.putString(ARG_RECORD_STRING_IDENTIFIER, it) }
        bundle.putString(ARG_DATA_PART_FILTERS, dataPartFilters)
        bundle.putString(ARG_DISPLAY_PATH, displayPath)
        bundle.putString(ARG_EXTRA_PARAMETERS, gson.toJson(extraParameters, mapType))
        bundle.putBundle(ARG_HEADERS, headers)
        bundle.putInt(ARG_PAGE_SIZE, pageSize)
        bundle.putBoolean(ARG_SEARCH_APPLIED_ONLINE, searchAppliedOnline)
        bundle.putStringArray(ARG_SEARCH_COLUMN_NAME, searchColumnsName)
        bundle.putBoolean(ARG_START_CONNECTION_AFTER_ACTIVITY_CREATED, startConnectionAfterActivityCreated)
        bundle.putString(ARG_DATA_CONNECTION_CLASS, dataConnectionModelClass.canonicalName)
        webServiceUrl?.let { bundle.putString(ARG_WEB_SERVICE_URL, it) }

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

    interface ValueListener
    {
        fun onOrchardModuleValueChanged()
    }
}