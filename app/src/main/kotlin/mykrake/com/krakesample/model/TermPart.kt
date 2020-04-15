/**
 * Created by Krake Generator 8.3 Daiquiri (1711.02.15) on 14/04/2020, 12:25**/
package mykrake.com.krakesample.model

import com.krake.core.model.RecordWithFilter
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey


open class TermPart : RealmObject() /*INTERFACES*/,com.krake.core.model.TermPart,RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    override var taxonomyId : Long? = null
    override var count : Long? = null
    override var fullPath : String? = null
    @Index override var autoroutePartDisplayAlias : String = ""
    override var weight : Long? = null
    open var path : String? = null
    open var iconFirstMediaUrl : String? = null
    open var selectable : Boolean? = null
    @PrimaryKey override var identifier : Long = 0
    open var container : Taxonomy? = null
    open var iconMediaParts : RealmList<MediaPart> = RealmList()
    open var datainizioselezione : DateTimeField? = null
    open var tipoUnivoco : EnumerationField? = null
    override var name : String? = null
/*ENDFIELDS*/
}
