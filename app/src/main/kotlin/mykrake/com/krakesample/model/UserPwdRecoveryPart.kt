/**
 * Created by Krake Generator 10.0 Bloody Mary (1903.22.10) on 09/05/2019, 09:16**/
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
