package com.krake.core.model

import io.realm.RealmModel

/**
 * Created by joel on 28/02/17.
 */

interface RecordWithAutoroute : RealmModel {
    val autoroutePartDisplayAlias: String

    companion object {
        const val AutorouteDisplayAliasFieldName = "autoroutePartDisplayAlias"
    }
}