package com.krake.gamequiz;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.krake.core.util.LayoutUtils;

/**
 * Fragment iniziale mostra solo un benvenuto al gioco,
 * utile per accogliere l'utente prima che il tempo inizi a scadere.
 */
public class BeginGameFragment extends Fragment implements View.OnClickListener {

    private PlayGameActivity mListener;

    public BeginGameFragment() {
        // Required empty public constructor
    }

    public static BeginGameFragment newInstance() {
        return new BeginGameFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_begin_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LayoutUtils.attachScrollingBehavior(view);
        view.findViewById(R.id.beginGameButton).setOnClickListener(this);
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mListener = (PlayGameActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        mListener.beginGame();
    }
}
