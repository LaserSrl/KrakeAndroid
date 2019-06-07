package com.krake.core.map

import android.app.Activity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.krake.core.R
import com.krake.core.app.ContentItemListMapActivity
import java.lang.ref.WeakReference

/**
 * Classe per effettuare lo switch tra 2 tipi di visualizzazioni di un contenuto (es Mappa/Lista)
 * Che devono essere visibili entrambe conteporaneamente su tablet, ma alternativamente su smartphone
 * L'activity deve inviare all'istanza dello switcher le chiamate
 *
 *  1. [.onCreateOptionsMenu]
 *  1. [.onOptionsItemSelected]
 *  1. [.onPrepareOptionsMenu]
 *  1. [.onDestroy]
 *
 *
 * E' possibile anche disattivare il menu andando ad impostare [.setMenuEnabled].
 *
 * Oppure anche di disattivare solo temporaneamente gli elementi visibili nella menu, andando a modificare
 * [.setHideAllButtons].
 * Dopo questa chiamata l'activity dovrà occuparsi in indicare che il menu non è più valido [Activity.invalidateOptionsMenu]
 */
open class MapListSwitcher
/**
 * Creazione della nuova istanza
 *
 * @param activity            che contiene i fragment
 * @param mapFragment         primo tipo di fragment (per convenzione la mappa)
 * @param listFragment        secondo tipo di fragment (per convenzione aspetto lista)
 * @param mapInitiallyVisible indicazione se la mappa deve essere visibile.
 */
@JvmOverloads constructor(
    activity: ContentItemListMapActivity?,
    private var floatingActionButton: FloatingActionButton? = null,
    mapInitiallyVisible: Boolean = true
) {
    private var weakActivity = WeakReference<ContentItemListMapActivity>(activity)
    private val mActivity: ContentItemListMapActivity? = weakActivity.get()

    private val menuEnabled: Boolean = floatingActionButton == null

    var isMapVisible = true
        private set

    var hideAllButtons: Boolean = false
        set(value) {
            field = value
            if (menuEnabled)
                mActivity?.invalidateOptionsMenu()
        }

    var listener: VisibiliyChangeListener? = null

    init {

        isMapVisible = mapInitiallyVisible

        updateMapVisibility(isMapVisible)

        if (mActivity is VisibiliyChangeListener) {
            listener = mActivity
            listener!!.onMapVisibilityChanged(isMapVisible, floatingActionButton)
        }

        floatingActionButton?.alpha = 1.0f
        floatingActionButton?.setOnClickListener {
            updateMapVisibility(!isMapVisible)
        }
    }

    fun onCreateOptionsMenu(menu: Menu): Boolean {

        if (menuEnabled)
            mActivity?.menuInflater?.inflate(R.menu.map_list, menu)

        return true
    }

    fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (menuEnabled) {
            if (item.itemId == R.id.action_show_map) {

                updateMapVisibility(true)

                return true
            } else if (item.itemId == R.id.action_show_list) {
                updateMapVisibility(false)

                return true
            }
        }
        return false
    }

    fun updateMapVisibility(mapVisible: Boolean) {
        mActivity?.let {
            if (it.mapFragment != null && it.gridFragment != null) {
                if (mapVisible)
                    it.supportFragmentManager.beginTransaction()
                        .show(it.mapFragment)
                        .hide(it.gridFragment)
                        .commit()
                else
                    it.supportFragmentManager.beginTransaction()
                        .hide(it.mapFragment)
                        .show(it.gridFragment)
                        .commit()

                isMapVisible = mapVisible

                if (menuEnabled)
                    it.invalidateOptionsMenu()

                if (isMapVisible)
                    floatingActionButton?.setImageResource(R.drawable.ic_list_24dp)
                else
                    floatingActionButton?.setImageResource(R.drawable.ic_map_24dp)
            }
        }


        listener?.onMapVisibilityChanged(mapVisible, floatingActionButton)

    }

    fun onPrepareOptionsMenu(menu: Menu): Boolean {

        if (menuEnabled) {
            menu.findItem(R.id.action_show_map).isVisible = !isMapVisible && !hideAllButtons
            menu.findItem(R.id.action_show_list).isVisible = isMapVisible && !hideAllButtons
        }
        return true
    }

    fun onDestroy() {
    }

    interface VisibiliyChangeListener {
        fun onMapVisibilityChanged(mapVisible: Boolean, source: View?)
    }
}