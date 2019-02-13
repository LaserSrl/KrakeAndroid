package com.krake.core.login.orchard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;
import com.krake.core.R;
import com.krake.core.contacts.ContactInfo;
import com.krake.core.contacts.ContactInfoManager;
import com.krake.core.login.LoginFragment;
import com.krake.core.login.LoginManager;
import com.krake.core.login.LoginParametersModification;
import com.krake.core.login.LoginViewManager;
import com.krake.core.network.RemoteRequest;

/**
 * Created by joel on 12/10/15.
 */
public class OrchardLoginManager extends LoginViewManager implements View.OnClickListener {
    public static final String ARG_PASSWORD = "argPassword";
    public static final String ARG_MAIL_USERNAME = "argMailUsername";

    private static final int ORCHARD_LOGIN_REQUEST_CODE = 31245;
    private final View loginView;
    private final Button mLoginButton, mRecoverPwdButton, mRegisterBtn;
    private TextInputLayout mUsernameTextInput, mPasswordTextInput;
    private View dividerView;
    private LoginFragment mLoginFragment;

    @SuppressLint("InflateParams")
    public OrchardLoginManager(Activity activity, LoginListener loginFragment) {
        super(activity, loginFragment);

        if (loginFragment instanceof LoginFragment) {
            mLoginFragment = (LoginFragment) loginFragment;
        }

        loginView = getActivity().getLayoutInflater().inflate(R.layout.partial_orchard_login_layout, null);

        mLoginButton = loginView.findViewById(R.id.btn_login);
        mRecoverPwdButton = loginView.findViewById(R.id.btn_recover_password);
        mRegisterBtn = loginView.findViewById(R.id.btn_register);

        boolean enabledRegistration = getActivity().getResources().getBoolean(R.bool.enable_orchard_registration);
        boolean enabledPasswordRecovery = getActivity().getResources().getBoolean(R.bool.enable_orchard_password_recovery);

        if (activity.getApplication() instanceof LoginParametersModification) {
            enabledRegistration = ((LoginParametersModification) activity.getApplication()).getEnableRegistration();
        }
        if (!enabledRegistration) {
            mRegisterBtn.setVisibility(View.GONE);
        }
        if (!enabledPasswordRecovery) {
            mRecoverPwdButton.setVisibility(View.GONE);
        }

        mLoginButton.setOnClickListener(this);
        mRecoverPwdButton.setOnClickListener(this);
        mRegisterBtn.setOnClickListener(this);

        dividerView = loginView.findViewById(R.id.login_divider_view);
        mUsernameTextInput = loginView.findViewById(R.id.username_input_text_layout);
        mPasswordTextInput = loginView.findViewById(R.id.password_input_text_layout);
    }

    @SuppressWarnings("ConstantConditions")
    public void onViewCreated(Bundle savedInstanceState, boolean onlyView) {
        if (!onlyView)
            dividerView.setVisibility(View.VISIBLE);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(ARG_MAIL_USERNAME)) {
                mUsernameTextInput.getEditText().append(savedInstanceState.getString(ARG_MAIL_USERNAME));
            }

            if (savedInstanceState.containsKey(ARG_PASSWORD)) {
                mPasswordTextInput.getEditText().append(savedInstanceState.getString(ARG_PASSWORD));
            }
        }

        ContactInfo userInfo;
        if (mUsernameTextInput.getEditText().length() == 0 && (userInfo = ContactInfoManager.Companion.readUserInfo(getActivity())) != null && userInfo.getMail() != null) {
            mUsernameTextInput.getEditText().append(userInfo.getMail());
        }
    }

    @Override
    public View getLoginView() {
        return loginView;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_login) {
            login();
        } else if (view.getId() == R.id.btn_recover_password) {
            startLoginActivity(false);
        } else if (view.getId() == R.id.btn_register) {
            startLoginActivity(true);
        }
    }

    private void startLoginActivity(boolean register) {
        Activity activity = getActivity();
        boolean isTablet = activity.getResources().getBoolean(R.bool.is_tablet);
        Intent i = new Intent(activity, OrchardLoginActivity.class);
        Bundle b = new Bundle();
        if (mLoginFragment != null && isTablet) {
            b.putInt(OrchardLoginActivity.ARG_DIALOG_HEIGHT, mLoginFragment.getDialogHeight());
            b.putInt(OrchardLoginActivity.ARG_DIALOG_WIDTH, mLoginFragment.getDialogWidth());
        }

        if (register) {
            b.putInt(OrchardLoginActivity.ARG_FRAGMENT_TYPE, OrchardLoginActivity.FRAGMENT_REGISTRATION);
            i.putExtras(b);
            activity.startActivityForResult(i, ORCHARD_LOGIN_REQUEST_CODE);
        } else {
            b.putInt(OrchardLoginActivity.ARG_FRAGMENT_TYPE, OrchardLoginActivity.FRAGMENT_RECOVER);
            i.putExtras(b);
            activity.startActivity(i);
        }

        if (!isTablet)
            activity.overridePendingTransition(R.anim.login_slide_in_second, R.anim.login_slide_out_first);
        else
            activity.overridePendingTransition(0, 0);
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ORCHARD_LOGIN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                getLoginFragment().onManagerDidCompleteLogin(this);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void setButtonEnabled(boolean enabled) {
        mLoginButton.setEnabled(enabled);
        mRecoverPwdButton.setEnabled(enabled);
        mRegisterBtn.setEnabled(enabled);
    }

    @Override
    public boolean sendTokenToOrchard() {
        return false;
    }

    private void login() {
        EditText etUsername = mUsernameTextInput.getEditText();
        EditText etPassword = mPasswordTextInput.getEditText();
        if (etUsername != null && etPassword != null && validateUserAndPassword(etUsername, etPassword)) {
            JsonObject bodyParameters = new JsonObject();

            bodyParameters.addProperty("Username", etUsername.getText().toString());
            bodyParameters.addProperty("Password", etPassword.getText().toString());

            RemoteRequest request = new RemoteRequest(getActivity());

            request.setPath(getString(R.string.orchard_custom_login_authenticate_path))
                    .setBody(bodyParameters)
                    .setMethod(RemoteRequest.Method.POST);

            LoginManager.shared.login(getActivity(), request);

            ContactInfoManager.Companion.updateUserInfo(getActivity(), new ContactInfo(null, etUsername.getText().toString(), null));
        }

    }

    private boolean validateUserAndPassword(EditText etUsername, EditText etPassword) {
        boolean validate = true;
        if (TextUtils.isEmpty(etUsername.getText())) {
            mUsernameTextInput.setError(getString(R.string.error_insert_your_mail));
            validate = false;
        } else {
            mUsernameTextInput.setError(null);
        }

        String password = etPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordTextInput.setError(getString(R.string.error_insert_password));
            validate = false;
        } else {
            mPasswordTextInput.setError(null);
        }
        return validate;
    }


    @Override
    public void logout() {

    }

    public String getUsernameOrMail() {
        return mUsernameTextInput.getEditText().getText().toString();
    }

    public String getPassword() {
        return mPasswordTextInput.getEditText().getText().toString();
    }
}