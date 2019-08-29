/**
 * Created by Krake Generator 10.0 Bloody Mary (1906.25.14) on 29/08/2019, 10:23**/
package mykrake.com.krakesample.model

import com.krake.core.model.ContentItemWithGallery
import com.krake.core.model.RecordWithAutoroute
import com.krake.core.model.RecordWithFilter
import com.krake.core.model.RecordWithIdentifier
import com.krake.puzzlegame.model.PuzzleGame
import com.krake.surveys.model.QuestionnairePart
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

open class Game : RealmObject(), PuzzleGame /*INTERFACES*/, com.krake.gamequiz.model.QuizGame,
    com.krake.events.model.Event, ContentItemWithGallery, RecordWithAutoroute, RecordWithIdentifier,
    RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    open var autoroutePartUseCulturePattern: Boolean? = null
    override var questionariContentItems: RealmList<Questionnaire> = RealmList()
    open var galleryFirstMediaUrl: String? = null
    override var titlePartTitle: String? = null
    open var autoroutePartPromoteToHomePage: Boolean? = null
    @Index
    override var autoroutePartDisplayAlias: String = ""
    open var contentType: String? = null
    override var gamePart: GamePart? = null
    override var galleryMediaParts: RealmList<MediaPart> = RealmList()
    @PrimaryKey
    override var identifier: Long = 0
    override var activityPart: ActivityPart? = null
    /*ENDFIELDS*/
    override val questionnairePart: QuestionnairePart?
        get() = questionariContentItems.firstOrNull()?.questionnairePart
}
