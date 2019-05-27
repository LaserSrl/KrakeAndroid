package com.krake.route.model

import com.krake.core.model.ContentItem
import com.krake.core.model.RecordWithStringIdentifier

class OtpBusRoute(val id: String,
                  val shortName: String,
                  val longName: String,
                  val agencyName: String,
                  val color: String,
                  val mode: String): ContentItem, RecordWithStringIdentifier {

    override val stringIdentifier: String
        get() = id

    override val titlePartTitle: String
        get() = longName
}