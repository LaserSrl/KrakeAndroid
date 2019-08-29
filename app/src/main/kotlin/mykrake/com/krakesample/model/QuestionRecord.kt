/**
 * Created by Krake Generator 10.0 Bloody Mary (1906.25.14) on 29/08/2019, 10:23**/
package mykrake.com.krakesample.model

import com.krake.core.model.RecordWithFilter
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class QuestionRecord : RealmObject() /*INTERFACES*/, com.krake.surveys.model.Question,
    RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    override var section: String? = null
    open var questionnairePartRecord_Id: Long? = null
    override var position: Long = 0
    open var isRequired: Boolean? = null
    override var published: Boolean = false
    override var questionType: String = ""
    override var allFiles: String? = null
    override var answers: RealmList<AnswerRecord> = RealmList()
    open var conditionType: String? = null
    @PrimaryKey
    override var identifier: Long = 0
    override var question: String? = null
    open var answerType: String? = null
/*ENDFIELDS*/
}
