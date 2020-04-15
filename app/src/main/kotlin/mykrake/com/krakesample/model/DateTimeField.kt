/**
 * Created by Krake Generator 8.3 Daiquiri (1711.02.15) on 14/04/2020, 12:25**/
package mykrake.com.krakesample.model

import com.krake.core.model.RecordWithFilter
import io.realm.RealmObject
import java.util.*


open class DateTimeField : RealmObject() /*INTERFACES*/,RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    open var dateTime : Date? = null
    open var display : String? = null
/*ENDFIELDS*/
}
