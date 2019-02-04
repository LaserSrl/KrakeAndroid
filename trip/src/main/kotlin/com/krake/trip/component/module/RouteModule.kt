package com.krake.trip.component.module;

import android.content.Context
import android.os.Bundle
import com.krake.core.component.base.ComponentModule
import com.krake.core.component.module.ThemableComponentModule
import com.krake.trip.Route
import com.krake.trip.TripPlanViewModel

/**
 * Modulo utilizzato per specificare gli attributi di un viaggio pianificato con trip planner.
 */
class RouteModule : ComponentModule {
    companion object {
        private const val ARG_ROUTE = "argItinerary"
    }

    lateinit var route: Route
        private set

    /**
     * Specifica l'oggetto di tipo [Route] usato per definire l'itinerario da mostrare.
     * DEFAULT: null
     *
     * @param itinerary oggetto di tipo [Route].
     */
    fun route(route: Route) = apply { this.route = route }

    /**
     * Legge il contenuto di un [Bundle] e modifica le sue proprietà.
     *
     * @param context il [Context] utilizzato per leggere il [Bundle].
     * @param bundle container dal quale vengono letti i vari arguments.
     */
    override fun readContent(context: Context, bundle: Bundle) {
        route = TripPlanViewModel.gson().fromJson(bundle.getString(ARG_ROUTE), Route::class.java)
    }

    /**
     * Scrive le proprietà di un modulo su un [Bundle].
     *
     * @param context il [Context] utilizzato per creare il [Bundle] e scrivere su di esso.
     * @return container che contiene le proprietà del modulo corrente.
     */
    override fun writeContent(context: Context): Bundle {
        val bundle = Bundle()
        bundle.putString(ARG_ROUTE, TripPlanViewModel.gson().toJson(route))
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