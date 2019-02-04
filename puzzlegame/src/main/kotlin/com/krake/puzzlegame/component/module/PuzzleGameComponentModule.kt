package com.krake.puzzlegame.component.module

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.krake.core.component.base.ComponentModule
import com.krake.core.component.module.ThemableComponentModule
import com.krake.core.extension.getClass
import com.krake.core.extension.putClass
import com.krake.puzzlegame.GameActivity
import com.krake.puzzlegame.PuzzleGameSelectionActivity

/**
 * Modulo utilizzato per specificare gli attributi di un puzzle game.
 * Utilizzato principalmente da [PuzzleGameSelectionActivity] e dalle [Activity] che la estendono.
 */
class PuzzleGameComponentModule : ComponentModule {
    companion object {
        private const val ARG_GAME_ACTIVITY = "argGameActivityClass"
    }

    var gameActivity: Class<out GameActivity>
        private set

    init {
        gameActivity = GameActivity::class.java
    }

    /**
     * Specifica la classe per mostrare l'effettiva sezione di gioco.
     * DEFAULT: [GameActivity]
     *
     * @param gameActivity [Activity] che estende [GameActivity] usata per la sezione del gioco.
     */
    fun gameActivity(gameActivity: Class<out GameActivity>) = apply { this.gameActivity = gameActivity }

    /**
     * Legge il contenuto di un [Bundle] e modifica le sue proprietà.
     *
     * @param context il [Context] utilizzato per leggere il [Bundle].
     * @param bundle container dal quale vengono letti i vari arguments.
     */
    override fun readContent(context: Context, bundle: Bundle) {
        gameActivity = bundle.getClass(ARG_GAME_ACTIVITY, gameActivity)!!
    }

    /**
     * Scrive le proprietà di un modulo su un [Bundle].
     *
     * @param context il [Context] utilizzato per creare il [Bundle] e scrivere su di esso.
     * @return container che contiene le proprietà del modulo corrente.
     */
    override fun writeContent(context: Context): Bundle {
        val bundle = Bundle()
        bundle.putClass(ARG_GAME_ACTIVITY, gameActivity)
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
}