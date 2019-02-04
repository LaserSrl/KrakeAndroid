package com.krake.beacons

import android.content.Context
import java.util.*

/**
 * Interfaccia per astrarre la gesitone dei beacon senza dover fare riferimento ad un'implementazione
 * specifica di uso dei beacon.
 * Il costruttore dovrebbe avere un parametro di tipo [Region].
 * Si basa su pattern observable per permettere di gestire le chiamate dall'lesterno.
 * L'implementazione deve inviare i messaggi come specifcare [BeaconEvent]
 * Created by joel on 21/04/16.
 */
interface BeaconManager {

    val isMonitoring: Boolean

    /**
     * Avvia la ricerca della region principale
     * Il context dell'App per verificare se sono presenti i permessi necessari,
     * ritorna true se è stato possibile avviare, false se mancano i permessi
     */
    fun startRegionMonitoring(context: Context?): Boolean

    /**
     * Ferma il monitoring
     */
    fun stopRegionMonitoring()

    /**
     * Riferimento ad un'implemntazione del pattern observable.

     * @param observer
     */
    fun addObserver(observer: Observer)

    fun deleteObserver(observer: Observer)

    fun isRegionMain(region: Region): Boolean

    /**
     * Avvia il ranging nella regione
     * @param region
     */
    fun startRangingInRegion(region: Region)

    /**
     * Stop ranging nella regione
     * @param region
     */
    fun stopRangingInRegion(region: Region)

    @Deprecated("Use startRegionMonitoring(context: Context?)")
    fun startRegionMonitoring() {
        startRegionMonitoring(null)
    }
}
