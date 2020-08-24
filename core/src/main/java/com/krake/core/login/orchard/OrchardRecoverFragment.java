package com.krake.core.login.orchard;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.krake.core.OrchardError;
import com.krake.core.R;
import com.krake.core.Signaler;
import com.krake.core.network.RemoteRequest;
import com.krake.core.network.RemoteResponse;
import com.krake.core.widget.EditTextExtensionTextInputLayoutKt;
import com.krake.core.widget.SnackbarUtils;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

/**
 * A simple {@link Fragment} subclass.
 */
public class OrchardRecoverFragment extends Fragment
        implements View.OnClickListener, Handler.Callback {
    private static final String ARG_MAIL_OR_SMS = "argMailOrSms";
    private static final int CLOSE_VIEW_MESSAGE = 378389;

    private EditText mMailOrPhoneText;
    private Button mMailButton;
    private Button mSMSButton;
    private View mView;

    private OrchardLoginActivity mListener;

    private Handler mHandler = new Handler(this);

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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String mailOrPhone = mMailOrPhoneText.getText().toString();

        if(!TextUtils.isEmpty(mailOrPhone))
            outState.putString(ARG_MAIL_OR_SMS, mailOrPhone);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_orchard_recover, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView mLoginMessageTextView = view.findViewById(R.id.loginMessageTextView);
        mLoginMessageTextView.setText(R.string.recover_password_label);

        View loginMainText = view.findViewById(R.id.loginMainText);
        View loginMainImage = view.findViewById(R.id.loginMainImage);

        boolean showLogo = getResources().getBoolean(R.bool.login_show_logo);

        if (showLogo) {
            loginMainImage.setVisibility(View.VISIBLE);
        } else {
            loginMainText.setVisibility(View.VISIBLE);
        }

        mView = view;
        mMailOrPhoneText = view.findViewById(R.id.mail_or_phone_edit_text);
        mMailButton = view.findViewById(R.id.recoverByMailButton);
        mSMSButton = view.findViewById(R.id.recoverBySMSButton);

        mMailButton.setOnClickListener(this);
        mSMSButton.setOnClickListener(this);

        String hintText;
        if (getResources().getBoolean(R.bool.orchard_registration_enable_telephone_number)) {
            mSMSButton.setVisibility(View.VISIBLE);
            hintText = getString(R.string.mail_or_phone);
        } else {
            hintText = getString(R.string.recover_insert_mail);
        }

        TextInputLayout mailOrPhoneLayout = view.findViewById(R.id.mailOrPhoneInputLayout);
        mailOrPhoneLayout.setHint(hintText);

        if(savedInstanceState != null && savedInstanceState.containsKey(ARG_MAIL_OR_SMS))
            mMailOrPhoneText.append(savedInstanceState.getString(ARG_MAIL_OR_SMS));
    }

    @Override
    public void onClick(View view) {
        if (view == mMailButton) {
            if (!mMailOrPhoneText.getText().toString().matches(getString(R.string.mail_regex_validation))) {
                EditTextExtensionTextInputLayoutKt.setErrorInThisOrInputLayout(mMailOrPhoneText, getString(R.string.error_invalid_mail));
            } else {
                hideKeyboard();
                JsonObject parameters = new JsonObject();
                parameters.addProperty("username", mMailOrPhoneText.getText().toString());

                RemoteRequest request = new RemoteRequest(getContext())
                        .setMethod(RemoteRequest.Method.POST)
                        .setPath(getString(R.string.orchard_api_recover_password_by_mail))
                        .setBody(parameters);

                Signaler.shared.invokeAPI(getActivity(),
                        request,
                        true,
                        null,
                        new Function2<RemoteResponse, OrchardError, Unit>() {
                            @Override
                            public Unit invoke(RemoteResponse remoteResponse, OrchardError orchardError) {
                                handleResponse(remoteResponse, orchardError, false);
                                return null;
                            }
                        });
            }
        } else if (view == mSMSButton) {
            hideKeyboard();
            String originalNumber = mMailOrPhoneText.getText().toString();

            originalNumber = PhoneNumberUtils.stripSeparators(originalNumber);

            try {
                Phonenumber.PhoneNumber number = PhoneNumberUtil.getInstance().parse(originalNumber, getString(R.string.default_sim_country_code));

                JsonObject phone = new JsonObject();

                phone.addProperty("internationalPrefix", Integer.valueOf(number.getCountryCode()).toString());
                phone.addProperty("phoneNumber", Long.valueOf(number.getNationalNumber()).toString());

                JsonObject parameters = new JsonObject();

                parameters.add("phoneNumber", phone);

                RemoteRequest request = new RemoteRequest(getContext())
                        .setMethod(RemoteRequest.Method.POST)
                        .setPath(getString(R.string.orchard_api_recover_password_by_phone))
                        .setBody(parameters);

                Signaler.shared.invokeAPI(getActivity(),
                        request,
                        true,
                        null,
                        new Function2<RemoteResponse, OrchardError, Unit>() {
                            @Override
                            public Unit invoke(RemoteResponse remoteResponse, OrchardError orchardError) {
                                handleResponse(remoteResponse, orchardError, true);
                                return null;
                            }
                        });

            } catch (NumberParseException e) {
                e.printStackTrace();
                EditTextExtensionTextInputLayoutKt.setErrorInThisOrInputLayout(mMailOrPhoneText, getString(R.string.error_invalid_phone_number));
            }
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        if (message.what == CLOSE_VIEW_MESSAGE) {
            if(mListener != null)
                mListener.finish();
        }

        return false;
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mView.getWindowToken(), 0);
    }

    private void handleResponse(RemoteResponse remoteResponse, OrchardError orchardError, boolean sms) {
        if (getActivity() == null)
            return;

        if (remoteResponse != null) {
            int text = sms ? R.string.password_reset_sent_sms : R.string.password_reset_sent_mail;
            SnackbarUtils.showCloseSnackbar(mView, text, mHandler, CLOSE_VIEW_MESSAGE);
        } else {
            SnackbarUtils.createSnackbar(mView, orchardError.getUserFriendlyMessage(getActivity()), Snackbar.LENGTH_SHORT).show();
        }
    }
}