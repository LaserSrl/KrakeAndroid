package com.krake.core.widget

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.RelativeLayout
import androidx.annotation.CallSuper
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.data.kml.KmlLayer
import com.krake.core.R
import com.krake.core.api.Request
import com.krake.core.app.ContentItemDetailModelFragment
import com.krake.core.map.MapZoomSupport
import com.krake.core.map.MarkerCreator
import com.krake.core.map.kml.KmlTask
import com.krake.core.map.manager.ContextMapManager
import com.krake.core.map.manager.MapManager
import com.krake.core.model.ContentItem
import com.krake.core.model.ContentItemWithLocation
import com.krake.core.view.MapUtils
import com.krake.core.widget.osm.OSMCopyrightView
import java.io.File
import java.io.FileInputStream

/**
 * Created by joel on 06/10/16.
 *
 * Classe per mostrare la mappa, le google maps per funzionare hanno bisogno di più legami col fragment che
 * le contiene. Quindi il ContentItemViewVisibilityListener deve anche implementare l'interfaccia
 * ContentItemViewListenerMapSupport.
 * Tra i metodi per cui è necessario usarlo sono da includere tutte le gestioni del ciclo di vita della MapView
 * (onStart, onResume, onPause, onLowMemory, onDestroy)
 * Se il listener implementa MapView.OnInfoWindowClickListener viene imposta come info window click listener
 *
 * La gestione della mappa viene tutta applicata tramite style xml.
 * Gli stili sono quasi tutti applicati alla ContentItemMapView R.styleable.ContentItemMapView
 * <ul>
 *     <li>showUserDirection: bool indica se mostrare la direction per raggiungere l'utente. Default false</li>
 *     <li>showKml: bool indica se mostrare i kml. Default true</li>
 *     <li>fullScreenMapId: id della view che rappresenta la mappa a schermo intero. Se presente al click della mappa sarà
 *     visualizzata la mappa secondaria a schermo intero. Sfruttando il behavior BottomSheet</li>
 *     <li>fullScreenMapLayout: layout da utilizzare per mostare la mappa full screen.
 *     Il layout viene inserito nel layout coordinator che contiene la mapView, solo se il fullScreenMapId è diverso da 0</li>
 *     <li>mapViewId: identificativo della MapView contenuta nella ContentItemMapView. Se non specificato viene utilizzato R.id.location_content_item_map_view</li>
 *     <li>showKml: bool indica se mostrare i kml. Default true</li>
 * </ul>
 *
 * Per mostrare la mappa in stile OSM bisogna modificare lo stile direttamente della MapView, andando ad indicare come mapType= None
 * In tal caso saranno utilizzati i tile specificati da OSMTileProvider
 *
 * La mappa gestisce autonomamente le sue sotto view.
 *
 * <ul>
 *     <li>R.id.mapCopyrightWebView: webView che permette di mostrare le indicazioni d'uso della mappa dei tile. Utile solo se si usano tile diversi da Google.
 *     Controllare MapUtils.loadOSMCopyrights per indicazioni su configurazione dei copyright<li>
 *      <li>R.id.map_navigate_fab: bottone per avviare la navigazione fino al punto indicato sulla mappa</li>
 *      <li>R.id.map_close_map_bar: bottone per chiudere la mappa. Utile solo per la mappa a schermo intero</li>
 * </ul>
 *
 *
 * @see MapUtils
 * @see OSMTileProvider
 * @see NavigationIntentManager
 */

open class ContentItemMapView : RelativeLayout, ContentItemView, Request.Listener, KmlTask.Listener {

    private companion object Init : ContentItemViewContainer {
        override fun setAppbarLock(locked: Boolean, forceExpansionCollapse: Boolean) {

        }
    }

    override var container: ContentItemViewContainer = Init
        set(value) {
            field = value
            (value as? ContentItemViewListenerMapSupport)?.addManagedMapView(this)
        }

    protected var mapManager: MapManager? = null
        private set
    private var showKmlDetail: Boolean = false
    /**
     * Implica showUserPosition
     */
    private var showUserDirection: Boolean = false
    private var showUserPosition: Boolean = false
    protected var mLocationItemMarker: Marker? = null
        private set
    protected val mapZoomSupport: MapZoomSupport by lazy { MapZoomSupport(this) }
    private var fullScreenMapId = 0
    private var mapIdentifier = 0 // R.id.location_content_item_map_view
    protected var mLocationItem: ContentItemWithLocation? = null
        private set
    private var fullScreenMapLayout = R.layout.partial_full_screen_map

    private var fullScreenMapBehavior: SafeBottomSheetBehavior<*>? = null

    private var directionRequest: Request? = null
    private var directionPolyline: List<LatLng>? = null

    private val mapSupportListener: ContentItemViewListenerMapSupport?
        get() = container as? ContentItemViewListenerMapSupport

    override val exploreChildToo: Boolean = false

    private var locationItemKmlFile: File? = null

    private var kmlTask: KmlTask? = null
    private var kmlLayer: KmlLayer? = null

    /**
     * parametri che mi servono per richiamare il show() della mappa in fullscreen nel caso in cui non sia stato chiamato dalla root può accadere infatti che la root chiami
     * @property setContentItemToChild prima che la mappa fullscreen venga creata, quindi mi salvo una variabile di stato quando sono nella mappa piccola
     * e uso gli stessi paramentri per chiamare lo show() della mappa in fullscreen dopo averla creata
     */
    private var mShowAlreadyCalled = false
    private var mContentItem: ContentItem? = null
    private var mCacheValid = false

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        readAttributes(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        readAttributes(context, attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        readAttributes(context, attrs)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (mapIdentifier != 0) {
            mapManager = ContextMapManager(context) {
                val mapView = findViewById<MapView>(mapIdentifier)
                mapView
            }
        }

        mapManager?.onCreate(null)

        val osmCopyrightView = findViewById<View?>(R.id.osm_copyright_view) as? OSMCopyrightView
        osmCopyrightView?.let {
            mapManager?.getMapAsync {
                if (it.mapType == GoogleMap.MAP_TYPE_NONE) {
                    osmCopyrightView.showOSMCopyright()
                }
            }
        }

        findViewById<View?>(R.id.map_navigate_fab)?.setOnClickListener { if (mLocationItem != null) NavigationIntentManager.startNavigationIntent(context, mLocationItem!!) }

        findViewById<View?>(R.id.map_close_map_bar)?.setOnClickListener {
            val behavior = (layoutParams as? CoordinatorLayout.LayoutParams)?.behavior as? SafeBottomSheetBehavior
            behavior?.isHideable = true
            behavior?.setStateAndNotify(BottomSheetBehavior.STATE_COLLAPSED)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        kmlTask = KmlTask(this)

        mapManager?.getMapAsync { googleMap -> setupMap(googleMap) }

        if (fullScreenMapId != 0) {
            this.setOnClickListener { showExpandedMap() }
        }

        val behavior = (layoutParams as? CoordinatorLayout.LayoutParams)?.behavior as? SafeBottomSheetBehavior
        behavior?.setAllowUserDrag(false)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        mapManager?.getMapAsync { googleMap -> unSetupMap(googleMap) }

        kmlTask?.release()
        directionRequest?.cancel()
        directionRequest = null
    }

    private fun readAttributes(context: Context, attrs: AttributeSet) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ContentItemMapView, 0, 0)

        showKmlDetail = a.getBoolean(R.styleable.ContentItemMapView_showKml, true)
        showUserPosition = a.getBoolean(R.styleable.ContentItemMapView_showUserPosition, false)
        showUserDirection = a.getBoolean(R.styleable.ContentItemMapView_showUserDirection, false)
        showUserPosition = showUserPosition || showUserDirection
        fullScreenMapId = a.getResourceId(R.styleable.ContentItemMapView_fullScreenMap, 0);
        mapIdentifier = a.getResourceId(R.styleable.ContentItemMapView_mapViewId, 0)
        fullScreenMapLayout = a.getResourceId(R.styleable.ContentItemMapView_fullScreenMapLayout, R.layout.partial_full_screen_map)
        a.recycle()

    }

    override fun show(contentItem: ContentItem, cacheValid: Boolean) {
        if (contentItem is ContentItemWithLocation && shouldShowMapWithContentItem(contentItem)) {
            mLocationItem = contentItem
            visibility = View.VISIBLE
            mapManager?.getMapAsync {
                it.clear()
                MapUtils.styleMap(it, context)
                showDataOnMap(it, contentItem, locationItemKmlFile, cacheValid)
            }
            if (showUserDirection || showUserPosition)
                mapSupportListener?.mapRequestUserLocation(this)
        } else
            visibility = View.GONE

        if (fullScreenMapId != 0) {
            mShowAlreadyCalled = true
            mContentItem = contentItem
            mCacheValid = cacheValid
        }

    }

    protected open fun shouldShowMapWithContentItem(contentItem: ContentItemWithLocation): Boolean {

        val mapPart = contentItem.mapPart
        val kmlMediaPart = if (mapPart != null && showKmlDetail) mapPart.kml else null
        return mapPart != null && (mapPart.isMapValid || kmlMediaPart != null)

    }

    @CallSuper
    protected open fun showDataOnMap(map: GoogleMap, contentItem: ContentItemWithLocation, kmlFile: File?, cacheValid: Boolean) {
        val cameraBounds = LatLngBounds.builder()
        val mapPart = contentItem.mapPart

        if (kmlFile != null) {
            try {
                val fos = FileInputStream(kmlFile)
                kmlLayer?.removeLayerFromMap()
                kmlLayer = KmlLayer(map, fos, getActivity())
                kmlLayer?.addLayerToMap()
                kmlLayer?.updateBounds(cameraBounds)

            } catch (ignored: Exception) {
                // Exception ignored
            }
        } else {
            val kmlUrl = mapPart?.kml?.mediaUrl
            if (showKmlDetail && kmlUrl != null) {
                kmlTask?.load(context, kmlUrl)
            }
        }


        if (mapPart != null && mapPart.isMapValid) {
            mLocationItemMarker?.remove()
            mLocationItemMarker = map.addMarker(MarkerCreator.shared.createMarker(context, contentItem))

            cameraBounds.include(mapPart.latLng)
        } else
            mLocationItemMarker = null

        if (kmlFile != null) {
            try
            {
                mapZoomSupport.updateCamera(
                        CameraUpdateFactory.newLatLngBounds(cameraBounds.build(), resources.getDimensionPixelSize(R.dimen.activity_horizontal_margin)), map)
            }
            catch (e: Exception)
            {
                //ignored
            }

        } else if (mapPart != null)
            mapZoomSupport.updateCamera(
                    CameraUpdateFactory.newLatLngZoom(mapPart.latLng, resources.getInteger(R.integer.default_zoom).toFloat()), map)

    }

    protected open fun setupMap(map: GoogleMap) {
        (container as? GoogleMap.OnInfoWindowClickListener)?.let {
            map.setOnInfoWindowClickListener(it)
        }
    }

    protected open fun unSetupMap(map: GoogleMap) {
        map.setOnMarkerClickListener(null)
        map.setOnMapClickListener(null)
        map.setOnInfoWindowClickListener(null)
    }

    private fun showExpandedMap() {
        val expandBehavior = {
            fullScreenMapBehavior!!.setStateAndNotify(BottomSheetBehavior.STATE_EXPANDED)
            fullScreenMapBehavior!!.isHideable = false
        }

        if (fullScreenMapBehavior == null) {
            val coordinator = getCoordinatorLayout()!!
            var mapFullScreenView = coordinator.findViewById<View>(fullScreenMapId) as ContentItemMapView?

            if (mapFullScreenView == null) {
                LayoutInflater.from(context).inflate(fullScreenMapLayout, coordinator, true)

                mapFullScreenView = coordinator.findViewById(fullScreenMapId)
                mapFullScreenView?.container = container
                mapFullScreenView?.onResume()

                fullScreenMapBehavior = (mapFullScreenView?.layoutParams as CoordinatorLayout.LayoutParams).behavior as SafeBottomSheetBehavior<*>

                if (container is ContentItemDetailModelFragment) {
                    (container as ContentItemDetailModelFragment).addSheetCallback(fullScreenMapBehavior!!)
                }

                if (mShowAlreadyCalled) {
                    mapFullScreenView.show(mContentItem!!, mCacheValid)
                }
            }

            mapFullScreenView.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    mapFullScreenView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    expandBehavior()
                }
            })
        } else {
            expandBehavior()
        }
    }

    @SuppressLint("MissingPermission")
    fun onLocationChanged(location: Location) {
        if (showUserDirection || showUserPosition)
        {
            mapManager?.getMapAsync { it.isMyLocationEnabled = true }

            if (showUserDirection && directionRequest == null && mLocationItem != null)
            {
                directionRequest = Request(context, location, mLocationItem!!.mapPart!!.location, null)
                directionRequest!!.load(this)
            }
        }
    }

    fun onResume() {
        mapManager?.onResume()
    }

    fun onPause() {
        mapManager?.onPause()
    }

    fun onLowMemory() {
        mapManager?.onLowMemory()
    }

    fun onDestroy() {
        kmlLayer?.removeLayerFromMap()
        kmlLayer = null
        mapManager?.onDestroy()
    }

    override fun onCompleted(e: Exception?, r: List<LatLng>?) {
        directionRequest = null

        if (r != null) {
            directionPolyline = r

            mapManager?.getMapAsync {
                it.addPolyline(PolylineOptions().addAll(directionPolyline).color(Color.RED))
                val builder = LatLngBounds.builder()
                for (i in r.indices) {
                    builder.include(r[i])
                }

                mapZoomSupport.updateCamera(
                        CameraUpdateFactory.newLatLngBounds(builder.build(), resources.getDimensionPixelSize(R.dimen.content_details_internal_padding)), it)
            }
        }
    }

    override fun onKmlLoadCompleted(kml: File) {
        locationItemKmlFile = kml
        mapManager?.getMapAsync { if (mLocationItem != null) showDataOnMap(it, mLocationItem!!, locationItemKmlFile, true) }
    }
}

/**
 * Interfaccia che deve implementare il fragment che contiene la ContentItemMapView.
 * Sono metodi necessari per poter gestire alcuni metodi della mappa
 */
interface ContentItemViewListenerMapSupport {

    /**
     * Aggiunge la mappa a quelle gestite dal fragment
     * @param mapView mappa de
     */
    fun addManagedMapView(mapView: ContentItemMapView)

    /**
     * Permette alla mappa di indicare che ha bisogno dei permessi di accesso alla posizione dell'utente
     */
    fun mapRequestUserLocation(mapView: ContentItemMapView)
}