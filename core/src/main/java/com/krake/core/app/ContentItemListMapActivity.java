package com.krake.core.app;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.TypedValue;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.krake.core.ClassUtils;
import com.krake.core.OrchardError;
import com.krake.core.R;
import com.krake.core.component.annotation.BundleResolvable;
import com.krake.core.component.base.ComponentManager;
import com.krake.core.component.base.ComponentModule;
import com.krake.core.component.module.*;
import com.krake.core.data.DataConnectionModel;
import com.krake.core.data.DataModel;
import com.krake.core.extension.BundleExtensionsKt;
import com.krake.core.map.MapListSwitcher;
import com.krake.core.media.MediaPartFullscreenActivity;
import com.krake.core.media.MediaSelectionListener;
import com.krake.core.model.*;
import com.krake.core.widget.BottomSheetNotUnderActionBehavior;
import com.krake.core.widget.FloatSearchView;
import com.krake.core.widget.SafeBottomSheetBehavior;
import com.krake.core.widget.SnackbarUtils;
import io.realm.RealmModel;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Classe per mostrare i contenuti di orchard.
 * <p/>
 * La classe gestisce autonomamente un fragment {@link ContentItemGridModelFragment} o sue estensione e
 * un {@link ContentItemMapModelFragment}.
 * Se i contenuti da mostrare implementano l'interfaccia {@link com.krake.core.model.ContentItemWithLocation} sarà mostrata anche la mappa.
 * In caso contrario non sarà inserito quel particolare fragment.
 * <p/>
 * Se presente anche la mappa l'activity utilizza come layout R.layout.activity_content_items_map_grid.
 * Sono predisposte diverse versioni di questo layout:
 * <ul>
 * <li>.R.layout.activity_content_items_map_grid_sliding_up layout moderno in stile con le mappa di google.
 * Sono mostrate nella stessa videtata lista e mappa.</li>
 * <li>R.layout.activity_content_items_map_or_grid switch tramite menu per il passaggio tra lista e mappa</li>
 * <li>R.layout.activity_content_items_map_and_grid lista e mappa mostrate fianco a fianco</li>
 * </ul>
 * in caso contrario viene utilizzato R.layout.activity_content_items_list_only
 * <p/>
 */
public class ContentItemListMapActivity extends LoginAndPrivacyActivity
        implements OnContentItemSelectedListener,
        MapListSwitcher.VisibiliyChangeListener,
        MediaSelectionListener,
        SwipeRefreshLayout.OnRefreshListener,
        DataConnectionLoadListener,
        AppBarLayout.OnOffsetChangedListener,
        TermsFragment.Listener,
        SearchView.OnQueryTextListener,
        FloatSearchView.AppearanceListener {

    private static final String STATE_MAP_VISIBLE = "otsStateMapVisible";

    @BundleResolvable
    public ListMapComponentModule listMapComponentModule;
    @BundleResolvable
    public OrchardComponentModule orchardComponentModule;
    protected boolean mContentItemsHaveLocation;
    protected boolean snackBarVisible;
    protected boolean mTwoPane;
    private MapListSwitcher mMapListSwitcher;
    private AppBarLayout mAppBarLayout;
    private ContentItemGridModelFragment mGridFragment;
    private ContentItemMapModelFragment mMapFragment;
    private View mMapFragmentContainer;
    private View mGridFragmentContainer;
    private SwipeRefreshLayout mRefresher;
    private boolean isSearchEnabled;
    private FloatSearchView mSearchView;
    private boolean mUsingMapAndGridLayout;
    private boolean mAppBarIsExpanded = true;
    private boolean mIsSwipeRefreshing = false;

    /**
     * Salva l'altezza corrente dell'AppBarLayout per evitare cambiamenti inutili nell'offset dello SwipeRefreshLayout.
     * <br/>
     * Non si può rimuovere la callback di {@link ViewTreeObserver.OnGlobalLayoutListener#onGlobalLayout()} perché non si riceverebbero più i cambiamenti d'altezza.
     */
    private int mCurrentAblHeight;

    private SafeBottomSheetBehavior.BottomSheetStateCallback bottomSheetCallback;

    /**
     * Salva l'ultimo offset registrato dalla callback onSlide() di un BottomSheetBehavior.
     * <br/>
     * Questo comportamento è utile per evitare di centrare inutilmente una view se l'offset non è cambiato,
     * per esempio quando si scorre la lista con il BottomSheet completamente espanso.
     */
    private float mLastSlideOffset = -1;

    public ContentItemGridModelFragment getGridFragment() {
        return mGridFragment;
    }

    public ContentItemMapModelFragment getMapFragment() {
        return mMapFragment;
    }

    public View getGridContainer() {
        return mGridFragmentContainer;
    }

    public View getMapContainer() {
        return mMapFragmentContainer;
    }

    @Override
    public void onCreate(Bundle savedInstanceState, int layout) {
        setDefaultTransitions();

        super.onCreate(savedInstanceState, layout);

        boolean showMap = listMapComponentModule.getShowMap();
        Class<? extends RealmModel> contentItemClass = orchardComponentModule.getDataClass();
        if (contentItemClass != null) {
            mContentItemsHaveLocation = ContentItemWithLocation.class.isAssignableFrom(contentItemClass) && showMap;
        } else {
            mContentItemsHaveLocation = showMap;
        }

        int layoutResourceToInflate = listMapComponentModule.getActivityLayout();

        if (layoutResourceToInflate == 0) {
            layoutResourceToInflate = mContentItemsHaveLocation ?
                    R.layout.activity_content_items_map_grid : R.layout.activity_content_items_list_only;
        }

        final ViewGroup activityElementRoot = (ViewGroup) getLayoutInflater().inflate(layoutResourceToInflate, mCoordinator, false);

        String rootTag = (String) activityElementRoot.getTag();
        boolean useSwitcher = rootTag != null && rootTag.equals(getString(R.string.tag_use_switcher));

        if (activityElementRoot instanceof CoordinatorLayout) {
            while (activityElementRoot.getChildCount() > 0) {
                View view = activityElementRoot.getChildAt(0);
                activityElementRoot.removeView(view);
                mCoordinator.addView(view);
            }
            mCoordinator.dispatchDependentViewsChanged(mCoordinator.getRootView());
        } else {
            mCoordinator.addView(activityElementRoot);
        }

        mAppBarLayout = findViewById(R.id.app_bar_layout);

        mMapFragmentContainer = findViewById(R.id.contentitem_map);
        mGridFragmentContainer = findViewById(R.id.contentitem_list);

        mGridFragment = (ContentItemGridModelFragment) getSupportFragmentManager().findFragmentById(R.id.contentitem_list);
        mMapFragment = (ContentItemMapModelFragment) getSupportFragmentManager().findFragmentById(R.id.contentitem_map);


        boolean useOffsetListener = true;

        if (mGridFragmentContainer.getLayoutParams() instanceof CoordinatorLayout.LayoutParams) {
            CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) mGridFragmentContainer.getLayoutParams()).getBehavior();

            useOffsetListener = !(behavior instanceof BottomSheetNotUnderActionBehavior);

            if (behavior instanceof BottomSheetNotUnderActionBehavior) {
                bottomSheetCallback = new BottomSheetCallback(this);
                ((BottomSheetNotUnderActionBehavior) behavior).addBottomSheetCallback(bottomSheetCallback);
            }
        }

        if (useOffsetListener) {
            mAppBarLayout.addOnOffsetChangedListener(this);
        }

        String[] searchColumnsName = orchardComponentModule.getSearchColumnsName();
        isSearchEnabled = searchColumnsName != null && searchColumnsName.length > 0;
        if (isSearchEnabled) {
            mSearchView = initializeSearchView();
            mSearchView.setOnQueryTextListener(this);
            mSearchView.setAppearanceListener(this);
        }

        if (mMapFragmentContainer != null) {
            if (mContentItemsHaveLocation) {
                if (useSwitcher) {
                    boolean mapVisible;
                    if (savedInstanceState != null)
                        mapVisible = savedInstanceState.getBoolean(STATE_MAP_VISIBLE);
                    else
                        mapVisible = listMapComponentModule.getContentPriority() == ListMapComponentModule.PRIORITY_MAP;

                    mMapListSwitcher = new MapListSwitcher(this,
                            findViewById(R.id.switch_maplist_fab),
                            mapVisible);
                } else {
                    mUsingMapAndGridLayout = true;
                }

            } else {
                mMapFragmentContainer.setVisibility(View.GONE);
            }
        }

        final View detailContainer = findViewById(R.id.detail_container);
        mTwoPane = detailContainer != null;

        if (!mTwoPane) {
            initializeSwipeRefresh();
        }
    }

    protected void updateContentsWithSearchFilter(String filter) {
        if (mGridFragment != null) {
            updateDataFragmentFilter(filter, mGridFragment);
        }

        if (mMapFragment != null) {
            updateDataFragmentFilter(filter, mMapFragment);
        }
    }

    private void setDefaultTransitions() {
        TypedValue value = new TypedValue();
        if (getTheme().resolveAttribute(R.attr.listMapTransition, value, true)) {
            Window contextWindow = getWindow();
            contextWindow.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            Transition transition = TransitionInflater.from(this).inflateTransition(value.resourceId);
            contextWindow.setExitTransition(transition);
        }
    }

    private void updateDataFragmentFilter(String filter, OrchardDataModelFragment fragment) {
        DataConnectionModel connection = fragment.getDataConnectionModel();
        OrchardComponentModule orchardModule = connection.getOrchardModule();
        if (!orchardModule.getSearchAppliedOnline()) {
            connection.restartDataLoading(filter);
        } else if (orchardModule.getSearchColumnsName() != null) {
            boolean extraChanged = false;
            for (String column : orchardModule.getSearchColumnsName()) {
                String prevFilter = orchardModule.getExtraParameters().get(column);
                if (TextUtils.isEmpty(prevFilter) != TextUtils.isEmpty(filter) || (prevFilter != null && !prevFilter.equals(filter))) {
                    orchardModule.putExtraParameter(column, !TextUtils.isEmpty(filter) ? filter : null);
                    extraChanged = true;
                }
            }
            if (extraChanged)
                connection.restartDataLoading();
        }
    }

    /**
     * Inizializza lo {@link SwipeRefreshLayout} settando il listener, i colori e cambiando il suo offset
     */
    private void initializeSwipeRefresh() {
        mRefresher = findViewById(R.id.swipe_refresh);
        if (mRefresher != null) {
            setSwipeRefreshEnabled(true);
            mRefresher.setOnRefreshListener(this);

            // setta i colori allo SwipeRefreshLayout
            int[] swipeColors = getResources().getIntArray(R.array.swipe_refresh_colors);
            mRefresher.setColorSchemeColors(swipeColors);

            // viene aggiunto all'AppBarLayout un listener per i cambiamenti del layout
            mAppBarLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int ablHeight = mAppBarLayout.getHeight();
                    if (mCurrentAblHeight != ablHeight && ablHeight != 0) {
                        int startHeight = (int) (ablHeight * 1.05);
                        int offsetEndHeight = (int) (ablHeight * 1.4);

                        // cambia l'offset di conseguenza
                        mRefresher.setProgressViewOffset(true, startHeight, offsetEndHeight);
                        mCurrentAblHeight = ablHeight;
                    }
                }
            });
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        mAppBarIsExpanded = verticalOffset >= 0;
        setSwipeRefreshEnabled(mAppBarIsExpanded);
    }

    @Override
    public void onRefresh() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment.isAdded() && fragment instanceof SwipeRefreshLayout.OnRefreshListener) {
                ((SwipeRefreshLayout.OnRefreshListener)fragment).onRefresh();
            }
        }
    }

    /**
     * Modifica lo stato dello {@link SwipeRefreshLayout} in base al caricamento dei dati.
     * <br/>
     * La visibilità dello {@link SwipeRefreshLayout} dipenderà dal boolean passato come parametro e dalla condizione attuale dello {@link SwipeRefreshLayout}
     *
     * @param refreshing true i dati stanno subendo dei cambiamenti
     */
    protected void updateRefreshStatus(final boolean refreshing) {
        if (mRefresher != null) {
            if ((refreshing && !mIsSwipeRefreshing) || (!refreshing && mIsSwipeRefreshing)) {
                mIsSwipeRefreshing = refreshing;
                mRefresher.post(() -> mRefresher.setRefreshing(mIsSwipeRefreshing));
            }
        }
    }

    /**
     * Abilita/disabilita lo {@link SwipeRefreshLayout}. Nel caso in cui venga disabilitato, lo swipe non permetterà il refresh
     *
     * @param enabled true se deve essere abilitato e mostrato
     */
    public void setSwipeRefreshEnabled(boolean enabled) {
        if (!mAppBarIsExpanded)
            enabled = false;

        if (mRefresher != null && enabled != mRefresher.isEnabled()) {
            mRefresher.setEnabled(enabled);
        }
    }

    /**
     * Nasconde lo {@link SwipeRefreshLayout} e ferma la sua animazione
     */
    private void clearRefreshLayout() {
        if (mRefresher != null) {
            if (mRefresher.isRefreshing()) {
                mRefresher.post(new Runnable() {
                    @Override
                    public void run() {
                        mRefresher.setRefreshing(false);
                    }
                });
            }
            mRefresher.destroyDrawingCache();
            mRefresher.clearAnimation();
        }
    }

    /**
     * Initialize the {@link FloatSearchView} that will be used if search functionality must be enabled.
     * The view must be added to the layout here.
     *
     * @return created instance of {@link FloatSearchView}.
     */
    protected FloatSearchView initializeSearchView() {
        return FloatSearchView.Companion.attach(mCoordinator, R.id.toolbar_actionbar, FloatSearchView.Companion.getDEFAULT_VIEW_ID());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mMapListSwitcher != null)
            outState.putBoolean(STATE_MAP_VISIBLE, mMapListSwitcher.isMapVisible());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onShowContentItemDetails(@NonNull Object sender, @NonNull ContentItem contentItem) {
        if (!listMapComponentModule.getNoDetails()) {
            final Intent detailIntent = getDetailIntent(contentItem);
            if (mTwoPane) {
                Class contentItemClass = ClassUtils.dataClassForName(contentItem.getClass().getSimpleName());
                Fragment detailFragment = ((KrakeApplication) getApplication()).instantiateDetailFragmentForClass(contentItemClass, detailIntent.getExtras());

                if (detailFragment != null)
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.detail_container, detailFragment)
                            .commit();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    startActivity(detailIntent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                else
                    startActivity(detailIntent);
            }
        }
    }

    protected Intent getDetailIntent(@NonNull ContentItem contentItem) {
        return ComponentManager.createIntent()
                .from(this)
                .to(listMapComponentModule.getDetailActivity())
                .put(detailBundleForItem(contentItem))
                .build();
    }

    /**
     * Crea il {@link Bundle} che verrà passato alla {@link ContentItemDetailActivity} dopo aver effettuato il tap su una cella.
     * Utilizzando {@link ListMapComponentModule#getDetailBundle()} come {@link Bundle} di partenza, vengono sovrascritti alcuni {@link ComponentModule}
     * usando delle proprietà relative al {@link ContentItem} scelto.
     *
     * @param contentItem elemento della lista selezionato.
     * @return {@link Bundle} con le proprietà relative al {@link ContentItem} selezionato.
     */
    protected Bundle detailBundleForItem(ContentItem contentItem) {
        Bundle detailBundle = listMapComponentModule.getDetailBundle();
        ThemableComponentModule themableModule = new ThemableComponentModule();
        themableModule.readContent(this, detailBundle);
        themableModule.upIntent(getDetailUpNavigationIntent(contentItem));
        themableModule.showNavigationDrawer(false);

        DetailComponentModule detailModule = new DetailComponentModule(this);
        detailModule.readContent(this, detailBundle);

        OrchardComponentModule orchardModule = new OrchardComponentModule();
        orchardModule.readContent(this, detailBundle);
        orchardModule.dataClass(contentItem.getClass());
        if ((orchardModule.getDataPartFilters() == null &&
                !listMapComponentModule.getLoadDetailsByPath()) ||
                !(contentItem instanceof RecordWithAutoroute)) {
            orchardModule.record(contentItem);
        } else {
            orchardModule.displayPath(((RecordWithAutoroute) contentItem).getAutoroutePartDisplayAlias());
        }

        /* Necessario per evitare delle references circolari che non sono supportate dai Parcelable. */
        Bundle bundle = new Bundle();
        bundle.putAll(detailBundle);
        BundleExtensionsKt.putModules(bundle, this, themableModule, detailModule, orchardModule);
        return bundle;
    }

    @Override
    public void onContentItemInEvidence(@NonNull Object senderFragment, @NonNull ContentItem contentItem) {
        if (mMapListSwitcher == null &&
                mContentItemsHaveLocation &&
                ((ContentItemWithLocation) contentItem).getMapPart().isMapValid() &&
                senderFragment == mMapFragment &&
                !isUsingMapAndGridLayout()) {
            mGridFragment.scrollAndSelectContentItem(contentItem);
        }
    }

    /**
     * Metodo per specificare l'intent da associare all'up navigation dell'acitvity che mostra il dettaglio di un
     * elemento selezionato.
     * <strong>default</strong> l'intent con cui è stata avviata l'activity corrente
     *
     * @param contentItem oggetto relativo di cui sarà mostrato il cdettaglio
     * @return intent per l'up navigation
     */
    protected Intent getDetailUpNavigationIntent(@NonNull ContentItem contentItem) {
        return getIntent();
    }

    @Override
    protected void onStart() {
        super.onStart();

        String name = listMapComponentModule.getAnalyticsName();
        if (name == null) {
            name = getTitle() != null ? getTitle().toString() : "";
        }

        if (!TextUtils.isEmpty(name))
            ((AnalyticsApplication) getApplication()).logItemList(name);

    }

    @Override
    public void changeContentVisibility(boolean visible) {
        if (visible) {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();

            if (mGridFragment == null) {
                try {
                    mGridFragment = listMapComponentModule.getListFragmentClass().getConstructor().newInstance();
                    mGridFragment.setArguments(getFragmentCreationExtras(FragmentMode.GRID));
                    transaction.add(R.id.contentitem_list, mGridFragment);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (mMapListSwitcher == null) {
                transaction.show(mGridFragment);
            }

            if (mContentItemsHaveLocation && mMapFragmentContainer != null) {
                if (mMapFragment == null) {
                    try {
                        mMapFragment = listMapComponentModule.getMapFragmentClass().getConstructor().newInstance();
                        mMapFragment.setArguments(getFragmentCreationExtras(FragmentMode.MAP));
                        transaction.add(R.id.contentitem_map, mMapFragment);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (mMapListSwitcher == null) {
                    transaction.show(mMapFragment);
                }
            }

            if (listMapComponentModule.getTermsBundle() != null) {
                ViewGroup tabContainer = findViewById(R.id.tabs_container);
                if (tabContainer == null) {
                    tabContainer = new FrameLayout(this);
                    tabContainer.setId(R.id.tabs_container);

                    final AppBarLayout appBarLayout = findViewById(R.id.app_bar_layout);
                    appBarLayout.addView(tabContainer, tabLayoutIndexInRoot());

                    if (!mContentItemsHaveLocation && !mTwoPane && mToolbar.getLayoutParams() instanceof AppBarLayout.LayoutParams) {
                        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
                        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
                        mToolbar.setLayoutParams(params);

                        AppBarLayout.LayoutParams tabParams = new AppBarLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        tabParams.setScrollFlags(0);
                        tabContainer.setLayoutParams(tabParams);
                    }
                }

                if (manager.findFragmentById(R.id.tabs_container) == null) {
                    TermsModule termsModule = new TermsModule();
                    termsModule.readContent(this, listMapComponentModule.getTermsBundle());
                    try {
                        Fragment fragment = termsModule.getTermsFragmentClass().getConstructor().newInstance();
                        fragment.setArguments(listMapComponentModule.getTermsBundle());
                        transaction.add(R.id.tabs_container, fragment);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            // fix for saveInstanceState commit
            transaction.commitAllowingStateLoss();
        }
    }

    protected int tabLayoutIndexInRoot() {
        return 1;
    }

    protected Bundle getFragmentCreationExtras(@NonNull FragmentMode mode) {
        return getIntent().getExtras();
    }

    @Override
    public void selectedFilterTermPart(@Nullable TermPart termPart, @NonNull TermsModule termsModule) {
        // se l'extra non è presente, di default è true
        if (termsModule.getFilterQueryString()) {
            String termId = null;

            if (termPart != null) {
                termId = String.valueOf(termPart.getIdentifier());
            } else if (termsModule.getAllTabTermId() != 0) {
                termId = String.valueOf(termsModule.getAllTabTermId());
            }

            // se il filtro è applicato in query, si setta l'extra ai fragment e si fa il reload dei dati in ognuno
            if (getMapFragment() != null) {
                getMapFragment().setExtraParameter(getString(R.string.orchard_query_term_ids), termId, false);
            }

            getGridFragment().setExtraParameter(getString(R.string.orchard_query_term_ids), termId, true);

        } else {
            String displayPath = orchardComponentModule.getDisplayPath();
            if (termPart != null) {
                displayPath = termPart.getAutoroutePartDisplayAlias();
            }

            if (getMapFragment() != null) {
                getMapFragment().updateDisplayPath(displayPath, false);
            }

            // se il filtro è applicato sul displayPath, il displayPath viene ricaricato
            getGridFragment().updateDisplayPath(displayPath, true);

        }
    }

    @Override
    public void unselectedFilterTermPart(@Nullable TermPart termPart, @NonNull TermsModule termsModule) {
        if (mSearchView != null)
            mSearchView.hide(true);
    }

    protected FloatSearchView getSearchView() {
        return mSearchView;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        if (mMapListSwitcher != null)
            mMapListSwitcher.onCreateOptionsMenu(menu);

        if (isSearchEnabled) {
            getMenuInflater().inflate(R.menu.menu_search_filter, menu);
        }
        return true;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        checkRefreshEnabled();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        checkRefreshEnabled();
    }

    private void checkRefreshEnabled() {
        if (mRefresher != null && mGridFragmentContainer.getLayoutParams() instanceof CoordinatorLayout.LayoutParams) {
            CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) mGridFragmentContainer.getLayoutParams()).getBehavior();

            if (behavior instanceof BottomSheetBehavior) {
                setSwipeRefreshEnabled(((BottomSheetBehavior) behavior).getState() == BottomSheetBehavior.STATE_COLLAPSED);
            }
        }
    }

    private void toggleMapState(boolean collapseList) {
        if (mGridFragmentContainer.getLayoutParams() instanceof CoordinatorLayout.LayoutParams && isUsingMapAndGridLayout()) {
            CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) mGridFragmentContainer.getLayoutParams()).getBehavior();

            if (behavior instanceof BottomSheetBehavior && isSearchEnabled) {
                final BottomSheetBehavior scrollingBehavior = (BottomSheetBehavior) behavior;

                scrollingBehavior.setState(collapseList ? BottomSheetBehavior.STATE_COLLAPSED : BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mMapListSwitcher != null)
            mMapListSwitcher.onPrepareOptionsMenu(menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            mSearchView.show(true);
        }
        return mMapListSwitcher != null && mMapListSwitcher.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();

        clearRefreshLayout();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMapListSwitcher != null) {
            mMapListSwitcher.onDestroy();
        }

        if (bottomSheetCallback != null) {
            if (mGridFragmentContainer.getLayoutParams() instanceof CoordinatorLayout.LayoutParams) {
                CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) mGridFragmentContainer.getLayoutParams()).getBehavior();
                //noinspection ConstantConditions
                ((BottomSheetNotUnderActionBehavior) behavior).removeBottomSheetCallback(bottomSheetCallback);
            }
        }
    }

    @Override
    public void onMapVisibilityChanged(boolean mapVisible, @org.jetbrains.annotations.Nullable View source) {

        View viewToShow;
        View viewToHide;

        if (mapVisible) {
            viewToShow = mMapFragmentContainer;
            viewToHide = mGridFragmentContainer;
        } else {
            viewToShow = mGridFragmentContainer;
            viewToHide = mMapFragmentContainer;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && viewToShow.isAttachedToWindow()) {
            animateViewChanges(viewToHide, viewToShow, source);

        } else {
            viewToShow.setVisibility(View.VISIBLE);
            viewToHide.setVisibility(View.INVISIBLE);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animateViewChanges(final View viewToHide, final View viewToShow, @Nullable final View sourceView) {
        viewToHide.setVisibility(View.INVISIBLE);
        Animator revealAnimation;

        if (sourceView == null) {
            revealAnimation =
                    ViewAnimationUtils.createCircularReveal(viewToShow,
                            viewToShow.getRight(),
                            viewToShow.getTop(),
                            0,
                            Math.max(viewToShow.getWidth(), viewToShow.getHeight()));

        } else {
            int[] location = new int[2];
            sourceView.getLocationInWindow(location);
            revealAnimation = ViewAnimationUtils.createCircularReveal(viewToShow,
                    location[0],
                    location[1],
                    0,
                    Math.max(viewToShow.getWidth(), viewToShow.getHeight()));
        }

        viewToShow.setVisibility(View.VISIBLE);

        revealAnimation.start();
    }

    @Override
    public void onMediaPartSelected(List<MediaPart> medias, MediaPart mediaPart) {
        @SuppressWarnings("unchecked")
        Intent intent = ComponentManager.createIntent()
                .from(this)
                .to(MediaPartFullscreenActivity.class)
                .with(new MediaComponentModule()
                        .mediaPartClass((Class<? extends RealmModel>) mediaPart.getClass())
                        .mediaPartList(medias)
                        .selectedMediaPart(mediaPart))
                .build();
        startActivity(intent);
    }

    protected MapListSwitcher getMapListSwitcher() {
        return mMapListSwitcher;
    }

    public boolean isUsingMapAndGridLayout() {
        return mUsingMapAndGridLayout;
    }

    public void changeFragments(@NonNull ContentItemGridModelFragment gridFragment,
                                @Nullable ContentItemMapModelFragment mapModelFragment)
    {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();

        transaction.replace(R.id.contentitem_list,gridFragment);

        if (mapModelFragment != null)
        {
            transaction.replace(R.id.contentitem_map, mapModelFragment);
        }

        transaction.commit();

        mGridFragment = gridFragment;
        mMapFragment = mapModelFragment;
    }

    @Override
    public void onBackPressed() {
        if (mSearchView != null && mSearchView.isSearchVisible()) {
            mSearchView.hide(true);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public void onDataLoadFailed(@NonNull OrchardError error,
                                 @Nullable DataModel dataModel) {
        if ((dataModel == null || dataModel.getListData().size() == 0) && error.getReactionCode() == 0 && !snackBarVisible) {
            showError();
        }
    }

    protected void showError() {
        snackBarVisible = true;
        SnackbarUtils.createSnackbar(findViewById(R.id.activity_layout_coordinator), R.string.data_loading_failed, Snackbar.LENGTH_LONG)
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);
                        snackBarVisible = false;
                    }

                    @Override
                    public void onShown(Snackbar snackbar) {
                        super.onShown(snackbar);
                    }
                })
                .show();
    }

    @Override
    public void onDataLoading(boolean isLoading, int page) {
        updateRefreshStatus(isLoading && page == 1);

        if (!isLoading) {
            if (mGridFragmentContainer.getLayoutParams() instanceof CoordinatorLayout.LayoutParams) {
                CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) mGridFragmentContainer.getLayoutParams()).getBehavior();

                if (behavior instanceof BottomSheetNotUnderActionBehavior) {
                    checkIfRefresherHasToBeEnabled(mGridFragmentContainer, ((BottomSheetNotUnderActionBehavior) behavior).getState());
                }
            }
        }
    }

    /**
     * Centra una View verticalmente nella parte visibile di un container che ha come behavior un BottomSheetBehavior.
     * <br/>
     * Questo processo richiede l'assenza dell'attributo android:layout_gravity="center" o "center_vertical" nel layout della ProgressBar
     * perchè la posizione verticale viene determinata runtime.
     *
     * @param container    container con il BottomSheetBehavior
     * @param viewToCenter view da centrare
     */
    public void centerViewInBottomSheet(@NonNull View container, @NonNull View viewToCenter) {
        int progressBarHeight = viewToCenter.getMeasuredHeight();

        View parent = (View) container.getParent();

        int[] bottomSheetXY = new int[2];
        // ottiene la Y assoluta del BottomSheet container per evitare una ricorsione sulle Y relative ai padri.
        container.getLocationOnScreen(bottomSheetXY);

        // non potendo affidarsi alle altezze (il BottomSheet viene ridimensionato in BottomSheetNotUnderActionBehavior), si utilizzano le distanze.
        float guessedHeight = parent.getBottom() - bottomSheetXY[1];

        if (guessedHeight != 0 && progressBarHeight != 0) {
            viewToCenter.setY((guessedHeight - progressBarHeight) / 2);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        updateContentsWithSearchFilter(query);
        getSearchView().clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (!orchardComponentModule.getSearchAppliedOnline() || TextUtils.isEmpty(newText)) {
            updateContentsWithSearchFilter(newText);
        }
        return true;
    }

    @Override
    public void onSearchViewAppeared(@NotNull FloatSearchView view) {
        toggleMapState(false);
    }

    @Override
    public void onSearchViewDisappeared(@NotNull FloatSearchView view) { /* empty */ }

    /**
     * check if the refresher has to be enabled, based on the state or by the allowUserDrag of the view's behavior passed
     */
    private void checkIfRefresherHasToBeEnabled(View bottomSheet, int state) {
        boolean allowUserDrag = true;
        if (bottomSheet.getLayoutParams() instanceof CoordinatorLayout.LayoutParams) {
            CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) bottomSheet.getLayoutParams()).getBehavior();

            if (behavior != null && behavior instanceof BottomSheetNotUnderActionBehavior) {
                BottomSheetNotUnderActionBehavior bottomSheetBehavior = (BottomSheetNotUnderActionBehavior) behavior;
                allowUserDrag = bottomSheetBehavior.isAllowedUserDrag();
            }
        }
        setSwipeRefreshEnabled(!allowUserDrag || state == BottomSheetBehavior.STATE_COLLAPSED);
    }

    private static class BottomSheetCallback extends SafeBottomSheetBehavior.BottomSheetStateCallback {
        private final WeakReference<ContentItemListMapActivity> weakActivity;

        BottomSheetCallback(ContentItemListMapActivity activity) {
            weakActivity = new WeakReference<>(activity);
        }

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            ContentItemListMapActivity act = weakActivity.get();
            if (act != null)
                act.checkIfRefresherHasToBeEnabled(bottomSheet, newState);
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            ContentItemListMapActivity act = weakActivity.get();
            if (act != null && act.mLastSlideOffset != slideOffset) {
                ProgressBar progressBar = act.getGridFragment().getProgressBar();
                if (progressBar != null) {
                    // centra la ProgressBar verticalmente nel BottomSheet
                    act.centerViewInBottomSheet(bottomSheet, progressBar);
                }
                act.mLastSlideOffset = slideOffset;
            }
        }

        @Override
        public void onStateWillChange(@NonNull View bottomSheet, int newState) { /* empty */ }
    }

    public enum FragmentMode {
        MAP,
        GRID
    }
}