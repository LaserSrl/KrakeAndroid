package com.krake.core.component.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.util.ArrayMap
import android.util.Log
import com.krake.core.component.annotation.BundleResolvable
import com.krake.core.component.base.ComponentManager.resolveBundle
import com.krake.core.component.module.ThemableComponentModule
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.util.*

/**
 * Manager che gestisce la creazione di nuovi [Intent] e di nuovi [Bundle] tramite i vari [ComponentBuilder].
 * Per risolvere [Intent] e [Bundle] creati con il [ComponentManager], bisogna inserire un field globale public
 * che rappresenta il [ComponentModule] che è stato passato per [Intent] annotato con l'annotation [BundleResolvable].
 * Inoltre bisogna richiamare il metodo [resolveBundle] (o uno dei metodi che lo utilizzano) prima che si acceda per la prima volta al field dichiarato.
 *
 * Per esempio, se si passa nell'[Intent] un modulo di tipo [ThemableComponentModule],
 * nell'[Activity] che lo riceve, bisogna specificare il field in questo modo:
 * <ul>
 * <li>
 * Java:
 * <pre><code>
 * @BundleResolvable
 * public ThemableComponentModule themableComponentModule;
 * </code></pre>
 * </li>
 * <li>
 * Kotlin:
 * <pre><code>
 * @BundleResolvable
 * lateinit var themableComponentModule: ThemableComponentModule
 * </code></pre>
 * </li>
 * </ul>
 * e inserire nell'[Activity.onCreate]:
 * <ul>
 * <li>
 * Java:
 * <pre><code>
 * ComponentManager.INSTANCE.resolveIntent(this);
 * </code></pre>
 * </li>
 * <li>
 * Kotlin:
 * <pre><code>
 * ComponentManager.resolveIntent(this)
 * </code></pre>
 * </li>
 * </ul>
 *
 * La risoluzione delle annotation supporta l'ereditarietà delle classi.
 * Questo vuol dire che se si utilizza il metodo [resolveBundle] (o uno dei metodi che lo utilizzano) in una classe base,
 * le sottoclassi risolveranno i field annotati specificati in esse senza dover richiamare nuovamente il metodo [resolveBundle].
 * Le sottoclassi avranno a disposizione i moduli specificati in esse e tutti i moduli specificati nelle classi padre.
 */
object ComponentManager {
    private const val TAG = "ComponentManager"

    /* I fields annotati di tipo ComponentModule vengono cachati */
    private val cachedFields: ArrayMap<Class<*>, Array<Field>> = ArrayMap()

    /**
     * Crea un builder di tipo [BundleBuilder] per creare un nuovo [Bundle] con dei [ComponentModule].
     *
     * @return builder per la creazione di un [Bundle].
     */
    @JvmStatic
    fun createBundle(): BundleBuilder {
        return BundleBuilder()
    }

    /**
     * Crea un builder di tipo [IntentBuilder] per creare un nuovo [Intent] con dei [ComponentModule].
     *
     * @return builder per la creazione di un [Intent].
     */
    @JvmStatic
    fun createIntent(): IntentBuilder {
        return IntentBuilder()
    }

    /**
     * Risolve gli extras dell'[Intent] di un'[Activity] creando i moduli generati dai fields annotati.
     * Sui moduli viene poi effettuata la lettura dagli extras dell'[Intent] per popolarli con i valori corretti.
     *
     * @param activity [Activity] di cui bisogna risolvere l'[Intent] di creazione
     */
    @JvmStatic
    fun resolveIntent(activity: Activity) {
        resolveBundle(activity, activity, activity.intent.extras)
    }

    /**
     * Risolve gli arguments di un [Fragment] creando i moduli generati dai fields annotati.
     * Sui moduli viene poi effettuata la lettura dagli arguments per popolarli con i valori corretti.
     *
     * @param fragment [Fragment] di cui bisogna risolvere gli arguments di creazione
     */
    @JvmStatic
    fun resolveArguments(fragment: Fragment) {
        val activity = fragment.activity ?: throw IllegalArgumentException("The activity mustn't be null.")
        resolveBundle(activity, fragment, fragment.arguments)
    }

    /**
     * Risolve un [Bundle] creando i moduli generati dai fields annotati.
     * Sui moduli viene poi effettuata la lettura dal [Bundle] per popolarli con i valori corretti.
     *
     * @param context [Context] usato per istanziare i [ComponentModule] che lo richiedono e per effettuare la lettura dal [Bundle].
     * @param holder oggetto nel quale si trovano i field annotati.
     * @param bundle [Bundle] da risolvere che contiene i vari moduli serializzati.
     */
    @JvmStatic
    fun resolveBundle(context: Context, holder: Any, bundle: Bundle?) {
        /* Per evitare un accesso concorrente all'ArrayMap, il codice viene eseguito in un blocco synchronized. */
        synchronized(this, {
            /* setta il field nell'oggetto passato per parametro. */
            val setField = { field: Field ->
                val componentModule = instantiateModule(field.type, context)
                if (componentModule != null) {
                    if (bundle != null) {
                        componentModule.readContent(context, bundle)
                    }
                    field.set(holder, componentModule)
                } else {
                    Log.e(TAG, "Cannot read content from component of type " + field.type.name)
                }
            }

            val cls = holder.javaClass
            var fields = cachedFields[cls]
            if (fields == null) {
                /* I fields annotati non sono in cache quindi bisogna ciclare su tutti quelli disponibili. */
                fields = cls.fields
                val fieldsToCache = LinkedList<Field>()
                for (field in fields) {
                    if (!field.isAnnotationPresent(BundleResolvable::class.java) || !ComponentModule::class.java.isAssignableFrom(field.type))
                        continue

                    fieldsToCache.add(field)
                    setField.invoke(field)
                }
                /* I fields annotati di tipo ComponentModule vengono salvati in cache. */
                cachedFields.put(cls, fieldsToCache.toTypedArray())
            } else {
                /* I fields annotati di tipo ComponentModule sono già in cache quindi basta ciclare su di essi e settare il valore corretto. */
                for (field in fields) {
                    setField.invoke(field)
                }
            }
        })
    }

    /**
     * Crea un [Bundle] effettuando la scrittura dei moduli e di tutte le loro dipendenze senza ripetizione.
     * La creazione e la scrittura dei [ComponentModule] avviene tramite ricorsione senza ripetizione.
     * I moduli che hanno priorità nella scrittura sono quelli passati come parametro.
     * Se questi moduli hanno delle dipendenze, vengono creati tramite reflection anche i moduli definiti come dipendenze (se non creati in precedenza).
     * Il processo si ripete fino a quando non si esauriscono le dipendenze.
     *
     * @param context [Context] con il quale viene effettuata la scrittura dei moduli
     * @param modules moduli principali da scrivere nel [Bundle]
     */
    @JvmStatic
    fun createBundleWithModules(context: Context, vararg modules: ComponentModule): Bundle {
        val bundle = Bundle()
        val allModules = LinkedList<ComponentModule>()
        /* Aggiunge inizialmente i moduli passati per parametro. */
        Collections.addAll(allModules, *modules)

        for (module in modules) {
            /* Per ogni modulo vengono aggiunte le dipendenze.
             * Le dipendenze vengono aggiunge ad una lista passata per parametro.
             * La lista viene popolata all'interno del metodo ad ogni passaggio ricorsivo */
            addDependencies(context, allModules, module)
        }

        /* Scrive tutti i moduli nel Bundle. */
        for (module in allModules) {
            bundle.putAll(module.writeContent(context))
        }
        return bundle
    }

    private fun addDependencies(context: Context, allModules: MutableList<ComponentModule>, componentModule: ComponentModule) {
        val dependencies = componentModule.moduleDependencies()
        if (dependencies.isEmpty())
            return

        for (dependencyClass in dependencies) {
            /* Non aggiunge il modulo se già presente nella lista dei moduli istanziati. */
            val skipAdd = allModules.any { it.javaClass == dependencyClass }
            if (!skipAdd) {
                /* Istanzia il modulo con la reflection. */
                val module = ComponentManager.instantiateModule(dependencyClass, context)
                if (module != null) {
                    allModules.add(module)
                    /* Richiama lo stesso metodo sulle dipendenze del modulo aggiunto. */
                    addDependencies(context, allModules, module)
                }
            }
        }
    }

    private fun instantiateModule(moduleClass: Class<*>, context: Context): ComponentModule? {
        /* Per istanziare il modulo è necessario almeno uno dei due costruttori:
         * - costruttore senza parametri
         * - costruttore con un parametro di tipo Context
         * Il costruttore con il Context ha la priorità rispetto a quello senza parametri. */
        var componentObj: Any? = null
        var constructor: Constructor<*>
        try {
            /* Si cerca di ottenere il costruttore con il Context e di istanziare il modulo */
            constructor = moduleClass.getConstructor(Context::class.java)
            createModuleInstance(constructor, context)?.let {
                componentObj = it
            }
        } catch (e: NoSuchMethodException) {
            try {
                /* Se non presente il costruttore con il Context,
                 * si cerca di ottenere quello senza parametri e di istanziare il modulo. */
                constructor = moduleClass.getConstructor()
                createModuleInstance(constructor)?.let {
                    componentObj = it
                }
            } catch (e: NoSuchMethodException) {
                throw NoSuchMethodException("You have to define a constructor with 0 arguments or a constructor with Context.")
            }
        }
        return componentObj as? ComponentModule
    }

    private fun <T> createModuleInstance(constructor: Constructor<T>, context: Context? = null): T? {
        var obj: T?
        val accessible = constructor.isAccessible
        if (!accessible) {
            constructor.isAccessible = true
        }
        try {
            if (context == null) {
                obj = constructor.newInstance()
            } else {
                obj = constructor.newInstance(context)
            }
        } catch (e: Exception) {
            obj = null
        }
        if (!accessible) {
            constructor.isAccessible = false
        }
        return obj
    }

    /**
     * Callback utilizzata per applicare dei cambiamenti ad un [Bundle]
     */
    interface BundleTransformCallback {

        /**
         * Permette di effettuare dei cambiamenti su un dato [Bundle].
         *
         * @param original il [Bundle] che può essere cambiato.
         * @return il [Bundle] dopo la trasformazione.
         */
        fun onTrasformBundle(original: Bundle): Bundle
    }
}