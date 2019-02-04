package com.krake.youtube;

import android.os.Bundle;

import com.krake.core.app.LoginAndPrivacyActivity;


public class YoutubeVideoActivity extends LoginAndPrivacyActivity {

    private YoutubeVideosFragment fragment;

    @Override
    public void onCreate(Bundle savedInstanceState, int layout) {
        super.onCreate(savedInstanceState, layout);

        fragment = (YoutubeVideosFragment) getSupportFragmentManager().findFragmentById(R.id.activity_layout_coordinator);
    }

    @Override
    public void changeContentVisibility(boolean visible) {

        if (fragment == null && visible) {
            fragment = new YoutubeVideosFragment();
            fragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction().replace(R.id.activity_layout_coordinator, fragment).commit();
        }
    }
}
