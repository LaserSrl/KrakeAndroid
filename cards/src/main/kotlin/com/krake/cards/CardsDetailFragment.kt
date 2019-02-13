package com.krake.cards

import android.Manifest
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.ContactsContract
import android.text.InputType
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.collection.ArrayMap
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.krake.core.OrchardError
import com.krake.core.Signaler
import com.krake.core.app.AnalyticsApplication
import com.krake.core.app.ContentItemDetailModelFragment
import com.krake.core.contacts.ContactInfo
import com.krake.core.contacts.ContactInfoManager
import com.krake.core.model.ContentItem
import com.krake.core.model.ContentItemWithGallery
import com.krake.core.model.identifierOrStringIdentifier
import com.krake.core.network.RemoteResponse
import com.krake.core.permission.PermissionListener
import com.krake.core.permission.PermissionManager
import com.krake.core.util.LayoutUtils
import com.krake.core.widget.*
import java.util.*

/**
 * Created by antoniolig on 04/05/2017.
 */
open class CardsDetailFragment : ContentItemDetailModelFragment(),
        View.OnClickListener,
        ObjectsRecyclerViewAdapter.ClickReceiver<ContactInfo>,
        CardsAutoCompleteTextView.CardsTextViewPickListener,
        Handler.Callback,
        PermissionListener {

    companion object {
        private const val STATE_SENDER_CONTACT_INFOS = "ContactInfos"
        private const val STATE_RECEIVER_CONTACT_INFOS = "ReceiverInfos"
        private const val STATE_SENDING_POSTCARD = "SendingPostcard"

        private const val MAIL_REGEX = "^(([A-Za-z0-9]+_+)|([A-Za-z0-9]+-+)|([A-Za-z0-9]+\\.+)|([A-Za-z0-9]+\\++))*[A-Za-z0-9]+@((\\w+-+)|(\\w+\\.))*\\w{1,63}\\.[a-zA-Z]{2,6}$"
        private const val MAIL_VALID = 1
        private const val MAIL_INVALID = 2
        private const val MAIL_MISSING = 3
        private const val CLOSE_FRAGMENT_MESSAGE = 283092
        private const val CONTACT_LOADER_ID = 10500
    }

    private lateinit var senderEditText: CardsAutoCompleteTextView
    private lateinit var receiverEditText: CardsAutoCompleteTextView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var contactListView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var permissionManager: PermissionManager
    private var pickContactTargetTextView: CardsAutoCompleteTextView? = null

    private var card: ContentItemWithGallery? = null
    private lateinit var contactsBehavior: BottomSheetNotUnderActionBehavior<*>
    private val allContacts = LinkedList<ContactInfo>()
    private var contactsAdapter: ContactAdapter? = null
    private lateinit var contactsLoader: ContactPickerLoader
    private var sendingPostcard: Boolean = false
    @Suppress("LeakingThis")
    private val closeHandler = Handler(this)
    private var inputType = InputType.TYPE_NULL

    private val focusListener = View.OnFocusChangeListener { v, hasFocus ->
        if (contactsBehavior.state == BottomSheetNotUnderActionBehavior.STATE_EXPANDED && hasFocus) {
            disableKeyboardAppear(v as CardsAutoCompleteTextView)
        }
    }

    //TODO: capire se ha senso permettere ancora questo. Perché rende impossibile modificarlo dall'esterno
    override val contentLayoutIdentifier: Int
        get() = R.layout.fragment_cards

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionManager = PermissionManager(this)
                .permissions(Manifest.permission.READ_CONTACTS)
                .addListener(this)

        permissionManager.create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        senderEditText = view!!.findViewById(R.id.senderEditText)
        senderEditText.setAdapter(FilterableContactInfoAdapter(activity!!, allContacts))
        senderEditText.setListener(this)
        senderEditText.onFocusChangeListener = focusListener

        receiverEditText = view.findViewById(R.id.receiverEditText)
        receiverEditText.setAdapter(FilterableContactInfoAdapter(activity!!, allContacts))
        receiverEditText.setListener(this)
        receiverEditText.onFocusChangeListener = focusListener
        progressBar = view.findViewById(android.R.id.progress)
        progressBar.visibility = View.GONE

        messageEditText = view.findViewById(R.id.messageEditText)

        sendButton = view.findViewById(R.id.sendButton)

        sendButton.setOnClickListener(this)

        contactListView = inflater.inflate(R.layout.card_pick_contacts_list_view, mCoordinator, false) as RecyclerView
        mCoordinator.addView(contactListView)
        contactsBehavior = (contactListView.layoutParams as CoordinatorLayout.LayoutParams).behavior as BottomSheetNotUnderActionBehavior<*>

        contactsBehavior.addBottomSheetCallback(object : SafeBottomSheetBehavior.BottomSheetStateCallback() {
            override fun onStateWillChange(bottomSheet: View, newState: Int) {}

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetNotUnderActionBehavior.STATE_COLLAPSED && pickContactTargetTextView != null && inputType != InputType.TYPE_NULL) {
                    pickContactTargetTextView!!.inputType = inputType
                    inputType = InputType.TYPE_NULL
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        addSheetCallback(contactsBehavior)
        contactsAdapter = ContactAdapter(activity!!, allContacts)
        contactsAdapter?.defaultClickReceiver = this

        contactListView.adapter = contactsAdapter
        contactsLoader = ContactPickerLoader()

        permissionManager.request()


        if (savedInstanceState != null) {
            sendingPostcard = savedInstanceState.getBoolean(STATE_SENDING_POSTCARD)

            (savedInstanceState.getString(STATE_RECEIVER_CONTACT_INFOS))?.let {
                receiverEditText.infos = Gson().fromJson(it, ContactInfo::class.java)
            }

            (savedInstanceState.getString(STATE_SENDER_CONTACT_INFOS))?.let {
                senderEditText.infos = Gson().fromJson(it, ContactInfo::class.java)
            }
            updateUI(sendingPostcard)
        }

        if (senderEditText.infos == null) {
            ContactInfoManager.readUserInfo(activity!!)?.let {
                senderEditText.infos = it
            }
        }

        return view
    }

    @CallSuper
    override fun loadDataInUI(contentItem: ContentItem, cacheValid: Boolean) {
        super.loadDataInUI(contentItem, cacheValid)
        card = contentItem as ContentItemWithGallery
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean(STATE_SENDING_POSTCARD, sendingPostcard)

        senderEditText.infos?.let {
            outState.putString(STATE_SENDER_CONTACT_INFOS, Gson().toJson(it))
        }

        receiverEditText.infos?.let {
            outState.putString(STATE_RECEIVER_CONTACT_INFOS, Gson().toJson(it))
        }
    }

    override fun onClick(v: View?) {
        if (v === sendButton) {
            sendPostcard()
        }
    }

    private fun updateUI(sendingPostcard: Boolean) {
        senderEditText.isEnabled = !sendingPostcard

        receiverEditText.isEnabled = !sendingPostcard

        messageEditText.isEnabled = !sendingPostcard

        sendButton.isEnabled = !sendingPostcard

        if (sendingPostcard) {
            progressBar.visibility = View.VISIBLE
        } else
            progressBar.visibility = View.INVISIBLE
    }

    fun sendPostcard() {
        val activity = activity ?: throw NullPointerException("The activity mustn't be null.")
        LayoutUtils.hideKeyboard(activity, mCoordinator)

        var canSendCard = true

        if (isMailValid(senderEditText) != MAIL_VALID) {
            canSendCard = false
            senderEditText.setErrorInThisOrInputLayout(getString(R.string.sender_mail_required))
        } else
            senderEditText.setErrorInThisOrInputLayout(null)


        if (isMailValid(receiverEditText) != MAIL_VALID) {
            canSendCard = false
            receiverEditText.setErrorInThisOrInputLayout(getString(R.string.receiver_mail_required))
        } else
            receiverEditText.setErrorInThisOrInputLayout(null)


        if (TextUtils.isEmpty(messageEditText.text.toString())) {
            canSendCard = false
            messageEditText.setErrorInThisOrInputLayout(getString(R.string.missing_message_error))
        } else
            messageEditText.setErrorInThisOrInputLayout(null)

        if (canSendCard && card != null) {

            val parameters = ArrayMap<String, String>()

            parameters.put("MessageText", messageEditText.text.toString())
            insertContactParameters(senderEditText, "SendFrom", "SenderName", parameters)
            if (senderEditText.infos != null) {
                ContactInfoManager.updateUserInfo(activity, senderEditText.infos!!)
            } else {
                ContactInfoManager.updateUserInfo(activity, ContactInfo(null, senderEditText.text.toString(), null))
            }

            val cardId = card?.identifierOrStringIdentifier
            insertContactParameters(receiverEditText, "SendTo", "RecipeName", parameters)
            parameters.put("ContentId", cardId)

            Signaler.shared.sendSignal(getActivity()!!,
                                       getString(R.string.orchard_signal_send_postcard),
                                       parameters,
                                       false,
                                       object : (RemoteResponse?, OrchardError?) -> Unit
                                       {
                                           override fun invoke(p1: RemoteResponse?, p2: OrchardError?)
                                           {
                                               sendingPostcard = false
                                               updateUI(sendingPostcard)
                                               if (p1 != null)
                                               {
                                                   SnackbarUtils.showCloseSnackbar(mCoordinator, R.string.card_send_correctly, closeHandler, CLOSE_FRAGMENT_MESSAGE)
                                               }
                                               else if (p2 != null)
                                               {
                                                   SnackbarUtils.createSnackbar(mCoordinator, p2.getUserFriendlyMessage(activity), Snackbar.LENGTH_LONG).show()
                                               }
                                           }
                                       })



            val b = Bundle()
            b.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Cartolina")

            b.putString(FirebaseAnalytics.Param.ITEM_ID, cardId)

            (activity.application as AnalyticsApplication).logEvent("postcard_sent", b)

            sendingPostcard = true
            updateUI(sendingPostcard)
        }
    }

    private fun isMailValid(autoComplete: CardsAutoCompleteTextView): Int {
        if (autoComplete.infos != null) {
            return MAIL_VALID
        } else {
            val enteredMail = autoComplete.text.toString()
            if (TextUtils.isEmpty(enteredMail)) {
                return MAIL_MISSING
            } else {
                if (enteredMail.matches(MAIL_REGEX.toRegex()))
                    return MAIL_VALID
                else
                    return MAIL_INVALID
            }
        }
    }

    override fun onViewClicked(recyclerView: RecyclerView, view: View, position: Int, item: ContactInfo)
    {
        if (recyclerView === contactListView)
        {
            if (pickContactTargetTextView != null) {
                pickContactTargetTextView!!.infos = item
                pickContactTargetTextView = null

                contactsBehavior.setStateAndNotify(BottomSheetBehavior.STATE_COLLAPSED)
            }
        }
    }

    private fun insertContactParameters(editText: CardsAutoCompleteTextView, mailKey: String, nameKey: String, parameters: MutableMap<String, String?>) {
        val contactInfo = editText.infos
        if (contactInfo != null) {
            parameters.put(mailKey, contactInfo.mail)
            parameters.put(nameKey, contactInfo.name)
        } else {
            val insertedText = editText.text.toString()

            parameters.put(mailKey, insertedText)

            val charIndex = insertedText.indexOf("@")
            if (charIndex != -1)
                parameters.put(nameKey, insertedText.substring(0, charIndex))
            else
                parameters.put(nameKey, insertedText)
        }
    }

    override fun pickContact(textView: CardsAutoCompleteTextView) {
        disableKeyboardAppear(textView)
        contactsBehavior.setStateAndNotify(BottomSheetBehavior.STATE_EXPANDED)
    }

    override fun handleMessage(message: Message): Boolean {
        if (message.what == CLOSE_FRAGMENT_MESSAGE) {
            closeListener?.onCloseDetails(this@CardsDetailFragment)
        }
        return false
    }

    override fun onPermissionsHandled(acceptedPermissions: Array<out String>)
    {
        if (acceptedPermissions.contains(Manifest.permission.READ_CONTACTS))
        {
            loaderManager.initLoader(CONTACT_LOADER_ID, Bundle(), contactsLoader)
        }
    }

    private fun disableKeyboardAppear(textView: CardsAutoCompleteTextView) {
        val activity = activity ?: throw IllegalArgumentException("The activity mustn't be null.")
        pickContactTargetTextView = textView
        inputType = textView.inputType
        textView.inputType = InputType.TYPE_NULL
        LayoutUtils.hideKeyboard(activity, textView)
    }

    private inner class ContactPickerLoader : LoaderManager.LoaderCallbacks<Cursor> {

        override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<Cursor>
        {
            val uri = ContactsContract.CommonDataKinds.Email.CONTENT_URI
            val projection = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Email.DATA)
            val selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + "=1"
            //showing only visible contacts
            val selectionArgs: Array<String>? = null
            val sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC"

            return CursorLoader(
                activity!!,
                uri,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )
        }

        override fun onLoadFinished(objectLoader: Loader<Cursor>, o: Cursor) {
            allContacts.clear()
            val displayNameIndex = o.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val mailIndex = o.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)

            if (o.moveToFirst()) {
                do {
                    val name = o.getString(displayNameIndex)
                    val mail = o.getString(mailIndex)

                    if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(mail)) {
                        allContacts.add(ContactInfo(name, mail, null))
                    }
                } while (o.moveToNext())
            }

            contactsAdapter?.swapList(allContacts, true)
        }

        override fun onLoaderReset(objectLoader: Loader<Cursor>) {

        }
    }
}


class ContactAdapter(context: Context, objects: List<ContactInfo>?) :
        ObjectsRecyclerViewAdapter<ContactInfo, ContactViewHolder>(context, R.layout.contact_mail_cell, objects, ContactViewHolder::class.java)
{
    override fun onBindViewHolder(holder: ContactViewHolder, position: Int)
    {
        holder.contactTextView?.text = getItem(position)?.toString()
    }
}

class ContactViewHolder(itemView: View) : GestureViewHolder(itemView), ViewHolderWithClickGesture
{
    val contactTextView = itemView.findViewById<TextView>(android.R.id.text1)
}