/**
 * Created by Krake Generator 8.3 Daiquiri (1711.02.15) on 14/04/2020, 12:25**/
package mykrake.com.krakesample.model

import com.krake.core.model.ContentItemWithDescription
import com.krake.core.model.RecordWithAutoroute
import com.krake.core.model.RecordWithFilter
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey


open class PolicyText : RealmObject() /*INTERFACES*/,ContentItemWithDescription,com.krake.core.model.PolicyText,RecordWithAutoroute,RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    open var autoroutePartUseCulturePattern : Boolean? = null
    override var bodyPartText : String? = null
    override var policyTextInfoPartPriority : Long? = null
    open var policyTextInfoPartAddPolicyToRegistration : Boolean? = null
    override var policyTextInfoPartPolicyType : String? = null
    open var autoroutePartPromoteToHomePage : Boolean? = null
    override var policyTextInfoPartUserHaveToAccept : Boolean = false
    @Index override var autoroutePartDisplayAlias : String = ""
    open var contentType : String? = null
    open var bodyPartFormat : String? = null
    override var titlePartTitle : String? = null
    @PrimaryKey override var identifier : Long = 0
/*ENDFIELDS*/
}
