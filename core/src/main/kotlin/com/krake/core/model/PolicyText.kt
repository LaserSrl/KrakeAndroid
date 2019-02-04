package com.krake.core.model

/**
 * Created by joel on 01/03/17.
 */

interface PolicyText : ContentItemWithDescription, RecordWithIdentifier {
    val policyTextInfoPartPriority: Long?
    val policyTextInfoPartUserHaveToAccept: Boolean
    val policyTextInfoPartPolicyType: String?

    companion object {
        const val policyTextInfoPartPriorityField = "policyTextInfoPartPriority"
    }
}
