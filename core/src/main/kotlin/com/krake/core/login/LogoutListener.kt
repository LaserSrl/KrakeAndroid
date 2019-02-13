package com.krake.core.login

import androidx.lifecycle.Observer

/**
 * Classe usata per essere notificata che l'utente ha eseguito il logout.
 * Utile per cancellare sessioni di autenticazione dei social, che potrebbero causare un riaccesso
 * automatico col social.
 */

interface LogoutListener : Observer<Boolean>
{
}