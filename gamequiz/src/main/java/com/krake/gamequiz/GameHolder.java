package com.krake.gamequiz;

import android.view.View;
import android.widget.TextView;

import com.krake.core.widget.ImageTextCellHolder;

/**
 * Created by joel on 30/03/17.
 */

public class GameHolder extends ImageTextCellHolder {
    private TextView mAbstractTextView;
    private View statusDrawableContainer;
    private TextView statusTextView;

    public GameHolder(View itemView) {
        super(itemView);
        mAbstractTextView = itemView.findViewById(R.id.gameAbstractTextView);
        statusDrawableContainer = itemView.findViewById(R.id.gameStatusBackgroundContainer);
        statusTextView = itemView.findViewById(R.id.gameStatusTextView);
    }

    public TextView getAbstractTextView() {
        return mAbstractTextView;
    }

    public View getStatusDrawableContainer() {
        return statusDrawableContainer;
    }

    public TextView getStatusTextView() {
        return statusTextView;
    }
}