package com.krake.bus.component.module

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.model.LatLng
import com.krake.bus.app.BusMapFragment
import com.krake.bus.app.BusSearchActivity
import com.krake.bus.app.BusStopsListActivity
import com.krake.bus.model.BusPattern
import com.krake.bus.model.BusStop
import com.krake.core.app.ContentItemListMapActivity
import com.krake.core.component.base.ComponentModule
import com.krake.core.component.module.ThemableComponentModule
import com.krake.core.extension.getDataClass
import com.krake.core.extension.putDataClass
import com.krake.trip.R

/**
 * Modulo utilizzato per specificare gli attributi di [Activity] e [Fragment] legati ad elementi di tipo [BusStop].
 * Utilizzato principalmente da:
 * <ul>
 * <li>[BusSearchActivity] e dalle [Activity] che la estendono</li>
 * <li>[BusStopsListActivity] e dalle [Activity] che la estendono</li>
 * </ul>
 */
class BusComponentModule : ComponentModule {
    companion object {
        private const val ARG_STOP_ITEM_CLASS = "argStopItemClass"
        private const val ARG_PATTERN_CLASS = "argPatternClass"
        private const val ARG_DEFAULT_LATITUDE = "argDefaultLat"
        private const val ARG_DEFAULT_LONGITUDE = "argDefaultLong"
        private const val ARG_AUTO_REFRESH_BUS_STOP_LIST = "argAutoRefreshBusStopList"

        val DEFAULT_ACTIVITY = BusSearchActivity::class.java
        val DEFAULT_MAP_FRAGMENT = BusMapFragment::class.java
        @LayoutRes
        val DEFAULT_LIST_CELL_LAYOUT = R.layout.cell_bus_stop_passage
        @LayoutRes
        val DEFAULT_LIST_ROOT_LAYOUT = R.layout.fragment_bus_list
    }

    var defaultLocation: LatLng?
        private set

    var patternClass: Class<out BusPattern>
        private set

    var stopItemClass: Class<out BusStop>
        private set

    var busStopsAutoRefreshPeriod : Int
        private set

    init {
        defaultLocation = null
        patternClass = BusPattern::class.java
        stopItemClass = BusStop::class.java
        busStopsAutoRefreshPeriod = 0
    }

    /**
     * Classe utilizzata in [BusStopsListActivity] per effettuare una connessione.
     * DEFAULT: [BusPattern]
     *
     * @param patternClass classe di tipo [BusPattern].
     */
    fun patternClass(patternClass: Class<out BusPattern>) = apply { this.patternClass = patternClass }

    /**
     * Classe utilizzata in [BusSearchActivity] che viene passata ad una [ContentItemListMapActivity],
     * per mostrare in una lista elementi di tipo [BusStop].
     * DEFAULT: [BusStop]
     *
     * @param stopItemClass classe di tipo [BusStop].
     */
    fun stopItemClass(stopItemClass: Class<out BusStop>) = apply { this.stopItemClass = stopItemClass }

    /**
     * Location di default da usare per caricare i dati iniziali in [BusSearchActivity] nel caso in cui non sia
     * disponibile la posizione dell'utente.
     */
    fun defaultLocation(defaultLocation: LatLng) = apply { this.defaultLocation = defaultLocation }

    /**
     * auto refresh range in seconds for refresh the bus stops list
     */
    fun busStopsAutoRefreshPeriod(busStopsAutoRefreshRange: Int) = apply { this.busStopsAutoRefreshPeriod = busStopsAutoRefreshRange }

    /**
     * Legge il contenuto di un [Bundle] e modifica le sue proprietà.
     *
     * @param context il [Context] utilizzato per leggere il [Bundle].
     * @param bundle container dal quale vengono letti i vari arguments.
     */
    @Suppress("UNCHECKED_CAST")
    override fun readContent(context: Context, bundle: Bundle) {
        defaultLocation = LatLng(bundle.getDouble(ARG_DEFAULT_LATITUDE), bundle.getDouble(ARG_DEFAULT_LONGITUDE))
        patternClass = (bundle.getDataClass(ARG_PATTERN_CLASS) as Class<out BusPattern>)
        stopItemClass = (bundle.getDataClass(ARG_STOP_ITEM_CLASS) as Class<out BusStop>)
        busStopsAutoRefreshPeriod = bundle.getInt(ARG_AUTO_REFRESH_BUS_STOP_LIST)
    }

    /**
     * Scrive le proprietà di un modulo su un [Bundle].
     *
     * @param context il [Context] utilizzato per creare il [Bundle] e scrivere su di esso.
     * @return container che contiene le proprietà del modulo corrente.
     */
    override fun writeContent(context: Context): Bundle {
        val bundle = Bundle()
        defaultLocation?.let {
            bundle.putDouble(ARG_DEFAULT_LATITUDE, it.latitude)
            bundle.putDouble(ARG_DEFAULT_LONGITUDE, it.longitude)
        }
        bundle.putDataClass(ARG_PATTERN_CLASS, patternClass)
        bundle.putDataClass(ARG_STOP_ITEM_CLASS, stopItemClass)
        bundle.putInt(ARG_AUTO_REFRESH_BUS_STOP_LIST, busStopsAutoRefreshPeriod)
        return bundle
    }

    /**
     * Definisce la lista delle classi da cui questo modulo deve dipendere.
     *
     * @return array di dipendenze.
     */
    override fun moduleDependencies(): Array<Class<out ComponentModule>> {
        return arrayOf(ThemableComponentModule::class.java)
    }
}