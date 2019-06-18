/**
 * Created by Krake Generator 10.0 Bloody Mary (1905.28.11) on 18/06/2019, 11:43**/
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
