package com.krake.puzzlegame;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.krake.core.util.LayoutUtils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EndGameFragment.OnCloseGameListener} interface
 * to handle interaction events.
 * Use the {@link EndGameFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EndGameFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_CORRECTLY = "correctly";

    private boolean correctly;
    private OnCloseGameListener mListener;
    private boolean mListenerAlreadyCalled = false;

    public EndGameFragment() {
        // Required empty public constructor
    }

    public static EndGameFragment newInstance(boolean correctly) {
        EndGameFragment fragment = new EndGameFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_CORRECTLY, correctly);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnCloseGameListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnCloseGameListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            correctly = getArguments().getBoolean(ARG_CORRECTLY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game_end, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LayoutUtils.attachScrollingBehavior(view);
        view.setOnClickListener(this);
        if (correctly) {
            view.findViewById(R.id.gameLostView).setVisibility(View.GONE);
            ImageView mImageView = view.findViewById(R.id.fireworksImageView);
            mImageView.setBackgroundResource(R.drawable.firework_3);
        } else {
            view.findViewById(R.id.gameWonView).setVisibility(View.GONE);
        }

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        if (!mListenerAlreadyCalled) {
            mListenerAlreadyCalled = true;
            mListener.onCloseGame();
        }
    }

    public interface OnCloseGameListener {
        void onCloseGame();
    }
}