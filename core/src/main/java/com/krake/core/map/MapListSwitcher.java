package com.krake.core.map;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import androidx.fragment.app.Fragment;
import com.krake.core.R;
import com.krake.core.app.ContentItemListMapActivity;

/**
 * Classe per effettuare lo switch tra 2 tipi di visualizzazioni di un contenuto (es Mappa/Lista)
 * Che devono essere visibili entrambe conteporaneamente su tablet, ma alternativamente su smartphone
 * L'activity deve inviare all'istanza dello switcher le chiamate
 * <ol>
 *     <li>{@link #onCreateOptionsMenu(Menu)}</li>
 *     <li>{@link #onOptionsItemSelected(MenuItem)}</li>
 *     <li>{@link #onPrepareOptionsMenu(Menu)}</li>
 *     <li>{@link #onDestroy()}</li>
 *     </ol>
 *
 * E' possibile anche disattivare il menu andando ad impostare {@link #setMenuEnabled(boolean)}.
 *
 * Oppure anche di disattivare solo temporaneamente gli elementi visibili nella menu, andando a modificare
 * {@link #setHideAllButtons(boolean)}.
 * Dopo questa chiamata l'activity dovrà occuparsi in indicare che il menu non è più valido {@link Activity#invalidateOptionsMenu()}
 */
public class MapListSwitcher {
    private boolean mMapVisible = true;
    private boolean mMenuEnabled;
    private ContentItemListMapActivity mActivity;

    private boolean mHideAllButtons;

    private VisibiliyChangeListener mListener;

    /**
     * Creazione della nuova istanza. La mappa è inizialmente visibile
     * @param activity che contiene i fragment
     * @param mapFragment primo tipo di fragment (per convenzione la mappa)
     * @param listFragment secondo tipo di fragment (per convenzione aspetto lista)
     */
    public MapListSwitcher(ContentItemListMapActivity activity, Fragment mapFragment, Fragment listFragment) {
        this(activity, mapFragment, listFragment, true);
    }

    /**
     * Creazione della nuova istanza
     *
     * @param activity            che contiene i fragment
     * @param mapFragment         primo tipo di fragment (per convenzione la mappa)
     * @param listFragment        secondo tipo di fragment (per convenzione aspetto lista)
     * @param mapInitiallyVisible indicazione se la mappa deve essere visibile.
     */
    public MapListSwitcher(ContentItemListMapActivity activity, Fragment mapFragment, Fragment listFragment, boolean mapInitiallyVisible) {
        mActivity = activity;
        mMenuEnabled = !activity.getResources().getBoolean(R.bool.is_tablet);

        mMapVisible = mapInitiallyVisible;

        updateMapVisibility(mMapVisible);

        if (mActivity instanceof VisibiliyChangeListener) {
            mListener = (VisibiliyChangeListener) mActivity;
            mListener.onMapVisibilityChanged(mMapVisible);
        }
    }

    public boolean isMapVisible() {
        return mMapVisible;
    }

    public boolean getMenuEnabled() {
        return mMenuEnabled;
    }

    public void setMenuEnabled(boolean menuEnabled) {
        mMenuEnabled = menuEnabled;
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        if (mMenuEnabled && mActivity != null)
            mActivity.getMenuInflater().inflate(R.menu.map_list, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        if (mMenuEnabled) {
            if (item.getItemId() == R.id.action_show_map) {

                updateMapVisibility(true);

                return true;
            } else if (item.getItemId() == R.id.action_show_list) {
                updateMapVisibility(false);

                return true;
            }
        }
        return false;
    }

    public void updateMapVisibility(boolean mapVisible) {
        if (mActivity.getMapFragment() != null && mActivity.getGridFragment() != null) {
            if (mapVisible)
                mActivity.getSupportFragmentManager().beginTransaction()
                        .show(mActivity.getMapFragment())
                        .hide(mActivity.getGridFragment())
                        .commit();
            else

                mActivity.getSupportFragmentManager().beginTransaction()
                        .hide(mActivity.getMapFragment())
                        .show(mActivity.getGridFragment())
                        .commit();
            mMapVisible = mapVisible;
            mActivity.invalidateOptionsMenu();
        }
        if (mListener != null)
            mListener.onMapVisibilityChanged(mapVisible);

    }

    public boolean onPrepareOptionsMenu(Menu menu) {

        if (mMenuEnabled) {
            menu.findItem(R.id.action_show_map).setVisible(!mMapVisible && !mHideAllButtons);
            menu.findItem(R.id.action_show_list).setVisible(mMapVisible && !mHideAllButtons);
        }
        return true;
    }

    public void onDestroy() {
        mActivity = null;
    }

    /**
     * Funzione per nascondere entrambi i bottoni dell'action bar
     *
     * @param mHideAllButtons
     */
    public void setHideAllButtons(boolean mHideAllButtons) {
        this.mHideAllButtons = mHideAllButtons;
    }

    public VisibiliyChangeListener getListener() {
        return mListener;
    }

    public void setListener(VisibiliyChangeListener mListener) {
        this.mListener = mListener;
    }

    public interface VisibiliyChangeListener {
        void onMapVisibilityChanged(boolean visible);
    }
}
