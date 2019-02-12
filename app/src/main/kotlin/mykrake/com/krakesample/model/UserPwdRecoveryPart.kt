/**
 * Created by Krake Generator 10.0 Bloody Mary (1902.11.10) on 12/02/2019, 10:17**/
package mykrake.com.krakesample.model

import com.krake.core.model.RecordWithFilter
import com.krake.core.model.RecordWithIdentifier
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class UserPwdRecoveryPart : RealmObject() /*INTERFACES*/, RecordWithIdentifier, RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    @PrimaryKey
    override var identifier: Long = 0
    open var internationalPrefix: String? = null
    open var phoneNumber: String? = null
/*ENDFIELDS*/
}
