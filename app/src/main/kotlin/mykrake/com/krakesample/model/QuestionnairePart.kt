/**
 * Created by Krake Generator 10.0 Bloody Mary (1905.28.11) on 18/06/2019, 11:43**/
package mykrake.com.krakesample.model

import com.krake.core.model.RecordWithFilter
import com.krake.core.model.RecordWithIdentifier
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class QuestionnairePart : RealmObject() /*INTERFACES*/, com.krake.surveys.model.QuestionnairePart,
    RecordWithIdentifier, RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    open var useRecaptcha: Boolean? = null
    @PrimaryKey
    override var identifier: Long = 0
    override var questions: RealmList<QuestionRecord> = RealmList()
    open var mustAcceptTerms: Boolean? = null
/*ENDFIELDS*/
}
