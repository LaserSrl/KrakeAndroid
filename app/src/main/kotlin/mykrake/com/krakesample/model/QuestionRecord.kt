/**
 * Created by Krake Generator 10.0 Bloody Mary (1902.11.10) on 12/02/2019, 10:17**/
package mykrake.com.krakesample.model

import com.krake.core.model.RecordWithFilter
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class QuestionRecord : RealmObject() /*INTERFACES*/, com.krake.surveys.model.Question,
    RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    open var isRequired: Boolean? = null
    override var position: Long = 0
    override var published: Boolean = false
    open var questionnairePartRecord_Id: Long? = null
    override var section: String? = null
    override var question: String? = null
    @PrimaryKey
    override var identifier: Long = 0
    open var answerType: String? = null
    override var questionType: String = ""
    override var answers: RealmList<AnswerRecord> = RealmList()
    open var conditionType: String? = null
/*ENDFIELDS*/
}
