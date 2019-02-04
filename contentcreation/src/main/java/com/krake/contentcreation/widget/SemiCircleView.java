package com.krake.contentcreation.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.krake.contentcreation.MediaPickerFragment;
import com.krake.contentcreation.R;

/**
 * Custom View utilizzata nel {@link MediaPickerFragment} con orientation portrait.
 * <br/>
 * Questa View disegna un semicerchio con all'interno le tre righe orizzontali dell'apertura del menu.
 * <br/>
 * Essendo una View piana non causa overdrawing.
 */
public class SemiCircleView extends View {
    private Paint mSemiCirclePaint;
    private Paint mLinesPaint;
    private Path mSemiCirclePath;
    private Path mLinesPath;

    public SemiCircleView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public SemiCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public SemiCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SemiCircleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Inizializza tutte le configurazioni di default della View per evitare un lavoro eccessivo nell'onDraw()
     */
    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mSemiCirclePaint = new Paint();
        mSemiCirclePaint.setStyle(Paint.Style.FILL);
        mSemiCirclePaint.setAntiAlias(true);

        mLinesPaint = new Paint();
        mLinesPaint.setStyle(Paint.Style.STROKE);
        // le linee avranno contorni curvi
        mLinesPaint.setStrokeJoin(Paint.Join.ROUND);
        mLinesPaint.setStrokeCap(Paint.Cap.ROUND);
        mSemiCirclePaint.setAntiAlias(true);

        mSemiCirclePath = new Path();
        mLinesPath = new Path();

        if (attrs != null) {
            final TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
            @ColorInt final int colorPrimary = typedValue.data;

            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SemiCircleView, defStyleAttr, defStyleRes);

            @ColorInt int backgroundColor = a.getColor(R.styleable.SemiCircleView_semiCircleColor, 0);
            if (backgroundColor == 0) {
                backgroundColor = colorPrimary;
            }
            @ColorInt int linesColor = a.getColor(R.styleable.SemiCircleView_linesColor, 0);

            a.recycle();

            if (backgroundColor != 0) {
                setSemiCircleColor(backgroundColor);
            }
            if (linesColor != 0) {
                setLinesColor(linesColor);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Reset dei due path nel caso in cui cambi l'altezza.
        mSemiCirclePath.reset();
        mLinesPath.reset();

        int top = getPaddingTop();
        int left = getPaddingLeft();
        int bottom = getMeasuredHeight() - getPaddingBottom();
        int right = getMeasuredWidth() - getPaddingRight();

        mSemiCirclePath.moveTo(left, bottom);
        mSemiCirclePath.cubicTo(right / 4, top, right / 4 * 3, top, right, bottom);
        mSemiCirclePath.close();
        // Disegna il path per il semicerchio con una curva di Bezier cubica.
        canvas.drawPath(mSemiCirclePath, mSemiCirclePaint);

        float halfHeight = bottom / 2;
        float heightDiff = halfHeight / 3;

        float strokeWidth = heightDiff / 3;
        mLinesPaint.setStrokeWidth(strokeWidth);

        int startLinesX = right / 3;
        int endLinesX = right / 3 * 2;

        mLinesPath.moveTo(startLinesX, halfHeight);
        mLinesPath.lineTo(endLinesX, halfHeight);
        mLinesPath.moveTo(startLinesX, halfHeight + heightDiff);
        mLinesPath.lineTo(endLinesX, halfHeight + heightDiff);
        mLinesPath.moveTo(startLinesX, halfHeight + heightDiff * 2);
        mLinesPath.lineTo(endLinesX, halfHeight + heightDiff * 2);
        mLinesPath.close();
        // Disegna il path per le righe.
        canvas.drawPath(mLinesPath, mLinesPaint);
    }

    /**
     * Setta il colore dello sfondo del semicerchio
     *
     * @param color colore già trasformato in ColorInt
     */
    public void setSemiCircleColor(@ColorInt int color) {
        mSemiCirclePaint.setColor(color);
    }

    /**
     * Setta il colore delle tre linee centrali
     *
     * @param color colore già trasformato in ColorInt
     */
    public void setLinesColor(@ColorInt int color) {
        mLinesPaint.setColor(color);
    }
}