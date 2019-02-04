package com.krake.events.component.module

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.annotation.LayoutRes
import com.krake.core.component.base.ComponentModule
import com.krake.core.component.module.ListMapComponentModule
import com.krake.events.EventActivity
import com.krake.events.EventAdapter
import com.krake.events.EventViewHolder
import com.krake.events.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Modulo utilizzato per specificare gli attributi di un'[Activity] che mostra gli eventi.
 * Utilizzato principalmente da [EventActivity] e dalle [Activity] che la estendono.
 */
class EventComponentModule : ComponentModule {
    companion object {
        private const val ARG_REMOTE_DATE_FORMAT = "argDateFormat"
        private const val ARG_END_DATE_USER_SELECTABLE = "argEndDateUserSelectable"
        private const val ARG_INITIAL_BEGIN_DATE = "argInitialBeginDate"
        private const val ARG_INITIAL_END_DATE = "argInitialEndDate"
        private const val ARG_INITIAL_MIN_DATE = "argInitialMinDate"
        private const val ARG_INITIAL_MAX_DATE = "argInitialMaxDate"

        /**
         * Classe che può essere usata come view holder di base per gli elementi di tipo [Event].
         */
        val DEFAULT_LIST_VIEW_HOLDER_CLASS = EventViewHolder::class.java

        /**
         * Classe che può essere usata come adapter di base per gli elementi di tipo [Event].
         */
        val DEFAULT_LIST_ADAPTER_CLASS = EventAdapter::class.java

        /**
         * Layout di una cella base per gli elementi di tipo [Event].
         */
        @LayoutRes
        val DEFAULT_LIST_CELL_LAYOUT = R.layout.event_row_cell

        @LayoutRes
        val DEFAULT_DETAIL_CONTENT_LAYOUT = R.layout.fragment_event_details

        private const val DAY_LENGTH = (24 * 3600 * 1000).toLong()
    }

    var remoteDateFormat: String
        private set

    var endDateSelectableByUser: Boolean
        private set

    var startDateMillis: Long?
        private set

    var endDateMillis: Long?
        private set

    var minDateMillis: Long? = null
        private set

    var maxDateMillis: Long? = null
        private set

    init {
        remoteDateFormat = "yyyy-MM-dd"
        endDateSelectableByUser = false
        startDateMillis = Date(Date().time / DAY_LENGTH * DAY_LENGTH).time
        endDateMillis = Date(startDateMillis!!).time
    }

    /**
     * Specifica il formato delle date che vengono spedite al WS.
     * DEFAULT: yyyy-MM-dd
     *
     * @param remoteDateFormat formato che segue gli standard di un [SimpleDateFormat].
     */
    fun remoteDateFormat(remoteDateFormat: String) = apply { this.remoteDateFormat = remoteDateFormat }

    /**
     * Specifica se la data di fine può essere selezionata o meno dall'utente.
     * Se la data di fine non potrà essere selezionata, in [EventActivity] verrà utilizzata la data d'inizio come data di fine.
     * DEFAULT: false
     *
     * @param endDateSelectableByUser true se la data di fine può essere selezionata dall'utente.
     */
    fun endDateSelectableByUser(endDateSelectableByUser: Boolean) = apply { this.endDateSelectableByUser = endDateSelectableByUser }

    /**
     * Specifica il numero di millisecondi dal 1970 alla data d'inizio.
     * DEFAULT: millisecondi corrispondenti alla mezzanotte del giorno corrente.
     *
     * @param millis millisecondi dal 1970 alla data d'inizio.
     */
    fun startDateMillis(millis: Long?) = apply { this.startDateMillis = millis }

    /**
     * Specifica il numero di millisecondi dal 1970 alla data di fine.
     * DEFAULT: millisecondi corrispondenti alla mezzanotte del giorno corrente.
     *
     * @param millis millisecondi dal 1970 alla data di fine.
     */
    fun endDateMillis(millis: Long?) = apply { this.endDateMillis = millis }

    /**
     * Specifica il numero di millisecondi dal 1970 alla data di fine.
     * DEFAULT: millisecondi corrispondenti alla mezzanotte del giorno corrente.
     *
     * @param millis millisecondi dal 1970 alla data di fine.
     */
    fun maxDateMillis(millis: Long) = apply { this.maxDateMillis = millis }

    /**
     * Specifica il numero di millisecondi dal 1970 alla data di fine.
     * DEFAULT: millisecondi corrispondenti alla mezzanotte del giorno corrente.
     *
     * @param millis millisecondi dal 1970 alla data di fine.
     */
    fun minDateMillis(millis: Long) = apply {
        this.minDateMillis = millis
        if (startDateMillis != null && startDateMillis!! < millis)
            startDateMillis = millis
    }

    /**
     * Legge il contenuto di un [Bundle] e modifica le sue proprietà.
     *
     * @param context il [Context] utilizzato per leggere il [Bundle].
     * @param bundle container dal quale vengono letti i vari arguments.
     */
    override fun readContent(context: Context, bundle: Bundle) {
        remoteDateFormat = bundle.getString(ARG_REMOTE_DATE_FORMAT, remoteDateFormat)
        endDateSelectableByUser = bundle.getBoolean(ARG_END_DATE_USER_SELECTABLE, endDateSelectableByUser)
        startDateMillis = if (bundle.containsKey(ARG_INITIAL_BEGIN_DATE)) bundle.getLong(ARG_INITIAL_BEGIN_DATE) else null
        endDateMillis = if (bundle.containsKey(ARG_INITIAL_END_DATE)) bundle.getLong(ARG_INITIAL_END_DATE) else null
        minDateMillis = if (bundle.containsKey(ARG_INITIAL_MIN_DATE)) bundle.getLong(ARG_INITIAL_MIN_DATE) else null
        maxDateMillis = if (bundle.containsKey(ARG_INITIAL_MAX_DATE)) bundle.getLong(ARG_INITIAL_MAX_DATE) else null
    }

    /**
     * Scrive le proprietà di un modulo su un [Bundle].
     *
     * @param context il [Context] utilizzato per creare il [Bundle] e scrivere su di esso.
     * @return container che contiene le proprietà del modulo corrente.
     */
    override fun writeContent(context: Context): Bundle {
        val bundle = Bundle()
        bundle.putString(ARG_REMOTE_DATE_FORMAT, remoteDateFormat)
        bundle.putBoolean(ARG_END_DATE_USER_SELECTABLE, endDateSelectableByUser)
        startDateMillis?.let { bundle.putLong(ARG_INITIAL_BEGIN_DATE, it) }
        endDateMillis?.let { bundle.putLong(ARG_INITIAL_END_DATE, it) }
        minDateMillis?.let { bundle.putLong(ARG_INITIAL_MIN_DATE, it) }
        maxDateMillis?.let { bundle.putLong(ARG_INITIAL_MAX_DATE, it) }
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