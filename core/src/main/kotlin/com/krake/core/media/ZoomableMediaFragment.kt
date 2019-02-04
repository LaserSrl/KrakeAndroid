package com.krake.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.krake.core.OrchardError
import com.krake.core.R
import com.krake.core.app.OrchardDataModelFragment
import com.krake.core.component.module.OrchardComponentModule
import com.krake.core.data.DataModel
import com.krake.core.media.loader.ImageHandler
import com.krake.core.media.loader.ImageLoader
import com.krake.core.media.loader.MediaLoader
import com.krake.core.media.widget.LoadableZoomableImageView
import com.krake.core.model.MediaPart
import io.realm.RealmModel

/**
 * Fragment utilizzato per mostrare una foto zoomabile a schermo intero.
 * Questo Fragment può caricare la foto in due modi:
 *
 *  * MediaPart: la foto viene scaricata dal WS
 *  * Uri: la foto è già presente nella memoria del dispositivo
 *
 */
class ZoomableMediaFragment : OrchardDataModelFragment(), ImageLoader.RequestListener<Drawable>
{

    companion object {
        private val ARG_MEDIA_URI = "argMediaUri"

        /**
         * Crea una nuova istanza di [ZoomableMediaFragment] per scaricare la foto dal WS
         *
         * @param mediaPart MediaPart relativa alla foto
         * @return istanza di [ZoomableMediaFragment] con gli argument già valorizzati
         */
        @Suppress("UNCHECKED_CAST")
        fun newInstance(context: Context, mediaPart: MediaPart): ZoomableMediaFragment {
            val fragment = ZoomableMediaFragment()
            val module = OrchardComponentModule()
                    .dataClass(mediaPart.javaClass as Class<out RealmModel>)
                    .record(mediaPart as RealmModel)

            fragment.arguments = module.writeContent(context)
            return fragment
        }

        /**
         * Crea una nuova istanza di [ZoomableMediaFragment] per caricare una foto tramite Uri dalla memoria del dispositivo
         *
         * @param mediaUri content uri della foto
         * @return istanza di [ZoomableMediaFragment] con gli argument già valorizzati
         */
        fun newInstance(mediaUri: Uri): ZoomableMediaFragment {
            val fragment = ZoomableMediaFragment()
            val args = Bundle()
            args.putString(ARG_MEDIA_URI, mediaUri.toString())
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var imageView: LoadableZoomableImageView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_zoomable_media, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageView = view.findViewById(R.id.photoView)
        progressBar = view.findViewById(android.R.id.progress)

        val uriString = arguments?.getString(ARG_MEDIA_URI)
        uriString?.let {
            val mediaUri = Uri.parse(it)
            // viene fatto partire il task per il load della Bitmap a schermo intero se l'immagine non arriva dal WS
            ImageHandler.loader<Uri, Bitmap>(activity!!)
                    .from(mediaUri)
                    .intoView()
                    .addListener(this)
                    .load(imageView)
        }
    }

    override fun onDataLoadSuccess(resource: Drawable?) {
        progressBar.visibility = View.GONE
    }

    override fun onDataLoadFailed() {
        progressBar.visibility = View.GONE
    }

    override fun onDataModelChanged(dataModel: DataModel?)
    {
        if (dataModel != null && dataModel.listData.isNotEmpty())
        {
            val mediaPart = dataModel.listData.firstOrNull() as? MediaPart
            if (mediaPart != null)
            {

                MediaLoader.with(activity!!, imageView)
                        .mediaPart(mediaPart)
                        .addListener(object : ImageLoader.RequestListener<Drawable>
                                     {
                                         override fun onDataLoadSuccess(resource: Drawable?)
                                         {
                                             progressBar.visibility = View.GONE
                                         }

                                         override fun onDataLoadFailed()
                                         {
                                         }
                                     })
                        .load()
            }
        }
    }

    override fun onDataLoadingError(orchardError: OrchardError)
    {
    }
}