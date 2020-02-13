package com.krake.contentcreation.adapter

import android.content.Context
import android.view.ViewGroup
import com.krake.contentcreation.R
import com.krake.contentcreation.adapter.PolicyAdapter.Listener
import com.krake.contentcreation.adapter.holder.PolicyHolder
import com.krake.contentcreation.model.Modifiable
import com.krake.core.model.Policy
import com.krake.core.widget.ObjectsRecyclerViewAdapter
import io.realm.Realm

/**
 * Implementation of [ObjectsRecyclerViewAdapter] used to show items of type [Policy] in a [PolicyHolder].
 *
 * @param listener [Listener] used to obtain additional information about the policies and
 * to be notified when a policy is accepted or declined.
 */
class PolicyAdapter(context: Context, private val listener: Listener) :
        ObjectsRecyclerViewAdapter<Policy, PolicyHolder>(context, R.layout.cell_policy, emptyList(), PolicyHolder::class.java) {

    private val realm by lazy { Realm.getDefaultInstance() }
    private var notifyListener = true

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewLayout: Int): PolicyHolder {
        val holder = super.onCreateViewHolder(viewGroup, viewLayout)
        holder.acceptPolicySwitch.setOnCheckedChangeListener { _, isChecked ->
            val policy = getItem(holder.adapterPosition)
                    ?: throw NullPointerException("The selected policy item can't be null")

            realm.executeTransaction {
                policy.accepted = isChecked
            }
            // Notify the listener when the policy is accepted or declined.
            if (notifyListener)
                listener.policyToggled(policy)
        }
        return holder
    }


    override fun onBindViewHolder(holder: PolicyHolder, position: Int) {
        val policy = getItem(position)
                ?: throw NullPointerException("The policy item can't be null")
        // Get the title of the policy from the listener.
        holder.titleTextView.text = listener.policyTitle(policy)
        val switch = holder.acceptPolicySwitch

        notifyListener = false

        switch.isChecked = policy.accepted
        // Enable or disable the possibility to accept or decline the policy.
        switch.isEnabled = !policy.policyTextInfoPartRecordUserHaveToAccept || !policy.accepted || policy is Modifiable

        notifyListener = true

        val switchTextSb = StringBuilder(context!!.getString(R.string.privacy_accept))
        if (policy.policyTextInfoPartRecordUserHaveToAccept) {
            switchTextSb.append(" *")
                    .append(context!!.getString(R.string.required))
        }
        switch.text = switchTextSb.toString()
    }

    /**
     * Used to obtain information about a [Policy] and to receive the callback when a [Policy] is accepted or declined.
     */
    interface Listener {

        /**
         * Get the title of the policy.
         *
         * @param policy instance of [Policy] used to retrieve the title.
         * @return the title of the [Policy].
         */
        fun policyTitle(policy: Policy): String

        /**
         * Called when a policy is accepted or declined.
         *
         * @param policy accepted or declined [Policy].
         */
        fun policyToggled(policy: Policy)
    }
}