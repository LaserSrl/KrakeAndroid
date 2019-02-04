package com.krake.core.component.module

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import com.krake.core.app.TermsFragment
import com.krake.core.component.base.ComponentModule
import com.krake.core.extension.getClass
import com.krake.core.extension.putClass

/**
 * Modulo utilizzato per specificare gli attributi del [TabLayout] quando vengono visualizzati contenuti di tipo [TermPart].
 * Utilizzato principalmente da [TermsFragment] e dai [Fragment] che lo estendono.
 */
class TermsModule : ComponentModule {
    companion object {
        val ARG_TERMS_FILTER_QUERY = "argTermFilterOnline"
        val ARG_SHOW_TAB_ALL = "argShowTabAll"
        val ARG_TERMS_FRAGMENT_CLASS = "argTermFragmentClass"
        val ARG_ALL_TAB_TERM_ID = "argAllTabTermId"
    }

    var filterQueryString: Boolean
        private set

    var showTabAll: Boolean
        private set

    var termsFragmentClass: Class<out TermsFragment>
        private set

    /*+
    Identificativo da passare alla chiamata corrispondente al tab tutti.
    Utile solo se showTabAll = true e filterQueryString = true
    Default: 0
     */
    var allTabTermId: Int
        private set

    init {
        filterQueryString = true
        showTabAll = true
        termsFragmentClass = TermsFragment::class.java
        allTabTermId = 0
    }

    /**
     * Specifica se il filtro dopo aver premuto su un altro tab deve essere effettuato in query string
     * oppure con il [TermPart.getAutoroutePartDisplayAlias].
     * DEFAULT: true
     *
     * @param filterQueryString true se il filtro deve essere effettuato in query string,
     * false se deve essere effettuato con il [TermPart.getAutoroutePartDisplayAlias]
     */
    fun filterQueryString(filterQueryString: Boolean): TermsModule {
        this.filterQueryString = filterQueryString
        return this
    }

    /**
     * Specifica se deve essere visualizzato il tab di tutti gli elementi senza categorizzarli.
     * DEFAULT: true
     *
     * @param showTabAll true nel caso in cui si voglia visualizzare il tab che al tap farà visualizzare tutti gli elementi.
     */
    fun showTabAll(showTabAll: Boolean): TermsModule {
        this.showTabAll = showTabAll
        return this
    }

    /**
     * Specifica il [Fragment] che estende [TermsFragment] utilizzato all'interno dell'[Activity] per mostrare il tablayout.
     * DEFAULT: [TermsFragment]
     *
     * @param termsFragmentClass classe del [Fragment] che estende [TermsFragment] utilizzato nell'[Activity].
     */
    fun termsFragmentClass(termsFragmentClass: Class<out TermsFragment>): TermsModule {
        this.termsFragmentClass = termsFragmentClass
        return this
    }

    /**
     * Legge il contenuto di un [Bundle] e modifica le sue proprietà.
     *
     * @param context il [Context] utilizzato per leggere il [Bundle].
     * @param bundle container dal quale vengono letti i vari arguments.
     */
    override fun readContent(context: Context, bundle: Bundle) {
        filterQueryString = bundle.getBoolean(ARG_TERMS_FILTER_QUERY, filterQueryString)
        showTabAll = bundle.getBoolean(ARG_SHOW_TAB_ALL, showTabAll)
        termsFragmentClass = bundle.getClass(ARG_TERMS_FRAGMENT_CLASS, termsFragmentClass)!!
        allTabTermId = bundle.getInt(ARG_ALL_TAB_TERM_ID, 0)
    }

    /**
     * Scrive le proprietà di un modulo su un [Bundle].
     *
     * @param context il [Context] utilizzato per creare il [Bundle] e scrivere su di esso.
     * @return container che contiene le proprietà del modulo corrente.
     */
    override fun writeContent(context: Context): Bundle {
        val bundle = Bundle()
        bundle.putBoolean(ARG_TERMS_FILTER_QUERY, filterQueryString)
        bundle.putBoolean(ARG_SHOW_TAB_ALL, showTabAll)
        bundle.putClass(ARG_TERMS_FRAGMENT_CLASS, termsFragmentClass)
        bundle.putInt(ARG_ALL_TAB_TERM_ID, allTabTermId)
        return bundle
    }
}