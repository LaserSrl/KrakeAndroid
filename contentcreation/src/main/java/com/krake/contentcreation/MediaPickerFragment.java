package com.krake.contentcreation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.MotionEventCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.krake.contentcreation.adapter.MediaPickerAdapter;
import com.krake.contentcreation.adapter.holder.MediaPickerHolder;
import com.krake.contentcreation.widget.ResizableLinearLayout;
import com.krake.contentcreation.widget.SemiCircleView;
import com.krake.core.ClassUtils;
import com.krake.core.StringUtils;
import com.krake.core.extension.ImageLoaderExtensionsKt;
import com.krake.core.media.*;
import com.krake.core.media.loader.ImageLoader;
import com.krake.core.media.loader.MediaLoader;
import com.krake.core.media.watermark.Watermark;
import com.krake.core.media.watermark.WatermarkCameraHelper;
import com.krake.core.model.MediaPart;
import com.krake.core.model.RecordWithIdentifier;
import com.krake.core.os.WeakRunnable;
import com.krake.core.permission.PermissionListener;
import com.krake.core.permission.PermissionManager;
import com.krake.core.util.DisplayUtils;
import com.krake.core.widget.BottomSheetNotUnderActionBehavior;
import com.krake.core.widget.SafeBottomSheetBehavior;
import kotlin.collections.ArraysKt;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Fragment per caricare una foto o un video da collegare ad un nuovo contenuto su Orchard.
 * L'activity che contiene questo fragment deve estendere la classe {@link ContentCreationActivity}
 */
public class MediaPickerFragment extends Fragment implements ContentCreationFragment,
        View.OnClickListener,
        View.OnTouchListener,
        WatermarkCameraHelper.Listener,
        PermissionListener {

    private static final String OUT_STATE_SELECTED_BTN_ID = "otsStateSelId";
    private static final int DRAG_OFFSET = 50;

    private ContentCreationActivity mActivity;
    private ContentCreationTabInfo.MediaInfo mInstanceCreationInfos;
    private MediaPickerInfos photoInfos = new MediaPickerInfos();
    private PermissionManager mPermissionManager;

    private MediaPickerAdapter mAdapter;

    private MediaPickerHelper mCameraPickerHelper;

    private TextView mErrorTextView;
    private View mPlaceholderView;
    private View mMediaActionsContainerView;
    private BottomSheetNotUnderActionBehavior mBottomBehavior;

    private List<AppCompatImageButton> mButtons;

    private boolean mBehaviorIsInExpandedState;
    private float mBottomSheetTouchEventStartY;
    private int mPreviousToggleViewBottom;

    @IdRes
    private int mClickedButtonId = -1;

    public MediaPickerFragment() {
        // costruttore vuoto per l'inizializzazione
    }

    public static MediaPickerFragment newInstance() {
        return new MediaPickerFragment();
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        mActivity = (ContentCreationActivity) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInstanceCreationInfos = (ContentCreationTabInfo.MediaInfo) mActivity.getFragmentCreationInfo(this);
        if (savedInstanceState != null && savedInstanceState.containsKey(OUT_STATE_SELECTED_BTN_ID)) {
            mClickedButtonId = savedInstanceState.getInt(OUT_STATE_SELECTED_BTN_ID, -1);
        }
        mPermissionManager = new PermissionManager(this)
                .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .rationalMsg(getString(R.string.error_permission_storage_for_media))
                .permanentlyRefusedMsg(getString(R.string.error_permanently_permission_storage_for_media))
                .addListener(this);

        mPermissionManager.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media_picker, container, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Context context = getContext();

        final boolean typeImage = (mInstanceCreationInfos.getMediaType() & MediaType.IMAGE) != 0;
        final boolean typeVideo = (mInstanceCreationInfos.getMediaType() & MediaType.VIDEO) != 0;
        final boolean typeAudio = (mInstanceCreationInfos.getMediaType() & MediaType.AUDIO) != 0;

        mButtons = new ArrayList<>();

        final boolean isPortrait = DisplayUtils.isPortrait(context);

        final View divider = view.findViewById(R.id.containers_divider);

        final View containerCreateLabel = view.findViewById(R.id.create_container_label);
        final View containerCreateParent = view.findViewById(R.id.create_container_parent);

        final ResizableLinearLayout containerCreate = view.findViewById(R.id.create_container);
        containerCreate.addResizeCallback(new ResizableLinearLayout.ResizeCallback() {
            @Override
            public void onResize(int newWidth, int newHeight) {
                int buttonSize = isPortrait ? newHeight : newWidth;
                int buttonPadding = getButtonPadding(buttonSize);

                int size = 0;
                if (MediaPickerHelper.isCameraAvailable(context) && typeImage) {
                    size += createButtonInContainer(containerCreate, R.id.action_take_photo, R.drawable.ic_create_photo_48dp, buttonSize, buttonPadding);
                }
                if (MediaPickerHelper.isCameraAvailable(context) && typeVideo) {
                    size += createButtonInContainer(containerCreate, R.id.action_take_video, R.drawable.ic_create_video_48dp, buttonSize, buttonPadding);
                }
                if (typeAudio) {
                    size += createButtonInContainer(containerCreate, R.id.action_take_audio, R.drawable.ic_create_audio_48dp, buttonSize, buttonPadding);
                }

                if (size != 0) {
                    fixMarginsForContainer(containerCreate, size, isPortrait);
                    updateEnabledButtons();
                } else {
                    hideMediaContainer(containerCreate, containerCreateLabel, divider, containerCreateParent);
                }
            }
        });

        final View containerPickLabel = view.findViewById(R.id.pick_container_label);
        final View containerPickParent = view.findViewById(R.id.pick_container_parent);

        final ResizableLinearLayout containerPick = view.findViewById(R.id.pick_container);
        containerPick.addResizeCallback(new ResizableLinearLayout.ResizeCallback() {
            @Override
            public void onResize(int newWidth, int newHeight) {
                int buttonSize = isPortrait ? newHeight : newWidth;
                int buttonPadding = getButtonPadding(buttonSize);

                int size = 0;
                if (MediaPickerHelper.isPickerAvailable(context, MediaType.IMAGE) && typeImage) {
                    size += createButtonInContainer(containerPick, R.id.action_pick_photo_from_library, R.drawable.ic_pick_photo_48dp, buttonSize, buttonPadding);
                }
                if (MediaPickerHelper.isPickerAvailable(context, MediaType.VIDEO) && typeVideo) {
                    size += createButtonInContainer(containerPick, R.id.action_pick_video_from_library, R.drawable.ic_pick_video_48dp, buttonSize, buttonPadding);
                }
                if (MediaPickerHelper.isPickerAvailable(context, MediaType.AUDIO) && typeAudio) {
                    size += createButtonInContainer(containerPick, R.id.action_pick_audio_from_library, R.drawable.ic_pick_audio_48dp, buttonSize, buttonPadding);
                }

                if (size != 0) {
                    fixMarginsForContainer(containerPick, size, isPortrait);
                    updateEnabledButtons();
                } else {
                    hideMediaContainer(containerPick, containerPickLabel, divider, containerPickParent);
                }
            }
        });

        mErrorTextView = view.findViewById(R.id.photo_error_text);
        RecyclerView mediasRecyclerView = view.findViewById(R.id.mediasPreviewGallery);

        mPlaceholderView = view.findViewById(R.id.placeholder_view);

        // solo in portrait è necessario configurare la view con il behavior del bottom sheet
        final View bottomContainer = view.findViewById(R.id.cc_bottom_container);
        if (isPortrait) {

            mBottomBehavior = (BottomSheetNotUnderActionBehavior) ((CoordinatorLayout.LayoutParams) bottomContainer.getLayoutParams()).getBehavior();
            if (mBottomBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
                //noinspection WrongConstant
                mBottomBehavior.setStateAndNotify(BottomSheetBehavior.STATE_COLLAPSED);
            }

            // quando la view è stata disegnata, il behavior la fa espandere
            bottomContainer.post(new WeakRunnable<BottomSheetNotUnderActionBehavior>(mBottomBehavior) {
                @Override
                public void runWithReferred(@NonNull BottomSheetNotUnderActionBehavior referred) {
                    if (referred.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                        //noinspection WrongConstant
                        referred.setStateAndNotify(BottomSheetBehavior.STATE_EXPANDED);
                    }
                }
            });
            mBottomBehavior.addBottomSheetCallback(new SafeBottomSheetBehavior.BottomSheetStateCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    boolean isCollapsed = newState == BottomSheetBehavior.STATE_COLLAPSED;
                    // blocca gli input events se il BottomSheet è collapsed
                    mBottomBehavior.setAllowUserDrag(!isCollapsed);
                    // setta il boolean che controlla il click
                    mBehaviorIsInExpandedState = !isCollapsed;
                }

                @Override
                public void onStateWillChange(@NonNull View bottomSheet, int newState) {

                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                }
            });

            mMediaActionsContainerView = view.findViewById(R.id.media_actions_container);

            //inizializza i valori della view semicircolare che rimarrà sempre visibile sullo schermo
            final SemiCircleView toggleView = view.findViewById(R.id.toggle_bottom_view);
            toggleView.setOnTouchListener(this);

            mPreviousToggleViewBottom = 0;

            final ViewTreeObserver vto = toggleView.getViewTreeObserver();
            // viene usato un listener per avere i pixels reali della toggleView
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int currentBottom = toggleView.getBottom();
                    if (currentBottom != mPreviousToggleViewBottom) {
                        mBottomBehavior.setPeekHeight(currentBottom);
                        mPreviousToggleViewBottom = currentBottom;
                    }
                }
            });

            // setta il touch listener per nascondere il BottomSheet su una View che è posta sopra a tutte le altre (BottomSheet escluso)
            view.findViewById(R.id.clickable_filter_view).setOnTouchListener(this);
        }


        if (!mInstanceCreationInfos.isEditingEnabled()) {
            if (bottomContainer != null)
                bottomContainer.setVisibility(View.GONE);

            if (containerCreateParent != null)
                containerCreateParent.setVisibility(View.GONE);
            if (containerPickParent != null)
                containerPickParent.setVisibility(View.GONE);
        }


        if (mInstanceCreationInfos.getWatermark() == null) {
            mCameraPickerHelper = new MediaPickerHelper(this, mInstanceCreationInfos.getMaxNumberOfMedias() <= 1);
        } else {
            mCameraPickerHelper = new WatermarkCameraHelper(this, mInstanceCreationInfos.getMaxNumberOfMedias() <= 1, this);

            int maxPixelSize = getResources().getDimensionPixelSize(R.dimen.attachment_max_photo_size) << 1;
            final ImageOptions options = new ImageOptions(maxPixelSize, maxPixelSize, ImageOptions.Mode.PAN);
            //noinspection ConstantConditions
            ImageLoader loader = com.krake.core.media.loader.MediaLoader.Companion.<File>typedWith(mActivity, new DownloadOnlyLoadable() {
                @Override
                public ImageOptions getOptions() {
                    return options;
                }
            })
                    .mediaPart(new MediaWatermarkWrapper(mInstanceCreationInfos.getWatermark()))
                    .getRequest();

            ImageLoaderExtensionsKt.asFile(loader)
                    .addListener(new ImageLoader.RequestListener<File>() {
                        @Override
                        public void onDataLoadSuccess(File resource) {
                            Uri resourceUri = MediaProvider.getUriForFile(mActivity, resource);
                            mInstanceCreationInfos.getWatermark().setLocalWaterMarkUri(resourceUri);
                        }

                        @Override
                        public void onDataLoadFailed() {

                        }
                    })
                    .load();
        }

        photoInfos = (MediaPickerInfos) mActivity.getFragmentData(this);
        if (photoInfos != null) {
            loadDataFromArguments(photoInfos);
        } else {
            photoInfos = new MediaPickerInfos();

            if (mActivity.getOriginalObject() != null && mInstanceCreationInfos.getDataKey() != null) {
                List<MediaPart> medias = (List<MediaPart>) ClassUtils.getValueInDestination(StringUtils.methodName(null,
                        mInstanceCreationInfos.getDataKey(),
                        null,
                        StringUtils.MethodType.GETTER),
                        mActivity.getOriginalObject());

                if (medias != null) {
                    // download each media part and update the preview
                    for (final MediaPart mediaPart : medias) {

                        MediaLoadable loadable = new DownloadOnlyLoadable() {
                            /* Empty implementation. */
                        };

                        ImageLoader loader = MediaLoader.Companion.<File>typedWith(mActivity, loadable)
                                .mediaPart(mediaPart)
                                .getRequest();
//
                        ImageLoaderExtensionsKt.asFile(loader)
                                .addListener(new ImageLoader.RequestListener<File>() {
                                    @Override
                                    public void onDataLoadSuccess(File resource) {
                                        if (resource == null)
                                            // Process the resource only if it's available.
                                            return;

                                        Uri resourceUri = MediaProvider.getUriForFile(mActivity, resource);
                                        long id = 0;
                                        if (mediaPart instanceof RecordWithIdentifier)
                                            id = ((RecordWithIdentifier) mediaPart).getIdentifier();
                                        UploadableMediaInfo mediaInfo = new UploadableMediaInfo(id, resourceUri, null, mediaPart.getMediaType());
                                        mediaInfo.setUploaded(true);
                                        //noinspection WrongConstant
                                        photoInfos.pickedInfos.add(mediaInfo);
                                        // Save the data obtained from DB to restore them on rotation.
                                        mActivity.updateFragmentData(MediaPickerFragment.this, photoInfos);
                                        if (mAdapter != null) {
                                            updatePreviewAndButtons();
                                        }

                                        //we are in initialization mode, so notify the parent activity that the data isn't changed
                                        mActivity.OnContentFragmentReady(MediaPickerFragment.this);
                                    }

                                    @Override
                                    public void onDataLoadFailed() {
                                        /* Empty implementation. */
                                    }
                                })
                                .load();
                    }
                }
            }
        }

        mAdapter = new MediaPickerAdapter(getContext(), mInstanceCreationInfos.isEditingEnabled(), this, R.layout.gallery_preview_media_cell, photoInfos.pickedInfos, MediaPickerHolder.class);
        mediasRecyclerView.setAdapter(mAdapter);
        updatePreviewAndButtons();

        mActivity.OnContentFragmentReady(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mCameraPickerHelper != null) {
            mCameraPickerHelper.destroy();
        }
        mAdapter.release();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(OUT_STATE_SELECTED_BTN_ID, mClickedButtonId);
    }

    /**
     * Calcola il padding del bottone basandosi sulla sua diagonale (che corrisponde al diametro del cerchio)
     *
     * @param size dimensione del bottone
     * @return padding del bottone
     */
    private int getButtonPadding(int size) {
        double maxResSize = size / Math.sqrt(2);
        double partialPadding = (size - maxResSize) / 2;
        return (int) (partialPadding + partialPadding / 2);
    }

    /**
     * Crea un bottone programmaticamente all'interno del container settando in anticipo le sue proprietà
     *
     * @param linearLayout layout parent dei bottoni
     * @param id           id del bottone
     * @param resource     image resource del bottone
     * @param size         dimensione del bottone
     * @param padding      padding tra il contorno del bottone e la sua immagine
     * @return la dimensione del bottone per sommarla alle altre
     */
    private int createButtonInContainer(@NonNull ResizableLinearLayout linearLayout, @IdRes int id, @DrawableRes int resource, int size, int padding) {
        AppCompatImageButton button = new AppCompatImageButton(getContext());
        button.setId(id);
        final Drawable selector = ContextCompat.getDrawable(getContext(), R.drawable.cc_selector_bg);
        button.setBackground(selector);
        button.setImageResource(resource);
        button.setScaleType(ImageView.ScaleType.FIT_CENTER);
        if (mInstanceCreationInfos.getMaxNumberOfMedias() > 1) {
            button.setEnabled(false);
        }

        button.setMaxHeight(size);
        button.setMaxWidth(size);

        button.setPadding(padding, padding, padding, padding);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        if (linearLayout.findViewById(id) == null) {
            linearLayout.addView(button, params);
            button.setOnClickListener(MediaPickerFragment.this);
            // il bottone viene aggiunto a una lista per essere più performanti nei cicli sulle view
            mButtons.add(button);
        } else {
            button.setLayoutParams(params);
        }

        // aggiorna i layout params per mostrare le view
        button.post(new WeakRunnable<AppCompatImageButton>(button) {
            @Override
            public void runWithReferred(@NonNull AppCompatImageButton referred) {
                if (referred.getHeight() == 0 || referred.getWidth() == 0) {
                    // forza i metodi onMeasure(), onLayout() e onDraw()
                    referred.requestLayout();
                }
            }
        });
        return size;
    }

    /**
     * Cambia i margini dei bottoni all'interno del loro container per distribuirli equamente nello spazio
     *
     * @param linearLayout layout parent dei bottoni
     * @param size         dimensione totale ottenuta con la somma delle dimensioni dei bottoni (somma di larghezze se in portrait, somma di altezze se in landscape)
     * @param isPortrait   true se il device è in portrait, false se è in landscape
     */
    private void fixMarginsForContainer(@NonNull ResizableLinearLayout linearLayout, int size, boolean isPortrait) {
        int count = linearLayout.getChildCount();

        int contentSize;
        if (isPortrait) {
            contentSize = linearLayout.getActualWidth() - linearLayout.getPaddingLeft() - linearLayout.getPaddingRight();
        } else {
            contentSize = linearLayout.getActualHeight() - linearLayout.getPaddingTop() - linearLayout.getPaddingBottom();
        }

        int margin = (contentSize - size) / (count + 1);

        for (int i = 0; i < count; i++) {
            View child = linearLayout.getChildAt(i);

            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) child.getLayoutParams();
            if (isPortrait) {
                lp.leftMargin = margin;
                if (i == count - 1) {
                    lp.rightMargin = margin;
                }
            } else {
                lp.topMargin = margin;
                if (i == count - 1) {
                    lp.bottomMargin = margin;
                }
            }
            child.setLayoutParams(lp);
        }
    }

    /**
     * Notifica l'adapter di un cambiamento e nasconde o mostra il placeholder della galleria
     */
    private void updatePreviewAndButtons() {
        mAdapter.swapList(photoInfos.pickedInfos, true);

        if (photoInfos.pickedInfos.size() > 0) {
            mPlaceholderView.setVisibility(View.GONE);
        } else {
            mPlaceholderView.setVisibility(View.VISIBLE);
        }
        updateEnabledButtons();
    }

    /**
     * Abilita o disabilita i bottoni dei media.
     * <br/>
     * I bottoni vengono disabilitati se si raggiunge il massimo numero di media consentiti (nel caso in cui si possa caricare più di un media)
     */
    private void updateEnabledButtons() {
        if (mInstanceCreationInfos.getMaxNumberOfMedias() > 1) {
            if (mButtons != null && mButtons.size() > 0 && mAdapter != null) {
                boolean enabled = mAdapter.getItemCount() < mInstanceCreationInfos.getMaxNumberOfMedias();
                for (AppCompatImageButton button : mButtons) {
                    button.setEnabled(enabled);
                }
            }
        }
    }

    /**
     * Nasconde un container di azioni multimediali e le view relative ad esso.
     * <br/>
     * Questo metodo si basa sulla presenza o meno delle view, tale presenza dipende dall'orientation, motivazion per cui, in caso si volesse sovrascrivere il layout,
     * bisogna mantenere gli stessi id, sia nel layout in portrait, che quello in landscape.
     *
     * @param container         container delle azioni multimediali
     * @param containerLabel    label usata in portrait per definire il contenuto del container
     * @param containersDivider divider usato in portrait per separare i due containers
     * @param containerParent   view parent del container usata in landscape
     */
    private void hideMediaContainer(@NonNull View container, @Nullable View containerLabel, @Nullable View containersDivider, @Nullable View containerParent) {
        if (containerLabel != null) {
            containerLabel.setVisibility(View.GONE);
        }

        if (containersDivider != null) {
            containersDivider.setVisibility(View.GONE);
        }

        // se il parent non è nullo, allora è inutile nascondere anche il container
        if (containerParent != null) {
            containerParent.setVisibility(View.GONE);
        } else {
            container.setVisibility(View.GONE);
        }
    }

    private void loadDataFromArguments(MediaPickerInfos arguments) {
        if (arguments != null) {
            if (arguments.pickedInfos != null && arguments.pickedInfos.size() == 1) {
                mCameraPickerHelper.setLastMediaInfo(arguments.pickedInfos.get(0));
            }
            setTextError(arguments.error);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mCameraPickerHelper.handleOnActivityResult(requestCode, resultCode, data) == MediaPickerHelper.HANDLED) {
            loadPickedMediaFromCameraHelper();
        }
    }

    /**
     * Carica il media appena ottenuto all'interno della galleria di preview
     */
    private void loadPickedMediaFromCameraHelper() {
        if (mInstanceCreationInfos.getMaxNumberOfMedias() == 1) {
            photoInfos.pickedInfos.clear();
        }
        if (mCameraPickerHelper.getLastMediaInfo() != null)
            photoInfos.pickedInfos.add(mCameraPickerHelper.getLastMediaInfo());

        mActivity.updateFragmentData(this, photoInfos);
        updatePreviewAndButtons();
        setTextError(null);
    }

    /**
     * Esegue l'azione corrispondente al bottone dei media premuto
     *
     * @param id id del bottone
     */
    private void performActionFromButtonId(@IdRes int id) {
        if (id == R.id.action_take_photo) {
            mCameraPickerHelper.takeNewPhoto();
        } else if (id == R.id.action_take_video) {
            mCameraPickerHelper.takeNewVideo();
        } else if (id == R.id.action_take_audio) {
            mCameraPickerHelper.takeNewAudio();
        } else if (id == R.id.action_pick_photo_from_library) {
            mCameraPickerHelper.pickMediaFromGallery(MediaType.IMAGE, MediaPickerHelper.Local_Photo);
        } else if (id == R.id.action_pick_video_from_library) {
            mCameraPickerHelper.pickMediaFromGallery(MediaType.VIDEO, MediaPickerHelper.Local_Video);
        } else if (id == R.id.action_pick_audio_from_library) {
            mCameraPickerHelper.pickMediaFromGallery(MediaType.AUDIO, MediaPickerHelper.Local_Audio);
        }
    }

    @Override
    public boolean validateDataAndSaveError(@NonNull ContentCreationActivity activity, @NonNull ContentCreationTabInfo.ContentCreationInfo creationInfo, @Nullable Object savedInfos) {
        MediaPickerInfos photoInfos = (MediaPickerInfos) savedInfos;
        LinkedList<UploadableMediaInfo> pickerInfos = photoInfos != null ? photoInfos.pickedInfos : null;
        ContentCreationTabInfo.MediaInfo photoInfo = (ContentCreationTabInfo.MediaInfo) creationInfo;
        String mError;

        if (!photoInfo.isRequired() || (pickerInfos != null && pickerInfos.size() > 0)) {
            mError = null;
        } else {
            mError = activity.getString(R.string.error_missing_content_media);
        }

        setTextError(mError);

        if (photoInfos == null) {
            photoInfos = new MediaPickerInfos();
        }

        photoInfos.error = mError;

        activity.updateFragmentData(this, photoInfos);

        return mError == null;
    }

    /**
     * Setta il testo di errore, se un errore viene sollevato, altrimenti nasconde la view
     *
     * @param error testo d'errore
     */
    private void setTextError(String error) {
        if (mErrorTextView != null) {
            if (TextUtils.isEmpty(error)) {
                mErrorTextView.setVisibility(View.GONE);
            } else {
                mErrorTextView.setText(error);
                mErrorTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean insertDataToUpload(@NonNull ContentCreationActivity activity, @NonNull ContentCreationTabInfo.ContentCreationInfo creationInfo, Object savedInfos, @NonNull JsonObject parameters) {
        MediaPickerInfos photoInfos = (MediaPickerInfos) savedInfos;
        ContentCreationTabInfo.MediaInfo photoInfo = (ContentCreationTabInfo.MediaInfo) creationInfo;

        JsonArray identifiers = new JsonArray();
        for (UploadableMediaInfo media : photoInfos.pickedInfos) {
            if (media.isUploaded()) {
                identifiers.add(new JsonPrimitive(media.getId()));
            }
        }
        parameters.add(photoInfo.getOrchardKey(), identifiers);
        return identifiers.size() == photoInfos.pickedInfos.size();
    }

    @Override
    public Object deserializeSavedInstanceState(@NonNull ContentCreationActivity activity, @NonNull ContentCreationTabInfo.ContentCreationInfo creationInfo, @NonNull Gson gson, String serializedInfos) {
        return gson.fromJson(serializedInfos, MediaPickerInfos.class);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.deleteMediaFab) {
            int index = ((Integer) v.getTag());
            if (mCameraPickerHelper.isSingleMediaMode()) {
                mCameraPickerHelper.deleteFile(mCameraPickerHelper.getOldMediaInfo());
                mCameraPickerHelper.deleteFile(mCameraPickerHelper.getLastMediaInfo());
            } else {
                mCameraPickerHelper.deleteFile(photoInfos.pickedInfos.get(index));
            }

            photoInfos.pickedInfos.remove(index);

            mActivity.updateFragmentData(this, photoInfos);
            updatePreviewAndButtons();
        } else if (id == R.id.cellClickView) {
            if (!mBehaviorIsInExpandedState && photoInfos.pickedInfos.size() > 0) {
                int index = ((Integer) v.getTag());
                startActivity(CameraPickedFullScreenActivity.newStartIntent(getActivity(), photoInfos.pickedInfos, index));
            }
        } else {
            mClickedButtonId = id;
            mPermissionManager.request();
        }
    }

    @Override
    public void onPermissionsHandled(@NotNull String[] acceptedPermissions) {
        if (ArraysKt.contains(acceptedPermissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            performActionFromButtonId(mClickedButtonId);
        }
    }

    /**
     * Metodo di callback quando un MotionEvent è stato triggerato
     *
     * @param v     view sulla quale è stato triggerato un evento
     * @param event evento triggerato
     * @return booleano che indica come gestire le notifiche degli eventi alle view.
     * <br/>
     * true -> notifica altri eventi sulla view che ha triggerato l'evento e non notifica le altre view sullo stesso evento
     * false -> notifica le altre view del click e non notifica altri eventi sulla stessa
     */
    @SuppressWarnings("WrongConstant")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        @IdRes final int id = v.getId();
        final float eventY = MotionEventCompat.getActionMasked(event);

        if (mBottomBehavior != null) {
            if (id == R.id.clickable_filter_view
                    && mMediaActionsContainerView != null
                    && mBottomBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED
                    // Il touch viene notificato solo se il dito preme un punto sullo schermo più in alto del BottomSheet
                    && eventY <= mMediaActionsContainerView.getTop()) {

                // comprime il BottomSheet e blocca gli input events
                mBottomBehavior.setStateAndNotify(BottomSheetBehavior.STATE_COLLAPSED);
                mBottomBehavior.setAllowUserDrag(false);
            } else if (id == R.id.toggle_bottom_view) {

                final int oldState = mBottomBehavior.getState();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mBottomSheetTouchEventStartY = eventY;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float gap = mBottomSheetTouchEventStartY - eventY;
                        if (gap > DRAG_OFFSET) {
                            mBottomBehavior.setStateAndNotify(BottomSheetBehavior.STATE_EXPANDED);
                            return false;
                        } else if (gap < -DRAG_OFFSET) {
                            mBottomBehavior.setStateAndNotify(BottomSheetBehavior.STATE_COLLAPSED);
                            return false;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        mBottomSheetTouchEventStartY = 0;
                        // cambia lo stato del BottonSheet basandosi sullo stato corrente
                        if (oldState == BottomSheetBehavior.STATE_COLLAPSED) {
                            mBottomBehavior.setStateAndNotify(BottomSheetBehavior.STATE_EXPANDED);
                        } else if (oldState == BottomSheetBehavior.STATE_EXPANDED) {
                            mBottomBehavior.setStateAndNotify(BottomSheetBehavior.STATE_COLLAPSED);
                        }
                        return false;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public Watermark getWatermark() {
        return mInstanceCreationInfos.getWatermark();
    }

    @Override
    public void onPhotoAppliedWaterMark() {
        loadPickedMediaFromCameraHelper();
    }

    static class MediaPickerInfos {
        LinkedList<UploadableMediaInfo> pickedInfos = new LinkedList<>();
        String error;

        protected MediaPickerInfos() {

        }
    }

    private class MediaWatermarkWrapper implements MediaPart {
        private final Watermark watermark;

        MediaWatermarkWrapper(Watermark watermark) {
            this.watermark = watermark;
        }

        @Override
        public String getMimeType() {
            return null;
        }

        @Override
        public String getFileName() {
            return null;
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getFolderPath() {
            return null;
        }

        @Override
        public String getLogicalType() {
            return null;
        }

        @Override
        public String getMediaUrl() {
            return watermark.getWatermarkURL();
        }

        @Override
        public int getMediaType() {
            return MediaType.IMAGE;
        }
    }
}