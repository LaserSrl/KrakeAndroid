/**
 * Created by Krake Generator 10.0 Bloody Mary (1903.22.10) on 09/05/2019, 09:16**/
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
