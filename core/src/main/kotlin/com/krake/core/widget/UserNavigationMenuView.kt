package com.krake.core.widget

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.krake.core.*
import com.krake.core.app.LoginAndPrivacyActivity
import com.krake.core.component.module.LoginComponentModule
import com.krake.core.component.module.OrchardComponentModule
import com.krake.core.data.DataConnectionModel
import com.krake.core.data.DataModel
import com.krake.core.login.LoginManager
import com.krake.core.media.loader.MediaLoader
import com.krake.core.media.widget.LoadableCircleImageView
import com.krake.core.model.User
import com.krake.core.network.RemoteRequest
import com.krake.core.network.RemoteResponse

/**
 * View to show shor info on user.
 * On click if user is not logged the login will be presented
 * The Application must implement UserNavigationViewListener to enable an action when logged user clicks
 * on the avatar
 * Created by joel on 01/08/17.
 */
class UserNavigationMenuView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), Observer<DataModel>, OrchardApiEndListener
{
    private lateinit var userImageView: LoadableCircleImageView
    private lateinit var usernameTextView: TextView
    private lateinit var logoutButton: ImageButton
    private lateinit var userInitialsText: TextView

    private var dataConnection: DataConnectionModel? = null

    private var currentUser: User? = null
    private var userLoggedIn: Boolean = false

    private val hideLoginButton: Boolean

    private var loginObserver = object : Observer<Boolean?>
    {
        override fun onChanged(t: Boolean?)
        {
            if (t != null)
            {
                if ((t && currentUser == null) || !t)
                {
                    updateUI(t, null)
                }
            }
        }
    }

    init
    {
        val a = context.obtainStyledAttributes(attrs, R.styleable.UserNavigationMenuView, 0, 0)

        hideLoginButton = a.getBoolean(R.styleable.UserNavigationMenuView_hide_logout_button, false)

        a.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        userImageView = findViewById(R.id.userImageView)
        usernameTextView = findViewById(R.id.usernameTextView)
        logoutButton = findViewById<ImageButton>(R.id.logoutButton)
        userInitialsText = findViewById(R.id.userInitialPlaceholder)
        updateUI(false, null)

        val color = ResourcesCompat.getColor(resources, R.color.colorPrimary, null)
        if (ColorUtils.calculateLuminance(color) > 0.505)
            userInitialsText.setTextColor(Color.BLACK)
        else
            userInitialsText.setTextColor(Color.WHITE)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        logoutButton.setOnClickListener { LoginManager.shared.logout() }

        userImageView.setOnClickListener { loginOrEditUser() }
        usernameTextView.setOnClickListener { loginOrEditUser() }

        val privacyViewModel = ViewModelProviders.of(context as FragmentActivity).get(PrivacyViewModel::class.java)

        dataConnection = DataConnectionModel(OrchardComponentModule()
                                                     .displayPath(getString(R.string.orchard_user_info_display_path)),
                                             LoginComponentModule().loginRequired(true),
                                             privacyViewModel
        )

        dataConnection?.model?.observeForever(this)
        LoginManager.shared.isLogged.observeForever(loginObserver)
        if (LoginManager.shared.isLogged.value == true) {
            updateUI(true, currentUser)
        }
        Signaler.shared.registerApiEndListener(getString(R.string.orchard_api_path_content_modify), this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        logoutButton.setOnClickListener(null)
        userImageView.setOnClickListener(null)
        usernameTextView.setOnClickListener(null)
        dataConnection?.model?.removeObserver(this)
        LoginManager.shared.isLogged.removeObserver(loginObserver)
        Signaler.shared.removeApiEndListener(getString(R.string.orchard_api_path_content_modify), this)
    }


    override fun onChanged(t: DataModel?)
    {
        if (t != null)
        {
            updateUI(true, t.listData.firstOrNull() as? User)
        }
    }

    override fun onApiInvoked(context: Context, remoteRequest: RemoteRequest, remoteResponse: RemoteResponse?, e: OrchardError?, endListenerParameters: Any?)
    {

        if (currentUser != null &&
                remoteRequest.method == RemoteRequest.Method.POST &&
                remoteResponse != null &&
                endListenerParameters is Bundle &&
                endListenerParameters.getString(getString(R.string.orchard_new_content_type_parameter))?.equals(getString(R.string.orchard_user_content_type)) ?: false)
        {
            dataConnection?.loadDataFromRemote()
        }
    }

    private fun loginOrEditUser() {
        if (!userLoggedIn)
        {
            (context as? LoginAndPrivacyActivity)?.showLoginFragment()
        }
        else if (currentUser != null)
        {
            (context.applicationContext as? UserNavigationViewListener)?.
                    userDidClick(this)
        }
    }

    private fun updateUI(loggedIn: Boolean, user: User?)
    {
        currentUser = user
        userLoggedIn = loggedIn

        when {
            user != null -> {
                val photo = user.firstPhoto
                if (photo != null) {
                    MediaLoader.with(context, userImageView)
                        .mediaPart(photo)
                        .load()

                    userInitialsText.visibility = View.GONE
                } else {
                    userImageView.setImageBitmap(null)
                    userInitialsText.visibility = View.VISIBLE
                }

                logoutButton.visibility = View.VISIBLE

                val nameSb = StringBuilder()
                val firstLettersSb = StringBuilder()

                val name = user.name
                if (!name.isNullOrEmpty()) {
                    nameSb.append(name)
                    firstLettersSb.append(name.first())

                    val surname = user.surname
                    if (!surname.isNullOrEmpty()) {
                        nameSb.append(" ").append(surname)
                        firstLettersSb.append(surname.first())
                    }
                } else {
                    val anonymousLabel = getString(R.string.usernamePlaceholder)
                    nameSb.append(anonymousLabel)
                    firstLettersSb.append(anonymousLabel.first())
                }
                usernameTextView.text = nameSb.toString()
                userInitialsText.text = firstLettersSb.toString()
            }
            loggedIn -> {
                userImageView.setImageResource(R.drawable.user_image_placeholder)
                userInitialsText.visibility = View.GONE
                logoutButton.visibility = View.VISIBLE
                usernameTextView.text = getString(R.string.loading_data)
            }
            else -> {
                userImageView.setImageResource(R.drawable.user_image_placeholder)
                userInitialsText.visibility = View.GONE
                logoutButton.visibility = View.GONE
                usernameTextView.text = getString(R.string.login)
            }
        }

        if (hideLoginButton)
            logoutButton.visibility = View.GONE

    }
}

/**
 * The interface must be implemented by the Application class,
 * to enable a reaction when a logged user clicks on it's avatar
 */
interface UserNavigationViewListener {
    fun userDidClick(onView: UserNavigationMenuView)
}