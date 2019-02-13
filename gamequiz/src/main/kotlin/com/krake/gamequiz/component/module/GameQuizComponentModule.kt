package com.krake.gamequiz.component.module

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.LayoutRes
import com.krake.core.app.ContentItemDetailActivity
import com.krake.core.component.base.ComponentModule
import com.krake.core.component.module.ListMapComponentModule
import com.krake.core.component.module.OrchardComponentModule
import com.krake.core.extension.putModules
import com.krake.gamequiz.GamesActivity
import com.krake.gamequiz.R

/**
 * Modulo utilizzato per specificare gli attributi di un'[Activity] che mostra oggetti di tipo [QuizGame].
 * Utilizzato principalmente da [GamesActivity] e dalle [Activity] che la estendono.
 */
class GameQuizComponentModule : ComponentModule {
    companion object {
        private const val ARG_HELP_DETAIL_BUNDLE = "argHelpDetailModule"

        /**
         * Layout di una cella base per gli elementi di tipo [QuizGame].
         */
        @LayoutRes
        val DEFAULT_LIST_CELL_LAYOUT = R.layout.game_cell

        /**
         * Content item part filters per scaricare solo le informazioni necessarie dei giochi.
         * @see [OrchardComponentModule.dataPartFilters]
         */
        const val DEFAULT_CONTENT_ITEM_PART_FILTERS = "TitlePart,AutoroutePart,Gallery,ActivityPart,GamePart"
    }

    var helpDetailBundle: Bundle?
        private set

    private var helpDetailModules: Array<out ComponentModule>?

    init {
        helpDetailBundle = null
        helpDetailModules = null
    }

    /**
     * Specifica i [ComponentModule] che verranno passati ad una [ContentItemDetailActivity]
     * per mostrare il regolamento o aiuto per questa sezione.
     * In [GamesActivity] verrà mostrato un [MenuItem] per aprire la nuova schermata
     * nel caso in cui [helpDetailBundle] sia diverso da null.
     * DEFAULT: null
     *
     * @param helpDetailModule varargs di moduli che verranno passati alla [ContentItemDetailActivity].
     */
    fun helpDetailModules(vararg helpDetailModule: ComponentModule) = apply { this.helpDetailModules = helpDetailModule }

    /**
     * Legge il contenuto di un [Bundle] e modifica le sue proprietà.
     *
     * @param context il [Context] utilizzato per leggere il [Bundle].
     * @param bundle container dal quale vengono letti i vari arguments.
     */
    override fun readContent(context: Context, bundle: Bundle) {
        helpDetailBundle = bundle.getBundle(ARG_HELP_DETAIL_BUNDLE)
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
        return bundle
    }

    /**
     * Definisce la lista delle classi da cui questo modulo deve dipendere.
     *
     * @return array di dipendenze.
     */
    override fun moduleDependencies(): Array<Class<out ComponentModule>> {
        return arrayOf(ListMapComponentModule::class.java)
    }
}