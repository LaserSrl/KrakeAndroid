package com.krake.core.login;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.krake.core.PrivacyViewModel;
import com.krake.core.R;
import com.krake.core.model.PolicyText;
import com.krake.core.widget.SnackbarUtils;

import java.util.List;

/**
 * Fragment che mostra e permette di accettare le privacies che proteggono un contenuto.
 * Sara' possibile procede ed inviare le privacy ad Orchard solo dopo che l'utente ha accettato tutte le privacy obbligatorie.
 * Non viene fatto nessun check sulle privacy opzionali.
 * I contenuti caricati dal fragment devono implementare l'interfaccia {@link PolicyText}
 */
public class AcceptPrivacyFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private ViewGroup mPrivacyContainer;
    private ProgressBar mProgress;

    private LongSparseArray<Boolean> mAcceptedPrivacies = new LongSparseArray<>();

    private List<PolicyText> mPrivacies;

    private View mSaveButton;
    private View mCancelButton;
    private PrivacyViewModel privacyViewModel;

    public AcceptPrivacyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        boolean embebbed = getParentFragment() != null;

        int layout = !embebbed ? R.layout.fragment_privacy_accept_list : R.layout.fragment_privacy_embedded_accept_list;
        View view = inflater.inflate(layout, container, false);

        mPrivacyContainer = view.findViewById(R.id.privacyContainer);

        mProgress = view.findViewById(android.R.id.progress);

        if (!embebbed) {
            mSaveButton = view.findViewById(R.id.saveButton);
            mSaveButton.setOnClickListener(this);

            mCancelButton = view.findViewById(R.id.cancelButton);
            mCancelButton.setOnClickListener(this);
        }

        privacyViewModel = ViewModelProviders.of(getActivity()).get(PrivacyViewModel.class);
        loadPrivacies(privacyViewModel.getPrivacyStatus().getValue().getPrivacyException().getPrivacyTexts());
        return view;
    }

    private void loadPrivacies(List<PolicyText> lazyList) {
        mPrivacies = lazyList;

        for (PolicyText privacyText : mPrivacies) {
            mAcceptedPrivacies.put(privacyText.getIdentifier(), false);
        }

        if (mProgress != null)
            mProgress.setVisibility(View.GONE);

        mPrivacyContainer.removeAllViews();

        LayoutInflater inflater = getActivity().getLayoutInflater();

        for (PolicyText privacy : mPrivacies) {
            View itemView = inflater.inflate(R.layout.privacy_cell, mPrivacyContainer, false);

            TextView mPrivacyTextView = itemView.findViewById(R.id.privacyTitleText);
            SwitchCompat mPrivacySwitch = itemView.findViewById(R.id.privacyAcceptSwitch);
            WebView mPrivacyWebview = itemView.findViewById(R.id.privacyTextWebView);

            mPrivacyTextView.setText(privacy.getTitlePartTitle());

            mPrivacyWebview.loadDataWithBaseURL(getResources().getString(R.string.orchard_base_service_url), privacy.getBodyPartText(), "text/html", "UTF-8", null);

            mPrivacySwitch.setTag(privacy.getIdentifier());

            mPrivacySwitch.setChecked(mAcceptedPrivacies.get(privacy.getIdentifier()));

            String textButton = getString(R.string.privacy_accept);

            if (privacy.getPolicyTextInfoPartUserHaveToAccept()) {
                textButton = textButton + " *" + getString(R.string.required);
            }

            mPrivacySwitch.setText(textButton);

            mPrivacySwitch.setOnCheckedChangeListener(this);


            mPrivacyContainer.addView(itemView);
        }
    }

    /**
     * Verifica che tutte le privacy obbligatorie siano state accettate.
     *
     * @return true se tutte le privacy obbligatorie sono accettate, false altrimenti
     */
    public boolean allRequiredPrivaciesAccepted() {
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
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        Long identifier = (Long) compoundButton.getTag();
        mAcceptedPrivacies.put(identifier, b);
    }

    @Override
    public void onClick(View view) {
        if (view == mCancelButton) {
            privacyViewModel.cancelPrivacyAcceptance();
        } else if (view == mSaveButton) {
            if (allRequiredPrivaciesAccepted()) {
                privacyViewModel.acceptPrivacies(getActivity(), mAcceptedPrivacies);
            } else {
                SnackbarUtils.createSnackbar(((View) mPrivacyContainer.getParent()), R.string.error_accept_all_required_privacies, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Indicazione delle stato delle privacy.
     * La Map contiene come chiave l'identificativo della privacy, come valore un booleano se è stata accettata.
     *
     * @return
     */
    public LongSparseArray<Boolean> getAcceptedPrivacies() {
        return mAcceptedPrivacies;
    }
}
