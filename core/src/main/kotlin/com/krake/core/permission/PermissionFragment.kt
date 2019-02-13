package com.krake.core.permission

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment

/**
 * Support [Fragment] che si occupa di richiedere i permessi runtime sopra le api 23.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
open class PermissionFragment : Fragment() {
    companion object {
        private val TAG = PermissionFragment::class.java.simpleName
        @JvmStatic
        protected val ARG_PERMANENTLY_REFUSED_MSG = "argPermanentlyRefusedMsg"
        @JvmStatic
        protected val ARG_PERMISSIONS = "argPermissions"
        @JvmStatic
        protected val ARG_RATIONAL_MSG = "argRationalMsg"
        @JvmStatic
        protected val REQUEST_CODE_PERMISSION = 986
        private val OUT_STATE_ASKING_PERMISSIONS = "otsAskingPerm"

        /**
         * Crea il tag del [Fragment] accodando tutti i permessi per avere un unico [Fragment]
         * per richiedere gli stessi permessi.
         */
        val FRAGMENT_TAG = { permissions: Array<out String> ->
            var tag = "PermFragment|"
            permissions.forEach { tag += it + '|' }
            tag
        }

        /**
         * Crea un nuovo [PermissionFragment] con un [Bundle].
         *
         * @param permissions lista di permessi da richiedere.
         * @param rationalMsg messaggio da mostrare come spiegazione per l'utilizzo dei permessi.
         * @param permanentlyRefusedMsg messaggio da mostrare nel caso in cui i permessi vengano negati in modo permanente.
         */
        fun newInstance(permissions: Array<out String>, rationalMsg: String? = null, permanentlyRefusedMsg: String? = null): PermissionFragment {
            val fragment = PermissionFragment()
            val args = Bundle()
            args.putString(ARG_PERMANENTLY_REFUSED_MSG, permanentlyRefusedMsg)
            args.putStringArray(ARG_PERMISSIONS, permissions)
            args.putString(ARG_RATIONAL_MSG, rationalMsg)
            fragment.arguments = args
            return fragment
        }
    }

    var callback: Callback? = null
    protected lateinit var permissions: Array<out String>
        private set
    protected var rationalMsg: String? = null
        private set
    protected var permanentlyRefusedMsg: String? = null
        private set
    private var askingPermissions = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askingPermissions = savedInstanceState?.getBoolean(OUT_STATE_ASKING_PERMISSIONS) ?: false
        val arguments = arguments ?: throw IllegalArgumentException("The arguments can't be null.")
        permanentlyRefusedMsg = arguments.getString(ARG_PERMANENTLY_REFUSED_MSG)
        permissions = arguments.getStringArray(ARG_PERMISSIONS)
        rationalMsg = arguments.getString(ARG_RATIONAL_MSG)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(OUT_STATE_ASKING_PERMISSIONS, askingPermissions)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "Called request permission result with code: $requestCode")
        if (requestCode != REQUEST_CODE_PERMISSION)
            return

        askingPermissions = false
        val acceptedPermissions = mutableListOf<String>()

        if (permissions.isNotEmpty() && permissions.size == grantResults.size)
        {
            var handleRefused = false
            grantResults.forEachIndexed { index, result ->
                if (result == PackageManager.PERMISSION_GRANTED) {
                    acceptedPermissions.add(permissions[index])
                } else {
                    handleRefused = true
                }
            }

            if (handleRefused) {
                if (shouldShowRequestPermissionRationale()) {
                    if (!rationalMsg.isNullOrEmpty()) {
                        showRationalMessage(rationalMsg!!)
                    }
                } else if (!permanentlyRefusedMsg.isNullOrEmpty()) {
                    Log.d(TAG, "permissions permanently denied")
                    callback?.showPermanentlyDeniedMessage(this, permanentlyRefusedMsg!!)
                }
            }
        }

        onPermissionsHandled(acceptedPermissions.toTypedArray())
    }

    /**
     * Questo metodo gestisce tre casi:
     * <ul>
     * <li>I permessi sono stati richiesti: notifica i listeners</li>
     * <li>I permessi non sono stati accettati e bisogna mostrare la spiegazione: mostra la ragione dell'utilizzo dei permessi</li>
     * <li>I permessi non sono stati accettati e non bisogna mostrare la spiegazione: richiede i permessi</li>
     * </ul>
     */
    open fun askPermissions() {
        val activity = activity
        if (!askingPermissions && activity != null) {
            if (!PermissionManager.areGranted(activity, *permissions)) {
                if (!rationalMsg.isNullOrEmpty() && shouldShowRequestPermissionRationale()) {
                    showRationalMessage(rationalMsg!!)
                } else {
                    requestPermissions()
                }
            } else {
                onPermissionsHandled(permissions)
            }
        }
    }

    /**
     * Richiede i permessi senza alcun check aggiuntivo.
     */
    fun requestPermissions() {
        askingPermissions = true
        Log.d(TAG, "requesting permissions")
        requestPermissions(permissions, REQUEST_CODE_PERMISSION)
    }

    /**
     * Specifica le azioni da effettuare quando i permessi sono stati accettati.
     */
    @CallSuper
    protected open fun onPermissionsHandled(acceptedPermissions: Array<out String>)
    {
        Log.d(TAG, "acceptedPermissions accepted")
        // Notifica il listener che i permessi sono stati accettati.
        callback?.onPermissionsHandled(this, acceptedPermissions)
    }

    /**
     * Verifica se bisogna mostrare la motivazione dei permessi.
     *
     * @return true se almeno un permesso deve mostrare la motivazione.
     */
    protected fun shouldShowRequestPermissionRationale(): Boolean {
        permissions.forEach {
            val show = shouldShowRequestPermissionRationale(it)
            if (show) {
                Log.d(TAG, "should show rational message for permission: $it")
                return true
            }
        }
        return false
    }

    /**
     * Mostra la ragione dei permessi.
     *
     * @param msg messaggio da mostrare.
     */
    protected fun showRationalMessage(msg: String) {
        Log.d(TAG, "showing rational message: $msg")
        callback?.showRationalMessage(this, msg)
    }

    /**
     * Listener che viene notificato quando ci sono delle modifiche ai permessi o c'è bisogno di mostrare un messaggio.
     */
    interface Callback {

        /**
         * Specifica le azioni da effettuare quando i permessi sono stati gestiti.
         *
         * @param permissionFragment [PermissionFragment] dal quale è partita la notifica.
         * @param acceptedPermissions lista di permessi che sono stati accettati.
         */
        fun onPermissionsHandled(permissionFragment: PermissionFragment, acceptedPermissions: Array<out String>)

        /**
         * Specifica come deve essere mostrato il messaggio che spiega la ragione dell'utilizzo dei permessi.
         *
         * @param permissionFragment [PermissionFragment] dal quale è partita la notifica.
         * @param message messaggio da mostrare.
         */
        fun showRationalMessage(permissionFragment: PermissionFragment, message: String)

        /**
         * Specifica come deve essere mostrato il messaggio quando i permessi sono stati negati in modo permanente.
         *
         * @param permissionFragment [PermissionFragment] dal quale è partita la notifica.
         * @param message messaggio da mostrare.
         */
        fun showPermanentlyDeniedMessage(permissionFragment: PermissionFragment, message: String)
    }
}