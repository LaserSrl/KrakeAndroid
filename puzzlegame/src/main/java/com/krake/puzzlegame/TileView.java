package com.krake.puzzlegame;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.Serializable;


public class TileView extends View implements Serializable {

    public static final int DIR_UP = 0;
    public static final int DIR_DOWN = 1;
    public static final int DIR_LEFT = 2;
    public static final int DIR_RIGHT = 3;
    private static final long serialVersionUID = -1725598749479639893L;
    private static final String LOG_TAG = TileView.class.getName();
    private static final boolean DEBUG = false;
    private static final int SHADOW_RADIUS = 1;
    //Offset of tile from top left corner of cell
    float mOffsetX;
    float mOffsetY;
    //Current position on screen, used for drag events
    float mX;
    float mY;
    int mSelected;
    int mSize = 1;
    int mSizeSqr = 1;
    boolean mShowOutlines;
    boolean mShowImage;
    Bitmap mBitmap;
    int mNumberSize;
    SharedPreferences mPrefs;
    int mNumberColor;
    int mOutlineColor;
    Paint mPaintText;
    Paint mTextBackgroundPaint;
    int mMisplaced; // When this is equal to 0 the puzzle is won
    int lastEnabledTile = 0;
    int mNumberPaddingSize;
    private Listener mListener;
    private Game mGame;

    public TileView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }


    public TileView(Context context) {
        super(context);

        init();
    }

    public static Bitmap getImageFromResource(Context context, int resId, int width, int height) {
        Resources resources = context.getResources();

        //get the dimensions of the image
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, resId, opts);

        // get the image and scale it appropriately
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = Math.max(opts.outWidth / width, opts.outHeight / height);

        int scaledWidth = opts.outWidth;
        int scaledHeight = opts.outHeight;

        if (scaledWidth < scaledHeight) {
            float scale = width / (float) scaledWidth;

            scaledWidth = width;
            scaledHeight = (int) Math.ceil(scaledHeight * scale);
            if (scaledHeight < height) {
                scale = height / (float) scaledHeight;

                scaledHeight = height;
                scaledWidth = (int) Math.ceil(scaledWidth * scale);
            }
        } else {
            float scale = height / (float) scaledHeight;

            scaledHeight = height;
            scaledWidth = (int) Math.ceil(scaledWidth * scale);

            if (scaledWidth < width) {
                scale = width / (float) scaledWidth;

                scaledWidth = width;
                scaledHeight = (int) Math.ceil(scaledHeight * scale);
            }
        }
        return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, resId, opts),
                scaledWidth, scaledHeight, false);
    }

    public void setListener(Listener mListener) {
        this.mListener = mListener;
    }

    private void init() {
        setFocusable(true);
        Context context = getContext();

        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        mTextBackgroundPaint = new Paint();

        mTextBackgroundPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));

        mPaintText = new Paint();
        mPaintText.setTextAlign(Paint.Align.CENTER);
        mPaintText.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));

        mNumberPaddingSize = getResources().getDimensionPixelSize(R.dimen.number_padding_size);
    }

    public void updateInstantPrefs() {
        //update the preferences which should have an immediate effect      
        //mShowNumbers = mPrefs.getBoolean(PuzzleGameActivity.SHOW_NUMBERS, true);
        mShowOutlines = true;
        mNumberColor = ContextCompat.getColor(getContext(), R.color.colorAccent);
        mOutlineColor = ContextCompat.getColor(getContext(), R.color.colorPrimary);
        mShowImage = true;
        mNumberSize = getResources().getDimensionPixelSize(R.dimen.help_text_size);

        requestLayout();
    }

    /**
     * Measure the view and its content to determine the measured width and the measured height.
     * This method is invoked by measure(int, int) and should be overriden by subclasses to provide accurate and efficient measurement of their contents.
     * <p/>
     * calcolo le dimensioni
     * prelevo l'immagine da utilizzare
     */
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int w = getMeasuredWidth();
        int h = getMeasuredHeight();

        if (w <= 0 || h <= 0) {
            return;
        }
        //mBitmap = Utility.getImageFromAsset(context, mImageFile, w, h);
    }

    public void newGame(Game game, Bitmap bitmap) {
        mGame = game;
        mMisplaced = 0;
        mBitmap = bitmap;
        mSelected = -1;

        {
            mSizeSqr = game.mTiles.length;
            mSize = (int) Math.sqrt(mSizeSqr);
            countMisplaced();
        }

        if (mMisplaced == 0) {
            onSolved();
        }

        requestLayout();
    }

    private void countMisplaced() {
        for (int i = 0; i < mSizeSqr; ++i) {
            if (null != mGame.mTiles[i] && mGame.mTiles[i].mNumber != i) {
                mMisplaced++;
            }
        }
        if (DEBUG) {
            Log.v(LOG_TAG, "mMisplaced: " + mMisplaced);
        }
    }

    private float getTileWidth() {
        return getWidth() / mSize;
    }

    private float getTileHeight() {
        return getHeight() / mSize;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (mGame.mTiles == null)
            return;

        if (mBitmap == null || mBitmap.isRecycled()) {
            mBitmap = getImageFromResource(getContext(), R.drawable.pausa,
                    getWidth(), getHeight());
        }

        float tileWidth = getTileWidth();
        float tileHeight = getTileHeight();

        for (int index = 0; index < mSizeSqr; ++index) {
            int i = index / mSize;
            int j = index % mSize;
            float x = tileWidth * j;
            float y = tileHeight * i;

            // if this is the empty cell do nothing                 
            if (mGame.mTiles[index] == null) {
                continue;
            }

            if (mSelected != -1) {
                int min = Math.min(mSelected, mGame.mEmptyIndex);
                int max = Math.max(mSelected, mGame.mEmptyIndex);
                int minX = min % mSize;
                int minY = min / mSize;
                int maxX = max % mSize;
                int maxY = max / mSize;

                if (i >= minY && i <= maxY && j == minX) {
                    y += mOffsetY;
                }
                if (j >= minX && j <= maxX && i == minY) {
                    x += mOffsetX;
                }
            }

            //Draw the image                        
            if (mShowImage) {
                int xCropOffset = (mBitmap.getWidth() - getWidth()) / 2;
                int yCropOffset = (mBitmap.getHeight() - getHeight()) / 2;
                int tileNumber = mGame.mTiles[index].mNumber;
                int xSrc = (int) ((tileNumber % mSize) * tileWidth) + xCropOffset;
                int ySrc = (int) ((tileNumber / mSize) * tileHeight) + yCropOffset;
                Rect src = new Rect(xSrc, ySrc, (int) (xSrc + tileWidth), (int) (ySrc + tileHeight));
                Rect dst = new Rect((int) x, (int) y, (int) (x + tileWidth), (int) (y + tileHeight));

                if (mGame.mTiles[index].mVisible == 0) {
                    mPaintText.setColor(getResources().getColor(android.R.color.white));
                    canvas.drawRect(dst, mPaintText);
                } else
                    canvas.drawBitmap(mBitmap, src, dst, mPaintText);

            } else {
                mPaintText.setColor(mGame.mTiles[index].mColor);
                canvas.drawRect(x, y, x + tileWidth, y + tileHeight, mPaintText);
            }

            //Drop shadow to make numbers and borders stand out
            mPaintText.setShadowLayer(SHADOW_RADIUS, 1, 1, 0xff000000);

            //Draw the number
            if (mGame.showTileNumber) {
                mPaintText.setColor(mNumberColor);
                mPaintText.setTextSize(mNumberSize);

                mTextBackgroundPaint.setColor(Color.WHITE);
                canvas.drawOval(new RectF(x + 1, y + 1, x + mNumberSize + mNumberPaddingSize, y + mNumberSize + mNumberPaddingSize), mTextBackgroundPaint);

                canvas.drawText(String.valueOf(mGame.mTiles[index].mNumber + 1), x + mNumberSize / 2 + mNumberPaddingSize, y + mNumberSize - mNumberPaddingSize, mPaintText);


            }

            //Draw the outline
            if (mShowOutlines) {
                float x2 = x + tileWidth - 1;
                float y2 = y + tileHeight - 1;
                float lines[] = {
                        x, y, x2, y,
                        x, y, x, y2,
                        x2, y, x2, y2,
                        x, y2, x2, y2
                };
                mPaintText.setColor(mOutlineColor);
                canvas.drawLines(lines, mPaintText);
            }

            // remove shadow layer for perfomance
            mPaintText.setShadowLayer(0, 0, 0, 0);
        }//end for
    }


    private int getCellIndex(float x, float y) {
        float tileWidth = getTileWidth();
        float tileHeight = getTileHeight();

        //    int loc[] = new int[2];
        //  getLocationOnScreen(loc);


        if (DEBUG) {
            //  Log.v(LOG_TAG, "Index: " + (int)((y - loc[1]) / tileHeight) * mSize + (int)((x -loc[0]) / tileWidth));
        }

        int xIndex = (int) ((x - getLeft()) / tileWidth);
        int yIndex = (int) ((y - getTop()) / tileHeight);

        //clamp selection to edges of puzzle
        if (xIndex >= mSize) {
            xIndex = mSize - 1;
        } else if (xIndex < 0) {
            xIndex = 0;
        }

        if (yIndex >= mSize) {
            yIndex = mSize - 1;
        } else if (yIndex < 0) {
            yIndex = 0;
        }

        return mSize * yIndex + xIndex;
    }


    private boolean isSelectable(int index) {
        return (index / mSize == mGame.mEmptyIndex / mSize || index % mSize == mGame.mEmptyIndex % mSize) &&
                index != mGame.mEmptyIndex;
    }


    public boolean move(int dir) {
        //prevent movement via dpad/trackball during touch
        if (mSelected >= 0) {
            return false;
        }

        int index;
        switch (dir) {
            case DIR_UP:
                index = mGame.mEmptyIndex + mSize;
                if ((index) < mSizeSqr) {
                    update(index);
                    return true;
                }
                return false;
            case DIR_DOWN:
                index = mGame.mEmptyIndex - mSize;
                if ((index) >= 0) {
                    update(index);
                    return true;
                }
                return false;
            case DIR_LEFT:
                index = mGame.mEmptyIndex + 1;
                if ((index % mSize) != 0) {
                    update(index);
                    return true;
                }
                return false;
            case DIR_RIGHT:
                index = mGame.mEmptyIndex - 1;
                if ((mGame.mEmptyIndex % mSize) != 0) {
                    update(index);
                    return true;
                }
                return false;
        }
        return false;
    }

    private void redrawRow() {
        int h = (int) getTileHeight();
        int tileY = h * (mGame.mEmptyIndex / mSize);
        invalidate(0, tileY - SHADOW_RADIUS, getRight(), tileY + h + SHADOW_RADIUS);
    }

    private void redrawColumn() {
        int w = (int) getTileWidth();
        int tileX = w * (mGame.mEmptyIndex % mSize);
        invalidate(tileX - SHADOW_RADIUS, 0, tileX + w + SHADOW_RADIUS, getBottom());
    }


    private void update(int index) {
        if (index / mSize == mGame.mEmptyIndex / mSize) {
            //Moving a row
            if (mGame.mEmptyIndex < index) {
                while (mGame.mEmptyIndex < index) {
                    mGame.mTiles = (Tile[]) ArrayUtil.swap(mGame.mTiles, mGame.mEmptyIndex, mGame.mEmptyIndex + 1);
                    if (mGame.mTiles[mGame.mEmptyIndex].mNumber == mGame.mEmptyIndex) {
                        mMisplaced--;
                    } else if (mGame.mTiles[mGame.mEmptyIndex].mNumber == mGame.mEmptyIndex + 1) {
                        mMisplaced++;
                    }
                    ++mGame.mEmptyIndex;
                }
            } else {
                while (mGame.mEmptyIndex > index) {
                    mGame.mTiles = (Tile[]) ArrayUtil.swap(mGame.mTiles, mGame.mEmptyIndex, mGame.mEmptyIndex - 1);
                    if (mGame.mTiles[mGame.mEmptyIndex].mNumber == mGame.mEmptyIndex) {
                        mMisplaced--;
                    } else if (mGame.mTiles[mGame.mEmptyIndex].mNumber == mGame.mEmptyIndex - 1) {
                        mMisplaced++;
                    }
                    --mGame.mEmptyIndex;
                }
            }
            redrawRow();
        } else if (index % mSize == mGame.mEmptyIndex % mSize) {
            //Moving a column
            if (mGame.mEmptyIndex < index) {
                while (mGame.mEmptyIndex < index) {
                    mGame.mTiles = (Tile[]) ArrayUtil.swap(mGame.mTiles, mGame.mEmptyIndex, mGame.mEmptyIndex + mSize);
                    if (mGame.mTiles[mGame.mEmptyIndex].mNumber == mGame.mEmptyIndex) {
                        mMisplaced--;
                    } else if (mGame.mTiles[mGame.mEmptyIndex].mNumber == mGame.mEmptyIndex + mSize) {
                        mMisplaced++;
                    }
                    mGame.mEmptyIndex += mSize;
                }
            } else {
                while (mGame.mEmptyIndex > index) {
                    mGame.mTiles = (Tile[]) ArrayUtil.swap(mGame.mTiles, mGame.mEmptyIndex, mGame.mEmptyIndex - mSize);
                    if (mGame.mTiles[mGame.mEmptyIndex].mNumber == mGame.mEmptyIndex) {
                        mMisplaced--;
                    } else if (mGame.mTiles[mGame.mEmptyIndex].mNumber == mGame.mEmptyIndex - mSize) {
                        mMisplaced++;
                    }
                    mGame.mEmptyIndex -= mSize;
                }
            }
            redrawColumn();
        }
    }


    public void grabTile(float x, float y) {
        int index = getCellIndex(x, y);
        mSelected = isSelectable(index) ? index : -1;

        //set coordinates to the upper left corner of the selected tile
        mX = x;
        mY = y;
        mOffsetX = 0;
        mOffsetY = 0;

        if (DEBUG) {
            Log.v(LOG_TAG, "Grab: " + mSelected + " at (" + x + ", " + y + ")");
        }
    }

    public boolean dropTile(float x, float y) {
        if (DEBUG) {
            Log.v(LOG_TAG, "Drop: " + mSelected + " at (" + x + ", " + y + ")");
        }

        if (mSelected != -1 && (Math.abs(mOffsetX) > getTileWidth() / 2 ||
                Math.abs(mOffsetY) > getTileHeight() / 2)) {
            update(mSelected);
            mSelected = -1;
            return true;
        } else if (mSelected % mSize == mGame.mEmptyIndex % mSize) {
            redrawColumn();
        } else if (mSelected / mSize == mGame.mEmptyIndex / mSize) {
            redrawRow();
        }
        mSelected = -1;
        return false;
    }

    public void dragTile(float x, float y) {
        if (mSelected < 0)
            return;

        int w = (int) getTileWidth();
        int h = (int) getTileHeight();

        //Only drag in a single plane, either x or y depending on location of empty cell
        //prevent tiles from being dragged onto other tiles
        if (mSelected % mSize == mGame.mEmptyIndex % mSize) {
            if (mSelected > mGame.mEmptyIndex) {
                mOffsetY += y - mY;
                if (mOffsetY > 0) {
                    mOffsetY = 0;
                } else if (Math.abs(mOffsetY) > h) {
                    mOffsetY = -h;
                }
                mY = y;

            } else {
                mOffsetY += y - mY;
                if (mOffsetY < 0) {
                    mOffsetY = 0;
                } else if (mOffsetY > h) {
                    mOffsetY = h;
                }
                mY = y;
            }
            redrawColumn();
        } else if (mSelected / mSize == mGame.mEmptyIndex / mSize) {
            if (mSelected > mGame.mEmptyIndex) {
                mOffsetX += x - mX;
                if (mOffsetX > 0) {
                    mOffsetX = 0;
                } else if (Math.abs(mOffsetX) > w) {
                    mOffsetX = -w;
                }
                mX = x;
            } else {
                mOffsetX += x - mX;
                if (mOffsetX < 0) {
                    mOffsetX = 0;
                } else if (mOffsetX > w) {
                    mOffsetX = w;
                }
                mX = x;
            }
            redrawRow();
        }
    }

    public boolean checkSolved() {
        if (DEBUG) {
            Log.v(LOG_TAG, "mMisPlaced: " + mMisplaced);
        }

        if (mGame.solved) {
            return true;
        }

        if (mMisplaced == 0) {
            onSolved();
            if (mListener != null)
                mListener.onSolved();
            return true;
        }

        return false;
    }

    private void onSolved() {
        mGame.solved = true;

        mGame.mTiles[mGame.mEmptyIndex] = new Tile(mGame.mEmptyIndex, ContextCompat.getColor(getContext(), android.R.color.white), 1);

        invalidate();
    }

    public Bitmap getCurrentImage() {
        return mBitmap;
    }

    public Game getGame() {
        return mGame;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                grabTile(event.getX(), event.getY());
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                dragTile(event.getX(), event.getY());
                return true;
            }
            case MotionEvent.ACTION_UP: {
                boolean moved = dropTile(event.getX(), event.getY());

                if (moved) {
                    ++mGame.numberOfMoves;
                    if (mListener != null)
                        mListener.onMovedTile();
                }

                checkSolved();

                return true;
            }
        }

        return super.onTouchEvent(event);
    }

    public interface Listener {
        void onMovedTile();

        void onSolved();
    }
}
