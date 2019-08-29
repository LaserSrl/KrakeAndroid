/**
 * Created by Krake Generator 10.0 Bloody Mary (1906.25.14) on 29/08/2019, 10:23**/
package mykrake.com.krakesample.model

import com.krake.core.model.RecordWithFilter
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class AnswerRecord : RealmObject() /*INTERFACES*/, com.krake.surveys.model.Answer,
    RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    open var questionRecord_Id: Long? = null
    override var answer: String? = null
    override var correctResponse: Boolean = false
    @PrimaryKey
    override var identifier: Long = 0
    override var published: Boolean = false
    override var position: Long = 0
/*ENDFIELDS*/
}
