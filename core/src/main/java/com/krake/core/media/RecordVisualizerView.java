package com.krake.core.media;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.krake.core.R;

import java.util.ArrayList;

/**
 * Created by joel on 09/12/15.
 */
public class RecordVisualizerView extends View {
    private static final int LINE_SCALE = 10; // scales visualizer lines
    private final int lineWidth; // width of visualizer lines
    private ArrayList<Float> amplitudes = new ArrayList<>(); // amplitudes for line lengths
    private int width; // width of this View
    private int height; // height of this View
    private Paint linePaint; // specifies line drawing characteristics

    // constructor
    public RecordVisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs); // call superclass constructor
        linePaint = new Paint(); // create Paint for lines
        linePaint.setColor(ContextCompat.getColor(context, R.color.visualizer_line_color)); // set color to green
        lineWidth = context.getResources().getDimensionPixelSize(R.dimen.visualizer_line_width);
        linePaint.setStrokeWidth(lineWidth); // set stroke width
    }

    // called when the dimensions of the View change
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        width = getMeasuredWidth() > 0 ? getMeasuredWidth() : width; // new width of this View
        height = getMeasuredHeight() > 0 ? getMeasuredHeight() : height; // new height of this View
        clear();
        amplitudes.ensureCapacity(width / lineWidth);
    }

    // clear all amplitudes to prepare for a new visualization
    public void clear() {
        amplitudes.clear();
    }

    // add the given amplitude to the amplitudes ArrayList
    public void addAmplitude(float amplitude) {
        amplitudes.add(amplitude); // add newest to the amplitudes ArrayList

        // if the power lines completely fill the VisualizerView
        if (amplitudes.size() * lineWidth >= width) {
            amplitudes.remove(0); // remove oldest power value
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        int middle = height / 2; // get the middle of the View
        float curX = 0; // start curX at zero

        // for each item in the amplitudes ArrayList
        for (float power : amplitudes) {
            float scaledHeight = power / LINE_SCALE; // scale the power
            curX += lineWidth; // increase X by LINE_WIDTH

            // draw a line representing this item in the amplitudes ArrayList
            canvas.drawLine(curX, middle + scaledHeight / 2, curX, middle
                    - scaledHeight / 2, linePaint);
        }
    }

}