/**
 * Created by Krake Generator 10.0 Bloody Mary (1902.11.10) on 12/02/2019, 10:17**/
package mykrake.com.krakesample.model

import com.krake.core.model.ContentItemWithDescription
import com.krake.core.model.RecordWithFilter
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey


open class Questionnaire : RealmObject() /*INTERFACES*/, com.krake.surveys.model.Questionnaire,
    ContentItemWithDescription, RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    open var autoroutePartPromoteToHomePage: Boolean? = null
    override var titlePartTitle: String? = null
    override var bodyPartText: String? = null
    override var questionnairePart: QuestionnairePart? = null
    open var contentType: String? = null
    @PrimaryKey
    override var identifier: Long = 0
    open var autoroutePartUseCulturePattern: Boolean? = null
    open var bodyPartFormat: String? = null
    @Index
    override var autoroutePartDisplayAlias: String = ""
/*ENDFIELDS*/
}
