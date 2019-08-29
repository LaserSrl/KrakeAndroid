/**
 * Created by Krake Generator 10.0 Bloody Mary (1906.25.14) on 29/08/2019, 10:23**/
package mykrake.com.krakesample.model

import com.krake.core.model.RecordWithFilter
import com.krake.core.model.RecordWithIdentifier
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*


open class ActivityPart : RealmObject() /*INTERFACES*/, com.krake.core.model.ActivityPart,
    RecordWithIdentifier, RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    override var allDay: Boolean? = null
    override var dateTimeEnd: Date? = null
    open var repeatValue: Long? = null
    open var repeatEnd: Boolean? = null
    open var repeatType: String? = null
    open var repeatDetails: String? = null
    override var dateTimeStart: Date? = null
    open var repeatEndDate: Date? = null
    @PrimaryKey
    override var identifier: Long = 0
/*ENDFIELDS*/
}
