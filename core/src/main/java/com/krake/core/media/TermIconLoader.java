package com.krake.core.media;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.krake.core.model.TermPart;
import org.jetbrains.annotations.NotNull;

/**
 * Classe che espone i metodi per ottenere l'icona da una {@link TermPart}.
 * L'icona verrà scaricata dal WS solo se non è già stata scaricata precedentemente dal plugin di Gradle.
 */
@Deprecated
public class TermIconLoader {

    private static ArrayMap<String, Drawable> mTermImages = new ArrayMap<>();

    private TermIconLoader() {
        // private empty constructor to avoid instantiation
    }

    /**
     * Scarica un'icona, come {@link Bitmap}, legata ad una {@link TermPart}
     *
     * @param activity activity corrente
     * @param termPart istanza di {@link TermPart} relativa ad un ContentItem
     * @param listener listener per gestire lo stato di download dell'icona
     */
    public static void loadTerms(@NonNull Activity activity, @NonNull TermPart termPart, @NonNull final OnTermIconLoadListener listener) {
        loadTerms(Glide.with(activity), activity, termPart, listener);
    }

    /**
     * Scarica un'icona, come {@link Bitmap}, legata ad una {@link TermPart}
     *
     * @param fragment fragment corrente
     * @param termPart istanza di {@link TermPart} relativa ad un ContentItem
     * @param listener listener per gestire lo stato di download dell'icona
     */
    public static void loadTerms(@NonNull Fragment fragment, @NonNull TermPart termPart, @NonNull final OnTermIconLoadListener listener) {
        loadTerms(Glide.with(fragment), fragment.getActivity(), termPart, listener);
    }

    /**
     * Scarica un'icona, come {@link Bitmap}, legata ad una {@link TermPart}
     *
     * @param context  context corrente
     * @param termPart istanza di {@link TermPart} relativa ad un ContentItem
     * @param listener listener per gestire lo stato di download dell'icona
     */
    public static void loadTerms(@NonNull Context context, @NonNull TermPart termPart, @NonNull final OnTermIconLoadListener listener) {
        loadTerms(Glide.with(context), context, termPart, listener);
    }

    /**
     * Scarica un'icona, come {@link Bitmap}, legata ad una {@link TermPart}
     *
     * @param manager  manager di {@link Glide}
     * @param context  context corrente
     * @param termPart istanza di {@link TermPart} relativa ad un ContentItem
     * @param listener listener per gestire lo stato di download dell'icona
     */
    private static void loadTerms(@NonNull RequestManager manager, @NonNull final Context context, @NonNull TermPart termPart, @NonNull final OnTermIconLoadListener listener) {

        com.krake.core.media.loader.TermIconLoader.OnTermIconLoadListener l = new com.krake.core.media.loader.TermIconLoader.OnTermIconLoadListener() {
            @Override
            public void onIconLoadCompleted(@NotNull Drawable icon, boolean fromWs) {
                listener.onIconLoadCompleted(icon, fromWs);
            }

            @Override
            public void onIconLoadFailed(boolean fromWs) {
                listener.onIconLoadFailed(fromWs);
            }
        };

        com.krake.core.media.loader.TermIconLoader.INSTANCE.loadTerms(context, termPart, l);
//
//
//        MediaPart termIcon = termPart.getIcon();
//        if (termIcon == null) {
//            assignListenerToDrawable(listener, null, false);
//            return;
//        }
//
//        // Accede ad un'icona legata alle categorie scaricate dal PinMapManager
//        int categoryResGen = 0;
//        if (termIcon instanceof RecordWithIdentifier) {
//            categoryResGen = ResourceUtil.INSTANCE.resourceForName(context, ResourceUtil.DRAWABLE, context.getString(R.string.partial_term_icon_name) +
//                    ((RecordWithIdentifier) termIcon).getIdentifier());
//        }
//        final String mediaUrl = termIcon.getMediaUrl();
//        Drawable icon = mTermImages.get(mediaUrl);
//
//        if (icon == null) {
//            if (categoryResGen == 0) {
//
//                ImageLoader loader = com.krake.core.media.loader.MediaLoader.Companion.<Drawable>typedWith(context)
//                        .mediaPart(termIcon)
//                        .getRequest();
//
//                ImageLoaderFromResourcesKt.toDrawable(loader)
//                        .load(new ImageLoader.Request.Listener<Drawable>() {
//                            @Override
//                            public void onDataLoadSuccess(Drawable resource) {
//                                mTermImages.put(mediaUrl, resource);
//                                assignListenerToDrawable(listener, resource, true);
//                            }
//
//                            @Override
//                            public void onDataLoadFailed() { }
//                        });
//
////                final RequestBuilder<Drawable> request = MediaLoader.requestWith(manager, context, new DownloadOnlyLoadable() {
////                }, termIcon);
////                if (request != null) {
////                    final Target<Drawable> drawableTarget = new SimpleTarget<Drawable>() {
////                        @Override
////                        public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
////                            mTermImages.put(mediaUrl, resource);
////                            assignListenerToDrawable(listener, resource, true);
////                        }
////                    };
////                    // l'icona verrà scaricata come Bitmap
////                    request.into(drawableTarget);
////                }
//            } else {
//                icon = ContextCompat.getDrawable(context, categoryResGen);
//                assignListenerToDrawable(listener, icon, false);
//            }
//        } else {
//            assignListenerToDrawable(listener, icon, false);
//        }
    }

    private static void assignListenerToDrawable(@NonNull OnTermIconLoadListener listener, @Nullable Drawable icon, boolean fromWs) {
        if (icon != null) {
            listener.onIconLoadCompleted(icon, fromWs);
        } else {
            listener.onIconLoadFailed(fromWs);
        }
    }

    /**
     * Listener che gestisce lo stato di download dell'icona.
     */
    public interface OnTermIconLoadListener {

        /**
         * Notifica che il caricamento è stato completato con successo.
         *
         * @param icon icona scaricata da WS o caricata dalle risorse
         */
        @UiThread
        void onIconLoadCompleted(@NonNull Drawable icon, boolean fromWs);

        /**
         * Notifica che il caricamento è fallito.
         * Se la {@link TermPart} non ha un'icona, questo metodo viene richiamato automaticamente.
         */
        @UiThread
        void onIconLoadFailed(boolean fromWs);
    }
}