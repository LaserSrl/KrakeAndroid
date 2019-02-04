package com.krake.core.widget;

import android.support.v7.widget.GridLayoutManager;

/**
 * Gestisce uno span size differente a seconda della posizione.
 * L'elemento in posizione 0 occuperà tutto lo spazio disponibile, quindi lo span sarà uguale a {@link GridLayoutManager#getSpanCount()}.
 * L'elemento in ultima posizione occuperà lo span rimanente di quella riga/colonna.
 */
public class FillFirstAndLastLookup extends GridLayoutManager.SpanSizeLookup {
    private int spanCount;
    private int listSize;

    /**
     * Crea un'istanza di {@link FillFirstAndLastLookup} per gestire lo span
     *
     * @param spanCount span del {@link GridLayoutManager}
     * @param listSize  numero di elementi nella lista
     */
    public FillFirstAndLastLookup(int spanCount, int listSize) {
        this.listSize = listSize;
        this.spanCount = spanCount;
    }

    @Override
    public int getSpanSize(int position) {
        if (position == 0) {
            return spanCount;
        } else if (position == listSize - 1) {
            int remainderInSpan = position % spanCount - 1;
            if (remainderInSpan != -1) {
                return spanCount - remainderInSpan;
            }
        }
        return 1;
    }

    /**
     * Permette di aggiornare il numero di elementi della lista dell'adapter in modo da adattare la logica di {@link #getSpanSize(int)} alla nuova lista
     *
     * @param listSize numero di elementi nella lista
     */
    public void setListSize(int listSize) {
        this.listSize = listSize;
    }
}