package com.krake.core.component.module

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.krake.core.app.LoginAndPrivacyActivity
import com.krake.core.component.base.ComponentModule
import com.krake.core.component.module.ThemableComponentModule
import java.util.*

/**
 * Modulo utilizzato per specificare la presenza del form di login.
 * Utilizzato principalmente da [LoginAndPrivacyActivity] e dalle [Activity] che la estendono.
 */
class LoginComponentModule : ComponentModule {
    companion object {
        const val ARG_LOGIN_REQUIRED = "LoginRequired"
    }

    var loginRequired: Boolean
        private set

    val valueListeners: MutableList<ValueListener>

    init {
        loginRequired = false
        valueListeners = LinkedList()
    }

    /**
     * Specifica se il contenuto è accessibile solo dopo aver effettuato l'accesso.
     * DEFAULT: false
     *
     * @param loginRequired true se il contenuto deve essere disponibile solo dopo aver effettuato l'accesso.
     * In [LoginAndPrivacyActivity] verrà presentato il form di login.
     */
    fun loginRequired(loginRequired: Boolean) = apply {
        this.loginRequired = loginRequired
        valueListeners.forEach { it.onLoginModuleValueChanged() }
    }

    /**
     * Legge il contenuto di un [Bundle] e modifica le sue proprietà.
     *
     * @param context il [Context] utilizzato per leggere il [Bundle].
     * @param bundle container dal quale vengono letti i vari arguments.
     */
    override fun readContent(context: Context, bundle: Bundle) {
        loginRequired = bundle.getBoolean(ARG_LOGIN_REQUIRED, loginRequired)
    }

    /**
     * Scrive le proprietà di un modulo su un [Bundle].
     *
     * @param context il [Context] utilizzato per creare il [Bundle] e scrivere su di esso.
     * @return container che contiene le proprietà del modulo corrente.
     */
    override fun writeContent(context: Context): Bundle {
        val bundle = Bundle()
        bundle.putBoolean(ARG_LOGIN_REQUIRED, loginRequired)
        return bundle
    }

    /**
     * Definisce la lista delle classi da cui questo modulo deve dipendere.
     *
     * @return array di dipendenze.
     */
    override fun moduleDependencies(): Array<Class<out ComponentModule>> {
        return arrayOf(ThemableComponentModule::class.java)
    }

    interface ValueListener {
        fun onLoginModuleValueChanged()
    }
}