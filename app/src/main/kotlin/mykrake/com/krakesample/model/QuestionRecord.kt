/**
 * Created by Krake Generator 8.3 Daiquiri (1711.02.15) on 14/04/2020, 12:25**/
package mykrake.com.krakesample.model

import com.krake.core.model.RecordWithFilter
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class QuestionRecord : RealmObject() /*INTERFACES*/,com.krake.surveys.model.Question,RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    override var section : String? = null
    open var questionnairePartRecord_Id : Long? = null
    override var position : Long = 0
    open var isRequired : Boolean? = null
    override var published : Boolean = false
    override var questionType : String = ""
    override var allFiles : String? = null
    override var answers : RealmList<AnswerRecord> = RealmList()
    override var conditionType : String? = null
    @PrimaryKey override var identifier : Long = 0
    override var question : String? = null
    override var answerType : String? = null
/*ENDFIELDS*/
    override var condition : String? = null
}
