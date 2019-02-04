package com.krake.core.component.base

import android.content.Context
import android.os.Bundle

/**
 * Definisce un oggetto utilizzato per il passaggio di dati tra due componenti di Android.
 * Il container dei dati è un [Bundle].
 * Il modulo deve specificare sia come effettuare la lettura da un [Bundle] sia come scrivere su di esso.
 * Le classi che implementano [ComponentModule], per essere risolte con [ComponentManager.resolveBundle],
 * devono avere almeno uno tra questi due costruttori:
 * <ul>
 * <li>costruttore senza parametri</li>
 * <li>costruttore con solo un parametro di tipo [Context]</li>
 * </ul>
 * Il costruttore con il [Context] ha la priorità rispetto a quello senza parametri.
 */
interface ComponentModule {

    /**
     * Legge il contenuto di un [Bundle] e modifica le sue proprietà.
     *
     * @param context il [Context] utilizzato per leggere il [Bundle].
     * @param bundle container dal quale vengono letti i vari arguments.
     */
    fun readContent(context: Context, bundle: Bundle)

    /**
     * Scrive le proprietà di un modulo su un [Bundle].
     *
     * @param context il [Context] utilizzato per creare il [Bundle] e scrivere su di esso.
     * @return container che contiene le proprietà del modulo corrente.
     */
    fun writeContent(context: Context): Bundle

    /**
     * Definisce la lista delle classi da cui questo modulo deve dipendere.
     *
     * @return array di dipendenze.
     */
    fun moduleDependencies(): Array<Class<out ComponentModule>> {
        return emptyArray()
    }
}