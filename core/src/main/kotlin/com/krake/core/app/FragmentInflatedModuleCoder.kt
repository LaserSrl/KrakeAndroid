package com.krake.core.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.support.annotation.StyleableRes
import android.support.v4.app.Fragment
import android.util.AttributeSet
import com.krake.core.component.base.ComponentModule

/**
 * Classe che permette di gestire il collegamento tra un [ComponentModule] e un [Fragment].
 * E' necessario perché il metodo [Fragment.onInflate] viene richiamato prima di [Fragment.onAttach].
 *
 * @param Module il tipo del [ComponentModule] nel quale verranno copiati gli attributi.
 * @param styleableRes risorsa relativa allo style del [Fragment].
 * @constructor crea un nuovo [FragmentInflatedModuleCoder].
 */
abstract class FragmentInflatedModuleCoder<in Module : ComponentModule>(@StyleableRes val styleableRes: IntArray) {
    private var attrs: TypedArray? = null

    /**
     * Permette di salvare gli attributi letti da un [Fragment] quando viene inserito in XML.
     * Da richiamare nel metodo [Fragment.onInflate].
     *
     * @param context [Context] corrente.
     * @param attributeSet [AttributeSet] ricevuti dal [Fragment] dichiarato in XML.
     */
    fun readAttrs(context: Context, attributeSet: AttributeSet) {
        @SuppressLint("Recycle")
        attrs = context.obtainStyledAttributes(attributeSet, styleableRes, 0, 0)
    }

    /**
     * Permette di risolvere gli attributi salvati in precedenza e di scriverli su un [ComponentModule]
     * Da richiamare nel metodo [Fragment.onAttach] oppure [Fragment.onCreate].
     *
     * @param module [ComponentModule] sul quale verranno copiati gli attributi.
     */
    fun writeAttrs(module: Module) {
        attrs?.let {
            copyToModule(attrs!!, module)
            attrs!!.recycle()
        }
    }

    /**
     * Permette di copiare gli attributi risolti in precedenza in un [ComponentModule].
     * Non c'è bisogno di richiamare il metodo [TypedArray.recycle] perché verrà richiamato automaticamente.
     *
     * @param attrs attributi salvati in precedenza.
     * @param module [ComponentModule] sul quale verranno copiati gli attributi.
     */
    protected abstract fun copyToModule(attrs: TypedArray, module: Module)
}