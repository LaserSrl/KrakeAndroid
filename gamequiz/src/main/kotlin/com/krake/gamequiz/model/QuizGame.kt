package com.krake.gamequiz.model

import com.krake.core.model.ActivityPart
import com.krake.core.model.ContentItemWithGallery
import com.krake.core.model.RecordWithAutoroute
import com.krake.core.model.RecordWithIdentifier
import com.krake.surveys.model.Questionnaire
import java.util.*

/**
 * Created by joel on 08/03/17.
 */
interface QuizGame : ContentItemWithGallery, RecordWithAutoroute, RecordWithIdentifier {

    val state: Long get() = gamePart?.state ?: Status.NO_YET_ACTIVE

    val startDate: Date? get() = activityPart?.dateTimeStart

    val endDate: Date? get() = activityPart?.dateTimeEnd

    val questionariContentItems: List<*>

    val questionnaire: Questionnaire? get() = questionariContentItems.firstOrNull() as? Questionnaire

    val answerTime: Long? get() = gamePart?.answerTime

    val answerPoint: Long? get() = gamePart?.answerPoint

    val rankingAndroidIdentifier: String? get() = gamePart?.rankingAndroidIdentifier

    val abstractText: String? get() = gamePart?.abstractText

    val activityPart: ActivityPart?

    val gamePart: GamePart?

    /**
     * Tipo di gioco, i valori possibili per questa proorieta' sono
     * [.GAME_TYPE_COMPETITION] [.GAME_TYPE_NO_RANK]

     * @return il tipo di gioco
     */
    val gameType: String get() = gamePart?.gameType ?: GAME_TYPE_NO_RANK

    object Status {
        /**
         * gioco scaduto, non piu' utilizzabile
         */
        const val CLOSED = 0L
        /**
         * gioco in corso, possibile giocarci
         */
        const val ACTIVE = 1L
        /**
         * gioco non ancora attivo, potra' essere utilizzato in futuro
         */
        const val NO_YET_ACTIVE = 2L
    }

    companion object {

        /**
         * Rappresenta un tipo di gioco collegato ad un concorso.
         * Allafine delle risposte sarà richiesto all'utente di inserire il proprio numero di telefono
         * e solo a questo punto il punteggio sarà inviato alla classifica di Google Play
         */
        const val GAME_TYPE_COMPETITION = "Competition"

        /**
         * Gioco non collegato ad un concorso, automaticamente alla fine della partita il punteggio sarà
         * condiviso su Google play.
         */
        const val GAME_TYPE_NO_RANK = "NoRanking"
    }
}