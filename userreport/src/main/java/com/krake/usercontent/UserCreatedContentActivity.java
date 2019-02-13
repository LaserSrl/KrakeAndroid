package com.krake.usercontent;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.krake.contentcreation.ContentCreationActivity;
import com.krake.core.Signaler;
import com.krake.core.app.ContentItemDetailActivity;
import com.krake.core.app.ContentItemListMapActivity;
import com.krake.core.component.annotation.BundleResolvable;
import com.krake.core.component.base.ComponentManager;
import com.krake.core.component.module.LoginComponentModule;
import com.krake.core.component.module.OrchardComponentModule;
import com.krake.core.component.module.ThemableComponentModule;
import com.krake.core.extension.BundleExtensionsKt;
import com.krake.core.login.LoginFragment;
import com.krake.core.model.ContentItem;
import com.krake.core.view.TabLayoutHelper;
import com.krake.usercontent.component.module.UserContentComponentModule;
import com.krake.usercontent.model.UserCreatedContent;

/**
 * Acitivity per mostrare i contenuti creati dagli utenti.
 * I contenuti devono implementare l'interfaccia {@link UserCreatedContent}.
 * <p/>
 * I contenuti vengono mostrati in 2 tab: il primo mostra i contenuti pubblicati di tutti gli utenti.
 * Il secondo, una volta effettuata la login, mostra tutti i contenuti dell'utente.
 * I contenuti dei 2 tab sono caricati da orchard tramite display alias, rispettivamente vengono utilizzati
 * R.string.orchard_reports_display_alias e R.string.orchard_user_reports_display_alias
 * <p/>
 * Necessario registrare il listener {@link InvalidateUserCacheAPIListener} per la chiamata di API con display alias:
 * R.string.orchard_api_path_new_content
 */
public class UserCreatedContentActivity extends ContentItemListMapActivity implements View.OnClickListener, TabLayout.OnTabSelectedListener {
    private static final int REQ_CODE_CONTENT_CREATION = 72;
    private static final String OUT_STATE_SELECTED_TAB = "SelectedTab";
    @BundleResolvable
    public UserContentComponentModule userContentComponentModule;
    private TabLayout mSlidingTab;

    @SuppressWarnings("ConstantConditions")
    @SuppressLint("InflateParams")
    @Override
    public void onCreate(Bundle savedInstanceState, int layout) {
        super.onCreate(savedInstanceState, layout);

        ViewGroup group = findViewById(R.id.activity_layout_coordinator);
        getLayoutInflater().inflate(R.layout.partial_fab_bottom, group, true);

        UserContentTab[] tabs = userContentComponentModule.getTabs();
        int tabCount = tabs.length;

        if (tabCount == 0)
            throw new RuntimeException("You must provide at least 1 tab.");

        UserContentTab firstTab = tabs[0];
        setLoginRequired(firstTab.isLoginRequired());

        if (tabCount > 1) {
            AppBarLayout verticalLayout = findViewById(R.id.app_bar_layout);

            TabLayoutHelper tabLayoutHelper = createTabHelper();
            tabLayoutHelper.addToView(verticalLayout, tabLayoutIndexInRoot());

            mSlidingTab = tabLayoutHelper.layout();

            for (UserContentTab tab : tabs) {
                tabLayoutHelper.addTab(getString(tab.getTitle()), null, tab);
            }

            tabLayoutHelper.layout().addOnTabSelectedListener(this);

            Toolbar toolbar = verticalLayout.findViewById(R.id.toolbar_actionbar);
            if (!isUsingMapAndGridLayout() && toolbar.getLayoutParams() instanceof AppBarLayout.LayoutParams) {
                AppBarLayout.LayoutParams toolbarParams = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
                toolbarParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
                toolbar.setLayoutParams(toolbarParams);
            }

            if (savedInstanceState != null) {
                mSlidingTab.getTabAt(savedInstanceState.getInt(OUT_STATE_SELECTED_TAB, 0)).select();
            }
        }

        FloatingActionButton newContentFab = findViewById(R.id.detail_fab);
        newContentFab.setOnClickListener(this);

        if (userContentComponentModule.getContentCreationBundle() == null) {
            newContentFab.setVisibility(View.GONE);
        } else {
            Signaler.shared.registerApiEndListener(getString(R.string.orchard_api_path_content_modify), new InvalidateUserCacheAPIListener());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_CONTENT_CREATION && resultCode == ContentCreationActivity.RESULT_CONTENT_SENT) {
            if (getGridFragment() != null) {
                getGridFragment().getDataConnectionModel().restartDataLoading();
            }
            if (getMapFragment() != null) {
                getMapFragment().getDataConnectionModel().restartDataLoading();
            }
        }
    }

    protected Bundle getFragmentCreationExtras() {
        UserContentTab firstTab = userContentComponentModule.getTabs()[0];
        Bundle extras = getIntent().getExtras();
        extras.putBoolean(LoginComponentModule.ARG_LOGIN_REQUIRED, firstTab.isLoginRequired());
        extras.putString(OrchardComponentModule.ARG_DISPLAY_PATH, firstTab.getDisplayAlias());
        return extras;
    }

    protected TabLayoutHelper createTabHelper() {
        final AppBarLayout.LayoutParams params = new AppBarLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setScrollFlags(0);
        return new TabLayoutHelper.InflaterBuilder(this)
                .layout(R.layout.partial_tabs_fixed)
                .layoutParams(params)
                .build();
    }

    protected int tabLayoutIndexInRoot() {
        return 1;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSlidingTab != null)
            outState.putInt(OUT_STATE_SELECTED_TAB, mSlidingTab.getSelectedTabPosition());
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        if (mSlidingTab != null)
            outState.putInt(OUT_STATE_SELECTED_TAB, mSlidingTab.getSelectedTabPosition());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);

        if (userContentComponentModule.getHelpDetailBundle() != null) {
            getMenuInflater().inflate(R.menu.menu_show_infos, menu);
        }

        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_show_infos) {
            Intent helpBundle = ComponentManager.createIntent()
                    .from(this)
                    .to(ContentItemDetailActivity.class)
                    .put(helpDetailBundle())
                    .build();
            startActivity(helpBundle);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Crea il {@link Bundle} che verrà passato alla {@link ContentItemDetailActivity} di help.
     * Utilizzando {@link UserContentComponentModule#getHelpDetailBundle()} come {@link Bundle} di partenza, viene aggiunto l'up {@link Intent}.
     *
     * @return {@link Bundle} con le proprietà relative al {@link ContentItem} selezionato.
     */
    protected Bundle helpDetailBundle() {
        ThemableComponentModule themableModule = new ThemableComponentModule();
        //noinspection ConstantConditions
        themableModule.readContent(this, userContentComponentModule.getHelpDetailBundle());
        themableModule.upIntent(getIntent());

        /* Necessario per evitare delle references circolari che non sono supportate dai Parcelable. */
        Bundle bundle = new Bundle();
        bundle.putAll(userContentComponentModule.getHelpDetailBundle());
        BundleExtensionsKt.putModules(bundle, this, themableModule);
        return bundle;
    }

    private void updateFragmentsAlias(boolean loginRequired, String displayPath) {
        setLoginRequired(loginRequired);
        getGridFragment().getLoginComponentModule().loginRequired(loginRequired);
        getGridFragment().updateDisplayPath(displayPath);

        if (getMapFragment() != null) {
            getMapFragment().getLoginComponentModule().loginRequired(loginRequired);
            getMapFragment().updateDisplayPath(displayPath);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onLoginCancelled(LoginFragment fragment) {
        UserContentTab[] tabs = userContentComponentModule.getTabs();
        int tabCount = tabs.length;

        if (tabCount == 0)
            throw new RuntimeException("You must provide at least 1 tab.");

        if (!tabs[0].isLoginRequired()) {
            mSlidingTab.getTabAt(0).select();
            setLoginRequired(false);
        } else {
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.detail_fab) {
            final Intent intent = createContentCreationIntent();
            startActivityForResult(intent, REQ_CODE_CONTENT_CREATION);
        }
    }

    protected Intent createContentCreationIntent() {
        return ComponentManager.createIntent()
                .from(this)
                .to(ContentCreationActivity.class)
                .put(userContentComponentModule.getContentCreationBundle())
                .build();
    }

    /**
     * Called when a tab enters the selected state.
     *
     * @param tab The tab that was selected
     */
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (getSearchView() != null)
            getSearchView().hide(true);

        UserContentTab tabInfo = (UserContentTab) tab.getTag();
        if (tabInfo != null) {
            updateFragmentsAlias(tabInfo.isLoginRequired(), tabInfo.getDisplayAlias());
            BundleExtensionsKt.putModules(listMapComponentModule.getDetailBundle(),
                    this,
                    new LoginComponentModule().loginRequired(tabInfo.isLoginRequired()));

        }
    }

    /**
     * Called when a tab exits the selected state.
     *
     * @param tab The tab that was unselected
     */
    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    /**
     * Called when a tab that is already selected is chosen again by the user. Some applications
     * may use this action to return to the top level of a category.
     *
     * @param tab The tab that was reselected.
     */
    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}