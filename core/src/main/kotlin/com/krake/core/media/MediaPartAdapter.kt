package com.krake.core.media

import android.app.Activity
import android.content.ComponentCallbacks
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import com.krake.core.media.loader.MediaLoader
import com.krake.core.model.MediaPart
import com.krake.core.widget.ImageViewHolder
import com.krake.core.widget.ObjectsRecyclerViewAdapter
import java.lang.ref.WeakReference

/**
 * Created by joel on 15/03/17.
 */

/**
 * Adapter utilizzato per mostrare i media in una RecyclerView.
 * <br></br>
 * I media vengono caricati tramite la classe [MediaLoader]
 */
open class MediaPartAdapter : ObjectsRecyclerViewAdapter<MediaPart, ImageViewHolder> {
    private val callerRef: WeakReference<ComponentCallbacks>

    constructor(fragment: Fragment, @LayoutRes resource: Int) : super(fragment.activity ?:
            throw IllegalArgumentException("The activity mustn't be null."), resource, null, ImageViewHolder::class.java) {
        callerRef = WeakReference(fragment)
    }

    constructor(activity: Activity, @LayoutRes resource: Int) : super(activity, resource, null, ImageViewHolder::class.java) {
        callerRef = WeakReference(activity)
    }

    override fun onBindViewHolder(imageViewHolder: ImageViewHolder, i: Int) {
        val mediaLoadable = imageViewHolder.imageView as MediaLoadable
        val mediaPart = getItem(i)

        val caller = callerRef.get()
        val loader = when (caller) {
            is Fragment -> MediaLoader.with(caller, mediaLoadable)
            is Activity -> MediaLoader.with(caller, mediaLoadable)
            else -> null
        }

        mediaPart?.let {
            loader?.mediaPart(it)
        }

        loader?.load()
    }
}