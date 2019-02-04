package com.krake.core.model

import io.realm.RealmModel
import java.util.*

/**
 * Created by joel on 28/02/17.
 */

interface TermPart : RecordWithAutoroute, RecordWithIdentifier, RealmModel {
    val taxonomyId: Long?
    val fullPath: String?
    val count: Long?
    val name: String?
    val iconaMediaParts: List<*>
        get() {
            return LinkedList<MediaPart>()
        }
    val icon get() = iconaMediaParts.firstOrNull() as? MediaPart

    val weight: Long?
}
