package com.krake.core.data

import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.krake.core.OrchardError
import com.krake.core.PrivacyStatus
import com.krake.core.PrivacyViewModel
import com.krake.core.cache.CacheManager
import com.krake.core.component.module.LoginComponentModule
import com.krake.core.component.module.OrchardComponentModule
import com.krake.core.login.LoginManager
import com.krake.core.login.PrivacyException
import com.krake.core.model.*
import com.krake.core.network.CancelableRequest
import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmQuery
import java.util.*

open class DataConnectionModel() : ViewModel(),
        LoginComponentModule.ValueListener,
        OrchardComponentModule.ValueListener,
        Observer<Boolean>,
    DataConnectionBase
{
    private val mutableModel = MutableLiveData<DataModel>()
    val model: LiveData<DataModel> = mutableModel
    val multiThreadModel: LiveData<DataModel> = Transformations.map(model) { source ->
        return@map DataModel(Realm.getDefaultInstance().copyFromRealm(source.listData), source.cacheValid)
    }

    private val mutableDataError = MutableLiveData<OrchardError>()
    val dataError: LiveData<OrchardError> = mutableDataError

    private val mutableLoadingData: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { this.value = false }
    val loadingData: LiveData<Boolean> = mutableLoadingData

    val waitingLogin: Boolean
        get()
        {
            return loginModule.loginRequired && !(LoginManager.shared.isLogged.value ?: false)
        }

    private var searchFilter: String? = null

    private var currentRequestCache: RequestCache? = null

    private var cacheName: String? = null

    private var waitingPrivacy: Boolean = false

    lateinit final override var orchardModule: OrchardComponentModule
        private set
    lateinit final override var loginModule: LoginComponentModule
        private set
    lateinit var privacyViewModel: PrivacyViewModel
        private set

    val privacyObserver: Observer<PrivacyStatus> = Observer {
        if (it != null)
        {
            if (it.equals(PrivacyStatus.ACCEPTED))
            {
                if (waitingPrivacy)
                {
                    waitingPrivacy = false
                    restartDataLoading()
                }
            }
            else
            {
                waitingPrivacy = true
            }
        }
    }

    private var setupCompleted = false

    private var cancelableDataLoading: CancelableRequest? = null

    override final var page = 1
        set(value)
        {
            if (orchardModule.pageSize == 0 || orchardModule.pageSize == OrchardComponentModule.VALUE_PAGE_SIZE_NO_PAGING)
            {
                field = 1
            }
            else
            {
                field = value
            }
        }

    override final val isLoadingData: Boolean
        get()
        {
            return loadingData.value ?: false
        }

    constructor(orchardModule: OrchardComponentModule,
                loginModule: LoginComponentModule,
                privacyViewModel: PrivacyViewModel) : this()
    {
        configure(orchardModule,
                  loginModule,
                  privacyViewModel)
    }

    /**
     * Metodo da richiamare in caso sia utilizzato il costruttore vuoto come per il carimaneto dal viewmodelfactory
     */
    fun configure(orchardModule: OrchardComponentModule,
                  loginModule: LoginComponentModule,
                  privacyViewModel: PrivacyViewModel)
    {
        if (!setupCompleted)
        {
            setupCompleted = true
            this.orchardModule = orchardModule
            this.loginModule = loginModule
            this.privacyViewModel = privacyViewModel
            orchardModule.valueListeners.add(this)
            loginModule.valueListeners.add(this)
            LoginManager.shared.isLogged.observeForever(this)

            privacyViewModel.privacyStatus.observeForever(privacyObserver)

            if (orchardModule.startConnectionAfterActivityCreated)
                restartDataLoading()
        }
    }

    override fun onCleared()
    {
        super.onCleared()
        if (setupCompleted)
        {
            orchardModule.valueListeners.remove(this)
            loginModule.valueListeners.remove(this)
            privacyViewModel.privacyStatus.removeObserver(privacyObserver)
            LoginManager.shared.isLogged.removeObserver(this)
        }
    }

    override fun onLoginModuleValueChanged()
    {
        if (!loginModule.loginRequired && LoginManager.shared.isLogged.value != true)
        {
            restartDataLoading(searchFilter)
        }
    }

    override fun onOrchardModuleValueChanged()
    {
        updateCacheName()
        page = 1
    }

    override fun onChanged(t: Boolean?)
    {
        if (t == true && loginModule.loginRequired)
        {
            restartDataLoading(searchFilter)
        }
    }

    @JvmOverloads
    fun restartDataLoading(filter: String? = searchFilter)
    {
        searchFilter = filter
        if (orchardModule.recordIdentifier != null)
        {
            loadSingleElement(true, { it.equalTo(RecordWithIdentifier.IdentifierFieldName, orchardModule.recordIdentifier!!) })
        }
        else if (!orchardModule.recordStringIdentifier.isNullOrEmpty())
        {
            loadSingleElement(true, { it.equalTo(RecordWithStringIdentifier.StringIdentifierFieldName, orchardModule.recordStringIdentifier!!) })
        }
        else if (!orchardModule.displayPath.isNullOrEmpty() && getCacheName() != null)
        {
            currentRequestCache = RequestCache.findCacheWith(getCacheName()!!)

            var needToLoadDataFromWebService = currentRequestCache == null || !CacheManager.shared.isCacheValid(currentRequestCache!!, orchardModule.extraParameters)

            if (!needToLoadDataFromWebService && orchardModule.headers.containsKey("Cache-Control"))
            {
                needToLoadDataFromWebService = currentRequestCache!!.dateExecuted.before(Date(Date().time - 10000))
            }

            if (currentRequestCache == null && canAnticipateLoadingWithDirectPath())
                loadSingleElement(false,
                                  { it.equalTo(RecordWithAutoroute.AutorouteDisplayAliasFieldName, orchardModule.displayPath!!) })

            cancelRemoteLoading()
            if (needToLoadDataFromWebService)
            {
                if (!waitingLogin && !waitingPrivacy)
                    loadDataFromRemote()
            }

            if (currentRequestCache != null)
                loadDataFromCache(currentRequestCache!!, !needToLoadDataFromWebService)
            else
                mutableModel.value = DataModel(listOf(), false)
        }
    }

    override final fun loadDataFromRemote()
    {
        cancelableDataLoading?.cancel()
        cancelableDataLoading = null
        mutableLoadingData.value = true
        Log.d("LOADTEST", "Inizio caricamento")
        val dataLoading = RemoteDataRepository.shared
                .loadData(loginModule,
                          orchardModule,
                          page,
                    object : (Int, RequestCache?, OrchardError?) -> Unit
                          {
                              override fun invoke(code: Int, p1: RequestCache?, p2: OrchardError?)
                              {
                                  if (cancelableDataLoading?.code == code) {

                                      mutableLoadingData.value = false

                                      cancelableDataLoading = null
                                      if (p1 != null && isCacheRelativeTorCurrentParameters(p1)) {
                                          currentRequestCache = p1
                                          loadDataFromCache(p1, true)
                                          mutableDataError.value = null
                                      } else if (p2 != null) {
                                          mutableDataError.value = p2

                                          if (p2.reactionCode == OrchardError.REACTION_PRIVACY) {
                                              privacyViewModel.needToAcceptPrivacy(p2.originalException as PrivacyException)
                                          } else if (p2.reactionCode == OrchardError.REACTION_LOGIN) {
                                              if (loginModule.loginRequired == false) {
                                                  loginModule.loginRequired(true)
                                                  restartDataLoading(searchFilter)
                                              }
                                          }
                                      }
                                  }
                              }
                          })

        cancelableDataLoading = dataLoading
    }

    private fun cancelRemoteLoading() {
        mutableLoadingData.value = false
        cancelableDataLoading?.cancel()
        cancelableDataLoading = null
    }

    private fun getCacheName(): String?
    {
        if (cacheName == null)
        {
            updateCacheName()
        }
        return cacheName
    }

    fun updateCacheName()
    {
        if (orchardModule.displayPath != null)
        {
            cacheName = CacheManager.shared.getCacheKey(orchardModule.displayPath!!, orchardModule.extraParameters)
        }
        else
        {
            cacheName = null
        }
    }

    private fun isCacheRelativeTorCurrentParameters(currentRequestCache: RequestCache): Boolean {
        return currentRequestCache.cacheName == cacheName || cacheName == null
    }

    private fun loadDataFromCache(currentRequestCache: RequestCache, isValid: Boolean)
    {
        Log.d(
            "LOADTEST",
            "Cache (${currentRequestCache.cacheName == cacheName}) key ($cacheName) newCache (${currentRequestCache.cacheName})"
        )
        if (isCacheRelativeTorCurrentParameters(currentRequestCache)) {
            var elements = currentRequestCache.elements(orchardModule.dataClass)

            if (orchardModule.searchColumnsName?.isNotEmpty() == true &&
                !searchFilter.isNullOrEmpty() &&
                orchardModule.pageSize == OrchardComponentModule.VALUE_PAGE_SIZE_NO_PAGING
            ) {
                val searchFilter = searchFilter!!

                elements = elements.filter {
                    (it as? RecordWithFilter)?.recordContains(searchFilter, orchardModule.searchColumnsName!!)
                        ?: true
                }
            }

            mutableModel.value = DataModel(elements, isValid)
        }
    }

    private fun loadSingleElement(willCacheBeValid: Boolean, applyQuery: (RealmQuery<*>) -> RealmQuery<*>)
    {
        val result = applyQuery(Realm.getDefaultInstance()
                                        .where(orchardModule.dataClass))
                .findFirst() as? RealmModel
        if (result != null)
        {
            mutableModel.value = DataModel(listOf(result), willCacheBeValid)
        }
    }

    private fun canAnticipateLoadingWithDirectPath(): Boolean
    {
        return orchardModule.dataClass != null &&
                orchardModule.displayPath != null &&
                orchardModule.dataPartFilters.isNullOrEmpty() &&
                RecordWithAutoroute::class.java.isAssignableFrom(orchardModule.dataClass) &&
                orchardModule.displayPath == getCacheName()
    }

}

class DataModel(val listData: List<RealmModel>, val cacheValid: Boolean)