package com.krake.core.location.settings

/**
 * Listener che viene notificato sui cambiamenti delle impostazioni del GPS.
 */
interface GpsSettingsListener {

    /**
     * Specifica le azioni da effettuare quando l'utilizzo del GPS è stato approvato ed
     * il GPS è stato acceso con successo.
     */
    fun onGpsSettingsAcquired()

    /**
     * il GPS non è stato acceso o non è possibile accenderlo.
     */
    fun onGpsSettingsUnavailable()
}