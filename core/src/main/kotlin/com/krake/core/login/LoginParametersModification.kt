package com.krake.core.login

/**
 * Protocollo che deve implementare l'Application per
 * Created by joel on 04/08/17.
 */
interface LoginParametersModification {
    val loginProviders: Array<String>

    val enableRegistration: Boolean
}