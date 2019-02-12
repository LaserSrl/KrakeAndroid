/**
 * Created by Krake Generator 10.0 Bloody Mary (1902.11.10) on 12/02/2019, 10:17**/
package mykrake.com.krakesample.model

import com.krake.core.model.RecordWithFilter
import io.realm.RealmObject
import java.util.*


open class DateTimeField : RealmObject() /*INTERFACES*/, RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    open var dateTime: Date? = null
    open var display: String? = null
/*ENDFIELDS*/
}
