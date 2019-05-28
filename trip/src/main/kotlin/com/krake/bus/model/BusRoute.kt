package com.krake.bus.model

import com.krake.core.model.RecordWithStringIdentifier
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.RealmObjectChangeListener
import io.realm.annotations.RealmClass

interface BusRoute: RecordWithStringIdentifier {
    val color: Int

    val mode: RouteMode
}