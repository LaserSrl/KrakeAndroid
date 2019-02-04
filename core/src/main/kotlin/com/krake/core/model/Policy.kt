package com.krake.core.model

import io.realm.RealmModel

/**
 * Model used to identify a policy that can be accepted or declined by the user.
 */
interface Policy : RealmModel {

    /**
     * The unique identifier of the policy.
     */
    val policyTextInfoPartRecordIdentifier: Long

    /**
     * True if the user has accepted the policy, false otherwise.
     */
    var accepted: Boolean

    /**
     * True if the user must accept the policy in order to proceed, false otherwise.
     */
    val policyTextInfoPartRecordUserHaveToAccept: Boolean
}