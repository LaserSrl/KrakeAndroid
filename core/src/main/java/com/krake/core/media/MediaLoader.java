package com.krake.core.media;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.krake.core.R;
import com.krake.core.model.MediaPart;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

/**
 * Questa classe compie tre azione principali:
 * <ul>
 * <li>Crea la richiesta per il WS per il download dell'immagine</li>
 * <li>Scarica l'immagine</li>
 * <li>Setta l'immagine in un widget che implementa l'interfaccia {@link MediaLoadable}, se presente</li>
 * </ul>
 */
@Deprecated
public class MediaLoader {
    private static final String TAG = MediaLoader.class.getSimpleName();

    /**
     * Setta i parametri della richiesta di Glide che verranno passati al WS e verranno utilizzati dal widget corrente
     *
     * @param activity      activity corrente
     * @param mediaLoadable istanza di MediaLoadable
     * @param mediaPart     {@link MediaPart} da caricare
     */
    public static void with(@NonNull Activity activity, @NonNull MediaLoadable mediaLoadable, @Nullable MediaPart mediaPart) {
        with(Glide.with(activity), activity, mediaLoadable, mediaPart);
    }

    /**
     * Setta i parametri della richiesta di Glide che verranno passati al WS e verranno utilizzati dal widget corrente
     *
     * @param fragment      fragment corrente
     * @param mediaLoadable istanza di MediaLoadable
     * @param mediaPart     {@link MediaPart} da caricare
     */
    public static void with(@NonNull Fragment fragment, @NonNull MediaLoadable mediaLoadable, @Nullable MediaPart mediaPart) {
        with(Glide.with(fragment), fragment.getActivity(), mediaLoadable, mediaPart);
    }

    /**
     * Setta i parametri della richiesta di Glide che verranno passati al WS e verranno utilizzati dal widget corrente
     *
     * @param context       context corrente
     * @param mediaLoadable istanza di MediaLoadable
     * @param mediaPart     {@link MediaPart} da caricare
     */
    public static void with(@NonNull Context context, @NonNull MediaLoadable mediaLoadable, @Nullable MediaPart mediaPart) {
        with(Glide.with(context), context, mediaLoadable, mediaPart);
    }

    /**
     * Setta i parametri della richiesta di Glide che verranno passati al WS e verranno utilizzati dal widget corrente
     *
     * @param manager       manager di Glide
     * @param context       context corrente
     * @param mediaLoadable istanza di MediaLoadable
     * @param mediaPart     {@link MediaPart} da caricare
     */
    private static void with(@NonNull RequestManager manager, @NonNull Context context, @NonNull MediaLoadable mediaLoadable, @Nullable MediaPart mediaPart) {
//        if (mediaLoadable instanceof ImageView) {
//            ImageView imageView = (ImageView) mediaLoadable;
            // crea la richiesta

            com.krake.core.media.loader.MediaLoader loader = com.krake.core.media.loader.MediaLoader.Companion.with(context, mediaLoadable);

            if (mediaPart != null)
                loader.mediaPart(mediaPart);

            loader.load();



//            RequestBuilder<Drawable> request = requestWith(manager, context, mediaLoadable, mediaPart);
//            if (request != null) {
//                // setta il target
//                request.into(imageView);
//            }
//        } else {
//            Log.e(TAG, "with: your widget cannot be casted to " + ImageView.class.getCanonicalName());
//        }
    }

    /**
     * Crea la richiesta per Glide nel caso in cui l'url sia valido, in caso contrario, setta il placeholder
     *
     * @param manager       manager di Glide
     * @param context       context corrente
     * @param mediaLoadable istanza di MediaLoadable
     * @param mediaPart     {@link MediaPart} da caricare
     * @return richiesta di Glide
     */
    @SuppressLint("SwitchIntDef")
    public static RequestBuilder<Drawable> requestWith(@NonNull RequestManager manager, @NonNull Context context, @NonNull MediaLoadable mediaLoadable, @Nullable MediaPart mediaPart) {
        ImageOptions options = mediaLoadable.getOptions();
        boolean animated = mediaLoadable.isAnimated();

        int placeholderIdentifier = 0;

        final ImageView imageView = mediaLoadable instanceof ImageView ? (ImageView) mediaLoadable : null;

        if (mediaPart != null) {
            if (!TextUtils.isEmpty(mediaPart.getMediaUrl()) && mediaPart.getMediaType() == MediaType.IMAGE) {
                String mediaUrl = mediaPart.getMediaUrl();

                int glidePlaceholder = mediaLoadable.showPlaceholder() ? mediaLoadable.getPhotoPlaceholder() : 0;

                URL externalURL = null;
                try {
                    externalURL = new URL(mediaUrl);
                } catch (MalformedURLException ignored) {
                }

                RequestBuilder<Drawable> builder;

                if (externalURL == null) {
                    builder = createRequestCreator(manager, context, imageView, options, mediaUrl);
                } else {
                    builder = manager.load(externalURL.toString());
                }

                RequestOptions reqOptions = new RequestOptions();
                if (imageView != null) {
                    // salva lo ScaleType dell'immagine
                    final ImageView.ScaleType imageScaleType = mediaLoadable.getMediaScaleType();
                    final ImageView.ScaleType placeholderScaleType = mediaLoadable.getPlaceholderScaleType();

                    if (mediaLoadable.showPlaceholder() && imageScaleType != placeholderScaleType && imageView.getScaleType() != placeholderScaleType) {
                        imageView.setScaleType(placeholderScaleType);
                    }
                    // se lo ScaleType dell'immagine e quello del placeholder sono differenti bisogna iniziare la procedura per la sostituzione dello ScaleType
                    // settando lo scaleType corretto prima di onResourceReady(), si evita il lag
                    if (imageScaleType == ImageView.ScaleType.FIT_CENTER) {
                        reqOptions.fitCenter();
                    } else if (imageScaleType == ImageView.ScaleType.CENTER_CROP) {
                        reqOptions.centerCrop();
                    }
                    // quando lo ScaleType è differente, probabilmente uno o due frame è possibile che vengano saltati (1 frame ogni 16ms).
                    // questo succede perchè deve richiamare i metodi invalidate() e requestLayout() dello scaleType
                    builder.listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            // setta il vecchio scaleType
                            if (imageView.getScaleType() != imageScaleType) {
                                imageView.setScaleType(imageScaleType);
                            }
                            return false;
                        }
                    });
                }

                if (glidePlaceholder > 0) {
                    reqOptions.placeholder(glidePlaceholder);
                }

                if (animated) {
                    DrawableCrossFadeFactory.Builder crossFadeFactoryBuilder = new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true);
                    builder.transition(DrawableTransitionOptions.withCrossFade(crossFadeFactoryBuilder));
                } else {
                    reqOptions.dontAnimate();
                }

                reqOptions.diskCacheStrategy(DiskCacheStrategy.DATA);
                builder.apply(reqOptions);
                return builder;
            } else if (mediaLoadable.showPlaceholder()) {
                switch (mediaPart.getMediaType()) {
                    case MediaType.IMAGE:
                        placeholderIdentifier = mediaLoadable.getPhotoPlaceholder();
                        break;
                    case MediaType.VIDEO:
                        placeholderIdentifier = mediaLoadable.getVideoPlaceholder();
                        break;
                    case MediaType.AUDIO:
                        placeholderIdentifier = mediaLoadable.getAudioPlaceholder();
                        break;
                }
            }
        } else if (mediaLoadable.showPlaceholder()) {
            placeholderIdentifier = mediaLoadable.getPhotoPlaceholder();
        }
        if (placeholderIdentifier > 0 && imageView != null) {
            imageView.setScaleType(mediaLoadable.getPlaceholderScaleType());
            imageView.setImageResource(placeholderIdentifier);
        }
        return null;
    }

    /**
     * Crea la richiesta per il WS aggiungendo in querystring tutti parametri stabiliti in app
     *
     * @param manager   manager di Glide
     * @param context   context corrente
     * @param imageView widget di tipo android.widget.ImageView se presente
     * @param options   configurazione da passare al WS
     * @param mediaUrl  url dell'immagine
     * @return richiesta per il WS
     */
    private static RequestBuilder<Drawable> createRequestCreator(@NonNull RequestManager manager, @NonNull Context context, @Nullable ImageView imageView, @NonNull ImageOptions options, @NonNull String mediaUrl) {
        RequestBuilder<Drawable> builder = null;
        int width = options.getWidth();
        int height = options.getHeight();
        boolean sizeIsZero = height == 0 || width == 0;

        if (sizeIsZero && imageView != null) {
            height = imageView.getMeasuredHeight();
            width = imageView.getMeasuredWidth();
        }

        sizeIsZero = height == 0 || width == 0;

        if (sizeIsZero) {
            Resources resources = context.getResources();
            height = resources.getDimensionPixelSize(R.dimen.orchard_image_default_height);
            width = resources.getDimensionPixelSize(R.dimen.orchard_image_default_width);
        }

        String mediaBasePath = Uri.withAppendedPath(Uri.parse(context.getString(R.string.orchard_base_service_url)), context.getString(R.string.orchard_medias_path)).toString();

        try {
            builder = manager.load(String.format(Locale.getDefault(), "%s?Path=%s&Width=%d&Height=%d&Mode=%s&Alignment=%s",
                    mediaBasePath,
                    URLEncoder.encode(mediaUrl, "UTF-8"),
                    width,
                    height,
                    options.getMode(),
                    options.getAlignment()));

        } catch (UnsupportedEncodingException ignored) {
        }
        return builder;
    }

    /**
     * Trasforma un url relativo/parziale in un url assoluto
     *
     * @param context  context corrette
     * @param mediaUrl url del media
     * @return url completo
     */
    public static String getAbsoluteMediaURL(@NonNull Context context, @Nullable String mediaUrl) {
        URL externalURL;
        try {
            externalURL = new URL(mediaUrl);
            return externalURL.toString();
        } catch (MalformedURLException ignored) {
        }
        try {
            return new URL(new URL(context.getString(R.string.orchard_base_service_url)), mediaUrl).toString();
        } catch (MalformedURLException ignored) {
        }
        return null;
    }
}