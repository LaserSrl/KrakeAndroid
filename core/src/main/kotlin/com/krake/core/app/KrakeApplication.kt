package com.krake.core.app

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.http.HttpResponseCache
import android.os.Build
import android.os.Bundle
import android.os.Trace
import android.util.Log
import android.util.SparseArray
import androidx.collection.ArrayMap
import androidx.core.app.NotificationCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.model.Marker
import com.google.firebase.analytics.FirebaseAnalytics
import com.krake.core.*
import com.krake.core.cache.AutoQueryCacheManager
import com.krake.core.cache.CacheManager
import com.krake.core.component.module.ThemableComponentModule
import com.krake.core.data.OrchardRemoteDataRepository
import com.krake.core.data.RemoteDataRepository
import com.krake.core.extension.equalsToIntent
import com.krake.core.fetcher.content.FetchableContent
import com.krake.core.fetcher.content.MapFetchableContent
import com.krake.core.fetcher.content.WebViewFetchableContent
import com.krake.core.fetcher.manager.PoolPreFetchManager
import com.krake.core.fetcher.manager.PreFetchManager
import com.krake.core.login.LoginManager
import com.krake.core.login.RegistrationListener
import com.krake.core.map.DefaultMarkerCreator
import com.krake.core.map.MarkerCreator
import com.krake.core.map.MarkerInvalidator
import com.krake.core.media.UploadableMediaInfo
import com.krake.core.media.loader.ImageManager
import com.krake.core.media.loader.ImageManagerProvider
import com.krake.core.media.loader.glide.GlideImageManager
import com.krake.core.media.streaming.StreamingProvider
import com.krake.core.model.ContentItem
import com.krake.core.model.ContentItemWithLocation
import com.krake.core.model.ShareLinkPart
import com.krake.core.network.OkHttpRemoteClient
import com.krake.core.network.RemoteClient
import com.krake.core.social.DetailIntentSharingInterceptor
import com.krake.core.thread.async
import com.krake.core.util.realmCleanClassName
import io.realm.Realm
import io.realm.RealmConfiguration
import java.io.File
import java.util.*

/**
 * Application base da utilizzare per accedere ad Orchard.
 * Tutte le App devono estenderla.
 * <p/>
 * Inoltre l'implementazione può modificare +
 * {@link #setCacheManager(CacheManager)} e {@link #setLocationContentItemsMapManager(LocationContentItemMapManager)}
 * <p/>
 * La classe gestisce autonomamente l'uso di GoogleAnalytics.
 * se non configurato la parte di analytics, queste funzionalità saranno disabilitate.
 * <p/>
 * Inoltre permette di gestire i listener per le diverse chiamate
 * <ul>
 * <li>{@link #addUserRegistrationEndListener(Class)}</li>
 * <li>{@link #registerApiEndListener(String, Class)}</li>
 * <li>{@link #registerDetailFragment(Class, Class, Bundle)}</li>
 * <li>{@link #registerProjectionContentClass(Class, Intent)}</li>
 * <li>{@link #registerSignalEndListener(String, Class)}</li>
 * <li>{@link #registerUploadCompleteListener(Integer, Class)}</li>
 * </ul>
 */
abstract class KrakeApplication : Application(),
        AnalyticsApplication,
        MarkerInvalidator.Provider,
        Application.ActivityLifecycleCallbacks,
        ImageManagerProvider {

    companion object {
        private val TAG = KrakeApplication::class.java.simpleName

        const val KRAKE_NOTIFICATION_CHANNEL = "krakeNotificationChannel"
        const val OPEN_PUSH_NOTIFICATION = "open_push_notification"
    }

    private val registeredDetailsFragment =
        ArrayMap<String, Pair<Class<out Fragment>, ((Bundle) -> Bundle)?>>()
    private val registeredProjectionClasses = ArrayMap<String, Intent>()
    private val uploadCompletedListener = SparseArray<Class<out UploadCompleteEndListener>>()
    private val detailSharingInterceptors = ArrayList<DetailIntentSharingInterceptor>()
    private val streamingProviders = ArrayList<StreamingProvider>()
    protected val activities: MutableList<Activity> = LinkedList()
    var foregroundActivitiesCount: Int = 0
        private set

    private val registrationEndListener = LinkedList<RegistrationListener>()

    /**
     * Ottiene la lista completa di tutti gli [UploadInterceptor] che sono stati aggiunti.
     * <br></br>
     * La lista conterrà sia l'interceptor di default che gli altri interceptor.

     * @return lista di [UploadInterceptor]
     */
    val uploadInterceptors = ArrayList<UploadInterceptor>()

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private var markerInvalidator: MarkerInvalidator? = null

    /**
     * Registra una classe di fragment da utlizzare per mostrare il dettaglio di un contenuto.

     * @param dataClass     classe del oggetto da mostrare
     * @param fragmentClass classe del fragment
     * @param callback      callback per trasformare il [Bundle] originale
     */
    @JvmOverloads
    fun registerDetailFragment(dataClass: Class<*>,
                               fragmentClass: Class<out Fragment>,
                               callback: ((Bundle) -> Bundle)? = null) {
        registeredDetailsFragment.put(dataClass.simpleName.realmCleanClassName(), Pair<Class<out Fragment>, ((Bundle) -> Bundle)?>(fragmentClass, callback))
    }

    /**
     * Indicazione se la classe dell'oggeto ha un fragment associato per mostrarne i dettagli.
     * Se non sono registrati fragment per quella specifica classe saranno utilizzati le classi di default
     *
     *  1. [ContentItemDetailModelFragment] se la classe implementa [ContentItemWithLocation]
     *  1. [ContentItemDetailModelFragment] se la classe implementa [ContentItem]
     *
     * @param dataClass
     * @return
     */
    fun isDataClassMappedForDetails(dataClass: Class<*>): Boolean {
        return registeredDetailsFragment.containsKey(dataClass.simpleName.realmCleanClassName()) ||
                ContentItem::class.java.isAssignableFrom(dataClass) ||
                ContentItemWithLocation::class.java.isAssignableFrom(dataClass)
    }

    /**
     * Metodo rapido per istanziare un fragment per mostrare un dettaglio

     * @param dataClass classe del dato da mostrare
     * @param arguments argomenti da passare al fragment
     * @return il fragment istanziato, oppure null se non ci sono classi associate
     */
    fun instantiateDetailFragmentForClass(dataClass: Class<*>, arguments: Bundle): Fragment? {
        var fragmentClass: Class<out Fragment>? = null

        val className = dataClass.simpleName.realmCleanClassName()
        if (registeredDetailsFragment.containsKey(className))
        {
            val p = registeredDetailsFragment[className]!!
            fragmentClass = p.first
            val callback = p.second
            callback?.invoke(arguments)
        } else if (ContentItemWithLocation::class.java.isAssignableFrom(dataClass))
            fragmentClass = ContentItemDetailModelFragment::class.java
        else if (ContentItem::class.java.isAssignableFrom(dataClass))
            fragmentClass = ContentItemDetailModelFragment::class.java

        if (fragmentClass != null) {
            try {
                val fragment = fragmentClass.newInstance()
                fragment.arguments = arguments
                return fragment
            } catch (e: InstantiationException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
        return null
    }

    /**
     * Permette di registrare un activity da avviare per mostrare un certo tipo di contenuto.
     * Viene utilizata per la ricezione delle notifiche delle push

     * @param contentClass   classe del contenuto da mostrare
     * @param activityIntent Intent per avviare l'activity
     */
    fun registerProjectionContentClass(contentClass: Class<*>, activityIntent: Intent) {
        registeredProjectionClasses.put(contentClass.simpleName.realmCleanClassName(), activityIntent)
    }

    /**
     * Ritorna la classe dell'activity associata al tipo di contenuto,
     * oppure null se non vi sono classi associate

     * @param contentClass classe del contenuto da ostrare
     * @return classe dellactivity associato al tipo di contenuto, oppure null
     */
    fun registeredActivityIntentForContentClass(contentClass: Class<*>): Intent? =
            registeredProjectionClasses[contentClass.simpleName.realmCleanClassName()]

    fun isDataClassRegisteredForProjection(dataClass: Class<*>): Boolean =
            registeredProjectionClasses.containsKey(dataClass.simpleName.realmCleanClassName())

    /**
     * Permette di registrare una classe da invocare a fine di un upload.
     * Gli upload sono utilizzati per la creazione di contenuti lato Orchard, caricando anche i file media.
     * La classe deve avere un costruttore senza parametri e implementare l'interfaccia [UploadCompleteEndListener]

     * @param uploadCode    codice indentificativo del tipo di upload
     * @param listenerClass classe per il listener da invocare
     */
    fun registerUploadCompleteListener(uploadCode: Int, listenerClass: Class<out UploadCompleteEndListener>) {
        uploadCompletedListener.put(uploadCode, listenerClass)
    }

    fun getUploadCompletedListener(uploadCode: Int): Class<out UploadCompleteEndListener>? =
            uploadCompletedListener.get(uploadCode)

    override fun onCreate() {
        super.onCreate()

        RemoteClient.clients[RemoteClient.Mode.LOGGED] = OkHttpRemoteClient(this, RemoteClient.Mode.LOGGED)
        RemoteClient.clients[RemoteClient.Mode.DEFAULT] = OkHttpRemoteClient(this, RemoteClient.Mode.DEFAULT)
        LoginManager.shared = LoginManager(this)


        Trace.beginSection("createPreFetchManager")
        // Check if the auto pre-fetch is enabled.
        val autoPreFetch = autoPreFetch()
        // Get the contents that can be pre-fetched or an empty array if it
        // can't be automatically pre-fetched.
        val preFetchableContents = if (autoPreFetch) preFetchableContents() else emptyArray()
        if (preFetchableContents.isNotEmpty()) {
            // Create the manager that will pre-fetch the contents.
            val preFetchManager = createPreFetchManager()
            // Pre-fetch the contents.
            preFetchManager.preFetch(preFetchableContents)
        }
        Trace.endSection()

        // Create the directory used for HTTP cache.
        createHttpCacheDir()

        ClassUtils.init(this)

        Trace.beginSection("realmInit")
        Realm.init(this)
        Trace.endSection()

        val version: Long = try {
            val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            // Read the metaData from the manifest.
            appInfo.metaData.getInt("com.krake.RealmSchemaVersion").toLong()
        } catch (ignored: PackageManager.NameNotFoundException) {
            // The default version is 1.
            1
        }

        Realm.setDefaultConfiguration(RealmConfiguration.Builder()
                .schemaVersion(version)
                                              .name(getString(R.string.database_name))
                .deleteRealmIfMigrationNeeded().build())

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseAnalytics.setAnalyticsCollectionEnabled(
                BuildConfig.DEBUG.not() && resources.getBoolean(R.bool.enable_analytics))

        CacheManager.shared = initializeCacheManager()
        MarkerCreator.shared = initializeMarkerCreator()

        if (getString(R.string.orchard_base_service_url).isEmpty()) {
            throw IllegalStateException("Non hai configurato R.string.orchard_base_service_url")
        }

        if (getString(R.string.app_package) == "none") {
            throw IllegalStateException("Non hai configurato R.string.app_package")
        }

        RemoteDataRepository.shared = OrchardRemoteDataRepository(this, Mapper(this))
        Signaler.shared = Signaler(this)


        LoginManager.shared.loggedUser.observeForever {
            LoginManager.updateSavedLoginOutput(this, it)
            if (it == null)
                LoginManager.shared.cleanSavedToken(this)
        }

        registerActivityLifecycleCallbacks(this)

        //handles creation of notification channels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            handleNotificationChannels()
        }

        if (LoginManager.shared.loggedUser.value == null) {
            LoginManager.updateSavedLoginOutput(this, null)
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun handleNotificationChannels() {
        val channels = getNotificationChannels().toMutableList()

        if (channels.none { it.id == KRAKE_NOTIFICATION_CHANNEL })
            channels.add(NotificationChannel(KRAKE_NOTIFICATION_CHANNEL, getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_HIGH))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        channels.forEach { notificationManager.createNotificationChannel(it) }

    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        activities.add(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        /* Unused */
    }

    override fun onActivityResumed(activity: Activity) {
        /* Unused */
        ++foregroundActivitiesCount;
    }

    override fun onActivityPaused(activity: Activity) {
        /* Unused */
        --foregroundActivitiesCount;
    }

    override fun onActivityStopped(activity: Activity) {
        /* Unused */
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {
        /* Unused */
    }

    override fun onActivityDestroyed(activity: Activity) {
        activities.remove(activity)
    }

    /**
     * Permette di modificare l'activity corrente.
     * Se l'intent è presente nella storia viene rirata indietro la cronologia.
     * In caso non sia presente tutta la cronologia di navigazione viene sostituita con quanto specificato
     * dall'intent avviato.

     * @param source   [Activity] che ha richiesto la navigazione
     * @param upIntent [Intent] di destinazione della navigazione
     */
    fun upNavigateToIntent(source: Activity, upIntent: Intent?) {
        if (upIntent != null) {
            var searchedIndex = -1
            for (index in activities.indices.reversed()) {
                val activity = activities[index]
                val activityIntent = activity.intent
                if (activityIntent.equalsToIntent(upIntent)) {
                    searchedIndex = index
                    break
                }
            }

            if (searchedIndex >= 0) {
                val currentActivities = LinkedList(activities)
                (currentActivities.size - 1 downTo searchedIndex + 1)
                        .map { currentActivities[it] }
                        .forEach { it.finish() }
            } else {
                val parentActivities = LinkedList<Intent>()
                var forIntent: Intent = upIntent

                val module = ThemableComponentModule()
                module.upIntent(upIntent)
                while (module.upIntent != null) {
                    forIntent = module.upIntent!!
                    parentActivities.add(forIntent)
                    val extras = forIntent.extras ?: break
                    module.readContent(this, extras)
                }

                val builder = TaskStackBuilder.create(this)
                builder.addNextIntentWithParentStack(forIntent)

                for (index in parentActivities.indices.reversed()) {
                    builder.addNextIntent(parentActivities[index])
                }

                builder.startActivities()
            }
        } else {
            source.finish()
        }
    }

    override fun logSelectContent(contentItem: ContentItem, extraParameters: Bundle?) {

        logSelectContent(contentItem::class.java.simpleName.realmCleanClassName(),
                         contentItem.titlePartTitle,
                         extraParameters)
    }

    override fun logSelectContent(contentType: String,
                                  itemId: String?,
                                  extraParameters: Bundle?) {
        val bundle = Bundle()

        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)

        extraParameters?.let { bundle.putAll(it) }
        itemId?.let { bundle.putString(FirebaseAnalytics.Param.ITEM_ID, it) }

        logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    override fun logItemList(itemCategory: String, extraParameters: Bundle?) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, itemCategory)
        extraParameters?.let {
            bundle.putAll(it)
        }
        logEvent(FirebaseAnalytics.Event.VIEW_ITEM_LIST, bundle)
    }

    override fun logEvent(event: String, parameters: Bundle) {
        firebaseAnalytics.logEvent(event, parameters)
    }

    override fun logShare(contentType: String, itemId: String?, socialIntent: String) {

        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
        if (itemId != null)
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId)
        bundle.putString("social", socialIntent)

        logEvent(FirebaseAnalytics.Event.SHARE, bundle)
    }

    override fun setUserProperty(key: String, value: String) {
        firebaseAnalytics.setUserProperty(key, value)
    }

    override fun setUserId(id: String) {
        firebaseAnalytics.setUserId(id)
    }

    /**
     * Inizializza il [CacheManager] dell'app.
     * DEFAULT: [AutoQueryCacheManager]
     *
     * @return [CacheManager] che verrà utilizzato per gestire la cache dell'app.
     */
    open fun initializeCacheManager(): CacheManager = AutoQueryCacheManager(this)

    /**
     * Inizializza il [MarkerCreator] dell'app.
     * DEFAULT: [DefaultMarkerCreator]
     *
     * @return [MarkerCreator] che verrà utilizzato per creare i [Marker] dell'app.
     */
    open fun initializeMarkerCreator(): MarkerCreator {
        val creator = DefaultMarkerCreator()
        provideMarkerInvalidator().registerInvalidator(creator)
        return creator
    }

    /**
     * Enable/disable the auto pre-fetching when the application starts.
     * By default, the auto pre-fetching is enabled.
     *
     * @return true if the auto pre-fetching must be enabled.
     */
    protected open fun autoPreFetch() = true

    /**
     * Creates an instance of [PreFetchManager] that will be used when the application starts.
     * It will be used only if [autoPreFetch] returns true.
     *
     * @return the [PreFetchManager] used when application starts.
     */
    protected open fun createPreFetchManager(): PreFetchManager = PoolPreFetchManager()

    /**
     * Creates the list of [FetchableContent] that will be pre-fetched when the application starts.
     *
     * @return the list of [FetchableContent] that must be pre-fetched.
     */
    protected open fun preFetchableContents(): Array<FetchableContent> =
            arrayOf(MapFetchableContent(this), WebViewFetchableContent())

    /**
     * Used to create the HTTP cache directory.
     */
    protected open fun createHttpCacheDir() {
        // The dir will be written in another thread.
        async {
            // Create the directory.
            val httpCacheDir = File(cacheDir, "http")
            val httpCacheSize: Long = 10 * 1024 * 1024 /* 10 MB */
            // Install the cache dir that will hold at maximum a size of "httpCacheSize".
            HttpResponseCache.install(httpCacheDir, httpCacheSize)
        }.error { e ->
            Log.w(TAG, "HTTP response cache installation failed.", e)
        }.load()
    }

    override fun provideMarkerInvalidator(): MarkerInvalidator {
        if (markerInvalidator == null) {
            markerInvalidator = MarkerInvalidator()
        }
        return markerInvalidator!!
    }

    /**
     * Metodo che permette di modificare la notifica generata prima che sia mostrata all'utente

     * @param notificationBuilder notifica in costruzion
     * @param contentIntent       Intent che verrà lanciato dopo aver ricevuto il click sulla push, se presente
     * @param notificationText    testo della notifica inviato da orchard
     * @param displayPath         displayAlias del contenuto collegato alla notifica
     * @param resultObject        oggetto risultato caricato per la notifica
     * @param remoteMessageExtras tutti i parametri che arrivano nel RemoteMessage
     */
    open fun updateCloudMessageNotification(notificationBuilder: NotificationCompat.Builder,
                                            contentIntent: Intent,
                                            notificationText: String,
                                            displayPath: String?,
                                            resultObject: Any?,
                                            remoteMessageExtras: Map<String, String>?): Intent = contentIntent

    /**
     * Definisce la possibilità di utilizzare un comportamento custom filtrato in base al package dell'app che si vuole aprire

     * @param senderFragment Fragment dal quale parte l'Intent di condivisione
     * @param shareLink      ShareLinkPart abbinata al ContentItem mostrato nel Fragment
     * @param mediaUri       Uri dell'immagine da condividere, se presente
     * @param sharingIntent  Intent valorizzato con il package dell'app scelta per la condivisione
     * @return true se l'Intent è gestito in modo custom
     */
    open fun handleDetailSharingIntent(senderFragment: Fragment, shareLink: ShareLinkPart, mediaUri: Uri?, sharingIntent: Intent): Boolean =
            detailSharingInterceptors.any { it.handleSharingDetail(senderFragment, shareLink, mediaUri, sharingIntent) }

    /**
     * Aggiunge un comportamento custom da applicare quando un utente vuole condividere un contenuto
     *
     * @param detailIntentSharingInterceptor DetailIntentSharingInterceptor da aggiungere alla lista
     */
    protected fun addDetailSharingIntent(detailIntentSharingInterceptor: DetailIntentSharingInterceptor) {
        detailSharingInterceptors.add(detailIntentSharingInterceptor)
    }

    /**
     * Ottiene lo StreamingProvider corretto partendo da una stringa d'origine.
     * <br></br>
     * Di default un provider viene considerato "corretto" nel caso in cui rispetti questa forma:
     * <br></br>
     * provider(case insensitive)|url di streaming.

     * @param source stringa che arriva dal WS per capire il provider per lo streaming
     * @return [StreamingProvider] presente nella lista di quelli aggiunti tramite [.addStreamingProvider] o null se non trovato
     */
    fun getStreamingProvider(source: String): StreamingProvider? {
        var sourceString = source
        val index = sourceString.indexOf("|")
        if (index != -1) {
            sourceString = sourceString.substring(0, index).toUpperCase()
            streamingProviders
                    .filter { it.providerName().toUpperCase() == sourceString }
                    .forEach { return it }
        }

        return null
    }

    /**
     * Aggiunge uno [StreamingProvider] alla lista di quelli possibili
     *
     * @param streamingProvider [da aggiungere][StreamingProvider]
     */
    protected fun addStreamingProvider(streamingProvider: StreamingProvider) {
        streamingProviders.add(streamingProvider)
    }

    /**
     * Aggiunge un'[UploadInterceptor] alla lista di quelli possibili inserendolo alla posizione corretta in base alla sua priorità

     * @param interceptor [UploadInterceptor] di default
     */
    fun addUploadInterceptor(interceptor: UploadInterceptor) {
        val size = uploadInterceptors.size
        if (size > 0) {
            for (i in 0 until size) {
                val currentInterceptor = uploadInterceptors[i]
                if (currentInterceptor == interceptor) {
                    break
                }

                if (i == size - 1) {
                    uploadInterceptors.add(interceptor)
                    break
                } else if (interceptor.priority > currentInterceptor.priority) {
                    uploadInterceptors.add(i, interceptor)
                    break
                }
            }
        } else {
            uploadInterceptors.add(interceptor)
        }
    }

    /**
     * Stabilisce la classe di [UploadInterceptor] da utilizzare con un [UploadableMediaInfo] e con una chiave di Orchard (se presente)

     * @param mediaInfo  [UploadableMediaInfo] con informazioni relative al media da caricare
     * @param orchardKey nome della chiave della MediaGalleryPickerField su Orchard in cui si vuole caricare il file
     * @return dell'[UploadInterceptor] da utilizzare per l'upload
     */
    open fun getUploadInterceptor(mediaInfo: UploadableMediaInfo, orchardKey: String?, uploadParams: Bundle?): UploadInterceptor? {
        return uploadInterceptors.indices
                .map { uploadInterceptors[it] }
                .firstOrNull { it.availableMedias and mediaInfo.type != 0 }
    }

    /**
     * Metodo per cancellare il vecchio db di greendao.
     * Dev'essere chiamatato dall'App perché il nome è noto solo all'App

     * @param dbName
     */
    protected fun deleteGreenDao(dbName: String) {
        val db = getDatabasePath(dbName)

        if (db.exists()) {
            db.delete()
        }
    }

    override fun provideManager(): ImageManager = GlideImageManager(this)

    /**
     * provide the notification channels that wll be created
     * override this fun for add custom channels or for update an existing one
     * @param channels channels already created
     */
    @TargetApi(Build.VERSION_CODES.O)
    open protected fun getNotificationChannels(): List<NotificationChannel> = listOf()
}