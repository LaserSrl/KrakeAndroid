package com.krake.core.login.orchard;

import android.content.Context;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.krake.core.ClassUtils;
import com.krake.core.OrchardError;
import com.krake.core.R;
import com.krake.core.Signaler;
import com.krake.core.component.base.ComponentManager;
import com.krake.core.component.module.OrchardComponentModule;
import com.krake.core.contacts.ContactInfo;
import com.krake.core.contacts.ContactInfoManager;
import com.krake.core.login.LoginManager;
import com.krake.core.network.RemoteRequest;
import com.krake.core.network.RemoteResponse;
import com.krake.core.widget.SnackbarUtils;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;


public class OrchardRegisterFragment extends Fragment implements View.OnClickListener {
    private static final String ARG_MAIL_USERNAME = "argMailUsername";
    private static final String ARG_PASSWORD = "argPass";
    private static final String ARG_PASSWORD_CONFIRM = "argPassConfirm";
    private static final String ARG_PHONE = "argPhone";

    private OrchardLoginActivity mListener;

    private TextInputLayout mUsernameTextInput;
    private TextInputLayout mPasswordTextInput;
    private TextInputLayout mConfirmPasswordInputText;
    private TextInputLayout mTelephoneInputText;

    private ScrollView mScrollView;

    private String mTelephoneNumber;
    private String mTelephonePrefix;

    private OrchardRegisterPrivacyFragment mOrchardRegisterPrivacyFragment;

    private void sendTelephoneNumber() {
        if (mTelephoneNumber != null && mTelephonePrefix != null) {
            JsonObject parameters = new JsonObject();

            parameters.addProperty("UserPwdRecoveryPart.InternationalPrefix", mTelephonePrefix);
            parameters.addProperty("UserPwdRecoveryPart.PhoneNumber", mTelephoneNumber);

            RemoteRequest request = new RemoteRequest(getActivity())
                    .setMethod(RemoteRequest.Method.POST)
                    .setPath("api/Laser.Orchard.StartupConfig/User")
                    .setBody(parameters);

            Signaler.shared.invokeAPI(getActivity(),
                    request,
                    true,
                    null,
                    new Function2<RemoteResponse, OrchardError, Unit>() {
                        @Override
                        public Unit invoke(RemoteResponse remoteResponse, OrchardError orchardError) {
                            return null;
                        }
                    }
            );
        }

    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        mListener = (OrchardLoginActivity) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String mailOrUsername = mUsernameTextInput.getEditText().getText().toString();
        String password = mPasswordTextInput.getEditText().getText().toString();
        String passwordConfirm = mConfirmPasswordInputText.getEditText().getText().toString();
        String phone = mTelephoneInputText.getEditText().getText().toString();

        if (!TextUtils.isEmpty(mailOrUsername))
            outState.putString(ARG_MAIL_USERNAME, mailOrUsername);

        if (!TextUtils.isEmpty(password))
            outState.putString(ARG_PASSWORD, password);

        if (!TextUtils.isEmpty(passwordConfirm))
            outState.putString(ARG_PASSWORD_CONFIRM, passwordConfirm);

        if (!TextUtils.isEmpty(phone))
            outState.putString(ARG_PHONE, phone);
    }

    // package protected
    ScrollView getScrollView() {
        return mScrollView;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orchard_registration, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoginManager.shared.isLogged().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (aBoolean != null && aBoolean) {
                    sendTelephoneNumber();
                    mListener.loginCompletedSuccesfully(null);
                }
            }
        });
        LoginManager.shared.getLoginUserError().observe(this, new Observer<OrchardError>() {
            @Override
            public void onChanged(@Nullable OrchardError orchardError) {
                if (orchardError != null) {
                    SnackbarUtils.createSnackbar(mScrollView, orchardError.getUserFriendlyMessage(getActivity()), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mScrollView = (ScrollView) view;

        TextView mLoginMessageTextView = view.findViewById(R.id.loginMessageTextView);
        mLoginMessageTextView.setText(R.string.registration);

        View loginMainText = view.findViewById(R.id.loginMainText);
        View loginMainImage = view.findViewById(R.id.loginMainImage);

        boolean showLogo = getResources().getBoolean(R.bool.login_show_logo);

        if (showLogo) {
            loginMainImage.setVisibility(View.VISIBLE);
        } else {
            loginMainText.setVisibility(View.VISIBLE);
        }

        mUsernameTextInput = view.findViewById(R.id.username_input_text_layout);
        mPasswordTextInput = view.findViewById(R.id.password_input_text_layout);
        mConfirmPasswordInputText = view.findViewById(R.id.password_confirm_text_layout);
        mTelephoneInputText = view.findViewById(R.id.telephone_number_text_layout);

        view.findViewById(R.id.btn_register).setOnClickListener(this);

        if (getResources().getBoolean(R.bool.orchard_registration_enable_telephone_number)) {
            mTelephoneInputText.setVisibility(View.VISIBLE);
        }

        mOrchardRegisterPrivacyFragment = (OrchardRegisterPrivacyFragment) getFragmentManager().findFragmentById(R.id.acceptPrivacyContainer);

        if (mOrchardRegisterPrivacyFragment == null) {

            Bundle arguments = ComponentManager.createBundle()
                    .from(getActivity())
                    .with(new OrchardComponentModule()
                            .webServiceUrl(getString(R.string.orchard_base_service_url) + getString(R.string.orchard_policies_service))
                            .dataClass(ClassUtils.dataClassForName(getString(R.string.policy_text_class_name)))
                            .displayPath(getString(R.string.policy_register_key))
                            .putExtraParameter(getString(R.string.policy_type_parameter), getString(R.string.policy_register_key)))
                    .build();
//TODO: verificare
            OrchardRegisterPrivacyFragment fragment = new OrchardRegisterPrivacyFragment();
            fragment.setArguments(arguments);
            getChildFragmentManager().beginTransaction().replace(R.id.acceptPrivacyContainer, fragment).commit();

            mOrchardRegisterPrivacyFragment = fragment;
        }

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(ARG_MAIL_USERNAME))
                mUsernameTextInput.getEditText().append(savedInstanceState.getString(ARG_MAIL_USERNAME));

            if (savedInstanceState.containsKey(ARG_PASSWORD))
                mPasswordTextInput.getEditText().append(savedInstanceState.getString(ARG_PASSWORD));

            if (savedInstanceState.containsKey(ARG_PASSWORD_CONFIRM))
                mConfirmPasswordInputText.getEditText().append(savedInstanceState.getString(ARG_PASSWORD_CONFIRM));

            if (savedInstanceState.containsKey(ARG_PHONE))
                mTelephoneInputText.getEditText().append(savedInstanceState.getString(ARG_PHONE));
        }

        ContactInfo userInfo;
        if (mUsernameTextInput.getEditText().length() == 0 && (userInfo = ContactInfoManager.Companion.readUserInfo(getActivity())) != null) {
            mUsernameTextInput.getEditText().append(userInfo.getMail());

            if (getResources().getBoolean(R.bool.orchard_registration_enable_telephone_number) && userInfo.getTelephone() != null)
                mTelephoneInputText.getEditText().append(userInfo.getTelephone());
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_register) {
            register();
        }
    }

    private void register() {
        hideKeyboard();

        if (validateUserAndPassword()) {
            JsonObject bundle = new JsonObject();

            bundle.addProperty("Username", mUsernameTextInput.getEditText().getText().toString());
            bundle.addProperty("Password", mPasswordTextInput.getEditText().getText().toString());

            bundle.addProperty("Email", mUsernameTextInput.getEditText().getText().toString());
            bundle.addProperty("ConfirmPassword", mPasswordTextInput.getEditText().getText().toString());
            bundle.addProperty("PasswordAnswer", "");
            bundle.addProperty("PasswordQuestion", "");
            bundle.addProperty("Culture", getString(R.string.orchard_language));
            LongSparseArray<Boolean> privacies = mOrchardRegisterPrivacyFragment.getAcceptedPrivacies();

            JsonArray privacieIds = new JsonArray();

            for (int i = 0; i < privacies.size(); ++i) {
                Long privacyID = privacies.keyAt(i);
                JsonObject privacy = new JsonObject();
                privacy.addProperty("PolicyId", privacyID);

                privacy.addProperty("PolicyAnswer", privacies.get(privacyID));
                privacieIds.add(privacy);
            }

            bundle.add("PolicyAnswers", privacieIds);

            RemoteRequest loginRequest = new RemoteRequest(getActivity())
                    .setMethod(RemoteRequest.Method.POST)
                    .setPath(getString(R.string.orchard_custom_login_registration_path))
                    .setBody(bundle);

            LoginManager.shared.login(getActivity(),
                    loginRequest);

            String telephone = null;
            if (mTelephoneNumber != null && mTelephonePrefix != null)
                telephone = mTelephonePrefix + mTelephoneNumber;

            ContactInfoManager.Companion.updateUserInfo(getActivity(), new ContactInfo(null,
                    mUsernameTextInput.getEditText().getText().toString(),
                    telephone
            ));
        } else {
            mScrollView.smoothScrollTo(0, 0);
        }
    }

    private boolean validateUserAndPassword() {
        boolean validate = true;
        if (TextUtils.isEmpty(mUsernameTextInput.getEditText().getText())) {
            mUsernameTextInput.setError(getString(R.string.error_insert_your_mail));
            validate = false;
        } else {
            mUsernameTextInput.setError(null);
        }

        String password = mPasswordTextInput.getEditText().getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordTextInput.setError(getString(R.string.error_insert_password));
            validate = false;
        } else {
            mPasswordTextInput.setError(null);
        }

        String confirmPassword = mConfirmPasswordInputText.getEditText().getText().toString();
        if (!confirmPassword.equals(password)) {
            mConfirmPasswordInputText.setError(getString(R.string.error_password_not_match));
            validate = false;
        } else {
            mConfirmPasswordInputText.setError(null);
        }

        if (!mOrchardRegisterPrivacyFragment.allRequiredPrivaciesAccepted()) {
            SnackbarUtils.createSnackbar(mScrollView, R.string.error_accept_all_required_privacies, Snackbar.LENGTH_SHORT).show();
            validate = false;
        }

        if (getResources().getBoolean(R.bool.orchard_registration_enable_telephone_number)) {
            String originalNumber = mTelephoneInputText.getEditText().getText().toString();

            originalNumber = PhoneNumberUtils.stripSeparators(originalNumber);

            try {
                Phonenumber.PhoneNumber number = PhoneNumberUtil.getInstance().parse(originalNumber, mListener.getString(R.string.default_sim_country_code));

                mTelephonePrefix = Integer.valueOf(number.getCountryCode()).toString();
                mTelephoneNumber = Long.valueOf(number.getNationalNumber()).toString();

                mTelephoneInputText.setError(null);
            } catch (NumberParseException e) {
                mTelephoneInputText.setError(getString(R.string.error_invalid_phone_number));
                validate = false;
            }
        }

        return validate;
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mScrollView.getWindowToken(), 0);
    }
}