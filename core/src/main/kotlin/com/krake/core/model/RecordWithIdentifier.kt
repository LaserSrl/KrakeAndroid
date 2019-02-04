package com.krake.core.model

import io.realm.RealmModel

/**
 * Created by joel on 28/02/17.
 */

interface RecordWithIdentifier : RealmModel {
    val identifier: Long

    companion object {
        const val IdentifierFieldName = "identifier"
    }
}
