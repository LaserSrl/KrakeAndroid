package com.krake.core.widget;

import android.support.annotation.IntDef;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Classe che aggiunge l'auto center sulla ScrollView
 */
public class SnapManager implements View.OnTouchListener, ViewTreeObserver.OnGlobalLayoutListener {
    /**
     * Tag per i vari log
     */
    private static final String TAG = "SnapManager";
    /**
     * tempo di aggiornamento in millisecondi
     */
    private static final int NEW_CHECK = 100;
    private static final int VERTICAL = 0;
    private static final int HORIZONTAL = 1;
    /**
     * Array dei child della ScrollView
     */
    private View[] childs;
    /**
     * Task per definire il momento in cui la ScrollView si ferma
     */
    private Runnable scrollerTask;
    /**
     * Scroll avvenuto quando la ScrollView si ferma
     */
    private int initialPosition;
    /**
     * centro reale della ScrollView
     */
    private int mScrollCenter;
    /**
     * può essere sia una ScrollView che un HorizontalScrollView
     */
    private ViewGroup scrollView;
    @ScrollOrientation
    private int orientation;

    private SnapManager(ViewGroup scrollView) {
        this.scrollView = scrollView;
        if (scrollView instanceof ScrollView) {
            orientation = VERTICAL;
        } else {
            orientation = HORIZONTAL;
        }
        init();
    }

    /**
     * Metodo che aggiunge la funzionalità di auto center alla ScrollView
     *
     * @param scrollView ScrollView verticale
     */
    public static void addTo(ScrollView scrollView) {
        new SnapManager(scrollView);
    }

    /**
     * Metodo che aggiunge la funzionalità di auto center alla ScrollView
     *
     * @param scrollView ScrollView orizzontale
     */
    public static void addTo(HorizontalScrollView scrollView) {
        new SnapManager(scrollView);
    }

    /**
     * Inizializzazione dell'auto center
     */
    private void init() {
        // aggiunge il listener per lo scroll
        scrollView.setOnTouchListener(this);
        // task che definisce quando la ScrollView si ferma
        scrollerTask = new Runnable() {
            public void run() {
                int newPosition = orientation == VERTICAL ? scrollView.getScrollY() : scrollView.getScrollX();
                if (initialPosition - newPosition == 0) {
                    // la ScrollView si è fermata
                    onScrollStopped();
                } else {
                    startScrollerTask();
                }
            }
        };

        // tutti i parametri del layout vengono ottenuti in un layout listener
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    /**
     * metodo che manda in run il task di aggiornamento per capire la condizione della ScrollView
     */
    private void startScrollerTask() {
        initialPosition = orientation == VERTICAL ? scrollView.getScrollY() : scrollView.getScrollX();
        scrollView.postDelayed(scrollerTask, NEW_CHECK);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // il task di aggiornamento deve iniziare quando il dito si solleva
        if (event.getAction() == MotionEvent.ACTION_UP) {
            startScrollerTask();
        }
        return false;
    }

    @Override
    public void onGlobalLayout() {
        // detach del layout listener
        scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

        // il centro della scrollview viene ottenuto facendo la media tra i suoi margini (top e bottom se verticale, left e right se orizzontale)
        if (mScrollCenter == 0) {
            mScrollCenter = orientation == VERTICAL ?
                    (scrollView.getTop() + scrollView.getBottom()) / 2 :
                    (scrollView.getLeft() + scrollView.getRight()) / 2;

            View directChild = scrollView.getChildAt(0);
            // se la ScrollView non ha child oppure il child non è un container allora non c'è bisogno di questa funzionalità
            if (directChild != null && directChild instanceof ViewGroup) {
                ViewGroup childGroup = (ViewGroup) directChild;

                // per lo snap sono necessari di più child
                int childCount = childGroup.getChildCount();
                if (childCount > 1) {
                    // popola l'array dei child
                    childs = new View[childCount];

                    for (int i = 0; i < childCount; i++) {
                        childs[i] = childGroup.getChildAt(i);
                    }
                }
            }
        }
    }

    /**
     * Metodo che definisce cosa fare quando la ScrollView è ferma
     */
    private void onScrollStopped() {
        // l'array deve essere popolato
        if (childs != null) {
            // pivot ipotetico della ScrollView basato sulla sua larghezza che cambia rispetto al numero dei contenuti
            final int scrollPivot = initialPosition + mScrollCenter;
            Log.d(TAG, "scrollPivot: " + String.valueOf(scrollPivot));

            for (View currentChild : childs) {
                // serve per capire quale view "contiene" il centro della ScrollView
                if (orientation == VERTICAL) {
                    if (currentChild.getTop() <= scrollPivot && currentChild.getBottom() >= scrollPivot) {
                        // centro della view
                        final int childPivot = (currentChild.getTop() + currentChild.getBottom()) / 2;
                        Log.d(TAG, "childPivot: " + String.valueOf(childPivot));
                        // scroll sul delta Y tra il pivot della ScrollView e il pivot del child
                        // con un delta Y positivo la ScrollView si sposta verso il basso, quindi il contenuto verso l'alto
                        // con un delta Y negativo la ScrollView si sposta verso l'alto, quindi il contenuto verso il basso
                        ((ScrollView) scrollView).smoothScrollBy(0, childPivot - scrollPivot);
                    }
                } else {
                    if (currentChild.getLeft() <= scrollPivot && currentChild.getRight() >= scrollPivot) {
                        // centro della view
                        final int childPivot = (currentChild.getRight() + currentChild.getLeft()) / 2;
                        Log.d(TAG, "childPivot: " + String.valueOf(childPivot));
                        // scroll sul delta X tra il pivot della ScrollView e il pivot del child
                        // con un delta X positivo la ScrollView si sposta verso destra, quindi il contenuto verso sinistra
                        // con un delta X negativo la ScrollView si sposta verso sinistra, quindi il contenuto verso destra
                        ((HorizontalScrollView) scrollView).smoothScrollBy(childPivot - scrollPivot, 0);
                    }
                }
            }
        }
    }

    /**
     * Definisce l'orientation della ScrollView
     */
    @IntDef({VERTICAL, HORIZONTAL})
    @Retention(RetentionPolicy.SOURCE)
    private @interface ScrollOrientation {
    }
}