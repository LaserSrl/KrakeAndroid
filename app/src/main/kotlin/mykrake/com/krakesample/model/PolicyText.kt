/**
 * Created by Krake Generator 10.0 Bloody Mary (1906.25.14) on 29/08/2019, 10:23**/
package mykrake.com.krakesample.model

import com.krake.core.model.ContentItemWithDescription
import com.krake.core.model.RecordWithAutoroute
import com.krake.core.model.RecordWithFilter
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey


open class PolicyText : RealmObject() /*INTERFACES*/, ContentItemWithDescription,
    com.krake.core.model.PolicyText, RecordWithAutoroute, RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    open var autoroutePartUseCulturePattern: Boolean? = null
    override var bodyPartText: String? = null
    override var policyTextInfoPartPriority: Long? = null
    open var policyTextInfoPartAddPolicyToRegistration: Boolean? = null
    override var policyTextInfoPartPolicyType: String? = null
    open var autoroutePartPromoteToHomePage: Boolean? = null
    override var policyTextInfoPartUserHaveToAccept: Boolean = false
    @Index
    override var autoroutePartDisplayAlias: String = ""
    open var contentType: String? = null
    open var bodyPartFormat: String? = null
    override var titlePartTitle: String? = null
    @PrimaryKey
    override var identifier: Long = 0
/*ENDFIELDS*/
}
