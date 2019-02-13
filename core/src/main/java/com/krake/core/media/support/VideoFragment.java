package com.krake.core.media.support;


import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.material.snackbar.Snackbar;
import com.krake.core.R;
import com.krake.core.widget.SnackbarUtils;

public class VideoFragment extends Fragment implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
    public static final String ARG_VIDEO_URL = "VideoURL";
    public static final String ARG_POSITION = "Position";
    private static final String TAG = VideoFragment.class.getSimpleName();
    private VideoView mVideoView;
    private MediaController mMediaController;
    private ProgressBar mProgressBar;
    private int mMediaPosition;
    private PagedActivity mPagedActity;
    private boolean mMediaPrepared;

    public static VideoFragment newInstance(String uri, int position) {

        VideoFragment fragment = new VideoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_VIDEO_URL, uri);
        args.putInt(ARG_POSITION, position);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);

        mMediaController = new MediaController(getActivity());

        mVideoView = view.findViewById(R.id.videoView);
        mProgressBar = view.findViewById(R.id.videoLoadingProgressBar);
        mMediaController.setAnchorView(mVideoView);
        mVideoView.setMediaController(mMediaController);

        mMediaPosition = getArguments().getInt(ARG_POSITION);
        String url = getArguments().getString(ARG_VIDEO_URL);
        if (url != null) {
            if (url.indexOf("http:") != 0 && url.indexOf("https:") != 0 && url.indexOf("content:") != 0) {
                url = com.krake.core.media.loader.MediaLoader.Companion.getAbsoluteMediaURL(getActivity(), url);
            }

            mMediaPrepared = false;
            Uri uriUrl = Uri.parse(url);
            mVideoView.setVideoURI(uriUrl);

            mVideoView.setOnPreparedListener(this);
            mVideoView.setOnErrorListener(this);

        } else {
            Log.d(TAG, "onCreateView: url is null");
        }
        return view;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mMediaPrepared = true;
        mProgressBar.setVisibility(View.GONE);
        if (mPagedActity == null || mPagedActity.getCurrentPagerItem() == mMediaPosition) {
            showControllerAndStartVideo();
        }
    }

    private void showControllerAndStartVideo() {
        mMediaController.show();
        mVideoView.start();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof PagedActivity) {
            mPagedActity = (PagedActivity) context;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

        SnackbarUtils.createSnackbar(mVideoView, R.string.video_not_available, Snackbar.LENGTH_LONG).show();

        return false;
    }

    public void onPagerSelectionChanged(boolean selected) {
        if (mMediaPrepared) {
            if (selected) {
                showControllerAndStartVideo();
            } else {
                mVideoView.pause();
            }
        }
    }

    public interface PagedActivity {
        int getCurrentPagerItem();
    }
}
