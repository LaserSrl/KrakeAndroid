package com.krake.core.login

import android.arch.lifecycle.Observer

/**
 * Classe usata per essere notificata che l'utente ha eseguito il logout.
 * Utile per cancellare sessioni di autenticazione dei social, che potrebbero causare un riaccesso
 * automatico col social.
 */

interface LogoutListener : Observer<Boolean>
{
}