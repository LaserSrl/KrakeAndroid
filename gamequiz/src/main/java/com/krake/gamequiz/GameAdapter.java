package com.krake.gamequiz;

import android.content.Context;

import com.krake.core.widget.ContentItemAdapter;
import com.krake.core.widget.ImageTextCellHolder;
import com.krake.gamequiz.model.QuizGame;

import java.text.NumberFormat;
import java.util.Date;

/**
 * Created by joel on 30/03/17.
 */

public class GameAdapter extends ContentItemAdapter {
    private NumberFormat mFormat;

    public GameAdapter(Context context, int layout, Class holderClass) {
        super(context, layout, holderClass);
        mFormat = new GameDurationFormat(context);
    }

    @Override
    public void onBindViewHolder(ImageTextCellHolder holder, int i) {
        super.onBindViewHolder(holder, i);

        GameHolder gameHolder = (GameHolder) holder;

        QuizGame game = (QuizGame) getItem(i);

        if (game != null) {
            int statusBackground;

            String statusFormat;
            Date referenceDate = null;


            if (game.getState() == QuizGame.Status.ACTIVE) {
                statusBackground = R.drawable.game_valid_background;
                statusFormat = getContext().getString(R.string.games_valid_status_format);
                referenceDate = game.getEndDate();
            } else if (game.getState() == QuizGame.Status.NO_YET_ACTIVE) {
                statusBackground = R.drawable.game_no_yet_enabled_background;
                statusFormat = getContext().getString(R.string.games_no_yet_enabled_status_format);
                referenceDate = game.getStartDate();
            } else //if(game.getState() == QuizGame.Status.CLOSED)
            {
                statusBackground = R.drawable.game_expired_background;
                statusFormat = getContext().getString(R.string.game_expired_status_format);
            }

            gameHolder.getStatusDrawableContainer().setBackgroundResource(statusBackground);

            gameHolder.getAbstractTextView().setText(game.getAbstractText());

            if (referenceDate != null) {
                gameHolder.getStatusTextView().setText(String.format(statusFormat, mFormat.format(Math.abs(referenceDate.getTime() - new Date().getTime()))));

            } else {
                gameHolder.getStatusTextView().setText(statusFormat);
            }
        }
    }
}