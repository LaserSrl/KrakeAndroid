/**
 * Created by Krake Generator 10.0 Bloody Mary (1905.28.11) on 18/06/2019, 11:43**/
package mykrake.com.krakesample.model

import com.krake.core.model.RecordWithFilter
import com.krake.core.model.RecordWithIdentifier
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class MediaPart : RealmObject() /*INTERFACES*/, com.krake.core.model.MediaPart, RecordWithIdentifier,
    RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    override var mimeType: String? = null
    open var alternateText: String? = null
    override var fileName: String? = null
    override var title: String? = null
    override var folderPath: String? = null
    override var logicalType: String? = null
    override var mediaUrl: String? = null
    @PrimaryKey
    override var identifier: Long = 0
    open var caption: String? = null
/*ENDFIELDS*/
}
