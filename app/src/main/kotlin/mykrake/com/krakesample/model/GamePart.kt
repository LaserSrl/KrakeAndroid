/**
 * Created by Krake Generator 10.0 Bloody Mary (1902.11.10) on 12/02/2019, 10:17**/
package mykrake.com.krakesample.model

import com.krake.core.model.RecordWithFilter
import com.krake.core.model.RecordWithIdentifier
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*


open class GamePart : RealmObject() /*INTERFACES*/, com.krake.gamequiz.model.GamePart, RecordWithIdentifier,
    RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    override var state: Long = 0
    override var answerPoint: Long? = null
    override var answerTime: Long? = null
    open var randomResponse: Boolean? = null
    open var rankingIOSIdentifier: String? = null
    open var workflowfired: Boolean? = null
    @PrimaryKey
    override var identifier: Long = 0
    open var gameDate: Date? = null
    override var gameType: String = ""
    override var rankingAndroidIdentifier: String? = null
    open var myOrder: Long? = null
    open var questionsSortedRandomlyNumber: Long? = null
    override var abstractText: String? = null
/*ENDFIELDS*/
}
