package com.krake.trip.component.module;

import android.content.Context
import android.os.Bundle
import com.google.gson.reflect.TypeToken
import com.krake.core.component.base.ComponentModule
import com.krake.core.component.module.ThemableComponentModule
import com.krake.trip.Route
import com.krake.trip.TripPlanViewModel
import java.util.*

/**
 * Modulo utilizzato per specificare gli attributi di un viaggio pianificato con trip planner.
 */
class RoutesModule : ComponentModule {
    companion object {
        private const val ARG_ROUTES = "argItinerary"
    }

    lateinit var routes: List<Route>
        private set

    /**
     * Specifica l'oggetto di tipo [Route] usato per definire l'itinerario da mostrare.
     * DEFAULT: null
     *
     * @param itinerary oggetto di tipo [Route].
     */
    fun routes(routes: List<Route>) = apply { this.routes = routes }

    /**
     * Legge il contenuto di un [Bundle] e modifica le sue proprietà.
     *
     * @param context il [Context] utilizzato per leggere il [Bundle].
     * @param bundle container dal quale vengono letti i vari arguments.
     */
    override fun readContent(context: Context, bundle: Bundle) {
        routes = TripPlanViewModel.gson().fromJson(bundle.getString(ARG_ROUTES), object : TypeToken<ArrayList<Route>>()
        {}.type)
    }

    /**
     * Scrive le proprietà di un modulo su un [Bundle].
     *
     * @param context il [Context] utilizzato per creare il [Bundle] e scrivere su di esso.
     * @return container che contiene le proprietà del modulo corrente.
     */
    override fun writeContent(context: Context): Bundle {
        val bundle = Bundle()
        bundle.putString(ARG_ROUTES, TripPlanViewModel.gson().toJson(routes))
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