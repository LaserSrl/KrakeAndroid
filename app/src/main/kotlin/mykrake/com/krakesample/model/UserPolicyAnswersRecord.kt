/**
 * Created by Krake Generator 8.3 Daiquiri (1711.02.15) on 14/04/2020, 12:25**/
package mykrake.com.krakesample.model

import com.krake.core.model.Policy
import com.krake.core.model.RecordWithFilter
import com.krake.core.model.RecordWithIdentifier
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*


open class UserPolicyAnswersRecord : RealmObject() /*INTERFACES*/,Policy,RecordWithIdentifier,RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    open var policyTextInfoPartRecordPolicyType : String? = null
    open var policyTextInfoPartRecordAddPolicyToRegistration : Boolean? = null
    open var answerDate : Date? = null
    override var policyTextInfoPartRecordIdentifier : Long = 0
    @PrimaryKey override var identifier : Long = 0
    override var policyTextInfoPartRecordUserHaveToAccept : Boolean = false
    open var policyTextInfoPartRecordPriority : Long? = null
    override var accepted : Boolean = false
/*ENDFIELDS*/
}
