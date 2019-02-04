package com.krake.core.model

import io.realm.RealmModel

/**
 * Created by joel on 28/02/17.
 */

interface RecordWithStringIdentifier : RealmModel {
    val stringIdentifier: String

    companion object {
        const val StringIdentifierFieldName = "stringIdentifier"
    }
}