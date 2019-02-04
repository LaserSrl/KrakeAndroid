package com.krake.puzzlegame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.krake.core.OrchardError;
import com.krake.core.app.OrchardDataModelFragment;
import com.krake.core.data.DataModel;
import com.krake.core.extension.ImageLoaderExtensionsKt;
import com.krake.core.media.DownloadOnlyLoadable;
import com.krake.core.media.ImageOptions;
import com.krake.core.media.loader.ImageLoader;
import com.krake.core.media.loader.MediaLoader;
import com.krake.core.util.LayoutUtils;
import com.krake.puzzlegame.model.PuzzleGame;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;

public class GameTilesFragment extends OrchardDataModelFragment
        implements TileView.Listener, Chronometer.OnChronometerTickListener, View.OnClickListener {
    private static final int CLOSE_IMAGE_VIEW = 12;
    private OnGameFinishedListener mListener;
    private TileView mTileView;
    private Bitmap mCurrentImageBitmap;
    private ImageView mCompleteImageView;
    private Handler mHanlder = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            if (msg.what == CLOSE_IMAGE_VIEW) {
                mCompleteImageView.animate().alpha(0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mCompleteImageView.setVisibility(View.GONE);
                    }
                });
                return true;
            }
            return false;
        }
    });
    private TextView mNumberOfMovesTextView;
    private TextView mTimeLeftTextView;
    private View mPauseView;
    private Button mResumeButton;
    private View mTileContainerView;
    private Game mGame;
    private int targetImageSize;
    private ScaleImageTask mTask;
    private boolean mAttachedToActivity;
    private DateFormat mFormat = new SimpleDateFormat("mm:ss", Locale.US);
    private CountDownTimer mTimer;
    private Chronometer mChronometer;
    private boolean mHideMenu;

    public GameTilesFragment() {
        // Required empty public constructor
    }

    protected Game getGame() {
        return mGame;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGame = new Game(savedInstanceState, getActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_tiles, container, false);

        setHasOptionsMenu(true);

        LayoutUtils.attachScrollingBehavior(view);

        mTileContainerView = view.findViewById(R.id.tileAndImageContainer);
        mPauseView = view.findViewById(R.id.pauseView);
        mResumeButton = view.findViewById(R.id.resumeButton);

        mResumeButton.setOnClickListener(this);

        mTileView = view.findViewById(R.id.tileView);
        mTileView.setListener(this);

        mTimeLeftTextView = view.findViewById(R.id.timeTextView);
        mNumberOfMovesTextView = view.findViewById(R.id.numberOfMovesTextView);
        mChronometer = view.findViewById(R.id.chronometer);
        mChronometer.setOnChronometerTickListener(this);

        mCompleteImageView = view.findViewById(R.id.completedImageView);

        mCompleteImageView.setOnClickListener(this);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        int actionBarHeight = actionBar == null ? 0 : actionBar.getHeight();
        targetImageSize = Math.min(displaymetrics.widthPixels, displaymetrics.heightPixels - actionBarHeight);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mTileContainerView.getLayoutParams();
        params.height = targetImageSize;
        params.width = targetImageSize;
        mTileContainerView.setLayoutParams(params);

        if (mCurrentImageBitmap != null && !mCurrentImageBitmap.isRecycled()) {
            startGame();
        }

        return view;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mListener = (OnGameFinishedListener) activity;
            mAttachedToActivity = true;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.puzzle_game_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem pauseGame = menu.findItem(R.id.action_game_pause);
        MenuItem helpItem = menu.findItem(R.id.action_game_show_completed_image);
        MenuItem showTiles = menu.findItem(R.id.action_game_show_tile_numbers);

        showTiles.setEnabled(!mGame.showTileNumber);
        helpItem.setEnabled(mGame.numberOfHelpsLeft > 0);

        pauseGame.setVisible(!mHideMenu);
        showTiles.setVisible(!mHideMenu);
        helpItem.setVisible(!mHideMenu);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {

        if (item.getItemId() == R.id.action_game_show_tile_numbers) {
            askShowNumberConfirmation();
            return true;
        } else if (item.getItemId() == R.id.action_game_show_completed_image) {
            askConfirmationToShowHelp();
            return true;
        } else if (item.getItemId() == R.id.action_game_pause) {
            updatePauseUI(true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void askShowNumberConfirmation() {
        new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.game_show_number_confirmation))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mGame.showTileNumber = true;
                        mListener.onRequestedTileNumbers(mGame);
                        mTileView.requestLayout();
                        getActivity().supportInvalidateOptionsMenu();

                    }
                }).show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mGame.saveState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mAttachedToActivity = false;
        if (mTask != null) {
            mTask.cancel(true);
            mTask = null;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onDataModelChanged(@org.jetbrains.annotations.Nullable DataModel dataModel) {
        if (mTileView.getCurrentImage() == null && dataModel != null && dataModel.getCacheValid()) {
            if (mCurrentImageBitmap == null || mCurrentImageBitmap.isRecycled()) {
                PuzzleGame game = (PuzzleGame) dataModel.getListData().get(0);

                mGame.question = Realm.getDefaultInstance().copyFromRealm(game);

                MediaLoader loader = MediaLoader.Companion.<File>typedWith(getActivity(), new DownloadOnlyLoadable() {
                    @Override
                    public ImageOptions getOptions() {
                        return new ImageOptions(targetImageSize, targetImageSize, ImageOptions.Mode.MAX);
                    }
                });

                if (game.getFirstPhoto() != null)
                    loader.mediaPart(game.getFirstPhoto());

                ImageLoader request = loader.getRequest();

                ImageLoaderExtensionsKt.asFile(request)
                        .addListener(new ImageLoader.RequestListener<File>() {
                            @Override
                            public void onDataLoadSuccess(File resource) {
                                mCurrentImageBitmap = BitmapFactory.decodeFile(resource.getPath());

                                if (mCurrentImageBitmap.getHeight() != targetImageSize || targetImageSize != mCurrentImageBitmap.getWidth()) {
                                    mTask = new ScaleImageTask();
                                    mTask.execute(mCurrentImageBitmap);
                                } else {
                                    startGame();
                                }
                            }

                            @Override
                            public void onDataLoadFailed() {

                            }
                        })
                        .load();
            }
        }
    }

    @Override
    public void onDataLoadingError(@NotNull OrchardError orchardError) {
        new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.error_loading_of_game_failed))
                .setNeutralButton(android.R.string.ok, null)
                .show();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mTask != null) {
            mTask.cancel(true);
            mTask = null;
        }
        stopTimer();

        updatePauseUI(true);
    }

    //Tile listener
    @Override
    public void onMovedTile() {
        updateGameUI();
    }

    @Override
    public void onSolved() {

        stopTimer();
        updateGameUI();
    }

    private void updateGameUI() {
        if (mTileView.getVisibility() == View.INVISIBLE) {
            mTileView.setVisibility(View.VISIBLE);
            mCompleteImageView.setVisibility(View.GONE);
            mNumberOfMovesTextView.setVisibility(View.VISIBLE);

            if (mGame.mTimeDescending) {
                mChronometer.setVisibility(View.GONE);
                mTimeLeftTextView.setVisibility(View.VISIBLE);
            } else {
                mChronometer.setVisibility(View.VISIBLE);
                mTimeLeftTextView.setVisibility(View.GONE);
            }

        }

        if (mGame.solved) {
            if (mTileView.getAlpha() > 0) {
                mTileView.animate().alpha(0);
                mCompleteImageView.setAlpha(0.0f);
                mCompleteImageView.setVisibility(View.VISIBLE);
                mCompleteImageView.animate().alpha(1);

            }
        } else {
            //mCompleteImageView.setOnClickListener(null);
            if (mTileView.getAlpha() == 0) {
                mTileView.animate().alpha(1);
                mCompleteImageView.animate().alpha(0).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mCompleteImageView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            }
        }

        mNumberOfMovesTextView.setText(String.format(getString(R.string.game_number_of_moves_format), mGame.numberOfMoves));

        updateTimeLeftTextView(mGame.mTimeLeft);
    }

    private void updateTimeLeftTextView(long numberOfMillisecondsLeft) {
        mTimeLeftTextView.setText(mFormat.format(new Date(numberOfMillisecondsLeft)));
    }

    @Override
    public void onChronometerTick(Chronometer chronometer) {
        mGame.mTimeLeft = SystemClock.elapsedRealtime() - chronometer.getBase();
    }

    @Override
    public void onClick(View v) {
        if (v == mResumeButton) {
            updatePauseUI(false);
            startTimer();
        } else if (v == mCompleteImageView) {
            if (mGame.solved) {
                mListener.onGameFinished(mGame);
            }
        }
    }

    private void startGame() {
        mTileView.requestFocus();

        mTileView.newGame(mGame, mCurrentImageBitmap);

        mCompleteImageView.setImageBitmap(mCurrentImageBitmap);

        mTileView.updateInstantPrefs();

        if (mGame.mTimeDescending) {
            mChronometer.setVisibility(View.GONE);
            mTimeLeftTextView.setVisibility(View.VISIBLE);
        } else {
            mChronometer.setVisibility(View.VISIBLE);
            mTimeLeftTextView.setVisibility(View.GONE);
        }

        updateGameUI();
        startTimer();
    }

    private void startTimer() {
        if (mGame.mTimeDescending) {
            mTimer = new CountDownTimer(mGame.mTimeLeft, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    mGame.mTimeLeft = millisUntilFinished;
                    updateTimeLeftTextView(millisUntilFinished);
                }

                @Override
                public void onFinish() {
                    mGame.mTimeLeft = 0;
                    mListener.onTimeElapsed(mGame);
                    mTimer = null;

                }
            };

            mTimer.start();
        } else {
            mChronometer.setBase(SystemClock.elapsedRealtime() - mGame.mTimeLeft);
            mChronometer.start();
        }
    }

    private void stopTimer() {
        if (mGame.mTimeDescending) {
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
        } else {
            mChronometer.stop();
        }
    }

    private void updatePauseUI(boolean visible) {
        int pauseAlphaVal, tileAlphaVal;
        Animator.AnimatorListener listener;

        if (!visible) {
            startTimer();
            pauseAlphaVal = 0;
            tileAlphaVal = 1;
            listener = new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mPauseView.setVisibility(View.GONE);
                    mHideMenu = false;
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.invalidateOptionsMenu();
                    }
                }
            };
        } else {
            stopTimer();
            pauseAlphaVal = 1;
            tileAlphaVal = 0;
            listener = new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mHideMenu = true;
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.invalidateOptionsMenu();
                    }
                    mPauseView.setVisibility(View.VISIBLE);
                }
            };
        }

        mPauseView.animate().alpha(pauseAlphaVal).setListener(listener);
        mTileContainerView.animate().alpha(tileAlphaVal);
    }

    private void askConfirmationToShowHelp() {
        new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.game_show_help_confirmation))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        --mGame.numberOfHelpsLeft;
                        getActivity().supportInvalidateOptionsMenu();
                        mListener.onRequestedShowCompleteImageHelp(mGame);
                        mCompleteImageView.setAlpha(0.0f);
                        mCompleteImageView.setVisibility(View.VISIBLE);
                        mCompleteImageView.animate().alpha(1).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                mCompleteImageView.setVisibility(View.VISIBLE);
                            }
                        });

                        mHanlder.sendEmptyMessageDelayed(CLOSE_IMAGE_VIEW, getResources().getInteger(R.integer.game_help_duration_second) * 1000);

                    }
                }).show();
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnGameFinishedListener {
        void onGameFinished(Game game);

        void onRequestedShowCompleteImageHelp(Game game);

        void onTimeElapsed(Game game);

        void onRequestedTileNumbers(Game game);
    }

    private class ScaleImageTask extends AsyncTask<Bitmap, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Bitmap... params) {

            Bitmap bitmap = params[0];
            int minSize = Math.min(bitmap.getHeight(), bitmap.getWidth());

            Matrix matrix = new Matrix();
            matrix.setScale(targetImageSize / minSize, targetImageSize / minSize);
            int x = (int) Math.ceil((bitmap.getWidth() - minSize) / 2);
            int y = (int) Math.ceil((bitmap.getHeight() - minSize) / 2);

            return Bitmap.createBitmap
                    (bitmap,
                            x,
                            y,
                            minSize,
                            minSize,
                            matrix,
                            false);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);


            mCurrentImageBitmap = bitmap;
            if (mAttachedToActivity) {
                startGame();
            }
            mTask = null;
        }
    }
}
