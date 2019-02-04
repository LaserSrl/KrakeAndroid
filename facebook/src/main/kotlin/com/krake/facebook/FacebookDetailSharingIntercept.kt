package com.krake.facebook

import android.content.Intent
import android.net.Uri
import android.support.v4.app.Fragment
import android.util.Patterns
import com.facebook.share.model.ShareContent
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.model.SharePhoto
import com.facebook.share.model.SharePhotoContent
import com.facebook.share.widget.ShareDialog
import com.krake.core.model.ShareLinkPart
import com.krake.core.social.DetailIntentSharingInterceptor

/**
 * Interceptor per l'intent di condivisione per Facebook.
 * <br></br>
 * Deve essere aggiunto nell'OrchardApplication con il metodo addDetailSharingIntent(DetailIntentSharingInterceptor)
 * <pre>
 * `<provider
 * android:authorities="com.facebook.app.FacebookContentProvider{APP_ID}"
 * android:name="com.facebook.FacebookContentProvider"
 * android:exported="true" />
` *
</pre> *
 */
class FacebookDetailSharingIntercept : DetailIntentSharingInterceptor {

    companion object {
        /**
         * Package di Facebook
         */
        private const val FACEBOOK_PACKAGE = "com.facebook.katana"
    }
    
    override fun handleSharingDetail(fragment: Fragment, shareLink: ShareLinkPart, mediaUri: Uri?, intent: Intent): Boolean {
        if (intent.component.packageName == FACEBOOK_PACKAGE) {
            val builder: ShareContent.Builder<*, *>?

            val link: String? = if (!shareLink.sharedLink.isNullOrEmpty() &&
                    Patterns.WEB_URL.matcher(shareLink.sharedLink!!).matches()) {
                shareLink.sharedLink!!
            } else null

            // su Facebook viene condivisa unicamente l'immagine oppure il link con un possibile testo custom
            builder = when {
                mediaUri != null -> {
                    val sharePhoto = SharePhoto.Builder()
                            .setImageUrl(mediaUri)
                            .build()

                    SharePhotoContent.Builder()
                            .addPhoto(sharePhoto)

                }
                link != null -> ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse(link))
                else -> null
            }

            if (builder != null) {
                // mostra il dialog della condivisione se il builder è stato inizializzato
                ShareDialog(fragment).show(builder.build() as ShareContent<*, *>, ShareDialog.Mode.AUTOMATIC)
                return true
            }
        }
        return false
    }
}
