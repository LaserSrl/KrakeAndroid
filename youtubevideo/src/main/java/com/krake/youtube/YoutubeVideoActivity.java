package com.krake.youtube;

import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.krake.core.app.LoginAndPrivacyActivity;

public class YoutubeVideoActivity extends LoginAndPrivacyActivity {
    private YoutubeVideosFragment fragment;

    @Override
    public void onCreate(Bundle savedInstanceState, int layout) {
        super.onCreate(savedInstanceState, layout);

        if (YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(this) != YouTubeInitializationResult.SUCCESS) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.youtube_initialize_error_title)
                    .setMessage(R.string.youtube_initialize_error_message)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                        finish();
                    })
                    .show();
        } else {
            fragment = (YoutubeVideosFragment) getSupportFragmentManager().findFragmentById(R.id.activity_layout_coordinator);
        }
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
