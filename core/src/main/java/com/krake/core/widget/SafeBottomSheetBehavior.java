package com.krake.core.widget;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

/**
 * BottomSheetBehavior utilizzato per evitare il crash sull'upper cast del restoreInstanceState() delle sdk di Android.
 * <br/>
 * Il BottomSheetBehavior crasha nel caso in cui due view con lo stesso id (dopo la rotazione) sono abbinate a due behavior diversi.
 * <br/>
 * La classe rende il behavior più flessibile permettendo di abbinarlo a view con behavior diversi dal BottomSheetBehavior.
 */
public class SafeBottomSheetBehavior<V extends View> extends BottomSheetBehavior<V> {

    private List<BottomSheetStateCallback> mCallbacks = new LinkedList<>();
    private WeakReference<V> mView;

    /**
     * Costruttore di default per istanziare un SafeBottomSheetBehavior
     */
    public SafeBottomSheetBehavior() {
        super();
    }
    /**
     * Costruttore di default per l'inflate di un SafeBottomSheetBehavior da layout
     *
     * @param context Context corrente
     * @param attrs   AttributeSet del tema
     */
    public SafeBottomSheetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);

        // di default tutte le callback vengono redirezionate ai listener
        super.setBottomSheetCallback(new BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                dispatchOnStateChanged(bottomSheet, newState);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //dispatchOnSlide(bottomSheet, slideOffset);
            }
        });
    }

    public void setStateAndNotify(final @State int state) {
        setState(state);
        if (mView != null)
            dispatchOnStateWillChange(mView.get(), state);
    }

    @Override
    public void onRestoreInstanceState(CoordinatorLayout parent, V child, Parcelable state) {
        // il check è per evitare che parta il processo di restoreInstanceState con uno stato che non è di BottomSheetBehavior (es. AbsSavedState)
        if (state instanceof SavedState) {
            super.onRestoreInstanceState(parent, child, state);
        }
    }


    @Override
    public void setBottomSheetCallback(BottomSheetCallback callback) {
        throw new RuntimeException("Use addBottomSheetCallback(BottomSheetCallback)");
    }

    /**
     * Redireziona la callback {@link BottomSheetCallback#onStateChanged(View, int)} principale
     * a tutte le callback aggiunte nella lista {@link #mCallbacks}
     *
     * @param bottomSheet View container con un BottomSheetBehavior
     * @param newState    stato corrente del BottomSheetBehavior
     */
    public void dispatchOnStateChanged(@NonNull View bottomSheet, int newState) {
        for (BottomSheetStateCallback callback : mCallbacks) {
            callback.onStateChanged(bottomSheet, newState);
        }
    }

    public void dispatchOnStateWillChange(@NonNull View bottomSheet, int newState) {
        for (BottomSheetStateCallback callback : mCallbacks) {
            callback.onStateWillChange(bottomSheet, newState);
        }
    }

    /**
     * Aggiunge una callback a {@link #mCallbacks} per la ricezione di update dalla callback principale
     *
     * @param callback callback da aggiungere
     */
    public void addBottomSheetCallback(@NonNull BottomSheetStateCallback callback) {
        mCallbacks.add(callback);
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, V child, int layoutDirection) {
        if (mView == null) {
            mView = new WeakReference<>(child);
        }

        return super.onLayoutChild(parent, child, layoutDirection);
    }

    /**
     * Rimuove una callback da {@link #mCallbacks} per interrompere la ricezione di update dalla callback principale
     *
     * @param callback callback da rimuovere
     */
    public void removeBottomSheetCallback(@NonNull BottomSheetStateCallback callback) {
        mCallbacks.remove(callback);
    }

    static public abstract class BottomSheetStateCallback extends BottomSheetCallback {
        public abstract void onStateWillChange(@NonNull View bottomSheet, int newState);
    }

}