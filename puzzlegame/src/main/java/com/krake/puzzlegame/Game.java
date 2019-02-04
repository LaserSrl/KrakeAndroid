package com.krake.puzzlegame;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.krake.puzzlegame.model.PuzzleGame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by joel on 01/12/14.
 */
public class Game {

    static final String BUNDLE_TILES = "f2_tiles";
    static final String BUNDLE_TIME_LEFT_OR_ELAPSED = "f2_timepassed";
    static final String BUNDLE_TIME_DESCENDING = "time_ascending";
    static final String BUNDLE_NUMBER_OF_MOVES = "f2_steps";
    static final String BUNDLE_NUMBER_HELPS_LEFT = "f2_number_helps";
    static final String BUNDLE_SHOW_TILE_NUMBERS = "f2_show_numbers";
    static final String BUNDLE_EMPTY_INDEX = "emptyIndex";

    private final Context mContext;

    protected int numberOfMoves;
    protected long mTimeLeft;
    protected boolean solved;
    protected boolean mTimeDescending;
    protected int numberOfHelpsLeft;
    protected PuzzleGame question;
    protected boolean showTileNumber;
    protected Tile[] mTiles;
    protected int mEmptyIndex;

    public Game(Bundle savedInstanceState, Context context) {
        mContext = context;
        if (savedInstanceState == null) {
            numberOfMoves = 0;
            mTimeLeft = context.getResources().getInteger(R.integer.game_max_time_seconds) * 1000;
            mTimeDescending = mTimeLeft > 0;
            solved = false;
            numberOfHelpsLeft = context.getResources().getInteger(R.integer.game_number_of_showcomplete_image_help);
            showTileNumber = false;

            generateNewGame();
        } else {
            numberOfHelpsLeft = savedInstanceState.getInt(BUNDLE_NUMBER_HELPS_LEFT);
            mTimeLeft = savedInstanceState.getLong(BUNDLE_TIME_LEFT_OR_ELAPSED);
            mTimeDescending = savedInstanceState.getBoolean(BUNDLE_TIME_DESCENDING);
            showTileNumber = savedInstanceState.getBoolean(BUNDLE_SHOW_TILE_NUMBERS);
            numberOfMoves = savedInstanceState.getInt(BUNDLE_NUMBER_OF_MOVES);
            mEmptyIndex = savedInstanceState.getInt(BUNDLE_EMPTY_INDEX);

            String tiles = savedInstanceState.getString(BUNDLE_TILES);

            if (!TextUtils.isEmpty(tiles)) {
                ArrayList<Tile> tileList = new Gson().fromJson(tiles, new TypeToken<ArrayList<Tile>>() {
                }.getType());

                mTiles = tileList.toArray(new Tile[0]);
            }
        }
    }

    public void saveState(Bundle outBundle) {
        outBundle.putInt(BUNDLE_NUMBER_HELPS_LEFT, numberOfHelpsLeft);
        outBundle.putLong(BUNDLE_TIME_LEFT_OR_ELAPSED, mTimeLeft);
        outBundle.putBoolean(BUNDLE_TIME_DESCENDING, mTimeDescending);
        outBundle.putBoolean(BUNDLE_SHOW_TILE_NUMBERS, showTileNumber);
        outBundle.putInt(BUNDLE_NUMBER_OF_MOVES, numberOfMoves);
        outBundle.putInt(BUNDLE_EMPTY_INDEX, mEmptyIndex);

        ArrayList<Tile> tileList = new ArrayList<Tile>(Arrays.asList(mTiles));

        outBundle.putString(BUNDLE_TILES, new Gson().toJson(tileList));
    }

    private void generateNewGame() {
        int mSize = mContext.getResources().getInteger(R.integer.game_number_of_tiles);
        int mSizeSqr = mSize * mSize;

        mTiles = new Tile[mSizeSqr];
        for (int i = 0; i < mSizeSqr; ++i) {
            mTiles[i] = new Tile(i, mContext.getResources().getColor(android.R.color.white), 1);
        }

        List<Integer> indexes;

        do {
            indexes = generateShuffledIndexes(mTiles.length);
        } while (!canSolveTiles(indexes));


        for (int i = 0; i < indexes.size(); ++i) {
            mTiles[i].mNumber = indexes.get(i);
        }

        mEmptyIndex = mSizeSqr - 1;

        mTiles[mEmptyIndex] = null;
    }

    private List<Integer> generateShuffledIndexes(int numberOfTiles) {
        List<Integer> indexes = new LinkedList<Integer>();

        for (int i = 0; i < numberOfTiles - 1; ++i) {
            indexes.add(i);
        }

        Collections.shuffle(indexes);

        return indexes;
    }

    private boolean canSolveTiles(List<Integer> indexes) {
        int totalPermutationCounts = 0;

        for (int examinatedIndex = 0; examinatedIndex < indexes.size(); ++examinatedIndex) {
            int numberToExamine = indexes.get(examinatedIndex);

            int numberOfPermutations = 0;

            for (int permutationIndex = examinatedIndex + 1; permutationIndex < indexes.size(); ++permutationIndex) {
                int permutatedNumber = indexes.get(permutationIndex);

                if (numberToExamine > permutatedNumber) {
                    ++numberOfPermutations;
                }
            }

            totalPermutationCounts += numberOfPermutations;
        }

        return (totalPermutationCounts & 1) == 0;
    }
}