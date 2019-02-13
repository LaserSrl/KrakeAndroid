package com.krake.core.social

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.annotation.StringRes

object SocialItems
{
    @IntDef(FACEBOOK, TWITTER, PINTEREST, INSTANGRAM, WEBSITE, MAIL, CALL)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class Type

    const val FACEBOOK = 1
    const val TWITTER = 2
    const val PINTEREST = 3
    const val INSTANGRAM = 4
    const val WEBSITE = 5
    const val MAIL = 6
    const val CALL = 7

    open class SocialToolbarItem(@StringRes val name: Int, @DrawableRes val image: Int, var value: String)
    {

        open fun doWork(context: Context)
        {
            val value = getSocialUriViewAction(context)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(value))
            if (intent.resolveActivity(context.packageManager) != null)
            {
                context.startActivity(intent)
            }
        }

        protected open fun getSocialUriViewAction(context: Context): String = value
    }

    open class FacebookSocialToolbarItem(name: Int, image: Int, value: String) : SocialToolbarItem(name, image, value)
    {
        override fun getSocialUriViewAction(context: Context): String
        {
            return try
            {
                context.packageManager.getPackageInfo("com.facebook.katana", 0)
                "fb://facewebmodal/f?href=$value"
            }
            catch (ex: Exception)
            {
                //default
                value
            }
        }
    }

    open class MailSocialToolbarItem(name: Int, image: Int, value: String) : SocialToolbarItem(name, image, value)
    {
        override fun getSocialUriViewAction(context: Context): String = "mailto:$value"
    }

    open class CallSocialToolbarItem(name: Int, image: Int, value: String) : SocialToolbarItem(name, image, value)
    {
        override fun getSocialUriViewAction(context: Context): String = "tel:" + value.replace(" ".toRegex(), "")
    }

}