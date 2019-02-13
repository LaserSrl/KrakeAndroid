package com.krake.core.widget

import android.annotation.TargetApi
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import com.google.gson.JsonObject
import com.krake.core.OrchardError
import com.krake.core.R
import com.krake.core.Signaler
import com.krake.core.app.LoginAndPrivacyActivity
import com.krake.core.login.LoginManager
import com.krake.core.model.ContentItem
import com.krake.core.model.RecordWithIdentifier
import com.krake.core.network.RemoteRequest
import com.krake.core.network.RemoteResponse

/**
 * Classe che gestice le User Reaction.
 * Le user reaction devono essere aggangiate ai contenuti lato BO di Orchard ed è necessario abilitare le
 * reaction che si vogliono per il singolo tipo di contenuto.
 *
 * La view fa l'inflate di R.styleable.UserReactionView_rootLayout (default R.layout.user_reaction_root_layout)
 * come radice della siua view. La view radice deve contenere una subView con id R.id.userReactionRootLayout
 * che deve ereditare da view group. All'interno di questa saranno inserite le singole view per gestire le reaction.
 *
 * La singola view per gestire la reaction è presa da R.styleable.UserReactionView_elementLayout (default R.layout.user_reaction_element).
 * Viene configurata dalla classe di libreria solo se è un Button. Altrimenti sarà necessario fare overwrite di
 * createViewForReaction()
 */
open class UserReactionView : FrameLayout,
        ContentItemView,
        View.OnClickListener, Observer<Boolean>
{


    override lateinit var container: ContentItemViewContainer

    private lateinit var inflater: LayoutInflater
    private var elementLayout = 0

    private lateinit var rootView: ViewGroup
    private var mUserAuthenticated = false
    private var mUserAuthorized = true
    private var contentIdentifier: Long = 0
    private var tintColorList: Int = 0

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        createContentView(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        createContentView(context, attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        createContentView(context, attrs)

    }

    private fun createContentView(context: Context, attrs: AttributeSet) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.UserReactionView, 0, 0)

        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        inflater.inflate(a.getResourceId(R.styleable.UserReactionView_rootLayout, R.layout.user_reaction_root_layout), this, true)

        rootView = findViewById(R.id.userReactionRootLayout)

        elementLayout = a.getResourceId(R.styleable.UserReactionView_elementLayout, R.layout.user_reaction_element)

        tintColorList = a.getResourceId(R.styleable.UserReactionView_elementColorList, R.color.user_reaction_color)

        this.visibility = View.GONE

        a.recycle()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        LoginManager.shared.isLogged.observeForever(this)
        onChanged(LoginManager.shared.isLogged.value)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        LoginManager.shared.isLogged.removeObserver(this)
    }

    override fun show(contentItem: ContentItem, cacheValid: Boolean) {
        if (contentItem is RecordWithIdentifier) {
            if (contentIdentifier != contentItem.identifier) {
                contentIdentifier = contentItem.identifier
                loadUpdatedUserReaction()
            }
        } else {
            visibility = View.GONE
        }
    }

    private fun loadUpdatedUserReaction() {

        val request = RemoteRequest(context)
                .setMethod(RemoteRequest.Method.GET)
                .setQuery(getString(R.string.orchard_api_identifier_for_user_reaction), this.contentIdentifier.toString())
                .setPath(getString(R.string.orchard_api_user_reaction))

        Signaler.shared
                .invokeAPI(context,
                           request,
                           true,
                           callback = object : (RemoteResponse?, OrchardError?) -> Unit
                           {
                               override fun invoke(p1: RemoteResponse?, p2: OrchardError?)
                               {
                                   if (p1 != null)
                                   {
                                       handleApiCallResult(p1)
                                   }
                                   else if (p2 != null)
                                   {
                                       visibility = View.GONE
                                   }
                               }
                           })
    }

    protected fun loadDataFromReaction(reactionObject: JsonObject) {
        rootView.removeAllViews()

        if (reactionObject.get("ContentId").asLong == this.contentIdentifier) {
            val reactions = reactionObject.getAsJsonArray("Reactions")

            mUserAuthorized = reactionObject.get("UserAuthorized").asBoolean
            mUserAuthenticated = reactionObject.get("UserAuthenticated").asBoolean

            for (index in 0..reactions.size() - 1) {
                val reaction = reactions.get(index).asJsonObject
                val view = createViewForReaction(reaction)
                if (view != null) {
                    view.tag = reaction.get(REACTION_ID_KEY).asLong
                    view.setOnClickListener(this)
                    view.isEnabled = mUserAuthorized || !mUserAuthenticated

                    rootView.addView(view)
                }
            }

            this.visibility = View.VISIBLE
        } else {
            this.visibility = View.GONE
        }
    }

    open protected fun createViewForReaction(reaction: JsonObject): View? {

        val reactionName = reaction.get("TypeName").asString

        val resID = resources.getIdentifier("rc_" + reactionName, "drawable", context.packageName)
        if (resID != 0) {
            val v = inflater.inflate(elementLayout, this, false)

            if (v is Button) {

                var drawable: Drawable = ResourcesCompat.getDrawable(resources, resID, null)!!
                drawable = DrawableCompat.wrap(drawable)

                DrawableCompat.setTintList(drawable, ResourcesCompat.getColorStateList(resources, tintColorList, null))
                //drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                (v as AppCompatButton).setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null)
                v.text = reaction.get(REACTION_QUANTITY_KEY).asString

                v.setSelected(reaction.get(REACTION_CLICKED_KEY).asInt == 1)
            }

            return v
        }
        return null
    }

    override fun onClick(v: View) {
        if (mUserAuthorized) {

            val query = JsonObject()
            query.addProperty(getString(R.string.orchard_api_identifier_for_user_reaction), this.contentIdentifier)
            query.addProperty(getString(R.string.orchard_api_reaction_identifier), v.tag as Long)

            val request = RemoteRequest(context)
                    .setPath(getString(R.string.orchard_api_user_reaction))
                    .setMethod(RemoteRequest.Method.POST)
                    .setBody(query)

            Signaler.shared
                    .invokeAPI(context,
                               request,
                               true,
                               callback = object : (RemoteResponse?, OrchardError?) -> Unit
                               {
                                   override fun invoke(p1: RemoteResponse?, p2: OrchardError?)
                                   {
                                       if (p1 != null)
                                       {
                                           handleApiCallResult(p1)
                                       }
                                       else if (p2 != null)
                                       {
                                           SnackbarUtils.createSnackbar(this@UserReactionView,
                                                                        p2.getUserFriendlyMessage(context),
                                                                        Toast.LENGTH_SHORT)
                                       }
                                   }
                               })
        } else {
            (context as? LoginAndPrivacyActivity)?.showLoginFragment()
        }
    }

    private fun handleApiCallResult(remoteResponse: RemoteResponse)
    {
        var reactionObject = remoteResponse.jsonObject()?.getAsJsonObject("Data")
        if (reactionObject != null)
        {
            if (reactionObject.has("Status"))
                reactionObject = reactionObject.getAsJsonObject("Status")

            reactionObject?.let { loadDataFromReaction(it) }
        }
    }

    override fun onChanged(t: Boolean?)
    {
        if (t == true && !mUserAuthenticated && !mUserAuthorized)
        {
            loadUpdatedUserReaction()
        }
    }

    companion object {

        val REACTION_ID_KEY = "TypeId"
        val REACTION_QUANTITY_KEY = "Quantity"
        val REACTION_CLICKED_KEY = "Clicked"
    }
}
