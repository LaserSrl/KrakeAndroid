package com.krake.core.os;

import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;

/**
 * Permette di referenziare un oggetto in modo weak per evitare il memory leak sulla coda d'esecuzione.
 */
public abstract class WeakRunnable<T> implements Runnable {
    private WeakReference<T> mRef;

    /**
     * Crea una nuova istanza di {@link WeakRunnable}
     *
     * @param referred oggetto da referenziare in modo weak
     */
    public WeakRunnable(@NonNull T referred) {
        mRef = new WeakReference<>(referred);
    }

    @Override
    public final void run() {
        T referred = mRef.get();
        if (referred != null) {
            // se l'oggetto non è stato deallocato dal GC, allora il metodo viene eseguito
            runWithReferred(referred);
        }
    }

    /**
     * Questo metodo viene richiamato solo nel caso in cui l'oggetto referenziato non sia stato deallocato
     *
     * @param referred oggetto referenziato in modo weak
     */
    public abstract void runWithReferred(@NonNull T referred);
}