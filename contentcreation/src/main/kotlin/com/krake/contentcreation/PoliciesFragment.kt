package com.krake.contentcreation

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.krake.contentcreation.adapter.PolicyAdapter
import com.krake.contentcreation.model.Modifiable
import com.krake.core.ClassUtils
import com.krake.core.OrchardError
import com.krake.core.StringUtils
import com.krake.core.app.OrchardDataModelFragment
import com.krake.core.component.base.ComponentManager
import com.krake.core.component.module.OrchardComponentModule
import com.krake.core.data.DataModel
import com.krake.core.model.Policy
import com.krake.core.model.PolicyText
import com.krake.core.widget.ContentItemWebView
import com.krake.core.widget.ObjectsRecyclerViewAdapter
import com.krake.core.widget.SnackbarUtils

/**
 * Type of [ContentCreationFragment] that permits to accept or decline the policies.
 * To show the list of policies related to an existing object, the model must implement [Policy].
 */
class PoliciesFragment : OrchardDataModelFragment(), ContentCreationFragment,
        PolicyAdapter.Listener,
        ObjectsRecyclerViewAdapter.ClickReceiver<Policy>,
        DialogInterface.OnDismissListener {

    companion object {

        private const val OTS_SELECTED_POLICY = "otsSelectedPolicy"

        /**
         * Creates a new instance of [PoliciesFragment] with arguments.
         *
         * @param context [Context] used to populate arguments.
         * @return [PoliciesFragment] with arguments set on it.
         */
        fun newInstance(context: Context): PoliciesFragment {
            val fragment = PoliciesFragment()

            val fullBundle = ComponentManager.createBundle()
                    .from(context)
                    .with(OrchardComponentModule()
                            .dataClass(ClassUtils.dataClassForName(context.getString(R.string.policy_text_class_name)))
                            .displayPath(context.getString(R.string.orchard_all_policies_path))
                                  .startConnectionAfterActivityCreated(false)
                                  .webServiceUrl(context.getString(R.string.orchard_base_service_url) + context.getString(R.string.orchard_policies_service)))

                    .build()

            fragment.arguments = fullBundle
            return fragment
        }
    }

    private var contentCreationActivity: ContentCreationActivity? = null
    private lateinit var policyInfo: ContentCreationTabInfo.PolicyInfo
    private lateinit var policies: MutableList<Policy>
    private lateinit var policiesText: List<PolicyText>

    private val policyAdapter by lazy {
        PolicyAdapter(activity ?:
                throw IllegalArgumentException("The activity mustn't be null."), this)
    }
    private var selectedPolicyId: Long? = null

    override fun onAttach(activity: Context?)
    {
        super.onAttach(context)
        contentCreationActivity = context as? ContentCreationActivity
    }

    override fun onDetach() {
        contentCreationActivity = null
        super.onDetach()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        policyInfo = contentCreationActivity!!.getFragmentCreationInfo(this) as ContentCreationTabInfo.PolicyInfo
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_policies, container, false)

    @Suppress("UNCHECKED_CAST")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // The restore of the selected policy can't be done actually because "savedInstanceState"
        // is always null due to the super.onCreate() of the ContentCreationActivity.
        // Equality check is necessary with a nullable boolean.
        if (savedInstanceState?.containsKey(OTS_SELECTED_POLICY) == true) {
            // Restore the selected policy.
            selectedPolicyId = savedInstanceState.getLong(OTS_SELECTED_POLICY)
        }

        val recyclerView = view.findViewById<RecyclerView>(android.R.id.list)
        // Creates the RecyclerView that will show the policies.
        recyclerView.adapter = policyAdapter
        policyAdapter.defaultClickReceiver = this

        val previousPolicies = contentCreationActivity?.getFragmentData(this) as? MutableList<Policy>
        policies = if (previousPolicies != null && previousPolicies.isNotEmpty()) {
            // Get the policies from the data saved in the Activity.
            previousPolicies
        } else {
            val originalObj = contentCreationActivity?.originalObject
            val dataKey = policyInfo.dataKey
            if (originalObj != null && dataKey != null) {
                // Get the policies from DB.
                ClassUtils.getValueInDestination(StringUtils.methodName(null,
                        dataKey,
                        null,
                        StringUtils.MethodType.GETTER),
                        originalObj) as MutableList<Policy>
            } else {
                mutableListOf()
            }
        }

        orchardComponentModule.putExtraParameter(getString(R.string.policy_type_parameter), policyInfo.type)
        dataConnectionModel.restartDataLoading()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selectedPolicyId?.let {
            // Save the current selected policy if any.
            outState.putLong(OTS_SELECTED_POLICY, it)
        }
    }

    override fun onDataModelChanged(dataModel: DataModel?)
    {
        if (dataModel != null)
        {
            // Filter the list only with policies.
            policiesText = dataModel.listData.filterIsInstance(PolicyText::class.java)

            // If the policies are loaded from shared preferences or database, they could
            // contain invalid ids related to some PolicyText items that aren't returned from the WS anymore.
            // So, they must be filtered to select only valid policies.
            policies = policies.filter { policy ->
                policiesText.find { policyText ->
                    policyText.identifier == policy.policyTextInfoPartRecordIdentifier
                } != null
            }.toMutableList()

            // Use an index to maintain the original policies' order.
            var index = 0
            policiesText.forEach {
                val policyId = it.identifier
                val addedPolicy = policies.find { it.policyTextInfoPartRecordIdentifier == policyId }

                if (addedPolicy == null)
                {
                    // The policy must be added only if there wasn't a policy with the same id.
                    // The index is used to maintain the original sort order.
                    policies.add(index, TempPolicy(policyId, it.policyTextInfoPartUserHaveToAccept))
                }

                if (index < policiesText.size)
                {
                    // Increment the index only if the index is in the size range to avoid an exception.
                    index++
                }
            }

            // Change the list in the adapter.
            policyAdapter.swapList(policies, true)
            persistData()

            selectedPolicyId?.let { selectedId ->
                // Find the selected policy.
                val policy = policies.find { it.policyTextInfoPartRecordIdentifier == selectedId }
                policy?.let {
                    // Show its detail.
                    showPolicyDetail(it)
                }
            }
        }
    }

    override fun policyTitle(policy: Policy): String =
            policiesText.first { it.identifier == policy.policyTextInfoPartRecordIdentifier }.titlePartTitle!!

    override fun policyToggled(policy: Policy) {
        // Change the value of the accepted policies.
        persistData()
    }

    override fun validateDataAndSaveError(activity: ContentCreationActivity, creationInfo: ContentCreationTabInfo.ContentCreationInfo, savedInfos: Any?): Boolean {
        val arePoliciesAccepted = allRequiredPoliciesAccepted()

        val rootView = contentCreationActivity?.findViewById<View>(R.id.activity_layout_coordinator)
        if (!arePoliciesAccepted && rootView != null) {
            // Show the error.
            SnackbarUtils.createSnackbar(rootView, R.string.privacy_required_to_show_content, Snackbar.LENGTH_LONG).show()
        }

        return arePoliciesAccepted
    }

    override fun insertDataToUpload(activity: ContentCreationActivity, creationInfo: ContentCreationTabInfo.ContentCreationInfo, savedInfos: Any?, parameters: JsonObject): Boolean {
        val policiesJson = JsonArray()
        policies.forEach {
            val policyObj = JsonObject()
            policyObj.addProperty("Id", it.policyTextInfoPartRecordIdentifier)
            policyObj.addProperty("Accepted", it.accepted)
            policiesJson.add(policyObj)
        }
        parameters.add(policyInfo.orchardKey, policiesJson)
        return true
    }

    override fun deserializeSavedInstanceState(activity: ContentCreationActivity, creationInfo: ContentCreationTabInfo.ContentCreationInfo, gson: Gson, serializedInfos: String?): Any =
            gson.fromJson(serializedInfos, object : TypeToken<List<Policy>>() {}.type)

    override fun onViewClicked(recyclerView: RecyclerView, view: View, position: Int, item: Policy) {
        showPolicyDetail(item)
    }

    override fun onDismiss(dialog: DialogInterface?) {
        // When the dialog is dismissed, reset the id of the selected policy.
        selectedPolicyId = null
    }

    private fun showPolicyDetail(item: Policy) {
        val policyId = item.policyTextInfoPartRecordIdentifier
        // Save the current selected policy to restore it back later.
        selectedPolicyId = policyId
        val policyText = policiesText.first { it.identifier == policyId }

        // Show the body part of the policy in a WebView.
        val webView = View.inflate(context, R.layout.fragment_detail_policy, null) as ContentItemWebView
        webView.show(policyText, true)

        activity?.let {
            // Show a dialog containing the policy.
            AlertDialog.Builder(it)
                    .setTitle(policyText.titlePartTitle!!)
                    .setView(webView)
                    .setCancelable(true)
                    .setOnDismissListener(this)
                    .show()
        }
    }

    private fun allRequiredPoliciesAccepted(): Boolean = policies.find {
        it.policyTextInfoPartRecordUserHaveToAccept && !it.accepted
    } == null

    private fun persistData() {
        contentCreationActivity?.updateFragmentData(this, policies)
    }

    private class TempPolicy(override val policyTextInfoPartRecordIdentifier: Long,
                             override val policyTextInfoPartRecordUserHaveToAccept: Boolean,
                             override var accepted: Boolean = false) : Policy, Modifiable

    override fun onDataLoadingError(orchardError: OrchardError)
    {
    }
}