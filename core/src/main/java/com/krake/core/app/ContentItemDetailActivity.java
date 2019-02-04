package com.krake.core.app;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.transition.Fade;
import android.transition.Transition;
import android.util.Log;
import android.util.TypedValue;
import android.view.Window;

import com.krake.core.OrchardError;
import com.krake.core.R;
import com.krake.core.cache.CacheManager;
import com.krake.core.component.annotation.BundleResolvable;
import com.krake.core.component.base.ComponentManager;
import com.krake.core.component.module.MediaComponentModule;
import com.krake.core.component.module.OrchardComponentModule;
import com.krake.core.data.DataConnectionModel;
import com.krake.core.data.DataModel;
import com.krake.core.media.MediaPartFullscreenActivity;
import com.krake.core.media.MediaSelectionListener;
import com.krake.core.model.ContentItem;
import com.krake.core.model.MediaPart;
import com.krake.core.widget.SnackbarUtils;

import java.util.List;

import javax.annotation.Nonnull;

import io.realm.RealmModel;

/**
 * Activity che mostra i contenuti di un oggetto per orchard deve implementare l'interfaccia
 * {@link ContentItem} o sue estensioni.
 * L'activity instanzia autonomamente il fragment per mostare i dati.
 * Per ottenere la classe corretta per mostrare l'oggetto della classe viene interrogata la classe {@link KrakeApplication OrchardApplication}
 * ottenuto con metodo {@link #getApplication() getApplication}, in particolare viene richiamato il metodo {@link KrakeApplication#instantiateDetailFragmentForClass(Class, Bundle) instantiateDetailFragmentForClass}
 * cui viene passata la classe da mostrare
 *
 * @see com.krake.core.model.ContentItem
 * @see com.krake.core.model.ContentItemWithLocation
 * @see KrakeApplication
 */
public class ContentItemDetailActivity extends LoginAndPrivacyActivity
        implements MediaSelectionListener,
        DetailFragmentCloseInterface,
        SwipeRefreshLayout.OnRefreshListener {

    @BundleResolvable
    public OrchardComponentModule orchardComponentModule;
    private Fragment mDetailsFragment;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState, int layout) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window contextWindow = getWindow();
            contextWindow.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

            TypedValue value = new TypedValue();
            String transitionClass = null;
            if (getTheme().resolveAttribute(R.attr.listMapTransition, value, true)) {
                transitionClass = value.string.toString();
            }

            Transition exitTransition = null;
            if (!TextUtils.isEmpty(transitionClass)) {
                try {
                    Object o = Class.forName(transitionClass).getConstructor().newInstance();
                    if (o instanceof Transition)
                        exitTransition = (Transition) o;
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "check the attribute listMapTransition in your BaseTheme");
                }
            } else {
                exitTransition = new Fade();
            }
            contextWindow.setExitTransition(exitTransition);
        }
        super.onCreate(savedInstanceState, layout);

        mDetailsFragment = getSupportFragmentManager().findFragmentById(R.id.activity_layout_coordinator);

        if (savedInstanceState == null) {
            setTitle(" ");
            Class detailClass = orchardComponentModule.getDataClass();
            if (!((KrakeApplication) getApplication()).isDataClassMappedForDetails(detailClass))
                finish();
        }

        final DataConnectionModel model = ViewModelProviders.of(this)
                .get(CacheManager.Companion.getShared()
                        .getModelKey(orchardComponentModule), DataConnectionModel.class);

        model.getDataError().observe(this, new Observer<OrchardError>() {
            @Override
            public void onChanged(@Nullable OrchardError orchardError) {
                if (orchardError != null) {
                    onDataLoadFailed(orchardError, model.getModel().getValue());
                }
            }
        });
    }

    @Override
    public void changeContentVisibility(boolean visible) {
        if (visible) {
            if (mDetailsFragment == null) {
                Class detailClass = orchardComponentModule.getDataClass();
                Fragment fragment = ((KrakeApplication) getApplication()).instantiateDetailFragmentForClass(detailClass, getIntent().getExtras());

                if (fragment != null && !isDestroyed()) {
                    mDetailsFragment = fragment;
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.activity_layout_coordinator, fragment)
                            .commit();
                    getSupportFragmentManager().executePendingTransactions();
                }
            }
        }
    }

    @Override
    public void onMediaPartSelected(List<MediaPart> medias, MediaPart mediaPart) {
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

    @Override
    public void onBackPressed() {
        if (!(mDetailsFragment instanceof BackPressHandler) || !((BackPressHandler) mDetailsFragment).onBackPressed())
            super.onBackPressed();
    }

    @Override
    public void onCloseDetails(Fragment sender) {
        finish();
    }

    public Fragment getDetailFragment() {
        return mDetailsFragment;
    }

    public void onDataLoadFailed(@Nonnull OrchardError error,
                                 @Nullable DataModel dataModel) {
        if (dataModel == null && error.getReactionCode() == 0) {
            SnackbarUtils.showCloseSnackbar(findViewById(R.id.activity_layout_coordinator),
                    R.string.data_loading_failed, mHandler, new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
        }
    }

    @Override
    public void onRefresh() {
        if (mDetailsFragment != null && mDetailsFragment instanceof OrchardDataModelFragment) {
            ((OrchardDataModelFragment) mDetailsFragment).getDataConnectionModel().restartDataLoading();
        }
    }
}