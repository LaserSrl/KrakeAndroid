package com.krake.core.model

/**
 * Created by joel on 07/03/17.
 */

val Any.identifierOrStringIdentifier: String
    get() {
        if (this is RecordWithIdentifier)
            return this.identifier.toString()
        else if (this is RecordWithStringIdentifier)
            return this.stringIdentifier

        return ""
    }