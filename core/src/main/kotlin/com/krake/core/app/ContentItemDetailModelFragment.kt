package com.krake.core.app

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Trace
import android.text.TextUtils
import android.view.*
import android.widget.ProgressBar
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ShareCompat
import androidx.core.widget.NestedScrollView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.krake.core.OrchardError
import com.krake.core.R
import com.krake.core.api.GoogleApiClientFactory
import com.krake.core.component.annotation.BundleResolvable
import com.krake.core.component.module.DetailComponentModule
import com.krake.core.data.DataConnectionModel
import com.krake.core.data.DataModel
import com.krake.core.extension.asFile
import com.krake.core.extension.krakeApplication
import com.krake.core.location.LocationRequirementsHelper
import com.krake.core.media.*
import com.krake.core.media.loader.ImageLoader
import com.krake.core.media.loader.MediaLoader
import com.krake.core.model.*
import com.krake.core.permission.PermissionListener
import com.krake.core.permission.PermissionManager
import com.krake.core.widget.*
import java.io.File
import java.lang.ref.WeakReference
import java.util.*

/**
 * Fragment per mostrare le info di base di un oggetto orchard.
 * **Nota** e' necessario che la classe sia conforme all'interfaccia [ContentItem] o sue estensioni.
 * Il fragment mostra
 *
 *  * titolo e actionbar, utilizzando la classe [CollapsingToolbarLayout]
 *  * se l'oggetto implementa l'interfaccia [ContentItemWithGallery] viene inserita anche la gallery per mostrare i [MediaPart]
 *  * se l'oggetto implementa l'intefaccia [ContentItemWithDescription] sarà mostrata anche la descrizione dell'oggetto
 *
 *
 *
 * Gli arguments del fragment devono essere derivati dai metodi della classe [DataConnectionModel].
 * In caso non sia presente questo parametro l'indicazione viene presa dal res R.bool.enable_social_sharing_on_details
 * Se viene abilitata la condivisione sui social il fragment inserira' un elemento nell'[AppBarLayout]
 * Il menu utilizzato viene prelevato dalla reference R.menu.menu_content_item_detail.
 * Ne esiste una versione gia' pronta: R.menu.menu_content_item_detail_share_intent
 * il click sul menu item apre un [com.krake.core.widget.IntentPickerSheetView] che condividerà le informazioni inserite in [.createSharingIntent].
 *
 *\
 * L'activity che include il fragment deve implementare l'interfaccia [MediaSelectionListener]
 *
 *
 * [ContentItem]
 * [ContentItemWithDescription]
 * [ContentItemWithGallery]
 */
open class ContentItemDetailModelFragment : OrchardDataModelFragment(),
        FloatingActionButtonMapBehavior.VisibilityListener,
        AppBarLayout.OnOffsetChangedListener,
        BackPressHandler,
        ContentItemViewContainer,
        LocationListener,
        GoogleMap.OnInfoWindowClickListener,
        ContentItemViewListenerMapSupport,
        PermissionListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClientFactory.ConnectionListener,
        ActivityResultCallback,
        View.OnClickListener
{

    @BundleResolvable
    lateinit var detailComponentModule: DetailComponentModule
    protected lateinit var mCoordinator: CoordinatorLayout
    protected var contentItem: ContentItem? = null
    protected lateinit var contentScrollView: NestedScrollView
        private set

    protected val locationContentItem: ContentItemWithLocation?
        get() = contentItem as? ContentItemWithLocation

    private var progressBar: ProgressBar? = null
    private var mEnableSocialSharing: Boolean = false
    private var mSocialImageUri: Uri? = null
    private var mShareSheetView: IntentPickerSheetView? = null
    private var mShareBehavior: SafeBottomSheetBehavior<*>? = null
    private val sheetCallback: SheetCallback by lazy { SheetCallback(this) }
    private lateinit var mAppBarLayout: AppBarLayout
    private var mToolbar: TouchControllableToolbar? = null
    private var mCollapsingToolbarLayout: CollapsingToolbarLayout? = null
    private var mAppBarLockBehavior: LockAppbarLayoutBehavior? = null

    private lateinit var mLocationRequirementsHelper: LocationRequirementsHelper
    private val mapViews = ArrayList<ContentItemMapView>()
    protected lateinit var apiClientFactory: GoogleApiClientFactory
        private set
    private var sentAnalytics = false

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        mLocationRequirementsHelper = LocationRequirementsHelper(this, this, null)
        mLocationRequirementsHelper.create()

        apiClientFactory = GoogleApiClientFactory(activity!!, this, LocationServices.API)
    }

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        Trace.beginSection("createDetailFragment")
        val contentView = inflater.inflate(contentLayoutIdentifier, container, false)
        mapViews.clear()

        mCoordinator = activity!!.findViewById(R.id.activity_layout_coordinator)

        val fragmentView: View
        val getScrollView: (rootView: View) -> Unit = {
            contentScrollView = it.findViewById(R.id.scroll_view)
        }

        if (fragmentLayoutIdentifier != 0)
        {
            fragmentView = inflater.inflate(fragmentLayoutIdentifier, mCoordinator, false)
            getScrollView(fragmentView)
            contentScrollView.addView(contentView, 0)
        }
        else
        {
            fragmentView = contentView
            getScrollView(fragmentView)
        }

        if (fragmentView is CoordinatorLayout)
            mCoordinator = fragmentView

        val contentItemClass = orchardComponentModule.dataClass

        mEnableSocialSharing = detailComponentModule.enableSocialSharing &&
                contentItemClass != null &&
                RecordWithShare::class.java.isAssignableFrom(contentItemClass)

        setHasOptionsMenu(true)

        val appbar: AppBarLayout? = fragmentView.findViewById(R.id.app_bar_layout)
        if (appbar != null)
        {

            mAppBarLayout = appbar
            mCollapsingToolbarLayout = fragmentView.findViewById(R.id.collapsing_toolbar_layout)
            mToolbar = fragmentView.findViewById(R.id.toolbar_actionbar)

            mAppBarLayout.addOnOffsetChangedListener(this)

            (activity as? AppCompatActivity)?.setSupportActionBar(mToolbar)
        }
        else
        {
            mAppBarLayout = activity!!.findViewById(R.id.app_bar_layout)
        }

        mAppBarLockBehavior = (mAppBarLayout.layoutParams as? CoordinatorLayout.LayoutParams)?.behavior as? LockAppbarLayoutBehavior

        progressBar = fragmentView.findViewById(android.R.id.progress)

        progressBar?.visibility = View.VISIBLE

//        mShareSheetView = mCoordinator.findViewById(R.id.shareSheetView)

//        if (mShareSheetView != null)
//        {
//            mShareBehavior = (mShareSheetView?.layoutParams as? CoordinatorLayout.LayoutParams)?.behavior as? BottomSheetNotUnderActionBehavior<*>
//        }
//        else
//        {
//            mEnableSocialSharing = false
//        }

        (fragmentView as ViewGroup).setVisibilityListenerToChild(this)

        val bottomSheetId: Int = savedInstanceState?.getInt(STATE_EXPANDED_BEHAVIOR, 0) ?: 0

        if (bottomSheetId != 0 && bottomSheetId != R.id.shareSheetView)
        {

            val subView: View? = mCoordinator.findViewById(bottomSheetId)
            if (subView != null)
            {
                val behavior = (subView.layoutParams as? CoordinatorLayout.LayoutParams)?.behavior

                if (behavior is BottomSheetNotUnderActionBehavior<*>)
                {
                    behavior.setStateAndNotify(BottomSheetBehavior.STATE_EXPANDED)
                }
            }
            else
            {
                mCoordinator.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener
                                                          {
                                                              override fun onChildViewAdded(view: View, child: View)
                                                              {
                                                                  if (child.id == bottomSheetId)
                                                                  {
                                                                      val behavior = (child.layoutParams as? CoordinatorLayout.LayoutParams)?.behavior

                                                                      if (behavior is BottomSheetNotUnderActionBehavior<*>)
                                                                      {
                                                                          behavior.setStateAndNotify(BottomSheetBehavior.STATE_EXPANDED)
                                                                      }
                                                                      mCoordinator.setOnHierarchyChangeListener(null)
                                                                  }
                                                              }

                                                              override fun onChildViewRemoved(view: View, view1: View)
                                                              {

                                                              }
                                                          })
            }
        }

        updateSheetCallbackForAllCoordinatorChild(true)

        Trace.endSection()
        return fragmentView
    }

    override fun onResume()
    {
        super.onResume()
        for (map in mapViews)
            map.onResume()
    }

    override fun onPause()
    {
        super.onPause()
        for (map in mapViews)
            map.onPause()
    }

    override fun onStop()
    {
        super.onStop()
        removeLocationUpdates(apiClientFactory)
        apiClientFactory.disconnect()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        apiClientFactory.destroy()
        for (map in mapViews)
            map.onDestroy()
    }

    override fun onLowMemory()
    {
        super.onLowMemory()
        for (map in mapViews)
            map.onLowMemory()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        updateSheetCallbackForAllCoordinatorChild(false)
    }

    override fun onStart()
    {
        super.onStart()

        contentItem.let { progressBar?.visibility = View.GONE }
    }

    open protected fun sendToAnalyticsSelection(contentItem: ContentItem)
    {

        val extras = Bundle()

        detailComponentModule.analyticsExtras?.let {
            extras.putAll(it)
        }

        (activity?.application as? AnalyticsApplication)?.logSelectContent(contentItem, extras)
    }

    override fun onSaveInstanceState(outState: Bundle)
    {
        super.onSaveInstanceState(outState)

        outState.putInt(STATE_EXPANDED_BEHAVIOR, expandedBottomSheetId())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        super.onCreateOptionsMenu(menu, inflater)

        if (mEnableSocialSharing)
        {
            inflater.inflate(R.menu.menu_content_item_detail, menu)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu)
    {
        super.onPrepareOptionsMenu(menu)

        val shareItem = menu.findItem(R.id.action_share_intent)
        shareItem?.isVisible = canContentItemBeShared()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {

        val state = mShareBehavior?.state ?: BottomSheetBehavior.STATE_HIDDEN
        if (state != BottomSheetBehavior.STATE_COLLAPSED && state != BottomSheetBehavior.STATE_HIDDEN)
        {
            mShareBehavior?.setStateAndNotify(BottomSheetBehavior.STATE_COLLAPSED)
        }
        else
        {
            if (item.itemId == R.id.action_share_intent)
            {
                showShareSheetView()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    protected open val contentLayoutIdentifier: Int
        @LayoutRes
        get() = detailComponentModule.contentLayout

    protected val fragmentLayoutIdentifier: Int
        @LayoutRes
        get() = detailComponentModule.rootLayout

    override fun setAppbarLock(locked: Boolean, forceExpansionCollapse: Boolean)
    {
        mAppBarLockBehavior?.let {
            it.isPermanentlyLocked = locked

            if (forceExpansionCollapse)
            {
                if (locked || !mCoordinator.isOneOfChildBottomSheetExpanded() && contentScrollView.scrollY == 0)
                    mAppBarLayout.setExpanded(!locked, false)
            }
        }
    }

    protected val appBarLocked: Boolean
        get() = mAppBarLockBehavior?.isPermanentlyLocked ?: false

    protected fun showShareSheetView()
    {
        if (mShareSheetView == null) {
            mShareSheetView = layoutInflater.inflate(R.layout.sheet_share_view, mCoordinator).findViewById(R.id.shareSheetView) as IntentPickerSheetView
            mShareBehavior = (mShareSheetView?.layoutParams as? CoordinatorLayout.LayoutParams)?.behavior as? SafeBottomSheetBehavior<*>
        }

        val shareLinkPart = (contentItem  as? RecordWithShare)?.shareLinkPart
        val shareSheetView = mShareSheetView
        val shareBehavior = mShareBehavior
        if (shareSheetView != null && shareBehavior != null && shareLinkPart != null)
        {
            val intent = createSharingIntent(shareLinkPart)

            shareSheetView.setIntent(intent)

            shareSheetView.post {
                shareBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }

//            shareBehavior.state = BottomSheetBehavior.STATE_EXPANDED

            shareSheetView.setOnIntentPicked { activityInfo ->
                shareBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

                val item = contentItem

                item?.let {
                    (activity?.application as? AnalyticsApplication)
                            ?.logShare(item.javaClass.simpleName,
                                       (item as? RecordWithAutoroute)?.autoroutePartDisplayAlias
                                               ?: item.titlePartTitle,
                                       activityInfo.componentName.packageName)
                }

                val finalIntent = activityInfo.getConcreteIntent(intent)

                if (!krakeApplication.handleDetailSharingIntent(this, shareLinkPart, mSocialImageUri, finalIntent))
                    startActivity(finalIntent)
            }

            mAppBarLayout.setExpanded(false, true)
        }
    }

    /**
     * Metodo per inizializzare l'intent da utilizzare per condividere un contenuto

     * @param shareLink il contenuto da condividere sui social
     * *
     * @return L'intent da utilizzare per condividere il contenuto sui principali social
     * *
     * @see Intent.ACTION_SEND
     */
    protected fun createSharingIntent(shareLink: ShareLinkPart): Intent
    {
        val builder = ShareCompat.IntentBuilder.from(activity)
        if (mSocialImageUri != null)
        {
            builder.setType("image/*")
            builder.setStream(mSocialImageUri)
        }
        else
        {
            builder.setType("text/plain")
        }

        val sb = StringBuilder()
        if (!TextUtils.isEmpty(shareLink.sharedText))
            sb.append(shareLink.sharedText)

        if (!TextUtils.isEmpty(shareLink.sharedLink))
        {
            if (sb.isNotEmpty())
            {
                sb.append(" ")
            }
            sb.append(shareLink.sharedLink)
        }
        if (sb.isNotEmpty())
        {
            builder.setText(sb.toString())
        }

        return builder.intent
    }

    override fun onDataModelChanged(dataModel: DataModel?)
    {
        if (dataModel?.listData != null)
        {
            contentItem = dataModel.listData.first() as? ContentItem

            contentItem?.let { loadDataInUI(it, dataModel.cacheValid) }

            activity?.invalidateOptionsMenu()
        }
    }

    override fun onDataLoadingError(orchardError: OrchardError)
    {

    }

    /**
     * Metodo chiamato alla fine della chiamata di [.onDefaultDataLoaded] se
     * il caricamento è andato a buon fine ed è stato caricato un contenuto.
     * Importante chiamare il metodo super per gestire correttamente la gallery di immagini e altri
     * componenti base del layout
     * Possibile che arrivno più chiamate a questo metodo se il contenuto mostrato la prima volta
     * era in cache ma scaduto.

     * @param contentItem il contenuto da mostrare sulla ui
     * *
     * @param cacheValid  indicazione se il dato caricato è parte di una cache valida
     */
    @CallSuper
    open fun loadDataInUI(contentItem: ContentItem, cacheValid: Boolean)
    {
        progressBar?.visibility = View.GONE

        setAppbarLock(true, false)
        setAppBarTitle(contentItem.titlePartTitle)

        if (!detailComponentModule.disableAnalytics && !sentAnalytics)
        {

            sentAnalytics = true
            sendToAnalyticsSelection(contentItem)
        }

        val shareLinkPart = (contentItem as? RecordWithShare)?.shareLinkPart
        if (mEnableSocialSharing && canContentItemBeShared() && shareLinkPart != null)
        {

            if (!shareLinkPart.sharedImage.isNullOrEmpty())
            {
                shareLinkPart.sharedImage?.let { image ->
                    val instagramPixelSize = resources.getDimensionPixelSize(R.dimen.instagram_image_size)

                    val mediaLoadable = object : DownloadOnlyLoadable()
                    {
                        override fun getOptions() = ImageOptions(instagramPixelSize, instagramPixelSize, ImageOptions.Mode.MAX)
                    }

                    activity?.let {
                        MediaLoader.typedWith<File>(it, mediaLoadable)
                                .mediaPart(MediaPartURLWrapper(image))
                                .getRequest()
                                .asFile()
                                .addListener(object : ImageLoader.RequestListener<File>
                                             {
                                                 override fun onDataLoadSuccess(resource: File?)
                                                 {
                                                     val activity = activity
                                                     if (resource != null && activity != null)
                                                     {
                                                         mSocialImageUri = MediaProvider.getUriForFile(activity, resource)
                                                         activity.invalidateOptionsMenu()
                                                     }
                                                 }

                                                 override fun onDataLoadFailed()
                                                 {
                                                 }
                                             })
                                .load()
                    }
                }
            }
        }

        mCoordinator.setContentItemToChild(contentItem, cacheValid)
    }

    /**
     * Imposta il titolo dell'activity e della [CollapsingToolbarLayout] se presente

     * @param title il nuovo titolo
     */
    fun setAppBarTitle(title: String?)
    {
        val activity = activity
        if (mToolbar != null && activity != null)
        {
            activity.title = title
            (mToolbar?.parent as? CollapsingToolbarLayout)?.let { it.title = title }
        }
    }

    private fun canContentItemBeShared(): Boolean
    {
        val share = (contentItem as? RecordWithShare)?.shareLinkPart
        return share?.isShareValid ?: false
    }

    override fun onVisibilityChange(visible: Boolean)
    {
        activity?.let {
            it.invalidateOptionsMenu()
        }
    }

    override fun onBackPressed(): Boolean
    {

        for (index in 0 until mCoordinator.childCount)
        {
            val view = mCoordinator.getChildAt(index)

            val behavior = (view.layoutParams as? CoordinatorLayout.LayoutParams)?.behavior as? BottomSheetBehavior<*>

            if (behavior != null && behavior.state == BottomSheetBehavior.STATE_EXPANDED)
            {
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                return true
            }

        }

        return false
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int)
    {
        val toolbar = mToolbar
        val collapsingLayout = mCollapsingToolbarLayout

        if (toolbar != null && collapsingLayout != null)
        {
            // se il check passa, vuol dire che l'AppBarLayout è compresso, in quel caso non deve essere cliccabile
            if (toolbar.height - collapsingLayout.bottom == verticalOffset)
            {
                if (!toolbar.isEatingTouchGestures)
                {
                    toolbar.eatTouchGestures(true)
                }
            }
            else if (toolbar.isEatingTouchGestures)
            {
                toolbar.eatTouchGestures(false)
            }
        }
    }

    private fun expandedBottomSheetId(): Int
    {
        for (index in 0 until mCoordinator.childCount)
        {
            val view = mCoordinator.getChildAt(index)

            val behavior = (view.layoutParams as? CoordinatorLayout.LayoutParams)?.behavior as? BottomSheetBehavior<*>

            if (behavior != null && behavior.state == BottomSheetBehavior.STATE_EXPANDED)
            {
                return view.id
            }
        }

        return 0
    }

    private fun updateSheetCallbackForAllCoordinatorChild(add: Boolean)
    {

        for (index in 0 until mCoordinator.childCount)
        {
            val view = mCoordinator.getChildAt(index)

            val behavior = (view.layoutParams as? CoordinatorLayout.LayoutParams)?.behavior as? BottomSheetNotUnderActionBehavior<*>

            if (behavior != null)
            {
                if (add)
                {
                    behavior.addBottomSheetCallback(sheetCallback)
                }
                else
                {
                    behavior.removeBottomSheetCallback(sheetCallback)
                }
            }
        }
    }

    fun addSheetCallback(behavior: SafeBottomSheetBehavior<*>)
    {
        behavior.addBottomSheetCallback(sheetCallback)
    }

    override fun onInfoWindowClick(marker: Marker)
    { /* empty */
    }

    protected val locationClient: GoogleApiClient
        @Deprecated("")
        get() = apiClientFactory.apiClient

    override fun addManagedMapView(mapView: ContentItemMapView)
    {
        mapViews.add(mapView)
    }

    override fun mapRequestUserLocation(mapView: ContentItemMapView)
    {
        apiClientFactory.connect()
    }

    @Deprecated("")
    override fun onConnected(bundle: Bundle?)
    {
        onApiClientConnected()
    }

    @Deprecated("")
    override fun onConnectionSuspended(i: Int)
    {
        onApiClientConnectionSuspended(i)
    }

    @Deprecated("")
    override fun onConnectionFailed(connectionResult: ConnectionResult)
    {
        onApiClientConnectionFailed(connectionResult)
    }

    override fun onApiClientConnected()
    {
        mLocationRequirementsHelper.request(false)
    }

    override fun onApiClientConnectionSuspended(code: Int)
    {
        GoogleApiClientFactory.defaultConnectionSuspendedResolution(code)
    }

    override fun onApiClientConnectionFailed(result: ConnectionResult)
    {
        GoogleApiClientFactory.defaultConnectionFailedResolution(result)
    }

    @SuppressLint("MissingPermission")
    @CallSuper
    override fun onPermissionsHandled(acceptedPermissions: Array<out String>)
    {
        val apiClient = apiClientFactory.apiClient
        // The api client could not be connected if this Fragment disconnects it before the
        // delivering of the permission's result above API 23.
        if (PermissionManager.containLocationPermissions(acceptedPermissions) && apiClient.isConnected)
        {
            val request = LocationRequest()
            request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            LocationServices.FusedLocationApi.getLastLocation(apiClient)
            request.maxWaitTime = 2000

            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, request, this)
        }
    }

    @CallSuper
    override fun onLocationChanged(location: Location)
    {
        removeLocationUpdates(apiClientFactory)
        for (map in mapViews)
        {
            map.onLocationChanged(location)
        }
    }

    private fun removeLocationUpdates(factory: GoogleApiClientFactory)
    {
        val apiClient = factory.apiClient
        if (apiClient.isConnected)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this)
        }
    }

    override fun onClick(v: View?)
    {
    }

    companion object
    {
        private const val STATE_EXPANDED_BEHAVIOR = "ExpandedBehavior"
    }

    private class SheetCallback internal constructor(fragment: ContentItemDetailModelFragment) : SafeBottomSheetBehavior.BottomSheetStateCallback()
    {
        internal var cdf: WeakReference<ContentItemDetailModelFragment> = WeakReference(fragment)

        override fun onStateWillChange(bottomSheet: View, newState: Int)
        {/*EMPTY*/
        }

        override fun onStateChanged(bottomSheet: View, newState: Int)
        {

            val fragment = cdf.get()
            if (fragment != null && (newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_HIDDEN) && fragment.expandedBottomSheetId() == 0)
            {

                if (fragment.contentScrollView.scrollY == 0 && !fragment.appBarLocked)
                {
                    fragment.mAppBarLayout.setExpanded(true, true)
                }
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float)
        {

        }
    }
}

