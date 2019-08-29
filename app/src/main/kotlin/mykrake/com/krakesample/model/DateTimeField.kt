/**
 * Created by Krake Generator 10.0 Bloody Mary (1906.25.14) on 29/08/2019, 10:23**/
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
