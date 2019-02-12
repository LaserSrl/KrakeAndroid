/**
 * Created by Krake Generator 10.0 Bloody Mary (1902.11.10) on 12/02/2019, 10:17**/
package mykrake.com.krakesample.model

import com.krake.core.model.ContentItem
import com.krake.core.model.RecordWithAutoroute
import com.krake.core.model.RecordWithFilter
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey


open class Taxonomy : RealmObject() /*INTERFACES*/, ContentItem, com.krake.core.model.Taxonomy, RecordWithAutoroute,
    RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    open var taxonomyPartIsAllTabVisibleInAppValue: Boolean? = null
    open var taxonomyExtensionPartOrderBy: String? = null
    open var autoroutePartUseCulturePattern: Boolean? = null
    override var titlePartTitle: String? = null
    override var taxonomyPartTerms: RealmList<TermPart> = RealmList()
    open var taxonomyPartAutoroutePartDisplayAlias: String? = null
    open var autoroutePartPromoteToHomePage: Boolean? = null
    open var contentType: String? = null
    override var taxonomyPartTermTypeName: String? = null
    @Index
    override var autoroutePartDisplayAlias: String = ""
    @PrimaryKey
    override var identifier: Long = 0
    open var taxonomyPartIsInternal: Boolean? = null
    override var taxonomyPartName: String? = null
/*ENDFIELDS*/
}
