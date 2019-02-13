package com.krake.core.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import com.krake.core.PrivacyViewModel;
import com.krake.core.R;
import com.krake.core.component.base.ComponentManager;
import com.krake.core.component.module.DetailComponentModule;
import com.krake.core.component.module.LoginComponentModule;
import com.krake.core.component.module.OrchardComponentModule;
import com.krake.core.component.module.ThemableComponentModule;
import com.krake.core.data.DataConnectionModel;
import com.krake.core.data.DataModel;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

public class OpenWebContentFragment extends DialogFragment implements View.OnClickListener, Observer<DataModel> {

    private static final String ARG_URL = "url";

    private String mUrl;
    private String mDisplayPath;

    private DataConnectionModel dataConnection;

    private boolean mOpenDialogAsSoonAsloadingCompleted;

    private Button mCancelButton;
    private Button mOpenButton;
    private ProgressBar mProgress;

    public OpenWebContentFragment() {
        // Required empty public constructor
    }

    public static OpenWebContentFragment newInstance(String url) {
        OpenWebContentFragment openWebContentFragment = new OpenWebContentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        openWebContentFragment.setArguments(args);
        return openWebContentFragment;
    }

    private void openDetails() {
        Intent intent = null;
        final Activity activity = getActivity();

        if (dataConnection != null && dataConnection.getModel().getValue() != null) {

            Class mResultClass = dataConnection.getModel().getValue().getListData().get(0).getClass();
            if (mDisplayPath != null && ((KrakeApplication) activity.getApplication()).isDataClassMappedForDetails(mResultClass)) {
                intent = ComponentManager.createIntent()
                        .from(activity)
                        .to(ContentItemDetailActivity.class)
                        .with(new ThemableComponentModule()
                                        .upIntent(activity.getIntent()),
                                new DetailComponentModule(activity),
                                new OrchardComponentModule()
                                        .dataClass(mResultClass)
                                        .displayPath(mDisplayPath))
                        .build();
            }
        }

        if (intent == null)
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));

        activity.startActivity(intent);
        dismiss();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUrl = getArguments().getString(ARG_URL);

        if (mUrl.indexOf(getString(R.string.orchard_base_service_url)) == 0 && !mUrl.contains("/Media/")) {
            mDisplayPath = mUrl.substring(getString(R.string.orchard_base_service_url).length());

            dataConnection = ViewModelProviders.of(this).get(mDisplayPath, DataConnectionModel.class);

            dataConnection.configure(new OrchardComponentModule()
                            .displayPath(mDisplayPath),
                    new LoginComponentModule().loginRequired(false),
                    ViewModelProviders.of(getActivity()).get(PrivacyViewModel.class));

            dataConnection.getModel().observe(this, this);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.setTitle(getString(R.string.open_content));

        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_open_web_content, container, false);

        mCancelButton = view.findViewById(R.id.cancelDialogButton);
        mCancelButton.setOnClickListener(this);
        mOpenButton = view.findViewById(R.id.confirmDialogButton);
        mOpenButton.setOnClickListener(this);

        mProgress = view.findViewById(android.R.id.progress);

        TextView messageText = view.findViewById(R.id.messageTextView);

        if (mDisplayPath != null)
            messageText.setText(getString(R.string.open_in_app_content));

        if (!mUrl.contains("tel:") && !mUrl.contains("mail:") && !mUrl.contains("mailto:")) {
            try {
                messageText.setText(String.format(getString(R.string.open_website), new URL(mUrl).getHost()));
            } catch (MalformedURLException e) {
                messageText.setText(String.format(getString(R.string.open_website), mUrl));
            }
        } else if (mUrl.indexOf("tel:") == 0) {
            try {
                messageText.setText(String.format(getString(R.string.call_number), URLDecoder.decode(mUrl.substring(4), "UTF-8")));
            } catch (Exception e) {
                messageText.setText(String.format(getString(R.string.call_number), ""));
            }
        } else if (mUrl.indexOf("mail:") == 0 || mUrl.indexOf("mailto:") == 0) {
            try {
                int index = (mUrl.contains("mail:")) ? 5 : 7;
                messageText.setText(String.format(getString(R.string.send_mail_with_address), URLDecoder.decode(mUrl.substring(index), "UTF-8")));
            } catch (Exception e) {
                messageText.setText(getString(R.string.send_mail));
            }
        }

        return view;
    }

    @Override
    public void onClick(View view) {
        if (view == mCancelButton) {
            dismiss();
        } else {
            if (mDisplayPath != null && dataConnection.getModel().getValue() == null) {
                mOpenDialogAsSoonAsloadingCompleted = true;
                mProgress.setVisibility(View.VISIBLE);
                mOpenButton.setEnabled(false);
            } else
                openDetails();
        }
    }

    @Override
    public void onChanged(@Nullable DataModel dataModel) {

        if (dataModel != null) {
            if (mOpenDialogAsSoonAsloadingCompleted)
                openDetails();
        }

    }
}