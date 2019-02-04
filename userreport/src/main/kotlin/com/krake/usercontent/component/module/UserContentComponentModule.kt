package com.krake.usercontent.component.module

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TabLayout
import android.view.MenuItem
import com.google.gson.Gson
import com.krake.contentcreation.ContentCreationActivity
import com.krake.core.app.ContentItemDetailActivity
import com.krake.core.component.base.ComponentModule
import com.krake.core.component.module.LoginComponentModule
import com.krake.core.extension.putModules
import com.krake.usercontent.R
import com.krake.usercontent.UserContentTab
import com.krake.usercontent.UserCreatedContentActivity
import com.krake.usercontent.model.UserCreatedContent
import com.krake.usercontent.widget.UserReportAdapter
import com.krake.usercontent.widget.UserReportHolder

/**
 * Modulo utilizzato per specificare gli attributi di un'[Activity] che mostra oggetti di tipo [UserCreatedContent].
 * Utilizzato principalmente da [UserCreatedContentActivity] e dalle [Activity] che la estendono.
 */
class UserContentComponentModule(context: Context) : ComponentModule {
    companion object {
        private const val ARG_HELP_DETAIL_BUNDLE = "argHelpModule"
        private const val ARG_CONTENT_CREATION_BUNDLE = "argNewContentBundle"
        private const val ARG_TABS = "argTabs"

        /**
         * Classe che può essere usata come view holder di base per gli elementi di tipo [UserCreatedContent].
         */
        val DEFAULT_LIST_VIEW_HOLDER_CLASS = UserReportHolder::class.java

        /**
         * Classe che può essere usata come adapter di base per gli elementi di tipo [UserCreatedContent].
         */
        val DEFAULT_LIST_ADAPTER_CLASS = UserReportAdapter::class.java

        /**
         * Layout di una cella base per gli elementi di tipo [UserCreatedContent].
         */
        @LayoutRes
        val DEFAULT_LIST_CELL_LAYOUT = R.layout.user_created_content_cell
    }

    var contentCreationBundle: Bundle?
        private set

    var helpDetailBundle: Bundle?
        private set

    private var contentCreationModules: Array<out ComponentModule>?
    private var helpDetailModules: Array<out ComponentModule>?

    var tabs: Array<out UserContentTab>
        private set

    private val gson: Gson = Gson()

    init {
        contentCreationBundle = null
        contentCreationModules = null
        helpDetailBundle = null
        helpDetailModules = null
        tabs = arrayOf(UserContentTab.createAllReportsTab(context), UserContentTab.createUserReportsTab(context))
    }

    /**
     * Specifica i [ComponentModule] che verranno passati ad una [ContentItemDetailActivity]
     * per mostrare il regolamento o aiuto per questa sezione.
     * In [UserCreatedContentActivity] verrà mostrato un [MenuItem] per aprire la nuova schermata
     * nel caso in cui [helpDetailBundle] sia diverso da null.
     * DEFAULT: null
     *
     * @param helpDetailModule varargs di moduli che verranno passati alla [ContentItemDetailActivity].
     */
    fun helpDetailModules(vararg helpDetailModule: ComponentModule) = apply { this.helpDetailModules = helpDetailModule }

    /**
     * Specifica i [ComponentModule] che verranno passati ad una [ContentCreationActivity]
     * per creare un nuovo contenuto o modificarne uno esistente.
     * In [UserCreatedContentActivity] verrà mostrato un [FloatingActionButton] per aprire la nuova schermata
     * nel caso in cui [contentCreationBundle] sia diverso da null.
     * DEFAULT: null
     *
     * @param contentCreationModule varargs di moduli che verranno passati alla [ContentCreationActivity].
     */
    fun contentCreationModules(vararg contentCreationModule: ComponentModule) = apply { this.contentCreationModules = contentCreationModule }

    /**
     * Specifica i tabs che dovranno essere visualizzati nel [TabLayout] di [UserCreatedContentActivity].
     * I tabs devono essere di tipo [UserContentTab] specificando il path da richiamare sul cambio tab.
     * DEFAULT: array formato da [UserContentTab.createAllReportsTab] e [UserContentTab.createUserReportsTab].
     *
     * @param tabs varargs di [UserContentTab] che verranno passati alla [ContentCreationActivity].
     */
    fun tabs(vararg tabs: UserContentTab) = apply { this.tabs = tabs }

    /**
     * Legge il contenuto di un [Bundle] e modifica le sue proprietà.
     *
     * @param context il [Context] utilizzato per leggere il [Bundle].
     * @param bundle container dal quale vengono letti i vari arguments.
     */
    override fun readContent(context: Context, bundle: Bundle) {
        helpDetailBundle = bundle.getBundle(ARG_HELP_DETAIL_BUNDLE)
        contentCreationBundle = bundle.getBundle(ARG_CONTENT_CREATION_BUNDLE)
        tabs = gson.fromJson(bundle.getString(ARG_TABS), Array<out UserContentTab>::class.java)
    }

    /**
     * Scrive le proprietà di un modulo su un [Bundle].
     *
     * @param context il [Context] utilizzato per creare il [Bundle] e scrivere su di esso.
     * @return container che contiene le proprietà del modulo corrente.
     */
    override fun writeContent(context: Context): Bundle {
        val bundle = Bundle()
        helpDetailModules?.let {
            bundle.putModules(context, ARG_HELP_DETAIL_BUNDLE, *helpDetailModules!!)
        }
        contentCreationModules?.let {
            bundle.putModules(context, ARG_CONTENT_CREATION_BUNDLE, *contentCreationModules!!)
        }

        bundle.putString(ARG_TABS, gson.toJson(tabs, Array<out UserContentTab>::class.java))
        return bundle
    }

    /**
     * Definisce la lista delle classi da cui questo modulo deve dipendere.
     *
     * @return array di dipendenze.
     */
    override fun moduleDependencies(): Array<Class<out ComponentModule>> {
        return arrayOf(LoginComponentModule::class.java)
    }
}