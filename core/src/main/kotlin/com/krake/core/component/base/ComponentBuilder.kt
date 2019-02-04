package com.krake.core.component.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle

/**
 * Classe per generare un nuovo componente che conterrà un [Bundle] con i vari [ComponentModule] serializzati.
 * @param Builder builder che estende il [ComponentBuilder] per avere il tipo di ritorno dei metodi base
 * automaticamente castato al tipo della sottoclasse.
 * @param Model tipo di ritorno del metodo [build].
 */
@Suppress("UNCHECKED_CAST")
abstract class ComponentBuilder<Builder : ComponentBuilder<Builder, Model>, Model> {
    private val bundle: Bundle = Bundle()
    protected var context: Context? = null
        private set

    /**
     * Specifica il [Context] utilizzato per creare il [Bundle].
     * Il [Context] è necessario, quindi, nel caso in cui il [Context] non venga settato, verrà lanciata una [IllegalArgumentException].
     *
     * @return istanza di [Builder] creata in precedenza.
     */
    fun from(context: Context): Builder {
        this.context = context
        return this as Builder
    }

    /**
     * Aggiunge al [Bundle] tutti i [ComponentModule] serializzati.
     *
     * @param modules vararg di moduli che verranno serializzati
     * @return istanza di [Builder] creata in precedenza.
     * @see ComponentManager.createBundleWithModules
     */
    fun with(vararg modules: ComponentModule): Builder {
        bundle.putAll(ComponentManager.createBundleWithModules(context!!, *modules))
        return this as Builder
    }

    /**
     * Aggiunge al [Bundle] un altro [Bundle] copiando tutti gli arguments del secondo all'interno del primo.
     *
     * @param bundle [Bundle] da copiare nel [Bundle] principale
     * @return istanza di [Builder] creata in precedenza.
     */
    fun put(bundle: Bundle?): Builder {
        if (bundle != null) {
            this.bundle.putAll(bundle)
        }
        return this as Builder
    }

    /**
     * Permette modifiche aggiuntive al [Bundle] utilizzato correntemente dal [ComponentBuilder].
     * Sul [Bundle] si può effettuare qualsiasi modifica, come aggiungere o rimuovere arguments o settare un custom [ClassLoader].
     * L'aggiunta degli extra con il metodo [transform] è sconsigliata perché può sovrascrivere dei comportamenti di default che sono ritenuti necessari.
     * Utilizzare invece il metodo [put] oppure creare un [ComponentModule] custom.
     *
     * @param bundleClosure closure che permette le modifiche sul [Bundle] principale passato come parametro.
     * @return istanza di [Builder] creata in precedenza.
     */
    fun transform(bundleClosure: (Bundle) -> Unit): Builder {
        bundleClosure.invoke(bundle)
        return this as Builder
    }

    /**
     * Verifica le condizioni necessarie prima che venga richiamato il metodo [build].
     * Permette di lanciare delle eccezioni nel caso in cui alcuni campi necessari siano mancanti.
     */
    protected open fun validateProperties() {
        if (context == null) {
            throw IllegalArgumentException("You must specify a Context.")
        }
    }

    /**
     * Dopo aver validato le proprietà del modulo, crea il componente di tipo [Model]
     * utilizzando il [Bundle] generato con i metodi del [ComponentBuilder].
     */
    fun build(): Model {
        validateProperties()
        return buildWithBundle(bundle)
    }

    /**
     * Gestisce la creazione del componente di tipo [Model] utilizzando il [Bundle] generato con i metodi del [ComponentBuilder].
     */
    protected abstract fun buildWithBundle(bundle: Bundle): Model
}

/**
 * Classe per generare un nuovo [Bundle] che conterrà un [Bundle] con i vari [ComponentModule] serializzati.
 */
class BundleBuilder internal constructor() : ComponentBuilder<BundleBuilder, Bundle>() {

    /**
     * Gestisce la creazione del [Bundle] utilizzando il [Bundle] generato con i metodi del [ComponentBuilder].
     */
    override fun buildWithBundle(bundle: Bundle): Bundle {
        return bundle
    }
}

/**
 * Classe per generare un nuovo [Intent] che conterrà un [Bundle] con i vari [ComponentModule] serializzati.
 */
class IntentBuilder internal constructor() : ComponentBuilder<IntentBuilder, Intent>() {
    private var destination: Class<out Activity>? = null

    /**
     * Specifica la classe dell'[Activity] di destinazione.
     * La classe dell'[Activity] di destinazione è necessaria, quindi, nel caso in cui la classe non venga settata, verrà lanciata una [IllegalArgumentException].
     *
     * @return istanza di [IntentBuilder] creata in precedenza.
     */
    fun to(destination: Class<out Activity>): IntentBuilder {
        this.destination = destination
        return this
    }

    /**
     * Verifica le condizioni necessarie prima che venga richiamato il metodo [build].
     * Permette di lanciare delle eccezioni nel caso in cui alcuni campi necessari siano mancanti.
     */
    override fun validateProperties() {
        super.validateProperties()
        if (destination == null) {
            throw IllegalArgumentException("You must specify an Activity as destination.")
        }
    }

    /**
     * Gestisce la creazione dell'[Intent] utilizzando il [Bundle] generato con i metodi del [ComponentBuilder].
     */
    override fun buildWithBundle(bundle: Bundle): Intent {
        val intent = Intent(context, destination)
        /* Gli extras del Bundle vengono copiati all'interno dell'Intent */
        intent.putExtras(bundle)
        return intent
    }
}