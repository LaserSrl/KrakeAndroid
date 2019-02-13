package com.krake.contentcreation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.*;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.krake.contentcreation.component.module.ContentCreationComponentModule;
import com.krake.core.*;
import com.krake.core.app.KrakeApplication;
import com.krake.core.app.LoginAndPrivacyActivity;
import com.krake.core.cache.CacheManager;
import com.krake.core.component.annotation.BundleResolvable;
import com.krake.core.component.module.LoginComponentModule;
import com.krake.core.component.module.OrchardComponentModule;
import com.krake.core.data.DataConnectionModel;
import com.krake.core.data.DataModel;
import com.krake.core.media.KeyMediaListMap;
import com.krake.core.media.MediaType;
import com.krake.core.media.UploadableMediaInfo;
import com.krake.core.model.RecordWithIdentifier;
import com.krake.core.network.RemoteClient;
import com.krake.core.network.RemoteRequest;
import com.krake.core.network.RemoteResponse;
import com.krake.core.service.MessengerAndWorkerServiceConnection;
import com.krake.core.util.LayoutUtils;
import com.krake.core.view.TabLayoutHelper;
import com.krake.core.widget.SnackbarUtils;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Activity per creare nuovi contenuti in Orchard, è configurabile in modo da permettere di creare
 * qualsiasi contenuto sia stato definito lato BO.
 * <p/
 * L'upload del contenuto multimediale viene effettuato utilizzando le chiamate {@link OrchardUploadService#startServiceToUploadFiles(Context, KeyMediaListMap, Integer, Bundle, Bundle, boolean)},
 * con un codice di upload {@link #CONTENT_CREATION_UPLOAD_MEDIA_BASE} modificato in base al contet type.
 * Alla fine del caricamento del multimedia sarà invocato {@link CreateContentUploadMediaEndListener}, è
 * necessario registrarlo nel metodo {@link KrakeApplication#registerUploadCompleteListener(int, Class)}.
 * Il caricamento effettivo del contenuto è effettuato sfruttando la chiamata {@link Signaler#invokeAPI(Context, RemoteRequest, boolean, Object, Function2)}
 * con apiPath impostato a R.string.orchard_api_path_new_content
 */
public class ContentCreationActivity extends LoginAndPrivacyActivity implements ContentApiServiceListener, Handler.Callback {
    public static final int CONTENT_CREATION_UPLOAD_MEDIA_BASE = 272;
    private static final String OUT_STATE_SAVED_INFOS = "SavedInfos";
    private static final String OUT_STATE_OBJECT_ENUM_INFOS = "ObjectEnumInfos";
    private static final String OUT_STATE_ORIGINAL_OBJECT_ID = "ObjectId";

    public static final int RESULT_CONTENT_SENT = 72;

    private static final int FINISH_ACTIVITY_MSG = 3761;
    @BundleResolvable
    public ContentCreationComponentModule contentCreationComponentModule;
    private TabLayout mTabLayout;
    private PagerAdapter mAdapter;
    private ViewPager mPager;
    private ProgressBar mProgress;
    private List<ContentCreationFragment> mContentEditFragments;
    private ArrayList<Object> mFragmentEditValues = new ArrayList<>();
    private boolean mSendingData;
    private Object originalObject;
    private Long originalObjectId;
    private JsonObject objectEnumInfos;
    private ContentDefinition mContentDefinition;
    private boolean mChangedData;
    private ArrayList<String> savedFragmentData;
    private DataConnectionModel mDataConnection;
    private MenuItem mCurrentMenuItem;
    private boolean mChangeMenuItemActionDispatched;
    private boolean mSnackBarIsLaunched = false;
    private ContentCreationUploadListener activityListener;
    private Handler mHandler = new Handler(this);
    private MessengerAndWorkerServiceConnection uploadServiceConnection = new MessengerAndWorkerServiceConnection(this, OrchardUploadService.class, mHandler);

    @SuppressLint("InflateParams")
    @Override
    public void onCreate(Bundle savedInstanceState, int layout) {
        super.onCreate(null, layout);

        activityListener =
                new ContentCreationUploadListener(this, this);
        // By default the result is always canceled to support every case in which the content is not sent.
        setResult(RESULT_CANCELED);

        KrakeApplication app = (KrakeApplication) getApplication();
        app.addUploadInterceptor(new ContentCreationUploadInterceptor(UploadInterceptor.PRIORITY_LOW, MediaType.IMAGE | MediaType.VIDEO | MediaType.AUDIO));

        inflateMainView(R.layout.activity_content_creation, true);

        mPager = findViewById(R.id.content_creation_pager);
        mProgress = findViewById(android.R.id.progress);

        AppBarLayout verticalLayout = findViewById(R.id.app_bar_layout);

        TabLayoutHelper tabLayoutHelper = createTabHelper();
        tabLayoutHelper.addToView(verticalLayout, tabLayoutIndexInRoot());

        mTabLayout = tabLayoutHelper.layout();
        mTabLayout.setVisibility(View.GONE);

        mAdapter = new ContentItemPagerAdapter(getSupportFragmentManager());
        mPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin));
        mPager.setPageMarginDrawable(R.drawable.pager_separation);
        mPager.setAdapter(mAdapter);

        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                disableViewPagerPages(position);
            }
        });

        mContentDefinition = contentCreationComponentModule.getContentDefinition();

        if (app.getUploadCompletedListener(CONTENT_CREATION_UPLOAD_MEDIA_BASE +
                mContentDefinition.getContentType().hashCode()) == null)
            app.registerUploadCompleteListener(CONTENT_CREATION_UPLOAD_MEDIA_BASE +
                    mContentDefinition.getContentType().hashCode(), CreateContentUploadMediaEndListener.class);

        getLifecycle().addObserver(activityListener);
        getLifecycle().addObserver(uploadServiceConnection);

        final OrchardComponentModule originalObjectConnection = contentCreationComponentModule.getOriginalObjectConnection();

        if (savedInstanceState != null) {
            savedFragmentData = savedInstanceState.getStringArrayList(OUT_STATE_SAVED_INFOS);
            originalObjectId = savedInstanceState.getLong(OUT_STATE_ORIGINAL_OBJECT_ID);
            objectEnumInfos = savedInstanceState.containsKey(OUT_STATE_OBJECT_ENUM_INFOS) ? ContentCreationUtils.getGsonInstance().fromJson(savedInstanceState.getString(OUT_STATE_OBJECT_ENUM_INFOS), JsonObject.class) : null;
        } else {
            if (originalObjectConnection == null && mContentDefinition.getSaveInfos())
                savedFragmentData = ContentCreationSaveManager.loadContentCreationBundle(this, mContentDefinition.getContentType());
        }

        if (objectEnumInfos != null && (savedFragmentData != null || originalObjectConnection == null)) {
            insertTabs();
        } else {
            if (originalObjectConnection != null) {
                originalObjectConnection.noCache();
                LoginComponentModule loginComponentModule = new LoginComponentModule()
                        .loginRequired(isLoginRequired());
                mDataConnection = ViewModelProviders.of(this).get(CacheManager.Companion.getShared().getModelKey(originalObjectConnection), DataConnectionModel.class);
                mDataConnection.configure(originalObjectConnection,
                        loginComponentModule,
                        ViewModelProviders.of(this).get(PrivacyViewModel.class));
                mDataConnection.getModel().observe(this, new Observer<DataModel>() {
                    @Override
                    public void onChanged(@Nullable DataModel dataModel) {
                        if (dataModel.getCacheValid() && dataModel.getListData().size() > 0) {
                            originalObject = dataModel.getListData().get(0);
                            if (originalObject instanceof RecordWithIdentifier)
                                originalObjectId = ((RecordWithIdentifier) originalObject).getIdentifier();

                            if (objectEnumInfos != null)
                                insertTabs();
                        }
                    }
                });
                mDataConnection.getDataError().observe(this, new Observer<OrchardError>() {
                    @Override
                    public void onChanged(@Nullable OrchardError orchardError) {
                        if (orchardError != null && orchardError.getReactionCode() == 0) {
                            SnackbarUtils.showCloseSnackbar(mPager, R.string.error_content_creation_loading_failed, mHandler, FINISH_ACTIVITY_MSG);
                            mSnackBarIsLaunched = true;
                        }
                    }
                });
            }

            if (objectEnumInfos == null) {
                RemoteRequest request = new RemoteRequest(this)
                        .setPath(getString(R.string.orchard_api_path_content_modify))
                        .setMethod(RemoteRequest.Method.GET)
                        .setQuery("ContentType", mContentDefinition.getContentType());

                RemoteClient.Companion.client(isLoginRequired())
                        .enqueue(request, new Function2<RemoteResponse, OrchardError, Unit>() {
                            @Override
                            public Unit invoke(RemoteResponse remoteResponse, OrchardError orchardError) {

                                if (remoteResponse != null) {
                                    objectEnumInfos = ContentCreationUtils.getGsonInstance().fromJson(remoteResponse.string(), JsonObject.class);
                                    if (contentCreationComponentModule.getOriginalObjectConnection() == null || originalObject != null)
                                        insertTabs();
                                } else if (orchardError != null) {
                                    SnackbarUtils.showCloseSnackbar(mPager, R.string.error_content_creation_loading_failed, mHandler, FINISH_ACTIVITY_MSG);
                                    mSnackBarIsLaunched = true;
                                }

                                return null;
                            }
                        });
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        disableViewPagerPages(mPager.getCurrentItem());
    }

    protected TabLayoutHelper createTabHelper() {
        return new TabLayoutHelper.InflaterBuilder(this)
                .layout(R.layout.partial_tabs_fixed)
                .build();
    }

    protected int tabLayoutIndexInRoot() {
        return 1;
    }

    /**
     * disabilito il focus sulle view delle pagine del viewpager diverse da quella corrente nel caso in cui la larghezza delle pagine sia inferiore a 1 (in landscape)
     * altrimenti se sono su una pagina riesco a cliccare su una view di un altra pagina, causando una brutta user experience, faccio questo andando a prendere la view root del fragment
     * e setto con {@link ViewGroup#setDescendantFocusability(int)} se le view sono focusables o no
     *
     * @param currentPosition posizione della pagina corrente
     */
    private void disableViewPagerPages(int currentPosition) {
        ViewGroup fragmentRoot;
        if (mAdapter != null && mAdapter.getCount() > 1 && mAdapter.getPageWidth(currentPosition) < 1) {
            for (int i = 0; i < mAdapter.getCount(); i++) {
                if ((fragmentRoot = (ViewGroup) ((ContentItemPagerAdapter) mAdapter).getItem(i).getView()) != null) {
                    fragmentRoot.setDescendantFocusability((i == currentPosition) ? ViewGroup.FOCUS_BEFORE_DESCENDANTS : ViewGroup.FOCUS_BLOCK_DESCENDANTS);
                }
            }
        }
    }

    private void insertTabs() {
        mContentEditFragments = new LinkedList<>();

        for (int i = 0; i < mContentDefinition.getTabs().size(); i++) {
            ContentCreationTabInfo tabInfo = mContentDefinition.getTabs().get(i);

            ContentCreationFragment fragment = createFragmentForTab(tabInfo.getInfo());

            mContentEditFragments.add(fragment);
            if (savedFragmentData != null && savedFragmentData.size() > i && !TextUtils.isEmpty(savedFragmentData.get(i)))
                mFragmentEditValues.add(fragment.deserializeSavedInstanceState(this, tabInfo.getInfo(), ContentCreationUtils.getGsonInstance(), savedFragmentData.get(i)));
            else
                mFragmentEditValues.add(null);
        }

        mAdapter.notifyDataSetChanged();
        int tabsCount = mContentDefinition.getTabs().size();
        if (tabsCount > 1) {
            // The page limit will be equal to the tabs count to load all Fragments together.
            // This must be reviewed.
            mPager.setOffscreenPageLimit(tabsCount);
            mTabLayout.setupWithViewPager(mPager);

            mTabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mPager) {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    super.onTabSelected(tab);
                    closeKeyboard();
                }
            });
            mTabLayout.setVisibility(View.VISIBLE);
        }

        changeContentVisibility(true);
        mChangedData = false;
        disableViewPagerPages(mPager.getCurrentItem());
    }

    @Override
    public void changeContentVisibility(boolean visible) {

        mSendingData = !visible;
        invalidateOptionsMenu();
        if (visible && mAdapter.getCount() > 0) {
            mProgress.setVisibility(View.GONE);

            if (mPager.getVisibility() != View.VISIBLE)
                mPager.setVisibility(View.VISIBLE);
        } else {
            if (mPager.getVisibility() != View.INVISIBLE)
                mPager.setVisibility(View.INVISIBLE);

            mProgress.setVisibility(View.VISIBLE);
        }
    }

    protected ContentCreationFragment createFragmentForTab(ContentCreationTabInfo.ContentCreationInfo info) {
        if (info instanceof ContentCreationTabInfo.MediaInfo) {
            return MediaPickerFragment.newInstance();
        }

        if (info instanceof ContentCreationTabInfo.ContentFieldsInfos) {
            return FieldsFragment.newInstance();
        }

        if (info instanceof ContentCreationTabInfo.MapInfo) {
            return LocationSelectionFragment.Companion.newInstance();
        }

        if (info instanceof ContentCreationTabInfo.PolicyInfo) {
            return PoliciesFragment.Companion.newInstance(this);
        }

        throw new RuntimeException();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.send_content_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_send_content).setVisible(!mSendingData);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_send_content) {
            if (canSendReport()) {
                closeKeyboard();
                sendDataToService();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onUpNavigationButtonSelected() {
        return askForSaveInfos();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (!mChangeMenuItemActionDispatched && askForSaveInfos()) {
            mCurrentMenuItem = item;
            return false;
        }
        mChangeMenuItemActionDispatched = false;
        mCurrentMenuItem = null;
        return super.onNavigationItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mSnackBarIsLaunched && !askForSaveInfos()) {
            super.onBackPressed();
        }
    }

    @Override
    public void finish() {
        // Used to close the keyboard before the Activity is dismissed.
        LayoutUtils.hideKeyboard(this, mCoordinator);
        super.finish();
    }

    private boolean askForSaveInfos() {
        if (!mSendingData && mChangedData) {
            OrchardComponentModule originalObjectConnection = contentCreationComponentModule.getOriginalObjectConnection();
            final boolean saveDataAction = originalObjectConnection == null && mContentDefinition.getSaveInfos();
            int messageCode = saveDataAction ? R.string.content_creation_save_for_later : R.string.content_creation_lose_modification;
            new AlertDialog.Builder(this)
                    .setTitle(this.getTitle())
                    .setMessage(getString(messageCode))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (saveDataAction) {
                                updateSavedPreferences();
                            }
                            if (mCurrentMenuItem == null) {
                                finish();
                            } else {
                                mChangeMenuItemActionDispatched = true;
                                onNavigationItemSelected(mCurrentMenuItem);
                            }
                        }
                    })
                    .setNeutralButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (saveDataAction) {
                                if (mContentDefinition.getSaveInfos()) {
                                    ContentCreationSaveManager.saveContentCreationInfos(ContentCreationActivity.this,
                                            mContentDefinition.getContentType(),
                                            null);
                                }
                                if (mCurrentMenuItem == null) {
                                    finish();
                                } else {
                                    mChangeMenuItemActionDispatched = true;
                                    onNavigationItemSelected(mCurrentMenuItem);
                                }
                            }
                        }
                    })
                    .show();
            return true;
        } else {
            finish();
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveFragmentValues(outState);
        if (!mSendingData && mContentDefinition != null && contentCreationComponentModule.getOriginalObjectConnection() == null) {
            ContentCreationSaveManager.saveContentCreationInfos(this, mContentDefinition.getContentType(), outState.getStringArrayList(OUT_STATE_SAVED_INFOS));
        }
        if (objectEnumInfos != null)
            outState.putString(OUT_STATE_OBJECT_ENUM_INFOS, new Gson().toJson(objectEnumInfos));

        if (originalObjectId != null)
            outState.putLong(OUT_STATE_ORIGINAL_OBJECT_ID, originalObjectId);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        saveFragmentValues(outState);
        if (!mSendingData && mContentDefinition != null && mContentDefinition.getSaveInfos() && contentCreationComponentModule.getOriginalObjectConnection() == null) {
            ContentCreationSaveManager.saveContentCreationInfos(this, mContentDefinition.getContentType(), outState.getStringArrayList(OUT_STATE_SAVED_INFOS));
        }
        if (objectEnumInfos != null)
            outState.putString(OUT_STATE_OBJECT_ENUM_INFOS, new Gson().toJson(objectEnumInfos));

        if (originalObjectId != null)
            outState.putLong(OUT_STATE_ORIGINAL_OBJECT_ID, originalObjectId);
    }

    private void saveFragmentValues(Bundle outState) {
        ArrayList<String> fragmentInfos = new ArrayList<>();
        final Gson gson = ContentCreationUtils.getGsonInstance();
        for (Object value : mFragmentEditValues) {
            if (value != null) {
                fragmentInfos.add(gson.toJson(value));
            } else {
                fragmentInfos.add("");
            }
        }
        outState.putStringArrayList(OUT_STATE_SAVED_INFOS, fragmentInfos);
    }

    private void sendDataToService() {
        final String contentType = mContentDefinition.getContentType();

        JsonObject jsonParameters = new JsonObject();

        if (mContentDefinition.getContentAdditionalNonEditableInfos() != null) {
            Set<Map.Entry<String, JsonElement>> entries = mContentDefinition.getContentAdditionalNonEditableInfos().entrySet();
            for (Map.Entry<String, JsonElement> entry : entries) {
                jsonParameters.add(entry.getKey(), entry.getValue());
            }
        }

        jsonParameters.addProperty(Constants.REQUEST_CONTENT_TYPE, contentType);
        if (originalObjectId != null && originalObjectId != 0)
            jsonParameters.addProperty(getString(R.string.orchard_new_content_id_parameter), originalObjectId);


        int index = 0;

        boolean needToUploadMedias = false;

        KeyMediaListMap keyMediaListMap = new KeyMediaListMap();

        for (ContentCreationFragment fragment : mContentEditFragments) {
            ContentCreationTabInfo.ContentCreationInfo creationInfos = mContentDefinition.getTabs().get(index).getInfo();
            if (!fragment.insertDataToUpload(this,
                    creationInfos,
                    getFragmentData(fragment),
                    jsonParameters)) {

                needToUploadMedias = true;

                MediaPickerFragment.MediaPickerInfos savedInfo = (MediaPickerFragment.MediaPickerInfos) getFragmentData(fragment);
                final String orchardKey = ((ContentCreationTabInfo.MediaInfo) getFragmentCreationInfo(fragment)).getOrchardKey();

                keyMediaListMap.put(orchardKey, savedInfo.pickedInfos);
            }
            ++index;
        }

        final Intent intent = getIntent();

        boolean loginRequired = loginComponentModule.getLoginRequired();

        Bundle endApiCallBundle = new Bundle();
        endApiCallBundle.putBoolean(OrchardUploadService.EXTRA_LOGIN_REQUIRED, loginRequired);
        endApiCallBundle.putString(Constants.REQUEST_CONTENT_TYPE, contentType);
        endApiCallBundle.putBundle(CreateContentUploadMediaEndListener.EXTRA_CONTENT_CREATION_INTENT_EXTRA, intent.getExtras());
        endApiCallBundle.putParcelable(CreateContentUploadMediaEndListener.EXTRA_CONTENT_CREATION_INTENT_CLASS_NAME, intent);
        endApiCallBundle.putString(CreateContentUploadMediaEndListener.EXTRA_CONTENT_PARAMETERS_JSON, new Gson().toJson(jsonParameters));

        updateSavedPreferences();

        if (needToUploadMedias && keyMediaListMap.size() > 0 && keyMediaListMap.getAllValues().size() > 0) {
            OrchardUploadService.startServiceToUploadFiles(this,
                    keyMediaListMap,
                    CONTENT_CREATION_UPLOAD_MEDIA_BASE + contentType.hashCode(),
                    contentCreationComponentModule.getUploadParams(),
                    endApiCallBundle,
                    loginRequired);
        } else {

            RemoteRequest request = new RemoteRequest(this)
                    .setMethod(RemoteRequest.Method.POST)
                    .setBody(jsonParameters)
                    .setPath(getString(R.string.orchard_api_path_content_modify));

            changeContentVisibility(false);
            Signaler.shared.invokeAPI(this,
                    request,
                    loginRequired,
                    endApiCallBundle,
                    new Function2<RemoteResponse, OrchardError, Unit>() {
                        @Override
                        public Unit invoke(RemoteResponse remoteResponse, OrchardError orchardError) {
                            changeContentVisibility(true);
                            return null;
                        }
                    });
        }
    }

    private boolean canSendReport() {
        boolean canSend = false;
        if (mContentEditFragments != null) {
            canSend = true;
            int index = 0;
            for (ContentCreationFragment fragment : mContentEditFragments) {
                if (!fragment.validateDataAndSaveError(this, getFragmentCreationInfo(fragment), getFragmentData(fragment))) {
                    if (canSend) {
                        canSend = false;
                        mPager.setCurrentItem(index);
                    }
                }
                ++index;
            }
        }

        if (!canSend) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

            if (v.hasVibrator())
                v.vibrate(200);
        }
        return canSend;
    }

    private void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mPager.getWindowToken(), 0);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item);
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Override
    public boolean handleMessage(Message msg) {
        boolean h = false;
        Bundle data = msg.getData();

        switch (msg.what) {
            case OrchardUploadService.Output.MESSAGE_STATUS: {
                OrchardUploadService.UploadStatus status = OrchardUploadService.UploadStatus.valueOf(data.getString(OrchardUploadService.Output.UPLOAD_STATUS));

                changeContentVisibility(!status.equals(OrchardUploadService.UploadStatus.UPLOADING_FILE));
            }
            break;

            case OrchardUploadService.Output.MESSAGE_FILE_UPLOADED:
                if (data.getInt(OrchardUploadService.Output.UPLOAD_CODE) == ContentCreationActivity.CONTENT_CREATION_UPLOAD_MEDIA_BASE + mContentDefinition.getContentType().hashCode()) {
                    final String serializedMedias = data.getString(OrchardUploadService.Output.UPDATED_MEDIA_LIST);

                    KeyListMap<UploadableMediaInfo> keyListMediaMap = null;
                    if (serializedMedias != null) {
                        keyListMediaMap = new KeyMediaListMap().deserialize(serializedMedias);

                        int index = 0;
                        for (ContentCreationFragment fragment : mContentEditFragments) {
                            ContentCreationTabInfo.ContentCreationInfo info = mContentDefinition.getTabs().get(index).getInfo();
                            if (fragment instanceof MediaPickerFragment) {
                                ContentCreationTabInfo.MediaInfo mediaInfo = (ContentCreationTabInfo.MediaInfo) info;
                                MediaPickerFragment.MediaPickerInfos infos = (MediaPickerFragment.MediaPickerInfos) getFragmentData(fragment);

                                final String orchardKey = mediaInfo.getOrchardKey();
                                List<UploadableMediaInfo> medias = keyListMediaMap.get(orchardKey);

                                if (medias != null) {
                                    infos.pickedInfos.clear();
                                    infos.pickedInfos.addAll(medias);

                                    updateFragmentData(fragment, infos);
                                    updateSavedPreferences();
                                }
                            }
                            ++index;
                        }
                    }

                    if (data.getBoolean(OrchardUploadService.Output.SUCCESS)) {
                        if (keyListMediaMap != null) {
                            ((KeyMediaListMap) keyListMediaMap).deleteAll(ContentCreationActivity.this);
                        }
                    } else {
                        OrchardError error = new Gson().fromJson(data.getString(OrchardUploadService.Output.ERROR_ORCHARD), OrchardError.class);
                        if (error.getReactionCode() == 0) {
                            SnackbarUtils.createSnackbar(mPager, error.getUserFriendlyMessage(this), Snackbar.LENGTH_LONG).show();
                        }
                    }
                }

                break;

            case FINISH_ACTIVITY_MSG:
                mSnackBarIsLaunched = false;
                if (!contentCreationComponentModule.getAvoidActivityClosingAfterContentSent())
                    finish();
                else
                    mChangedData = false;

                break;
        }
        return h;
    }

    public void updateFragmentData(ContentCreationFragment fragment, Object savedInfos) {
        mChangedData = true;
        int index = mContentEditFragments.indexOf(fragment);
        if (index != -1) {
            mFragmentEditValues.add(index, savedInfos);
            mFragmentEditValues.remove(index + 1);
        }
    }

    public
    @Nullable
    Object getFragmentData(ContentCreationFragment fragment) {
        int index = mContentEditFragments.indexOf(fragment);
        if (index != -1)
            return mFragmentEditValues.get(index);
        return null;
    }

    private ContentCreationTabInfo getFragmentTabInfo(ContentCreationFragment fragment) {
        int index = mContentEditFragments.indexOf(fragment);

        if (index >= 0) {
            return mContentDefinition.getTabs().get(index);
        }
        return null;
    }

    public ContentCreationTabInfo.ContentCreationInfo getFragmentCreationInfo(ContentCreationFragment fragment) {
        //noinspection ConstantConditions
        return getFragmentTabInfo(fragment).getInfo();
    }

    private void updateSavedPreferences() {
        Bundle bundle = new Bundle();
        saveFragmentValues(bundle);
        if (contentCreationComponentModule.getOriginalObjectConnection() == null && mContentDefinition.getSaveInfos())
            ContentCreationSaveManager.saveContentCreationInfos(this, mContentDefinition.getContentType(), bundle.getStringArrayList(OUT_STATE_SAVED_INFOS));
    }

    public Object getOriginalObject() {
        return originalObject;
    }

    public JsonObject getObjectEnumInfos() {
        return objectEnumInfos;
    }

    @Override
    public void onContentCreated() {
        setResult(RESULT_CONTENT_SENT);
        SnackbarUtils.showCloseSnackbar(mPager, originalObject == null ? R.string.new_content_created : R.string.updated_content, mHandler, FINISH_ACTIVITY_MSG);
        mSnackBarIsLaunched = true;
        mChangedData = false;
    }

    @Override
    public void onContentCreationFailed(@NotNull OrchardError orchardError) {
        if (orchardError.getReactionCode() == 0)
            SnackbarUtils.createSnackbar(mPager, orchardError.getUserFriendlyMessage(this), Snackbar.LENGTH_LONG).show();
    }

    private class ContentItemPagerAdapter extends FragmentPagerAdapter {
        ContentItemPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return ((Fragment) mContentEditFragments.get(i));
        }

        @Override
        public int getCount() {
            return mContentEditFragments != null ? mContentEditFragments.size() : 0;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(mContentDefinition.getTabs().get(position).getInfo().getTabTitle());
        }

        @Override
        public float getPageWidth(int position) {
            return getCount() > 1 ? getResources().getInteger(R.integer.content_creation_page_percentage_width) / 100.0f : 1;
        }
    }
}