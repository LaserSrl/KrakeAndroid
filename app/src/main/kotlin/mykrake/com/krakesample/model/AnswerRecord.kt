/**
 * Created by Krake Generator 10.0 Bloody Mary (1902.11.10) on 12/02/2019, 10:17**/
package mykrake.com.krakesample.model

import com.krake.core.model.RecordWithFilter
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class AnswerRecord : RealmObject() /*INTERFACES*/, com.krake.surveys.model.Answer,
    RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    override var answer: String? = null
    open var questionRecord_Id: Long? = null
    override var published: Boolean = false
    override var allFiles: String? = null
    override var position: Long = 0
    @PrimaryKey
    override var identifier: Long = 0
    override var correctResponse: Boolean = false
/*ENDFIELDS*/
}
