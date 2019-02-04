package com.krake.core.media.loader.glide

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.Excludes
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpLibraryGlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.krake.core.network.OkHttpRemoteClient
import com.krake.core.network.RemoteClient
import java.io.InputStream

@Excludes(OkHttpLibraryGlideModule::class)
@GlideModule
/**
 * glide module that permits the use of the logged [OkHttpRemoteClient] if available
 * for download the images
 */
class OkHttpGlideModule : AppGlideModule()
{
    override fun isManifestParsingEnabled(): Boolean = false

    override fun registerComponents(context: Context, glide: Glide, registry: Registry)
    {
        val loggedClient = RemoteClient.clients[RemoteClient.Mode.LOGGED] as? OkHttpRemoteClient
        val factory = if (loggedClient != null)
        {
            OkHttpUrlLoader.Factory(loggedClient.client)
        }
        else
        {
            OkHttpUrlLoader.Factory()
        }
        registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
    }
}