/**
 * Created by Krake Generator 10.0 Bloody Mary (1905.28.11) on 18/06/2019, 11:43**/
package mykrake.com.krakesample.model

import com.krake.core.model.RecordWithFilter
import com.krake.core.model.RecordWithIdentifier
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class ShareLinkPart : RealmObject() /*INTERFACES*/, com.krake.core.model.ShareLinkPart, RecordWithIdentifier,
    RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    override var sharedText: String? = null
    open var sharedBody: String? = null
    override var sharedLink: String? = null
    open var sharedIdImage: String? = null
    override var sharedImage: String? = null
    @PrimaryKey
    override var identifier: Long = 0
/*ENDFIELDS*/
}
