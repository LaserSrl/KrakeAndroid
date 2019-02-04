package com.krake.core.permission

/**
 * Listener che viene notificato sui cambiamenti dei permessi.
 */
interface PermissionListener {

    /**
     * Specifica le azioni da effettuare quando alcuni permessi sono stati accettati.
     *
     * @param acceptedPermissions lista di permessi che sono stati accettati.
     */
    fun onPermissionsHandled(acceptedPermissions: Array<out String>)
}