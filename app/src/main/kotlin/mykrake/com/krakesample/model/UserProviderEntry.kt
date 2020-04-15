/**
* Created by Krake Generator 8.3 Daiquiri (1711.02.15) on 14/04/2020, 12:25**/
package mykrake.com.krakesample.model

import com.krake.core.model.*
import io.realm.*
import io.realm.annotations.*
import java.util.*


open class UserProviderEntry: RealmObject() /*INTERFACES*/,RecordWithIdentifier,RecordWithFilter/*ENDINTERFACES*/
{
/*FIELDS*/
    open var providerUserId : String? = null
    open var providerName : String? = null
    @PrimaryKey override var identifier : Long = 0
/*ENDFIELDS*/
}
