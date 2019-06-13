package com.krake.core.component.module

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.annotation.LongDef
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.maps.android.clustering.ClusterManager
import com.krake.core.R
import com.krake.core.app.*
import com.krake.core.component.base.ComponentModule
import com.krake.core.extension.getBundle
import com.krake.core.extension.getClass
import com.krake.core.extension.putClass
import com.krake.core.extension.putModules
import com.krake.core.model.ContentItem
import com.krake.core.widget.ContentItemAdapter
import com.krake.core.widget.ImageTextCellHolder

/**
 * Modulo utilizzato per specificare gli attributi di un'[Activity] lista/mappa.
 * Utilizzato principalmente da:
 * <ul>
 * <li>[ContentItemListMapActivity] e dalle [Activity] che la estendono</li>
 * <li>[ContentItemGridFragment] e dai [Fragment] che lo estendono</li>
 * <li>[ContentItemMapFragment] e dai [Fragment] che lo estendono</li>
 * </ul>
 */
class ListMapComponentModule(val context: Context) : ComponentModule {
    companion object {
        const val PRIORITY_LIST = 1L
        const val PRIORITY_MAP = 2L

        private const val ARG_ACTIVITY_LAYOUT = "argActivityLayout"
        private const val ARG_ANALYTICS_NAME = "argAnalyticsName"
        private const val ARG_CONTENT_PRIORITY = "argContentPriority"
        private const val ARG_DETAIL_ACTIVITY = "argDetActivity"
        private const val ARG_DETAIL_BUNDLE = "argDetFragmentArgs"
        private const val ARG_LIST_ADAPTER_CLASS = "argAdapterClass"
        private const val ARG_LIST_CELL_LAYOUT = "argListCellLayout"
        private const val ARG_LIST_FRAGMENT_CLASS = "argListFragmentClass"
        private const val ARG_LIST_NO_ELEMENTS_MSG = "argListNoElementsMsg"
        private const val ARG_LIST_ROOT_LAYOUT = "argListRootLayout"
        private const val ARG_LIST_VIEW_HOLDER_CLASS = "argViewHolderClass"
        private const val ARG_LOAD_DETAILS_BY_PATH = "argLoadDetByPath"
        private const val ARG_MAP_AVOID_LOCATION_PERMISSIONS = "argMapAvoidLocationPermissions"
        private const val ARG_MAP_FRAGMENT_CLASS = "argMapFragmentClass"
        private const val ARG_MAP_USE_CLUSTER = "argMapUseCluster"
        private const val ARG_NO_DETAILS = "argNoDetails"
        private const val ARG_SHOW_MAP = "argShowMap"
        private const val ARG_TERMS_MODULE = "argTermsModule"
        private const val ARG_SHOW_GPS_DIALOG = "argShowGpsDialog"
    }

    @LayoutRes
    var activityLayout: Int
        private set

    var analyticsName: String?
        private set

    @Priority
    var contentPriority: Long
        private set

    var detailActivity: Class<out ContentItemDetailActivity>
        private set

    private var detailModules: MutableList<ComponentModule>

    /**
     * Usa il detail bundle per leggere i module dopo l'apertura della nuova activity.
     * In fase di scrittura può essere che non siano ancora coerenti le informazioni contenute in
     * detailModules e detailBundle
     *
     */
    var detailBundle: Bundle
        private set

    var listAdapterClass: Class<out ContentItemAdapter>
        private set

    @LayoutRes
    var listCellLayout: Int
        private set

    var listFragmentClass: Class<out ContentItemGridModelFragment>
        private set

    var listNoElementsMsg: String
        private set

    @LayoutRes
    var listRootLayout: Int
        private set

    var listViewHolderClass: Class<out RecyclerView.ViewHolder>
        private set

    var loadDetailsByPath: Boolean
        private set

    var mapAvoidLocationPermissions: Boolean
        private set

    var mapShowGpsDialogIfDisabled: Boolean
        private set

    var mapFragmentClass: Class<out ContentItemMapModelFragment>
        private set

    var mapUseCluster: Boolean
        private set

    var noDetails: Boolean
        private set

    var showMap: Boolean
        private set

    private var termsModules: Array<out ComponentModule>

    var termsBundle: Bundle?
        private set

    init {
        activityLayout = 0
        analyticsName = null
        contentPriority = PRIORITY_LIST
        detailActivity = ContentItemDetailActivity::class.java
        detailModules = mutableListOf(DetailComponentModule(context))
        detailBundle = Bundle()
        listAdapterClass = ContentItemAdapter::class.java
        listCellLayout = R.layout.content_item_image_cell
        listFragmentClass = ContentItemGridModelFragment::class.java
        listNoElementsMsg = context.getString(R.string.content_item_grid_no_elements_message)
        listRootLayout = R.layout.fragment_content_items_selection
        listViewHolderClass = ImageTextCellHolder::class.java
        loadDetailsByPath = true
        mapFragmentClass = ContentItemMapModelFragment::class.java
        mapAvoidLocationPermissions = false
        mapUseCluster = false
        noDetails = false
        showMap = true
        mapShowGpsDialogIfDisabled = false
        termsModules = emptyArray()
        termsBundle = createBundleWithModules(context, *termsModules)
    }

    /**
     * Specifica il layout dell'[Activity].
     * Vengono gestiti automaticamente due [FrameLayout] all'interno dell'[activityLayout]:
     * <ul>
     * <li>[R.id.contentitem_list] al cui interno verrà mostrata la lista</li>
     * <li>[R.id.contentitem_map] al cui interno verrà mostrata la mappa</li>
     * </ul>
     * DEFAULT: 0 -> in [ContentItemListMapActivity] se la mappa deve essere visualizzata [R.layout.activity_content_items_map_grid],
     * altrimenti [R.layout.activity_content_items_list_only]
     *
     * @param activityLayout layout dell'[Activity]
     */
    fun activityLayout(@LayoutRes activityLayout: Int) = apply { this.activityLayout = activityLayout }

    /**
     * Specifica il testo che verrà mandato ad analytics all'apertura della sezione.
     * DEFAULT: null -> in [ContentItemListMapActivity] il testo è uguale a [ContentItemListMapActivity.getTitle]
     *
     * @param analyticsName testo in formato [String] da mandare ad analytics.
     */
    fun analyticsName(analyticsName: String?) = apply { this.analyticsName = analyticsName }

    /**
     * Specifica quale contenuto della UI avrà la priorità.
     * La priorità incide su quale [Fragment] viene mostrato prima se l'[Activity] usa lo switch oppure quale [Fragment] si espande quando non ci sono elementi.
     * DEFAULT: [PRIORITY_LIST]
     *
     * @param contentPriority un valore tra [PRIORITY_LIST] e [PRIORITY_MAP].
     */
    fun contentPriority(contentPriority: Long) = apply { this.contentPriority = contentPriority }

    /**
     * Specifica un'[Activity] che estende [ContentItemDetailActivity] che verrà aperta dopo il tap su una cella.
     * DEFAULT: [ContentItemDetailActivity]
     *
     * @param detailActivity classe dell'[Activity] che estende [ContentItemDetailActivity].
     */
    fun detailActivity(detailActivity: Class<out ContentItemDetailActivity>) = apply { this.detailActivity = detailActivity }

    /**
     * Specifica i [ComponentModule] che verranno passati alla [ContentItemDetailActivity] e al [ContentItemDetailModelFragment].
     * DEFAULT: array con all'interno un [DetailComponentModule]
     *
     * @param detailModules varargs di moduli che verranno passati al dettaglio.
     */
    fun detailModules(vararg detailModules: ComponentModule) = apply { this.detailModules = mutableListOf<ComponentModule>().apply { this.addAll(detailModules) } }

    /**
     * Aggiunge un [ComponentModule] a quelli che verranno passati alla [ContentItemDetailActivity] e al [ContentItemDetailModelFragment].
     *
     * @param detailModules varargs di moduli che verranno passati al dettaglio.
     */
    fun addDetailModules(detailModule: ComponentModule) = apply { this.detailModules.add(detailModule) }

    /**
     * Specifica un adapter che estende [ContentItemAdapter] che verrà utilizzata dalla [RecyclerView] all'interno del [ContentItemGridFragment].
     * DEFAULT: [ContentItemAdapter]
     *
     * @param listAdapterClass classe dell'adapter che estende [ContentItemAdapter].
     */
    fun listAdapterClass(listAdapterClass: Class<out ContentItemAdapter>) = apply { this.listAdapterClass = listAdapterClass }

    /**
     * Specifica il layout della cella all'interno della [RecyclerView].
     * DEFAULT: [R.layout.content_item_image_cell]
     *
     * @param listCellLayout layout res della cella.
     */
    fun listCellLayout(@LayoutRes listCellLayout: Int) = apply { this.listCellLayout = listCellLayout }

    /**
     * Specifica il [Fragment] che estende [ContentItemGridFragment] utilizzato all'interno dell'[Activity] per mostrare la lista.
     * DEFAULT: [ContentItemGridFragment]
     *
     * @param listFragmentClass classe del [Fragment] che estende [ContentItemGridFragment] utilizzato nell'[Activity].
     */
    fun listFragmentClass(listFragmentClass: Class<out ContentItemGridModelFragment>) = apply { this.listFragmentClass = listFragmentClass }

    /**
     * Specifica il testo da visualizzare nel [ContentItemGridFragment] nel caso in cui ci siano degli errori.
     * DEFAULT: [R.string.content_item_grid_no_elements_message]
     *
     * @param listNoElementsMsg testo in formato [String] da visualizzare nel caso in cui ci siano errori.
     */
    fun listNoElementsMsg(listNoElementsMsg: String) = apply { this.listNoElementsMsg = listNoElementsMsg }

    /**
     * Specifica il layout del [ContentItemGridFragment].
     * Il layout deve contenere necessariamente una [RecyclerView] con id [android.R.id.list].
     * DEFAULT: [R.layout.fragment_content_items_selection]
     *
     * @param listRootLayout del [ContentItemGridFragment]
     */
    fun listRootLayout(@LayoutRes listRootLayout: Int) = apply { this.listRootLayout = listRootLayout }

    /**
     * Specifica un view holder che estende [RecyclerView.ViewHolder] che verrà utilizzata dall'adapter all'interno del [ContentItemGridFragment].
     * DEFAULT: [ImageTextCellHolder]
     *
     * @param listViewHolderClass classe del view holder che estende [RecyclerView.ViewHolder].
     */
    fun listViewHolderClass(listViewHolderClass: Class<out RecyclerView.ViewHolder>) = apply { this.listViewHolderClass = listViewHolderClass }

    /**
     * Specifica se i dettagli devono essere caricati tramite [ContentItem.getId] (quindi viene letto il record dal DB) oppure
     * tramite [ContentItem.getAutoroutePartDisplayAlias] (quindi viene scaricato da WS)
     * DEFAULT: false
     *
     * @param loadDetailsByPath true se il dettaglio deve essere scaricato da WS, false se deve essere caricato tramite id.
     */
    fun loadDetailsByPath(loadDetailsByPath: Boolean) = apply { this.loadDetailsByPath = loadDetailsByPath }

    /**
     * Specifica se il [Fragment] che estende [ContentItemMapFragment] deve evitare di richiedere i permessi per la localizzazione.
     * DEFAULT: false
     */
    fun mapAvoidLocationPermissions() = apply { this.mapAvoidLocationPermissions = true }

    /**
     * Specifica se il [Fragment] che estende [ContentItemMapFragment] deve mostrare l'accensione del gps in caso fosse spento
     * DEFAULT: false
     */
    fun mapShowGpsDialogIfDisabled() = apply { this.mapShowGpsDialogIfDisabled = true }

    /**
     * Specifica il [Fragment] che estende [ContentItemMapFragment] utilizzato all'interno dell'[Activity] per mostrare la mappa.
     * DEFAULT: [ContentItemMapFragment]
     *
     * @param mapFragmentClass classe del [Fragment] che estende [ContentItemMapFragment] utilizzato nell'[Activity].
     */
    fun mapFragmentClass(mapFragmentClass: Class<out ContentItemMapModelFragment>) = apply { this.mapFragmentClass = mapFragmentClass }

    /**
     * Specifica se utilizzare o meno il [ClusterManager] nel [ContentItemMapFragment].
     * DEFAULT: false
     *
     * @param mapUseCluster true nel caso in cui si voglia utilizzare il [ClusterManager].
     */
    fun mapUseCluster(mapUseCluster: Boolean) = apply { this.mapUseCluster = mapUseCluster }

    /**
     * Impedisce l'apertura dei dettagli dopo il tap su una cella.
     * DEFAULT: false (i dettagli vengono sempre aperti)
     */
    fun notShowDetails() = apply { noDetails = true }

    /**
     * Specifica se visualizzare o meno la mappa quando viene mostrata una lista di [LocationContentItem].
     * DEFAULT: true
     *
     * @param showMap true per visualizzare la mappa quando l'item è di tipo [LocationContentItem].
     */
    fun showMap(showMap: Boolean) = apply { this.showMap = showMap }

    /**
     * Specifica i [ComponentModule] che verranno passati al [TermsFragment].
     * Se non verranno inseriti moduli, il [TermsFragment] non verrà aggiunto.
     * DEFAULT: array vuoto.
     *
     * @param termsModules varargs di moduli che verranno passati al [TermsFragment].
     */
    fun termsModules(vararg termsModules: ComponentModule) = apply { this.termsModules = termsModules }

    /**
     * Legge il contenuto di un [Bundle] e modifica le sue proprietà.
     *
     * @param context il [Context] utilizzato per leggere il [Bundle].
     * @param bundle container dal quale vengono letti i vari arguments.
     */
    override fun readContent(context: Context, bundle: Bundle) {
        activityLayout = bundle.getInt(ARG_ACTIVITY_LAYOUT, activityLayout)
        analyticsName = bundle.getString(ARG_ANALYTICS_NAME, analyticsName)
        contentPriority = bundle.getLong(ARG_CONTENT_PRIORITY, contentPriority)
        detailActivity = bundle.getClass(ARG_DETAIL_ACTIVITY, detailActivity)!!
        detailBundle = bundle.getBundle(ARG_DETAIL_BUNDLE, Bundle())
        listAdapterClass = bundle.getClass(ARG_LIST_ADAPTER_CLASS, listAdapterClass)!!
        listCellLayout = bundle.getInt(ARG_LIST_CELL_LAYOUT, listCellLayout)
        listFragmentClass = bundle.getClass(ARG_LIST_FRAGMENT_CLASS, listFragmentClass)!!
        listNoElementsMsg = bundle.getString(ARG_LIST_NO_ELEMENTS_MSG, listNoElementsMsg)
        listRootLayout = bundle.getInt(ARG_LIST_ROOT_LAYOUT, listRootLayout)
        listViewHolderClass = bundle.getClass(ARG_LIST_VIEW_HOLDER_CLASS, listViewHolderClass)!!
        loadDetailsByPath = bundle.getBoolean(ARG_LOAD_DETAILS_BY_PATH, loadDetailsByPath)
        mapAvoidLocationPermissions = bundle.getBoolean(ARG_MAP_AVOID_LOCATION_PERMISSIONS, mapAvoidLocationPermissions)
        mapFragmentClass = bundle.getClass(ARG_MAP_FRAGMENT_CLASS, mapFragmentClass)!!
        mapUseCluster = bundle.getBoolean(ARG_MAP_USE_CLUSTER, mapUseCluster)
        noDetails = bundle.getBoolean(ARG_NO_DETAILS, noDetails)
        showMap = bundle.getBoolean(ARG_SHOW_MAP, showMap)
        termsBundle = bundle.getBundle(ARG_TERMS_MODULE)
        mapShowGpsDialogIfDisabled = bundle.getBoolean(ARG_SHOW_GPS_DIALOG)
    }

    /**
     * Scrive le proprietà di un modulo su un [Bundle].
     *
     * @param context il [Context] utilizzato per creare il [Bundle] e scrivere su di esso.
     * @return container che contiene le proprietà del modulo corrente.
     */
    override fun writeContent(context: Context): Bundle {
        val bundle = Bundle()
        bundle.putInt(ARG_ACTIVITY_LAYOUT, activityLayout)
        bundle.putString(ARG_ANALYTICS_NAME, analyticsName)
        bundle.putLong(ARG_CONTENT_PRIORITY, contentPriority)
        bundle.putClass(ARG_DETAIL_ACTIVITY, detailActivity)
        detailBundle.putModules(context, *detailModules.toTypedArray())
        bundle.putBundle(ARG_DETAIL_BUNDLE, detailBundle)
        bundle.putClass(ARG_LIST_ADAPTER_CLASS, listAdapterClass)
        bundle.putInt(ARG_LIST_CELL_LAYOUT, listCellLayout)
        bundle.putClass(ARG_LIST_FRAGMENT_CLASS, listFragmentClass)
        bundle.putInt(ARG_LIST_ROOT_LAYOUT, listRootLayout)
        bundle.putString(ARG_LIST_NO_ELEMENTS_MSG, listNoElementsMsg)
        bundle.putClass(ARG_LIST_VIEW_HOLDER_CLASS, listViewHolderClass)
        bundle.putBoolean(ARG_LOAD_DETAILS_BY_PATH, loadDetailsByPath)
        bundle.putBoolean(ARG_MAP_AVOID_LOCATION_PERMISSIONS, mapAvoidLocationPermissions)
        bundle.putClass(ARG_MAP_FRAGMENT_CLASS, mapFragmentClass)
        bundle.putBoolean(ARG_MAP_USE_CLUSTER, mapUseCluster)
        bundle.putBoolean(ARG_NO_DETAILS, noDetails)
        bundle.putBoolean(ARG_SHOW_MAP, showMap)
        bundle.putBoolean(ARG_SHOW_GPS_DIALOG, mapShowGpsDialogIfDisabled)
        if (termsModules.isNotEmpty()) {
            bundle.putModules(context, ARG_TERMS_MODULE, *termsModules)
        }
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

    private fun createBundleWithModules(context: Context, vararg modules: ComponentModule): Bundle {
        val bundle = Bundle()
        bundle.putModules(context, *modules)
        return bundle
    }

    @LongDef(PRIORITY_LIST, PRIORITY_MAP)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Priority
}