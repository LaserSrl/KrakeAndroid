package com.krake.core.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Trace;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NavUtils;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import com.krake.core.R;
import com.krake.core.component.annotation.BundleResolvable;
import com.krake.core.component.base.ComponentManager;
import com.krake.core.component.module.ThemableComponentModule;
import com.krake.core.drawer.NavigationItemIntentSelectionListener;
import com.krake.core.extension.ActivityExtensionsKt;
import com.krake.core.extension.IntentExtensionsKt;
import com.krake.core.util.LayoutUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Activity base che include opzionalmente la navigazione basata sul drawer.
 * <p/>
 * Lo stile di naivigazione dell'App viene impostato dal parametro mainNavigationMode del theme.
 * Sono possibili 3 valori:
 * <ul>
 * <li>NavigationViewDrawer: drawer che utilizza la NavigationView di Google. La navigation viene configurata in base a diversi parametri
 * <ol><li>R.layout.nav_header_main refernce da impostare al layout da utilizzare in cime alla navigation view</li>
 * <li>R.menu.activity_main_drawer menu per caricare i contenuti del drawer</li>
 * <li>navigationViewListenerClass</li> classe da utilizzare per gestire il click sui bottoni del drawer.
 * La classe deve implementare l'interfaccia {@link NavigationItemIntentSelectionListener} con un costruttore pubblico che accetta un parametro di tipo Context</ol>
 * </li>
 * </ul>
 * <p/>
 * Created by    joel on 05/09/14.
 */
public class ThemableNavigationActivity extends AppCompatActivity implements DrawerLayout.DrawerListener,
        NavigationView.OnNavigationItemSelectedListener,
        ResultManager.Provider {

    public static final int MAIN_NAVIGATION_MODE_DRAWER_NAVIGATION_VIEW = 1;
    public static final int MAIN_NAVIGATION_MODE_DRAWER_NONE = 2;
    private static final String EXTRA_NAVIGATION_SECTION_IDENTIFIER = "NavSectionId";
    private static final int INTENT_DELAY_TIME = 250;
    private ResultManager mResultManager;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private final Handler mDelayedIntentHandler = new Handler();
    @BundleResolvable
    public ThemableComponentModule themableComponentModule = new ThemableComponentModule();
    protected CoordinatorLayout mCoordinator;
    protected Toolbar mToolbar;
    private int mNavigationMode = MAIN_NAVIGATION_MODE_DRAWER_NAVIGATION_VIEW;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private NavigationItemIntentSelectionListener mSelectionListener;
    private boolean mNavigationClearParallelsTask;

    public static
    @Nullable
    Intent getSetupIntentForOriginalIntent(Application app, Intent intent) {
        if (intent.getAction() != null &&
                intent.getAction().equals(Intent.ACTION_MAIN) &&
                intent.hasCategory(Intent.CATEGORY_LAUNCHER) &&
                app instanceof CustomLaunchIntentProvider) {

            Intent launchIntent = ((CustomLaunchIntentProvider) app).getLaunchIntent();
            launchIntent.setAction(intent.getAction());
            Set<String> categories = intent.getCategories();
            if (categories != null) {
                for (String category : categories) {
                    if (!launchIntent.hasCategory(category)) {
                        launchIntent.addCategory(category);
                    }
                }
            }
            return launchIntent;
        }
        return null;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Trace.beginSection("themableCreate");
        onCreate(savedInstanceState, 0);

        if (!TextUtils.isEmpty(getString(R.string.version_subtitle)) && getSupportActionBar() != null) {
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                String activitySubTitle = String.format(Locale.getDefault(), "%s - %s (%d)", getString(R.string.version_subtitle), pInfo.versionName, pInfo.versionCode);
                getSupportActionBar().setSubtitle(activitySubTitle);
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
        Trace.endSection();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout != null && (mDrawerLayout.isDrawerOpen(GravityCompat.START) || mDrawerLayout.isDrawerOpen(GravityCompat.END))) {
            mDrawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Creazione del layout dell'activity.
     * Il layout da utilizzare per l'activity deve avere
     * <ol>
     * <li>{@link DrawerLayout} con id R.id.drawer_layout</li>
     * <li>{@link android.view.ViewGroup} con id R.id.activity_content_container</li>
     * </ol>
     * <p/>
     * Importante che un'activity che modifica questo metodo nella sua chiamata <strong>NON</strong>
     * utilizzi la chiamate setContentView(int), ma che effettuare l'inflate
     * del suo contenuto specifico all'interno del ViewGroup con id R.id.activity_content_container
     *
     * @param savedInstanceState bundle savedInstanceState
     * @param layout             default:  R.layout.activity_drawer_with_appbar
     */
    public void onCreate(Bundle savedInstanceState, int layout) {

        ComponentManager.resolveIntent(this);

        mResultManager = new ResultManager();

        @StyleRes int theme = themableComponentModule.getTheme();
        if (theme != 0) {
            setTheme(theme);
        }

        // set the orientation taking the value from ActivityInfo.ScreenOrientation
        final int activityOrientation = getResources().getInteger(R.integer.activity_orientation);
        //noinspection WrongConstant
        if (activityOrientation != getRequestedOrientation()) {
            //noinspection WrongConstant
            setRequestedOrientation(activityOrientation);
        }

        super.onCreate(savedInstanceState);

        TypedArray elements = obtainStyledAttributes(R.styleable.BaseTheme);

        mNavigationMode = elements.getInt(R.styleable.BaseTheme_mainNavigationMode, MAIN_NAVIGATION_MODE_DRAWER_NAVIGATION_VIEW);

        mNavigationClearParallelsTask = elements.getBoolean(R.styleable.BaseTheme_navigationIntentClearParallelsTasks, false);

        if (layout == 0) {
            switch (mNavigationMode) {
                case MAIN_NAVIGATION_MODE_DRAWER_NAVIGATION_VIEW:
                    layout = R.layout.activity_drawer_navigationview;
                    break;

                case MAIN_NAVIGATION_MODE_DRAWER_NONE:
                default:
                    layout = R.layout.activity_no_drawer;
                    break;
            }
        }

        setContentView(layout);

        mToolbar = findViewById(R.id.toolbar_actionbar);
        mCoordinator = findViewById(R.id.activity_layout_coordinator);
        if (mToolbar == null) {

            if (elements.getBoolean(R.styleable.BaseTheme_includeToolbar, true)) {

                View viewToInsert = getLayoutInflater().inflate(R.layout.partial_top_pinned_appbar_layout, mCoordinator);

                if (mCoordinator != null) {
                    mCoordinator.dispatchDependentViewsChanged(viewToInsert);
                }
                mToolbar = findViewById(R.id.toolbar_actionbar);
            }
        }
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }

        if (themableComponentModule.getShowFloating() || elements.getBoolean(R.styleable.BaseTheme_isFloatingWindow, false)) {
            setupFloatingWindow();
        }

        String title = themableComponentModule.getTitle();
        if (title != null) {
            setTitle(title);
        }

        elements.recycle();
    }

    /**
     * Fa l'inflate del main layout nel CoordinatorLayout della {@link ThemableNavigationActivity}
     *
     * @param layout                layout da aggiungere
     * @param withScrollingBehavior se "true" viene aggiunto al layout principale lo ScrollingViewBehavior
     */
    protected View inflateMainView(@LayoutRes int layout, boolean withScrollingBehavior) {
        View view = getLayoutInflater().inflate(layout, mCoordinator, false);
        // aggiunge il layout come ultima view
        mCoordinator.addView(view, mCoordinator.getChildCount());
        if (withScrollingBehavior) {
            LayoutUtils.attachScrollingBehavior(view);
        }
        return view;
    }

    @SuppressWarnings("RestrictedApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mResultManager.dispatchActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mNavigationMode == MAIN_NAVIGATION_MODE_DRAWER_NONE && item.getItemId() == android.R.id.home) {
            onUpNavigationButtonSelected();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        super.setSupportActionBar(toolbar);

        mToolbar = toolbar;

        switch (mNavigationMode) {
            case MAIN_NAVIGATION_MODE_DRAWER_NONE:

                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setDisplayHomeAsUpEnabled(true);
                    actionBar.setHomeButtonEnabled(true);
                    if (getParentActivityIntent() == null)
                        actionBar.setHomeAsUpIndicator(R.drawable.ic_close_24dp);
                }
                break;


            case MAIN_NAVIGATION_MODE_DRAWER_NAVIGATION_VIEW:

                NavigationView navigationView = findViewById(R.id.nav_view);

                TypedValue value = new TypedValue();
                String className = null;
                if (getTheme().resolveAttribute(R.attr.navigationViewListenerClass, value, true)) {
                    className = value.string.toString();
                }

                MenuItem activityMenuItem;
                if (getIntent().hasExtra(EXTRA_NAVIGATION_SECTION_IDENTIFIER)) {
                    activityMenuItem = navigationView.getMenu().findItem(getIntent().getIntExtra(EXTRA_NAVIGATION_SECTION_IDENTIFIER, 0));
                    if (activityMenuItem != null) {
                        activityMenuItem.setChecked(true);
                    }
                }

                try {
                    Class adapterClass = Class.forName(className);
                    mSelectionListener = (NavigationItemIntentSelectionListener) adapterClass.getDeclaredConstructor(Context.class).newInstance(this);
                    navigationView.setNavigationItemSelectedListener(this);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new IllegalStateException("Non è stato configurato correttamente il parametro navigationViewListenerClass nel theme dell'App");
                }

                mDrawerLayout = findViewById(R.id.drawer_layout);
                mDrawerToggle = new ActionBarDrawerToggle(
                        this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

                mDrawerLayout.addDrawerListener(this);
                mDrawerToggle.syncState();

                break;
        }

        if (getParentActivityIntent() != null) {
            if (mDrawerToggle != null) {
                mDrawerToggle.setDrawerIndicatorEnabled(false);
                mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onUpNavigationButtonSelected();
                    }
                });
            }

            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    private void setupFloatingWindow() {
        // configure this Activity as a floating window, dimming the background
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = getResources().getDimensionPixelSize(R.dimen.content_details_floating_width);
        params.height = getResources().getDimensionPixelSize(R.dimen.content_details_floating_height);
        params.alpha = 1;
        params.dimAmount = 0.4f;
        params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        getWindow().setAttributes(params);
    }

    protected void lockClosedDrawer(boolean lockClosed) {

        if (mDrawerLayout != null) {
            mDrawerLayout.setDrawerLockMode(lockClosed ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED : DrawerLayout.LOCK_MODE_UNLOCKED);
            mDrawerToggle.setDrawerIndicatorEnabled(!lockClosed);
        }
    }

    //Drawer listener

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
        if (drawerView instanceof NavigationView) {
            Menu menu = ((NavigationView) drawerView).getMenu();
            int id = getIntent().getIntExtra(EXTRA_NAVIGATION_SECTION_IDENTIFIER, 0);
            MenuItem menuItem = id != 0 ? menu.findItem(id) : null;

            if (menuItem != null)
                menuItem.setChecked(true);
            else {

                for (int index = 0; index < menu.size(); ++index) {
                    menuItem = menu.getItem(index);
                    if (menuItem.isChecked()) {
                        menuItem.setChecked(false);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onDrawerOpened(View drawerView) {
        mDrawerToggle.onDrawerOpened(drawerView);
        LayoutUtils.hideKeyboard(this, drawerView);
    }

    @Override
    public void onDrawerClosed(View drawerView) {
        mDrawerToggle.onDrawerClosed(drawerView);
    }

    @Override
    public void onDrawerStateChanged(int newState) {
        mDrawerToggle.onDrawerStateChanged(newState);
    }

    @Nullable
    @Override
    public Intent getParentActivityIntent() {
        Intent upIntent = themableComponentModule.getUpIntent();
        if (upIntent != null) {
            return upIntent;
        }
        return super.getParentActivityIntent();
    }

    public boolean onUpNavigationButtonSelected() {
        Intent upIntent = NavUtils.getParentActivityIntent(this);
        ActivityExtensionsKt.getKrakeApplication(this).upNavigateToIntent(this, upIntent);
        return true;
    }

    public void startActivity(final Intent activityIntent, boolean delayed) {

        mDelayedIntentHandler.removeCallbacksAndMessages(null);

        if (mDrawerLayout != null)
            mDrawerLayout.closeDrawers();

        if (delayed) {
            mDelayedIntentHandler.postDelayed(new WeakRunnable(ThemableNavigationActivity.this, activityIntent), INTENT_DELAY_TIME);
        } else
            startActivity(activityIntent);
    }

    public void startActivityAndParentsIfRequired(final Intent intent, boolean delayed) {
        if (!mNavigationClearParallelsTask) {
            startActivity(intent, delayed);
        } else {
            List<Intent> parentActivities = new LinkedList<>();
            Intent forIntent = intent;

            ThemableComponentModule module = new ThemableComponentModule();
            module.upIntent(themableComponentModule.getUpIntent());
            while (module.getUpIntent() != null) {
                parentActivities.add(forIntent);
                Bundle extras = forIntent.getExtras();
                module.readContent(this, extras);
                forIntent = module.getUpIntent();
            }

            final TaskStackBuilder builder = TaskStackBuilder.create(ThemableNavigationActivity.this);
            builder.addNextIntentWithParentStack(forIntent);

            for (int index = parentActivities.size() - 1; index >= 0; --index) {
                builder.addNextIntent(parentActivities.get(index));
            }

            mDelayedIntentHandler.removeCallbacksAndMessages(null);

            if (mDrawerLayout != null)
                mDrawerLayout.closeDrawers();

            if (delayed) {
                mDelayedIntentHandler.postDelayed(new WeakRunnable(builder), INTENT_DELAY_TIME);
            } else
                builder.startActivities();
        }
    }

    @Override
    public void startActivity(Intent intent) {
        if (!intent.hasExtra(EXTRA_NAVIGATION_SECTION_IDENTIFIER) && getIntent().hasExtra(EXTRA_NAVIGATION_SECTION_IDENTIFIER)) {
            intent.putExtra(EXTRA_NAVIGATION_SECTION_IDENTIFIER, getIntent().getIntExtra(EXTRA_NAVIGATION_SECTION_IDENTIFIER, 0));
        }
        super.startActivity(intent);
    }

    @Override
    public void startActivity(Intent intent, Bundle options) {
        if (!intent.hasExtra(EXTRA_NAVIGATION_SECTION_IDENTIFIER) && getIntent().hasExtra(EXTRA_NAVIGATION_SECTION_IDENTIFIER)) {
            intent.putExtra(EXTRA_NAVIGATION_SECTION_IDENTIFIER, getIntent().getIntExtra(EXTRA_NAVIGATION_SECTION_IDENTIFIER, 0));
        }
        super.startActivity(intent, options);
    }

    public final boolean isTablet() {
        return getResources().getBoolean(R.bool.is_tablet);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (mSelectionListener != null) {
            Intent returnIntent = mSelectionListener.createIntentForNavigationItemSelected(item);
            if (returnIntent != null) {
                Intent setupIntent = getSetupIntentForOriginalIntent(getApplication(), returnIntent);
                boolean equalsIntent = false;

                Intent currentIntent = (Intent) getIntent().clone();
                //this extra is added from the facebook library, so if it isn't removed for the comparison the equalsIntent will be always false
                if (currentIntent.hasExtra("_fbSourceApplicationHasBeenSet"))
                    currentIntent.removeExtra("_fbSourceApplicationHasBeenSet");

                if (!returnIntent.hasExtra(EXTRA_NAVIGATION_SECTION_IDENTIFIER)) {
                    equalsIntent = IntentExtensionsKt.equalsToIntent(setupIntent != null ? setupIntent : returnIntent, currentIntent);
                    returnIntent.putExtra(EXTRA_NAVIGATION_SECTION_IDENTIFIER, item.getItemId());
                    if (setupIntent != null)
                        setupIntent.putExtra(EXTRA_NAVIGATION_SECTION_IDENTIFIER, item.getItemId());
                }

                equalsIntent = equalsIntent || IntentExtensionsKt.equalsToIntent(setupIntent != null ? setupIntent : returnIntent, currentIntent);
                if (!equalsIntent) {
                    if (returnIntent.getIntExtra(EXTRA_NAVIGATION_SECTION_IDENTIFIER, 0) != getIntent().getIntExtra(EXTRA_NAVIGATION_SECTION_IDENTIFIER, 0)) {
                        startActivityAndParentsIfRequired(returnIntent, true);
                    } else
                        navigateUpTo(returnIntent);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSelectionListener = null;
    }

    public int getNavigationMode() {
        return mNavigationMode;
    }

    @NotNull
    @Override
    public ResultManager provideResultManager() {
        return mResultManager;
    }

    /**
     * Runnable che accetta un'activity per richiamare il metodo {@link ThemableNavigationActivity#startActivity(Intent)} sulla stessa
     * evitando il memory leak durante il tempo di delay.
     */
    private static class WeakRunnable implements Runnable {
        private WeakReference<Activity> mActivityRef;
        private WeakReference<TaskStackBuilder> mBuilderRef;

        private Intent mIntent;

        /**
         * Utilizzato per far partire un'activity singola
         *
         * @param activity activity corrente
         * @param intent   intent da far partire
         */
        public WeakRunnable(Activity activity, Intent intent) {
            mActivityRef = new WeakReference<>(activity);
            mIntent = intent;
        }

        /**
         * Utilizzato per far partire più activity
         *
         * @param builder builder che contiene le activity
         */
        public WeakRunnable(TaskStackBuilder builder) {
            mBuilderRef = new WeakReference<>(builder);
        }

        @Override
        public void run() {
            if (mActivityRef != null) {
                final Activity activity = mActivityRef.get();
                if (activity != null) {
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    activity.startActivity(mIntent);
                }
            } else if (mBuilderRef != null) {
                final TaskStackBuilder builder = mBuilderRef.get();
                if (builder != null) {
                    builder.startActivities();
                }
            }
        }
    }
}
