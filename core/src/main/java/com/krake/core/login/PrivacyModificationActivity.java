package com.krake.core.login;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.krake.core.ClassUtils;
import com.krake.core.Constants;
import com.krake.core.OrchardError;
import com.krake.core.PrivacyStatus;
import com.krake.core.PrivacyViewModel;
import com.krake.core.R;
import com.krake.core.app.LoginAndPrivacyActivity;
import com.krake.core.component.base.ComponentManager;
import com.krake.core.component.module.OrchardComponentModule;
import com.krake.core.login.orchard.OrchardRegisterPrivacyFragment;
import com.krake.core.model.PolicyText;
import com.krake.core.network.RemoteClient;
import com.krake.core.network.RemoteResponse;
import com.krake.core.util.SerializableLongSparseArray;
import com.krake.core.widget.SnackbarUtils;

import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class PrivacyModificationActivity extends LoginAndPrivacyActivity {
    private OrchardRegisterPrivacyFragment registerFragment;
    private SerializableLongSparseArray<Boolean> acceptedPrivacies = new SerializableLongSparseArray<>();
    private Handler mHandler = new Handler();
    @Override
    public void onCreate(Bundle savedInstanceState, int layout) {
        super.onCreate(savedInstanceState, layout);
        inflateMainView(R.layout.activity_privacy_modification, true);

        String pCookies = RemoteClient.Companion.client(RemoteClient.Mode.LOGGED).cookieValue(this, Constants.COOKIE_PRIVACY_ANSWERS);

        if (!TextUtils.isEmpty(pCookies)) {
            String decoded = new String(Base64.decode(pCookies, 0));

            JsonArray privacies = new JsonParser().parse(decoded).getAsJsonArray();

            acceptedPrivacies.clear();

            for (int index = 0; index < privacies.size(); ++index) {
                JsonObject element = privacies.get(index).getAsJsonObject();

                JsonPrimitive identifier = element.getAsJsonPrimitive("PolicyTextId");
                JsonPrimitive accepted = element.getAsJsonPrimitive("Accepted");
                acceptedPrivacies.put(identifier.getAsLong(), accepted.getAsBoolean());
            }
            if (registerFragment != null) {
                registerFragment.updatePrivacies(acceptedPrivacies);
            }
        }
    }

    @Override
    public void changeContentVisibility(boolean visible) {
        if (visible && (registerFragment = (OrchardRegisterPrivacyFragment) getSupportFragmentManager().findFragmentById(R.id.activity_layout_coordinator)) == null) {
            Bundle acceptedPrivaciesBundle = new Bundle();
            acceptedPrivaciesBundle.putSerializable(OrchardRegisterPrivacyFragment.ARG_ACCEPTED_PRIVACIES, acceptedPrivacies);

            Bundle fullBundle = ComponentManager.createBundle()
                    .from(this)
                    .with(new OrchardComponentModule()
                            .webServiceUrl(getString(R.string.orchard_base_service_url) + getString(R.string.orchard_policies_service))
                            .dataClass(ClassUtils.dataClassForName(getString(R.string.policy_text_class_name)))
                            .displayPath(getString(R.string.orchard_all_policies_path))
                            .putExtraParameter(getString(R.string.policy_type_parameter), getString(R.string.policy_all_key)))
                    .put(acceptedPrivaciesBundle)
                    .build();

            registerFragment = new OrchardRegisterPrivacyFragment();
            registerFragment.setArguments(fullBundle);

            getSupportFragmentManager().beginTransaction().replace(R.id.acceptPrivacyContainer, registerFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.send_content_menu, menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (item.getItemId() == R.id.action_send_content) {
            if (allRequiredPrivaciesAccepted()) {
                ViewModelProviders.of(this).get(PrivacyViewModel.class)
                        .acceptPrivacies(this,
                                registerFragment.getAcceptedPrivacies()
                                , new Function2<RemoteResponse, OrchardError, Unit>() {
                                    @Override
                                    public Unit invoke(RemoteResponse remoteResponse, OrchardError orchardError) {
                                        if (remoteResponse != null)
                                            SnackbarUtils.showCloseSnackbar(mCoordinator,
                                                    R.string.privacy_updated,
                                                    mHandler, new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            finish();
                                                        }
                                                    });
                                        return null;
                                    }
                                });
            } else {
                SnackbarUtils.createSnackbar(mCoordinator, R.string.error_accept_all_required_privacies, Snackbar.LENGTH_SHORT).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Verifica che tutte le privacy obbligatorie siano state accettate.
     *
     * @return true se tutte le privacy obbligatorie sono accettate, false altrimenti
     */
    public boolean allRequiredPrivaciesAccepted() {
        List<PolicyText> mPrivacies = registerFragment.getPrivacies();
        android.support.v4.util.LongSparseArray<Boolean> mAcceptedPrivacies = registerFragment.getAcceptedPrivacies();
        if (mPrivacies != null) {
            for (int i = 0; i < mPrivacies.size(); ++i) {
                PolicyText privacy = mPrivacies.get(i);

                if (privacy.getPolicyTextInfoPartUserHaveToAccept()) {

                    if (!mAcceptedPrivacies.get(privacy.getIdentifier(), false)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void onChanged(@Nullable PrivacyStatus privacyStatus) {
        super.onChanged(privacyStatus);
    }
}