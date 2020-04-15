/**
 * Created by Krake Generator 8.3 Daiquiri (1711.02.15) on 14/04/2020, 12:25**/
package mykrake.com.krakesample.model

import com.krake.core.model.RecordWithFilter
import com.krake.core.model.RecordWithIdentifier
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class UserPwdRecoveryPart : RealmObject() /*INTERFACES*/,RecordWithIdentifier,RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    @PrimaryKey override var identifier : Long = 0
    open var internationalPrefix : String? = null
    open var phoneNumber : String? = null
/*ENDFIELDS*/
}
