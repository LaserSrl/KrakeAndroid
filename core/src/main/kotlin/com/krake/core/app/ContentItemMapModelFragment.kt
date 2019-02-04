package com.krake.core.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Trace
import android.support.annotation.CallSuper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.clustering.ClusterManager
import com.krake.core.OrchardError
import com.krake.core.R
import com.krake.core.component.annotation.BundleResolvable
import com.krake.core.component.module.ListMapComponentModule
import com.krake.core.content.UpdatebleOrchardDataLoader
import com.krake.core.data.DataModel
import com.krake.core.location.LocationRequirementsHelper
import com.krake.core.location.settings.GpsSettingsListener
import com.krake.core.map.*
import com.krake.core.map.manager.FragmentMapManager
import com.krake.core.map.manager.MapManager
import com.krake.core.model.ContentItem
import com.krake.core.model.ContentItemWithLocation
import com.krake.core.model.identifierOrStringIdentifier
import com.krake.core.permission.PermissionListener
import com.krake.core.permission.PermissionManager
import com.krake.core.view.MapUtils
import com.krake.core.view.MapViewPagerSupport
import com.krake.core.widget.osm.WebOSMCopyrightView
import java.util.*

/**
 * Classe per mostare su una mappa i contenuti prelevati da orchard
 * I dati devono implementare l'interfaccia [ContentItemWithLocation]
 * I dati vengono inseriti sulla mappa sfruttando l'interazione con [KrakeApplication.getMarkerCreator]
 *
 *
 * Il fragment carica i dati paginati, e procede autonomamente a caricare una pagina dopo l'altra
 * fino a quando non saranno stati scaricati tutti i dati.
 *
 *
 * **Importante** l'activity che utilizza questo fragment deve implementare l'interfaccia [OnContentItemSelectedListener]
 */
open class ContentItemMapModelFragment : OrchardDataModelFragment(),
        GoogleMap.OnInfoWindowClickListener,
        UpdatebleOrchardDataLoader,
        GoogleMap.OnMarkerClickListener,
        TouchableMapView.OnMapTouchListener,
        ClusterManager.OnClusterItemClickListener<MarkerConfiguration>,
        ClusterManager.OnClusterItemInfoWindowClickListener<MarkerConfiguration>,
        PermissionListener,
        GpsSettingsListener
{
    @BundleResolvable
    lateinit var listMapComponentModule: ListMapComponentModule

    protected lateinit var mMapManager: MapManager

    var items: MutableList<ContentItemWithLocation>? = null
        private set

    private val mMarkersToIdentifier = HashMap<Marker, String>()
    private val mIdentifiersToMarker = HashMap<String, Marker>()

    lateinit private var mapZoomSupport: MapZoomSupport
    protected var clusterManager: ClusterManager<MarkerConfiguration>? = null
        private set
    private var mListener: OnContentItemSelectedListener? = null
    lateinit private var mMapPager: MapViewPagerSupport

    lateinit protected var locationRequirementsHelper: LocationRequirementsHelper
        private set

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        mMapPager = MapViewPagerSupport(dataConnectionModel)
        locationRequirementsHelper = LocationRequirementsHelper(this, this, this)
        locationRequirementsHelper.create()
    }


    override fun onAttach(activity: Context?)
    {
        super.onAttach(activity)
        mListener = activity as? OnContentItemSelectedListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        Trace.beginSection("createMapView")
        val view = inflater.inflate(R.layout.fragment_content_items_map, container, false)
        Trace.endSection()
        return view
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        Trace.beginSection("createMapInfo")

        val mapView = view.findViewById<TouchableMapView>(R.id.map)
        mapView.setOnMapTouchListener(this)
        val osmCopyrightView = view.findViewById<WebOSMCopyrightView>(R.id.osm_copyright_view)

        mMapManager = FragmentMapManager(this) { mapView }

        mMapManager.onCreate(savedInstanceState)
        mMapManager.getMapAsync { googleMap ->
            if (googleMap.mapType == GoogleMap.MAP_TYPE_NONE)
            {
                osmCopyrightView.showOSMCopyright()
            }
        }

        if (!listMapComponentModule.mapAvoidLocationPermissions)
        {
            locationRequirementsHelper.request(false)
        }

        Trace.endSection()
    }

    @SuppressLint("MissingPermission")
    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        mMapManager.getMapAsync { googleMap ->
            if (PermissionManager.areGranted(activity!!, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            {
                googleMap.isMyLocationEnabled = true
            }

            if (listMapComponentModule.mapUseCluster)
            {
                clusterManager = ClusterManager(activity!!, googleMap)
            }

            if (clusterManager != null)
            {
                MapUtils.styleMap(googleMap, activity!!)
                clusterManager?.let {
                    it.setRenderer(LocationItemRenderer(this@ContentItemMapModelFragment, googleMap, it))
                    googleMap.setOnMarkerClickListener(it)
                    it.setOnClusterItemClickListener(this@ContentItemMapModelFragment)
                    it.setOnClusterItemInfoWindowClickListener(this@ContentItemMapModelFragment)
                    googleMap.setOnInfoWindowClickListener(it)
                    googleMap.setOnMarkerClickListener(it)
                    googleMap.setOnCameraIdleListener(it)
                }

            }
            else
            {
                MapUtils.styleMap(googleMap, activity!!)
                googleMap.setOnMarkerClickListener(this@ContentItemMapModelFragment)
                googleMap.setOnInfoWindowClickListener(this@ContentItemMapModelFragment)
            }
        }

        mapZoomSupport = MapZoomSupport(view)
        super.onActivityCreated(savedInstanceState)
    }

    override fun onResume()
    {
        super.onResume()
        mMapManager.onResume()
    }

    override fun onPause()
    {
        super.onPause()
        mMapManager.onPause()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mMapManager.onDestroy()
    }

    override fun onLowMemory()
    {
        super.onLowMemory()
        mMapManager.onLowMemory()
    }

    override fun onDataModelChanged(dataModel: DataModel?)
    {
        if (dataModel != null)
        {
            mMapManager.getMapAsync { googleMap ->

                loadItemsInMap(googleMap, dataModel.listData as List<ContentItemWithLocation>, dataModel.cacheValid)
            }
        }
    }

    override fun needToAccessDataInMultiThread(): Boolean
    {
        return listMapComponentModule.mapUseCluster
    }

    override fun setExtraParameter(key: String, value: String?, reload: Boolean)
    {
        dataConnectionModel.orchardModule.putExtraParameter(key, value)
        if (reload)
            dataConnectionModel.restartDataLoading()
    }

    /**
     * Mostra effettivamente i dati sulla mappa. Viene invocata per ogni chiamata di
     * [.onDataModelChanged]
     * Potrebbero arrivare più chiamate a questo metodo nel caso in cui la cache iniziale sia scaduta
     *
     * @param googleMap  mappa su cui mostrare i contenuti
     * @param lazyList   contenuti da mostrare
     * @param cacheValid indicazione se la cache è valida
     */
    open fun loadItemsInMap(googleMap: GoogleMap, lazyList: List<ContentItemWithLocation>, cacheValid: Boolean)
    {
        Trace.beginSection("loadItemsInMap")
        val activity = activity
        if (activity != null)
        {
            val itemsToLoad: MutableList<ContentItemWithLocation>
            if (dataConnectionModel.page == 1)
            {
                clearMapOnDataChange(googleMap)
                items = LinkedList(lazyList as? List<ContentItemWithLocation>)
                itemsToLoad = items as MutableList<ContentItemWithLocation>
                // Svuota la lista se deve ricaricare tutti gli elementi.
                mMarkersToIdentifier.clear()
                mIdentifiersToMarker.clear()
            }
            else
            {
                itemsToLoad = LinkedList(lazyList as? List<ContentItemWithLocation>)

                val itemsIdentifiers = items?.map { it.identifierOrStringIdentifier }

                if (itemsIdentifiers != null)
                {
                    itemsToLoad.removeAll { itemsIdentifiers.contains(it.identifierOrStringIdentifier) }
                }

                items = LinkedList(lazyList as? List<ContentItemWithLocation>)
            }


            for (cItem in itemsToLoad)
            {
                val mapPart = cItem.mapPart
                if (mapPart != null && mapPart.isMapValid)
                {
                    if (clusterManager == null)
                    {
                        val marker = googleMap.addMarker(MarkerCreator.shared.createMarker(activity, cItem))
                        onMarkerCreated(marker, cItem)
                    }
                    else
                    {
                        clusterManager?.addItem(cItem)
                    }
                }
            }
            if (clusterManager != null)
                clusterManager?.cluster()

            if (dataConnectionModel.page == 1 || (dataConnectionModel.page * dataConnectionModel.orchardModule.pageSize) > lazyList.size)
            {
                val builder = LatLngBounds.Builder()

                var boundsContent = 0

                lazyList.forEach {
                    val mapPart = it.mapPart
                    if (mapPart != null && mapPart.isMapValid)
                    {
                        builder.include(mapPart.latLng)
                        ++boundsContent
                    }
                }

                if (boundsContent > 1)
                    mapZoomSupport.updateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), resources.getDimensionPixelSize(R.dimen.map_padding)), googleMap)
                else if (boundsContent > 0)
                    mapZoomSupport.updateCamera(CameraUpdateFactory.newLatLngZoom(builder.build().center, resources.getInteger(R.integer.default_zoom).toFloat()), googleMap)
            }
            mMapPager.onDataLoaded(lazyList)
        }
        Trace.endSection()
    }

    private fun onMarkerCreated(marker: Marker, configuration: MarkerConfiguration)
    {
        val sId = configuration.identifierOrStringIdentifier
        mMarkersToIdentifier[marker] = sId
        mIdentifiersToMarker[sId] = marker
    }

    /**
     * Specifica gli elementi da rimuovere quando i dati cambiano.
     * Nell'implementazione base, rimuove tutti i markers.
     *
     * @param googleMap istanza di [GoogleMap] dalla quale rimuovere gli elementi
     */
    @CallSuper
    open protected fun clearMapOnDataChange(googleMap: GoogleMap)
    {
        if (clusterManager == null)
        {
            for (marker in mMarkersToIdentifier.keys)
            {
                marker.remove()
            }
        }
        else
        {
            clusterManager?.clearItems()
        }
    }

    fun openInfoWindowAndZoomOnItem(item: ContentItemWithLocation)
    {
        val sId = item.identifierOrStringIdentifier
        val marker = mIdentifiersToMarker[sId]
        if (marker != null)
        {
            marker.showInfoWindow()
            mMapManager.getMapAsync { googleMap ->
                mapZoomSupport.updateCamera(CameraUpdateFactory.newLatLngZoom(item.mapPart!!.latLng, resources.getInteger(R.integer.default_zoom).toFloat()), googleMap)
            }
        }
    }

    override fun onInfoWindowClick(marker: Marker)
    {
        getItemForMarker(marker)?.let {
            mListener?.onShowContentItemDetails(this,
                                                it)
        }
    }

    override fun updateDisplayPath(displayPath: String)
    {
        dataConnectionModel.orchardModule.displayPath(displayPath)
        dataConnectionModel.restartDataLoading()
    }

    open fun getItemForMarker(marker: Marker): ContentItemWithLocation?
    {
        val identifier = mMarkersToIdentifier[marker]

        items?.let {
            for (item in it)
            {
                if (item.identifierOrStringIdentifier == identifier)
                    return item
            }
        }

        return null
    }

    override fun onMarkerClick(marker: Marker): Boolean
    {
        val contentItemWithLocation = getItemForMarker(marker)
        if (contentItemWithLocation != null)
        {
            mListener?.onContentItemInEvidence(this, contentItemWithLocation)
        }
        return false
    }

    @SuppressLint("MissingPermission")
    override fun onPermissionsHandled(acceptedPermissions: Array<out String>)
    {
        if (PermissionManager.containLocationPermissions(acceptedPermissions))
        {
            mMapManager.getMapAsync { googleMap ->
                googleMap.isMyLocationEnabled = true
                googleMap.uiSettings.isMyLocationButtonEnabled = true
            }
        }
    }

    override fun onGpsSettingsAcquired()
    {

    }

    override fun onMapTouch(event: MotionEvent)
    {
        val mActivity = activity
        if (mActivity != null && mActivity is ContentItemListMapActivity)
        {
            val action = event.action
            (activity as ContentItemListMapActivity)
                    .setSwipeRefreshEnabled(action != MotionEvent.ACTION_MOVE && action != MotionEvent.ACTION_DOWN)
        }
    }

    override fun onClusterItemClick(item: MarkerConfiguration): Boolean
    {
        if (item is ContentItem)
        {
            mListener?.onContentItemInEvidence(this, item)
        }
        return false
    }

    override fun onClusterItemInfoWindowClick(item: MarkerConfiguration)
    {
        if (item is ContentItem)
        {
            mListener?.onShowContentItemDetails(this, item)
        }
    }

    override fun onDataLoadingError(orchardError: OrchardError)
    {

    }
}
