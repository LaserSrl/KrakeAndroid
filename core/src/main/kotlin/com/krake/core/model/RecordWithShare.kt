package com.krake.core.model

import io.realm.RealmModel

/**
 * Created by joel on 01/03/17.
 */

interface RecordWithShare : RealmModel {

    val shareLinkPart: ShareLinkPart?
}