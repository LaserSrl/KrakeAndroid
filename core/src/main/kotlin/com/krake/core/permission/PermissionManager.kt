package com.krake.core.permission

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.krake.core.extension.commitSyncAllowingStateLoss
import com.krake.core.permission.presenter.PermissionPresenter
import com.krake.core.permission.presenter.SnackbarPermissionPresenter


/**
 * Manager che gestisce la richiesta dei permessi tramite un builder pattern.
 * E' obbligatorio specificare i permessi con il metodo [PermissionManager.permissions]
 * Successivamente, bisogna richiamare il metodo [PermissionManager.create].
 *
 * @param activity [Activity] utilizzata per mostrare i messaggi relativi alla richiesta dei permessi.
 * @param fragmentManager support [FragmentManager] utilizzato per inserire il [Fragment] che gestirà la richiesta dei permessi.
 * @constructor crea un [PermissionManager] per la richiesta e il check dei permessi.
 */
open class PermissionManager constructor(protected val activity: Activity, protected var fragmentManager: FragmentManager) : PermissionFragment.Callback {

    /**
     * Crea un [PermissionManager] per la richiesta e il check dei permessi.
     *
     * @param activity [FragmentActivity] utilizzata per mostrare i messaggi relativi alla richiesta dei permessi e
     * dalla quale verrà ricavato il support [FragmentManager].
     */
    constructor(activity: FragmentActivity) : this(activity, activity.supportFragmentManager)

    /**
     * Crea un [PermissionManager] per la richiesta e il check dei permessi.
     *
     * @param fragment [Fragment] dal quale vengono ricavati l'[Activity] utilizzata per mostrare i messaggi relativi alla
     * richiesta dei permessi e il child [FragmentManager].
     */
    constructor(fragment: Fragment) : this(fragment.activity ?:
            throw IllegalArgumentException("The activity mustn't be null"), fragment.childFragmentManager)

    companion object {

        /**
         * Verifica che tutti i permessi siano stati accettati.
         *
         * @param context [Context] utilizzato per verificare i permessi.
         * @param permissions lista di permessi da verificare.
         * @return true se tutti i permessi sono stati accettati.
         */
        @JvmStatic
        fun areGranted(context: Context, vararg permissions: String): Boolean {
            permissions.forEach {
                if (ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED)
                    return false
            }
            return true
        }

        /**
         * Verifica che una lista di permessi contenga i due permessi necessari per la localizzazione.
         *
         * @param permissions lista di permessi in cui cercare.
         * @return true se la lista dei permessi contiene entrambi i permessi della localizzazione.
         */
        @JvmStatic
        fun containLocationPermissions(permissions: Array<out String>): Boolean {
            return permissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION) && permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        /**
         * Verifica che una lista di permessi contenga i due permessi necessari per la localizzazione.
         *
         * @param permissions lista di permessi in cui cercare.
         * @return true se la lista dei permessi contiene entrambi i permessi della localizzazione.
         */
        @JvmStatic
        fun locationPermissionsGranted(context: Context): Boolean {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).forEach {
                if (ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED)
                    return false
            }
            return true
        }
    }

    protected var listeners: MutableSet<PermissionListener> = HashSet()
        private set
    protected var permanentlyRefusedMsg: String? = null
        private set
    protected lateinit var permissions: Array<out String>
        private set
    protected var presenter: PermissionPresenter = SnackbarPermissionPresenter()
        private set
    protected var rationalMsg: String? = null
        private set

    private var fragment: PermissionFragment? = null

    /**
     * Specifica il messaggio che deve essere mostrato quando i permessi sono stati rifiutati in modo permanente.
     * DEFAULT: null
     *
     * @param msg messaggio da mostrare oppure null per non mostrare nulla.
     */
    fun permanentlyRefusedMsg(msg: String?) = apply { permanentlyRefusedMsg = msg }

    /**
     * Specifica i permessi che devono essere richiesti.
     * DEFAULT: nessun permesso (almeno uno è necessario)
     *
     * @param permissions permessi da richiedere.
     */
    fun permissions(vararg permissions: String) = apply { this.permissions = permissions }

    /**
     * Specifica l'oggetto che si occuperà di mostrare i permessi sulla UI.
     * L'oggetto deve implementare [PermissionPresenter].
     * DEFAULT: [SnackbarPermissionPresenter]
     *
     * @param presenter [PermissionPresenter] che mostra i permessi sulla UI.
     */
    fun presenter(presenter: PermissionPresenter) = apply { this.presenter = presenter }

    /**
     * Specifica il messaggio che deve essere mostrato quando deve essere mostrata una spiegazione sull'utilizzo dei permessi.
     * DEFAULT: null
     *
     * @param msg messaggio da mostrare oppure null per non mostrare nulla.
     */
    fun rationalMsg(msg: String?) = apply { rationalMsg = msg }

    /**
     * Aggiunge un listener per gli eventi sui permessi.
     *
     * @param listener [PermissionListener] listener da aggiungere.
     */
    fun addListener(listener: PermissionListener) = apply { listeners.add(listener) }

    /**
     * Aggiunge un listener che si occuperà di invokare la onHandled passata
     *
     * @param onHandled verrà richiamata dal [PermissionListener].
     */
    fun addListener(onHandled: (Array<out String>) -> Unit) = apply {
        listeners.add(object : PermissionListener
                      {
                          override fun onPermissionsHandled(acceptedPermissions: Array<out String>)
                          {
                              onHandled.invoke(acceptedPermissions)
                          }
                      })
    }

    /**
     * Rimuove un listener registrato per gli eventi sui permessi.
     *
     * @param listener [PermissionListener] listener da rimuovere.
     */
    fun removeListener(listener: PermissionListener) = apply { listeners.remove(listener) }

    /**
     * Rimuove tutti i listener registrati per gli eventi sui permessi.
     */
    fun removeAllListeners() = apply { listeners.clear() }

    /**
     * Crea il [PermissionFragment] e lo inserisce con il [FragmentManager] passato come parametro.
     * Sotto le api 23, il [PermissionFragment] non viene istanziato.
     */
    @SuppressLint("CommitTransaction")
    open fun create() = apply {
        if (permissions.isEmpty())
            throw IllegalArgumentException("You must request at least one permission.")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val tag = PermissionFragment.FRAGMENT_TAG(permissions)
            fragment = fragmentManager.findFragmentByTag(tag) as? PermissionFragment
            if (fragment == null) {
                fragment = permissionFragment()
                fragmentManager.beginTransaction()
                    .add(fragment!!, tag)
                        .commitSyncAllowingStateLoss(fragmentManager)
            }
            fragment?.callback = this
        }
    }

    /**
     * Verifica che tutti i permessi siano stati accettati.
     *
     * @return true se tutti i permessi sono stati accettati.
     */
    open fun areGranted(): Boolean {
        return areGranted(activity, *permissions)
    }

    /**
     * Richiede i permessi se non sono stati accettati, altrimenti notifica i [PermissionListener] registrati.
     */
    @SuppressLint("NewApi")
    open fun request() {
        if (areGranted()) {
            dispatchPermissionsAccepted(permissions)
        } else {
            fragment?.askPermissions()
        }
    }

    override fun onPermissionsHandled(permissionFragment: PermissionFragment, acceptedPermissions: Array<out String>)
    {
        dispatchPermissionsAccepted(acceptedPermissions)
    }

    override fun showRationalMessage(permissionFragment: PermissionFragment, message: String) {
        presenter.showRationalMessage(activity, permissionFragment, message)
    }

    override fun showPermanentlyDeniedMessage(permissionFragment: PermissionFragment, message: String) {
        presenter.showPermanentlyDeniedMessage(activity, permissionFragment, message)
    }

    /**
     * Notifica i [PermissionListener] registrati che sono stati accettati i permessi.
     */
    protected open fun dispatchPermissionsAccepted(permissions: Array<out String>) {
        listeners.forEach {
            it.onPermissionsHandled(permissions)
        }
    }

    /**
     * Permette di sovrascrivere il [PermissionFragment] che deve essere utilizzato per la richiesta dei permessi.
     *
     * @return istanza di [PermissionFragment] da creare.
     */
    protected open fun permissionFragment(): PermissionFragment {
        return PermissionFragment.newInstance(permissions, rationalMsg, permanentlyRefusedMsg)
    }
}