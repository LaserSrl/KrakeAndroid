package com.krake.usercontent;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import com.krake.core.app.ContentItemDetailModelFragment;
import com.krake.core.model.ContentItem;
import com.krake.usercontent.model.UserCreatedContent;


/**
 * A simple Fragment subclass.
 */
public class UserContentDetailFragment extends ContentItemDetailModelFragment {


    private TextView mStatusTextView;
    private TextView mSubtitleTextView;

    public UserContentDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mStatusTextView = view.findViewById(R.id.statusTextView);
        mSubtitleTextView = view.findViewById(R.id.subtitleTextView);
        return view;
    }

    @Override
    protected int getContentLayoutIdentifier() {
        return R.layout.fragment_user_content_detail;
    }

    @CallSuper
    @Override
    public void loadDataInUI(@NonNull ContentItem contentItem, boolean cacheValid) {
        super.loadDataInUI(contentItem, cacheValid);

        UserCreatedContent userContent = (UserCreatedContent) contentItem;

        StatusLabelUtils.setStatusLabel(mStatusTextView,
                getLoginComponentModule().getLoginRequired(),
                userContent.getPublishExtensionPartPublishExtensionStatus());


        mSubtitleTextView.setText(userContent.getSottotitoloValue());
    }
}
