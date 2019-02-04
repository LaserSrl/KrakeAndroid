package com.krake.core.model

import com.krake.core.getProperty

/**
 * Created by joel on 02/11/17.
 */

interface RecordWithFilter {
    fun recordContains(searchFilter: String, columns: Array<out String>): Boolean {
        var contained = false
        columns.forEach { contained = contained || (this.getProperty(it)?.toString() ?: "").contains(searchFilter, true) }
        return contained
    }
}