package com.krake.core.data

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import com.krake.core.PrivacyViewModel
import com.krake.core.component.module.LoginComponentModule
import com.krake.core.component.module.OrchardComponentModule


/**
 * Classe usata per integrare il caricamento dati dal servizio di Orchard.
 * Può essere utilizzata in {@link Activity} o {@link Fragment}
 * <p/>
 * Questa classe permette di specificare i parametri da passare ad orchard per il caricamento dei dati.
 * Integra la gestione per l'accesso a {@link com.krake.core.OrchardService} la gestione della cache
 * che viene gestita appoggiandosi a {@link CacheManager} ottenuto da {@link OrchardApplication#getCacheManager()}
 * <p/>
 * Arguments possibilit:
 * <ol>
 * <li>{@link BaseOrchardServiceDataConnection#ARG_DATA_CLASS} String: {@link Class#getCanonicalName()} della classe di dato da caricare da Orchard.
 * Per caricare i dati in App è sempre necessario sapere quale tipo di informazione sarà restituito.
 * Corrisponde all'attributo xml R.styleable.orchard_data_fragment_orchard_data_class</li>
 * <li>{@link BaseOrchardServiceDataConnection#ARG_ORCHARD_DISPLAY_PATH} String: autoroutePath corrispondente al contenuto da mostrare. La connection si occupa autonomamente di gestire la validità della cache del contenuto.
 * Corrisponde all'attributo xml  R.styleable.orchard_data_fragment_orchard_display_path</li>
 * <li>{@link BaseOrchardServiceDataConnection#ARG_ORCHARD_PAGE_SIZE} Dimensione della pagina da richiedere ad orchard.
 * Se pari a {@link #PAGE_SIZE_NO_PAGING} non sarà utilizzata paginazione.
 * Rende superfluo il parametro {@link #ARG_ORCHARD_IDENTIFIER}</li>
 * <li>{@link BaseOrchardServiceDataConnection#ARG_ORCHARD_ITEM_FILTER}  String: parametro per indicare quali campi dei contenuti di orchard devono essere restituiti. I campi
 * devono essere separati da virgola ed indicare il nome del campo indicato in Orchard (case sensitive).
 * Questa opzione è utilizzabile solo caricando dati propri di Orchard tramite una projection.
 * In caso non sia indicato saranno restituiti tutti i campi degli oggetti.</li>
 * <li>{@link BaseOrchardServiceDataConnection#ARG_ORCHARD_EXTRA_PARAMETERS_BUNDLE} Bundle: parametri aggiuntivi da passare ad orchard udrante la chiamata.
 * Contiene la serializzazione con {@link Gson} di una {@link HashMap HashMap<String,String>}</li>
 * <li>{@link BaseOrchardServiceDataConnection#ARG_ORCHARD_IDENTIFIER} Long: identificativo del contenuto da mostrare.
 * Il caricamento dei dati da identificativo non esegue nessuna chiamata al WS per caricare i dati</li>
 * <li>{@link com.krake.core.content.BaseOrchardServiceDataConnection#ARG_ORCHARD_LOGIN_REQUIRED} Bool: indica se è necessario accedere ai dati solo dopo aver effettuato la login
 * {@link com.krake.core.app.LoginAndPrivacyActivity}</li>
 * <li>{@link com.krake.core.content.BaseOrchardServiceDataConnection#ARG_ORCHARD_LOAD_DATA_HEADERS_BUNDLE}  Bundle: header aggiuntivi per la chiamata ad Orchard.
 * Sono normalmente utilizzati per specificare le indicazioni noCache dell'html
 * Contiene la serializzazione con {@link Gson} di una {@link HashMap HashMap<String,String>}</li>
 * <li>{@link com.krake.core.content.BaseOrchardServiceDataConnection#ARG_SERVICE_INTENT_EXTRAS}  Bundle: parametri aggiuntivi da passare direttamente all'intent per avviare un'azione su {@link com.krake.core.OrchardService}</li>
 * </ol>
 * <p/>
 * Il comportamento del caricamento è molto diverso in base agli argomenti passati
 * <ol>
 * <li>tramite parametro {@link BaseOrchardServiceDataConnection#ARG_ORCHARD_IDENTIFIER} identificativo numerico che
 * rappresenta il recorda da caricare. In questo caos il record viene caricato direttamente dalla base dati
 * non viene mai effettuata nessuna chiamata ad orchard</li>
 * <li>tramite {@link BaseOrchardServiceDataConnection#ARG_ORCHARD_DISPLAY_PATH} viene verificata la validità della cache
 * in caso questa sia scaduta viene effettuata la chaimata ad orchard coi parametri indicati</li>
 * </ol>
 * <p/>
 * Per collegarlo correttamente ad un activity o fragment è necessario che siano richiamati i seguenti metodi:
 * <ol>
 * <li>{@link #onCreate(Bundle, Bundle)} da richiamare nei metodi {@link Activity#onCreate(Bundle)} o {@link Fragment#onCreate(Bundle)}</li>
 * <li>{@link #onActivityCreated(Bundle)} da richiamare nel metodo {@link Fragment#onActivityCreated(Bundle)}</li>
 * <li>{@link #onStart()} da richiamare nei metodi {@link Activity#onStart()} o {@link Fragment#onStart()}</li>
 * <li>{@link #onStop()} da richiamare nei metodi {@link Activity#onStop()} o {@link Fragment#onStop()} </li>
 * <li>{@link #onDestroy()} da richiamare nei metodi {@link Activity#onDestroy()} o {@link Fragment#onDestroy()} </li>
 * </ol>
 */
@Deprecated("Use DataConnectionModel")
open class DataConnection(context: Context, val dataListener: DataListener) : DataConnectionBase, Observer<DataModel>
{

    var dataConnectionModel: DataConnectionModel

    override val isLoadingData: Boolean
        get()
        {
            return dataConnectionModel.isLoadingData
        }

    private val privacyViewModel: PrivacyViewModel
    override val loginModule: LoginComponentModule
        get()
        {
            return dataConnectionModel.loginModule
        }

    override val orchardModule: OrchardComponentModule
        get()
        {
            return dataConnectionModel.orchardModule
        }


    override var page: Int
        get()
        {
            return dataConnectionModel.page
        }
        set(value) {
            dataConnectionModel.page = 1
        }

    init {
        //TODO: prenderlo da viewmodelproviders
        dataConnectionModel = DataConnectionModel()

        if (context is FragmentActivity)
        {
            privacyViewModel = ViewModelProviders.of(context).get(PrivacyViewModel::class.java)
        }
        else
        {
            privacyViewModel = PrivacyViewModel()
        }
    }

    private var fragment: Fragment? = null

    /**
     * Construttore secondario da un fragment
     * @param fragment che contiene la connection
     * @param dataListener
     */
    constructor(fragment: Fragment, dataListener: DataListener) : this(fragment.activity ?:
            throw IllegalArgumentException("The activity mustn't be null."), dataListener) {
        //noinspection deprecation
        this.fragment = fragment
    }

    @JvmOverloads
    fun startServiceLoading(forceLoadingOnRefresh: Boolean = false) {
        dataConnectionModel.loadDataFromRemote()
    }

    override fun loadDataFromRemote()
    {
        dataConnectionModel.loadDataFromRemote()
    }

    @JvmOverloads
    @Override
    fun onCreate(orchardModule: OrchardComponentModule,
                 loginModule: LoginComponentModule = LoginComponentModule(),
                 savedInstanceState: Bundle? = null)
    {

        dataConnectionModel.configure(orchardModule,
                                      loginModule,
                                      privacyViewModel)

        if (dataListener.needToAccessDataInMultiThread())
        {
            dataConnectionModel.multiThreadModel.observeForever(this)
        }
        else
        {
            dataConnectionModel.model.observeForever(this)
        }
        dataConnectionModel.dataError.observeForever { if (it != null) dataListener.onDefaultDataLoadFailed(it, false) }
    }


    fun onActivityCreated(savedInstanceState: Bundle?) {

    }

    fun onStart()
    {

    }

    fun onStop()
    {

    }

    fun onDestroy() {

    }

    @JvmOverloads
    fun restartDataLoading(filter: String? = null)
    {
        dataConnectionModel.restartDataLoading(filter)
    }

    override fun onChanged(t: DataModel?)
    {
        if (t != null)
            dataListener.onDefaultDataLoaded(t.listData, t.cacheValid)
    }


}