package com.krake.puzzlegame;

public class Tile {
    public int mColor;
    public int mNumber;
    public int mVisible;

    public Tile(int number, int color, int visible) {
        mNumber = number;
        mColor = color;
        mVisible = visible;
    }
}