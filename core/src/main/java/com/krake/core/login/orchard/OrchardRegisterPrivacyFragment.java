package com.krake.core.login.orchard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.SwitchCompat;
import android.text.Html;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.krake.core.OrchardError;
import com.krake.core.R;
import com.krake.core.app.OrchardDataModelFragment;
import com.krake.core.data.DataModel;
import com.krake.core.model.PolicyText;
import com.krake.core.util.LayoutUtils;
import com.krake.core.util.SerializableLongSparseArray;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OrchardRegisterPrivacyFragment extends OrchardDataModelFragment implements CompoundButton.OnCheckedChangeListener {
    private static final String STATE_ACCEPTED_PRIVACIES = "argAcceptedPriv";
    public static final String ARG_ACCEPTED_PRIVACIES = STATE_ACCEPTED_PRIVACIES;
    private static final String STATE_STATES_EXPANDED = "argStExpanded";

    private long[] expandedPrivacies;
    private ViewGroup mPrivacyContainer;
    private ProgressBar mProgress;
    private View mView;

    private SerializableLongSparseArray<Boolean> mAcceptedPrivacies;

    private List<PolicyText> mPrivacies;

    public OrchardRegisterPrivacyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_privacy_embedded_accept_list, container, false);

        mPrivacyContainer = mView.findViewById(R.id.privacyContainer);
        mProgress = mView.findViewById(android.R.id.progress);

        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_STATES_EXPANDED)) {
            expandedPrivacies = savedInstanceState.getLongArray(STATE_STATES_EXPANDED);
            mAcceptedPrivacies = (SerializableLongSparseArray<Boolean>) savedInstanceState.getSerializable(STATE_ACCEPTED_PRIVACIES);
        } else {
            if (!getArguments().containsKey(ARG_ACCEPTED_PRIVACIES))
                mAcceptedPrivacies = new SerializableLongSparseArray<>();
            else
                mAcceptedPrivacies = (SerializableLongSparseArray<Boolean>) getArguments().getSerializable(ARG_ACCEPTED_PRIVACIES);
        }

        return mView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!(getParentFragment() instanceof OrchardRegisterFragment)) {
            LayoutUtils.attachScrollingBehavior(view);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (expandedPrivacies != null && expandedPrivacies.length > 0) {
            outState.putLongArray(STATE_STATES_EXPANDED, expandedPrivacies);
        }

        if (mAcceptedPrivacies != null && mAcceptedPrivacies.size() > 0) {
            outState.putSerializable(STATE_ACCEPTED_PRIVACIES, mAcceptedPrivacies);
        }
    }


    public void updatePrivacies(SerializableLongSparseArray<Boolean> acceptedPrivacies) {
        if (mPrivacies != null && isAdded()) {
            mAcceptedPrivacies = acceptedPrivacies;

            mPrivacyContainer.removeAllViews();

            LayoutInflater inflater = getActivity().getLayoutInflater();

            final long[] tempExpanded = new long[mPrivacies.size()];
            boolean embebbed = getParentFragment() != null;

            for (int i = 0; i < mPrivacies.size(); i++) {
                final PolicyText privacy = mPrivacies.get(i);
                View itemView = inflater.inflate(embebbed ? R.layout.register_privacy_cell : R.layout.register_privacy_cell_non_embed, mPrivacyContainer, false);
                itemView.setTag(privacy.getIdentifier());

                final TextView mPrivacyTitleText = itemView.findViewById(R.id.privacyTitleText);
                final SwitchCompat mPrivacySwitch = itemView.findViewById(R.id.privacyAcceptSwitch);
                final TextView mPrivacyBodyText = itemView.findViewById(R.id.privacyBodyText);
                final ImageView mArrow = itemView.findViewById(R.id.iv_arrow);

                mPrivacyTitleText.setText(privacy.getTitlePartTitle());
                mPrivacyBodyText.setText(Html.fromHtml(privacy.getBodyPartText()));

                boolean isPrivacyAccepted = mAcceptedPrivacies.get(privacy.getIdentifier(), false);

                mPrivacySwitch.setTag(privacy.getIdentifier());
                mPrivacySwitch.setChecked(isPrivacyAccepted);

                String textButton = getString(R.string.privacy_accept);

                if (privacy.getPolicyTextInfoPartUserHaveToAccept()) {
                    textButton = textButton + " *" + getString(R.string.required);
                    mPrivacySwitch.setEnabled(!isPrivacyAccepted);
                }

                mPrivacySwitch.setText(textButton);
                mPrivacySwitch.setOnCheckedChangeListener(this);

                final TextPaint textPaint = mPrivacyBodyText.getPaint();
                final ViewGroup mContainer = ((ViewGroup) mPrivacyTitleText.getParent());
                final int finalI = i;
                mContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        int boundedWidth = mContainer.getWidth();
                        final StaticLayout layout = new StaticLayout(Html.fromHtml(privacy.getBodyPartText()), textPaint, boundedWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                        final int height = layout.getHeight();

                        final AnimatorSet animationSet = initializeAnimationSet();

                        ((ViewGroup) mPrivacyTitleText.getParent()).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mPrivacyBodyText.getVisibility() == View.GONE) {
                                    mArrow.setImageResource(R.drawable.ic_arrow_drop_up_24dp);

                                    startExpandSet(animationSet, mPrivacyBodyText, height, finalI, privacy.getIdentifier(), tempExpanded);
                                } else {
                                    mArrow.setImageResource(R.drawable.ic_arrow_drop_down_24dp);

                                    startCollapseSet(animationSet, mPrivacyBodyText, height, finalI, privacy.getIdentifier(), tempExpanded);
                                }
                            }
                        });
                    }
                });

                if (expandedPrivacies != null && expandedPrivacies.length == tempExpanded.length) {
                    for (int j = 0; j < expandedPrivacies.length; j++) {
                        long expandedPrivacy = expandedPrivacies[j];
                        if (expandedPrivacy == privacy.getIdentifier()) {
                            mPrivacyBodyText.setVisibility(View.VISIBLE);
                            tempExpanded[j] = expandedPrivacies[j];
                        }
                    }
                }

                mPrivacyContainer.addView(itemView);
            }

            expandedPrivacies = tempExpanded;
        }
    }

    private AnimatorSet initializeAnimationSet() {
        AnimatorSet expandSet = new AnimatorSet();
        expandSet.setDuration(1000);
        expandSet.setInterpolator(new AccelerateDecelerateInterpolator());

        return expandSet;
    }

    private void startExpandSet(final AnimatorSet set,
                                final View mPrivacyBodyText,
                                int height,
                                final int position,
                                final Long privacyId,
                                final long[] tempExpanded) {

        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mPrivacyBodyText.getLayoutParams();
                layoutParams.height = 0;
                mPrivacyBodyText.setLayoutParams(layoutParams);
                mPrivacyBodyText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                set.removeListener(this);
                tempExpanded[position] = privacyId;
            }
        });
        ValueAnimator parentScorll = scrollExpandAnimator(privacyId);
        if (parentScorll != null)
            set.playTogether(expandPrivacyAnimator(mPrivacyBodyText, height), parentScorll);
        else
            set.play(expandPrivacyAnimator(mPrivacyBodyText, height));

        set.start();
    }

    private void startCollapseSet(final AnimatorSet set,
                                  final View mPrivacyBodyText,
                                  int height,
                                  final int position,
                                  final Long privacyId,
                                  final long[] tempExpanded) {

        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                set.removeListener(this);
                tempExpanded[position] = 0;
                mPrivacyBodyText.setVisibility(View.GONE);
            }
        });
        ValueAnimator animator = scrollCollapseAnimator(privacyId);
        if (animator != null)
            set.playTogether(collapsePrivacyAnimator(mPrivacyBodyText, height), animator);
        else
            set.play(collapsePrivacyAnimator(mPrivacyBodyText, height));
        set.start();
    }

    private ValueAnimator expandPrivacyAnimator(final View mPrivacyBodyText, int height) {
        ValueAnimator expandAnimator = ValueAnimator.ofInt(0, height);
        expandAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = mPrivacyBodyText.getLayoutParams();
                layoutParams.height = value;
                mPrivacyBodyText.setLayoutParams(layoutParams);
            }
        });
        return expandAnimator;
    }

    private ValueAnimator collapsePrivacyAnimator(final View mPrivacyBodyText, int height) {
        ValueAnimator collapseAnimator = ValueAnimator.ofInt(height, 0);
        collapseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = mPrivacyBodyText.getLayoutParams();
                layoutParams.height = value;
                mPrivacyBodyText.setLayoutParams(layoutParams);
            }
        });
        return collapseAnimator;
    }

    private ValueAnimator scrollExpandAnimator(long privacyId) {
        if (getParentFragment() instanceof OrchardRegisterFragment) {
            OrchardRegisterFragment parentFragment = (OrchardRegisterFragment) getParentFragment();
            final ScrollView parentScrollView = parentFragment.getScrollView();
            ValueAnimator scrollAnimator = ValueAnimator.ofInt(parentScrollView.getScrollY(),
                    (int) (parentScrollView.findViewById(R.id.acceptPrivacyContainer).getY() +
                            mView.findViewWithTag(privacyId).getY()));

            scrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int scrollTo = (Integer) animation.getAnimatedValue();
                    parentScrollView.scrollTo(0, scrollTo);
                }
            });
            return scrollAnimator;
        }
        return null;
    }

    private ValueAnimator scrollCollapseAnimator(long privacyId) {
        if (getParentFragment() instanceof OrchardRegisterFragment) {
            OrchardRegisterFragment parentFragment = (OrchardRegisterFragment) getParentFragment();
            final ScrollView parentScrollView = parentFragment.getScrollView();
            ValueAnimator scrollAnimator = ValueAnimator.ofInt(parentScrollView.getScrollY(),
                    (int) mView.findViewWithTag(privacyId).getY());

            scrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int scrollTo = (Integer) animation.getAnimatedValue();
                    parentScrollView.scrollTo(0, scrollTo);
                }
            });
            return scrollAnimator;
        }
        return null;
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

    /**
     * Indicazione delle stato delle privacy.
     * La Map contiene come chiave l'identificativo della privacy, come valore un booleano se è stata accettata.
     *
     * @return
     */
    public LongSparseArray<Boolean> getAcceptedPrivacies() {
        return mAcceptedPrivacies;
    }

    public List<PolicyText> getPrivacies() {
        return mPrivacies;
    }

    @Override
    public void onDataModelChanged(@org.jetbrains.annotations.Nullable DataModel dataModel) {
        if (mPrivacies == null && dataModel.getCacheValid()) {


            mPrivacies = new ArrayList<>(dataModel.getListData().size());

            for (int i = 0; i < dataModel.getListData().size(); ++i) {
                mPrivacies.add((PolicyText) dataModel.getListData().get(i));
            }

            if (mAcceptedPrivacies.size() == 0) {
                for (PolicyText privacyText : mPrivacies) {
                    mAcceptedPrivacies.put(privacyText.getIdentifier(), false);
                }
            }

            updatePrivacies(mAcceptedPrivacies);

            if (mProgress != null)
                mProgress.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDataLoadingError(@NotNull OrchardError orchardError) {

    }
}