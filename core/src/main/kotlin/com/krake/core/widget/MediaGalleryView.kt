package com.krake.core.widget

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.krake.core.R
import com.krake.core.media.MediaDetailsAdapter
import com.krake.core.media.MediaSelectionListener
import com.krake.core.model.ContentItem
import com.krake.core.model.ContentItemWithGallery
import com.krake.core.model.MediaPart
import java.lang.ref.WeakReference

/**
 * View per gestire la gallery di un elemento.
 * L'elemento deve essere di classe ContentItemWithGallery, in caso contrario non sarà mostrata la gallery.
 * Le celle sono mostrate in base agli attributi della view.
 * <ul>
 *     <li>R.styleable.MediaDetailGallery_mediaCell: elemento della cella da utilizzare per mostrare la cella.
 *     Default: R.layout.detail_media_cell. La cella deve includere un'image view con R.id.cellImageView</li>
 *     <li>R.styleable.MediaDetailGallery_adapterClass: nome della classe da utilizzare per adapter.
 *     Se non indicato sarà utilizzato MediaDetailsAdapter. La classe deve estendere ListRecyclerViewAdapter<MediaPart, *>
 *         ed avere un costruttore pubblico con Activity, int: layout della cella
 * </ul>
 *
 *  L'activity che include la view deve implementare l'interfaccia {@link MediaSelectionListener}
 *  per poter mostrare a schermo intero le gallery
 */
open class MediaGalleryView : RecyclerView, ContentItemView {

    override lateinit var container: ContentItemViewContainer

    private lateinit var mediaPartAdapter: ObjectsRecyclerViewAdapter<MediaPart, *>
    private val mMediaSelectionListener: WeakReference<MediaSelectionListener>
    private var collapseAppbarContainer = false

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        readAttributes(context, attrs)
        mMediaSelectionListener = WeakReference(getActivity() as MediaSelectionListener)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        readAttributes(context, attrs)
        mMediaSelectionListener = WeakReference(getActivity() as MediaSelectionListener)
    }

    private fun readAttributes(context: Context, attrs: AttributeSet) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MediaDetailGallery, 0, 0)

        @LayoutRes val mediaCell = a.getResourceId(R.styleable.MediaDetailGallery_mediaCell, R.layout.detail_media_cell)

        val className = a.getString(R.styleable.MediaDetailGallery_adapterClass)
        if (className == null) {
            mediaPartAdapter = MediaDetailsAdapter(getActivity()!!, mediaCell)
        } else {
            @Suppress("UNCHECKED_CAST")
            mediaPartAdapter = Class.forName(className).getConstructor(Activity::class.java, Int::class.java).newInstance(getActivity()!!, mediaCell) as ObjectsRecyclerViewAdapter<MediaPart, *>
        }

        collapseAppbarContainer = a.getBoolean(R.styleable.MediaDetailGallery_collapseAppbarLayout, false)

        mediaPartAdapter.defaultClickReceiver = object : ObjectsRecyclerViewAdapter.ClickReceiver<MediaPart> {
            override fun onViewClicked(recyclerView: RecyclerView, view: View, position: Int, item: MediaPart) {
                mMediaSelectionListener.get()?.onMediaPartSelected(mediaPartAdapter.items, item)
            }
        }

        adapter = mediaPartAdapter

        a.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        val manager = layoutManager
        if (manager is GridLayoutManager) {
            manager.spanSizeLookup = FillFirstAndLastLookup(manager.spanCount, mediaPartAdapter.itemCount)
        } else if (manager is LinearLayoutManager) {
            addOnScrollListener(RecycleViewSnapScrollListener())
        }
    }

    override fun show(contentItem: ContentItem, cacheValid: Boolean) {
        if (contentItem is ContentItemWithGallery) {
            mediaPartAdapter.swapList(contentItem.medias, true)
            val manager = layoutManager
            if (manager is GridLayoutManager) {
                val lookup = manager.spanSizeLookup
                if (lookup is FillFirstAndLastLookup) {
                    lookup.setListSize(mediaPartAdapter.itemCount)
                }
            }
            mediaPartAdapter.notifyDataSetChanged()
        }

        if (mediaPartAdapter.itemCount > 0)
            visibility = View.VISIBLE
        else
            visibility = View.GONE

        if (collapseAppbarContainer) {
            val lock = visibility != View.VISIBLE
            container.setAppbarLock(locked = lock, forceExpansionCollapse = true)
        }
    }
}