package com.krake.puzzlegame;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.krake.core.OrchardError;
import com.krake.core.app.OrchardDataModelFragment;
import com.krake.core.data.DataModel;
import com.krake.core.util.LayoutUtils;
import com.krake.core.widget.ObjectsRecyclerViewAdapter;
import com.krake.surveys.model.Answer;
import com.krake.surveys.model.Question;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 * A simple {@link com.krake.core.app.support.OrchardDataFragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link QuestionaryDetailsFragment.OnQuestionAnsweredListener} interface
 * to handle interaction events.
 */
public class QuestionaryDetailsFragment extends OrchardDataModelFragment implements ObjectsRecyclerViewAdapter.ClickReceiver<Answer> {

    private OnQuestionAnsweredListener mListener;
    private TextView mQuestionTextView;
    private ProgressBar mProgress;
    private ObjectsRecyclerViewAdapter<Answer, AnswerViewHolder> mAdapter;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mListener = (OnQuestionAnsweredListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_questionary_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LayoutUtils.attachScrollingBehavior(view);

        mQuestionTextView = view.findViewById(R.id.questionTextView);
        RecyclerView mAnswersGridView = view.findViewById(android.R.id.list);
        mProgress = view.findViewById(android.R.id.progress);
        mAdapter = new ObjectsRecyclerViewAdapter<Answer, AnswerViewHolder>(getActivity(), R.layout.answer_cell, null, AnswerViewHolder.class) {
            @Override
            public void onBindViewHolder(AnswerViewHolder holder, int position) {
                Answer record = getItem(position);
                if (record != null) {
                    holder.getTextView().setText(record.getAnswer());
                }
            }
        };

        mAdapter.setDefaultClickReceiver(this);
        mAnswersGridView.setAdapter(mAdapter);
    }

    @Override
    public void onDataLoadingError(@NotNull OrchardError orchardError) {

    }

    @Override
    public void onDataModelChanged(@org.jetbrains.annotations.Nullable DataModel dataModel) {

        if (dataModel != null && dataModel.getListData().size() > 0) {
            Question mQuestion = ((Question) dataModel.getListData().get(0));

            mQuestionTextView.setText(mQuestion.getQuestion());

            List<Answer> answers = new LinkedList<>(mQuestion.getPublishedAnswers());
            Collections.shuffle(answers);
            mAdapter.swapList(answers, true);

            mProgress.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onViewClicked(@NotNull RecyclerView recyclerView, @NotNull View view, int position, Answer item) {
        if (mListener != null && item != null) {
            mListener.userAnsweredCorrecly(item.getPosition() == 0);
        }
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
    public interface OnQuestionAnsweredListener {
        void userAnsweredCorrecly(boolean correctly);
    }


}