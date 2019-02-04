package com.krake.gamequiz.model

/**
 * Created by joel on 08/03/17.
 */

interface GamePart {

    val state: Long

    val answerPoint: Long?

    val answerTime: Long?

    val gameType: String

    val rankingAndroidIdentifier: String?

    val abstractText: String?
}
