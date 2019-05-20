package com.krake.facebook

import com.facebook.login.LoginManager
import com.krake.core.login.LogoutListener

/**
 * Classe per cancellare la sessione di Facebook quando l'utente esegue il logout.
 * La classe dev'essere inserita nella {@link KrakeApplication}
 */

class FacebookLogoutListener : LogoutListener {
    override fun onChanged(t: Boolean?)
    {
        if (t != null && !t && com.krake.core.login.LoginManager.shared.isLogged.value == true)
        {
            LoginManager.getInstance().logOut()
        }
    }
}