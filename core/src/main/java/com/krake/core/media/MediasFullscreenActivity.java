package com.krake.core.media;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.krake.core.R;
import com.krake.core.media.support.VideoFragment;

import java.util.List;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public abstract class MediasFullscreenActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, VideoFragment.PagedActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            // mContentView.setSystemUiVisibility(View.systemUst
            //| View.SYSTEM_UI_FLAG_FULLSCREEN
            //| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            // | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            // | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                   /* | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION*///);
        }
    };
    private final Handler mHideHandler = new Handler();
    protected TextView mMediaPartTextView;
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    private FragmentPagerAdapter mPagerAdapter;
    private ViewPager mMediaPager;
    private List mMediasList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_media_part_fullscreen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        View mContentView = findViewById(R.id.fullscreen_content);

        mMediaPartTextView = findViewById(R.id.media_part_title_text_view);

        mMediaPager = findViewById(R.id.medias_view_pager);

        mMediaPager.addOnPageChangeListener(this);

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        mMediaPartTextView.setOnTouchListener(mDelayHideTouchListener);

        startDataLoading(getIntent());

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        mMediaPartTextView.setOnTouchListener(mDelayHideTouchListener);
    }

    protected abstract void startDataLoading(Intent intent);

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    protected void showLoadedData(List loadedMedias, Object selectedMedia) {
        mMediasList = loadedMedias;
        mPagerAdapter = createPagerAdapter();
        mMediaPager.setAdapter(mPagerAdapter);

        mMediaPager.setCurrentItem(mMediasList.indexOf(selectedMedia));
    }

    protected abstract FragmentPagerAdapter createPagerAdapter();

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

        if (mMediasList != null) {

            notifySelectedItemChanged(position - 1);
            notifySelectedItemChanged(position);
            notifySelectedItemChanged(position + 1);

        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public int getCurrentPagerItem() {
        if (mMediaPager != null)
            return mMediaPager.getCurrentItem();
        return -1;
    }

    private void notifySelectedItemChanged(int position) {
        if (position > 0 && position < mMediasList.size()) {
            Fragment fragment = mPagerAdapter.getItem(position);
            if (fragment instanceof VideoFragment) {
                ((VideoFragment) fragment).onPagerSelectionChanged(position == getCurrentPagerItem());
            }
        }
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
       /* mContentView.setSystemUiVisibility(//View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                );*/
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public List getMediasList() {
        return mMediasList;
    }
}
